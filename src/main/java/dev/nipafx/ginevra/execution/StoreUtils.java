package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.util.RecordMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;

class StoreUtils {

	static <RESULT extends Record & Document> RESULT queryRoot(
			Class<RESULT> type,
			Function<String, Optional<?>> rootEntry,
			Function<String, Optional<Stream<Document>>> collectionEntry) {
		var data = Stream
				.of(type.getRecordComponents())
				.collect(toUnmodifiableMap(
						RecordComponent::getName,
						component -> queryRootData(component, rootEntry, collectionEntry)));
		return RecordMapper.createRecordFromValueMap(type, data);
	}

	private static Object queryRootData(
			RecordComponent component,
			Function<String, Optional<?>> rootEntry,
			Function<String, Optional<Stream<Document>>> collectionEntry) {
		var key = component.getName();

		var rootValue = rootEntry.apply(key);
		if (rootValue.isPresent())
			return rootValue.get();

		var collectionValue = collectionEntry.apply(key);
		if (collectionValue.isPresent())
			return queryCollection(collectionValue.get(), key, component.getGenericType());

		throw new IllegalArgumentException("There's no data associated with the root query component '%s'".formatted(key));
	}

	private static List<?> queryCollection(Stream<Document> documentStream, String collectionName, Type genericType) {
		if (!(genericType instanceof ParameterizedType parameterizedType
			  && parameterizedType.getRawType().getTypeName().equals("java.util.List")))
			throw new IllegalArgumentException(
					"A root query's component that queries a collection needs to be of type 'java.util.List' but '%s' is of type '%s'."
							.formatted(collectionName, genericType.getTypeName()));

		try {
			@SuppressWarnings("unchecked")
			var type = (Class<Record>) ByteArrayClassLoader
					.currentOrApp()
					.loadClass(parameterizedType.getActualTypeArguments()[0].getTypeName());
			if (!type.isRecord())
				throw new IllegalArgumentException(
						"A root query's component that queries a collection needs to be a list of some record type but '%s' is a list of '%s'."
								.formatted(collectionName, type.getTypeName()));

			return documentStream
					.map(document -> RecordMapper.createRecordFromRecord(type, (Record) document))
					.toList();
		} catch (ClassNotFoundException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}
}
