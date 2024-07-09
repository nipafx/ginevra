package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.util.RecordMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class Store implements StoreFront {

	private final Map<String, Object> root;
	private final Map<String, List<Envelope<?>>> collections;
	private final Map<String, Envelope<? extends FileDocument>> resources;

	public Store() {
		root = new HashMap<>();
		collections = new HashMap<>();
		resources = new HashMap<>();
	}

	public void store(Envelope<?> envelope) {
		var documentData = RecordMapper.createValueMapFromRecord(envelope.document());
		mergeData(root, documentData);
	}

	@SuppressWarnings("unchecked")
	private static void mergeData(Map<String, Object> existingData, Map<String, Object> newData) {
		newData.forEach((newKey, newValue) -> {
			if (existingData.containsKey(newKey)) {
				var existingValue = existingData.get(newKey);
				if (existingValue instanceof Map && newValue instanceof Map)
					mergeData((Map<String, Object>) existingValue, (Map<String, Object>) newValue);
				else
					throw new IllegalArgumentException("Duplicate value");
			} else
				existingData.put(newKey, newValue);
		});
	}

	public void store(String collection, Envelope<?> envelope) {
		collections
				.computeIfAbsent(collection, _ -> new ArrayList<>())
				.add(envelope);
	}

	public void storeResource(String name, Envelope<? extends FileDocument> envelope) {
		var previous = resources.put(name, envelope);
		if (previous != null) {
			var message = "Resources must have unique names, but both %s and %s are named '%s'.".formatted(previous.document(), envelope, name);
			throw new IllegalArgumentException(message);
		}
	}

	public <RESULT extends Record & Document> RESULT query(RootQuery<RESULT> query) {
		var type = query.resultType();
		var data = Stream
				.of(type.getRecordComponents())
				.collect(toMap(RecordComponent::getName, this::queryData));
		return RecordMapper.createRecordFromValueMap(type, data);
	}

	private Object queryData(RecordComponent component) {
		var key = component.getName();
		if (root.containsKey(key))
			return queryRoot(key);
		if (collections.containsKey(key))
			return queryCollection(key, component.getGenericType());
		return Optional.empty();
	}

	private Object queryRoot(String entry) {
		return root.get(entry);
	}

	private List<?> queryCollection(String collection, Type genericType) {
		if (!collections.containsKey(collection))
			throw new IllegalArgumentException("Unknown document collection: " + collection);
		if (!(genericType instanceof ParameterizedType parameterizedType
			  && parameterizedType.getRawType().getTypeName().equals("java.util.List")))
			throw new IllegalArgumentException(
					"A root query's component that queries a collection needs to be of type 'java.util.List' but '%s' is of type '%s'."
							.formatted(collection, genericType.getTypeName()));

		try {
			@SuppressWarnings("unchecked")
			var type = (Class<Record>) Class.forName(parameterizedType.getActualTypeArguments()[0].getTypeName());
			if (!type.isRecord())
				throw new IllegalArgumentException(
						"A root query's component that queries a collection needs to be a list of some record type but '%s' is a list of '%s'."
								.formatted(collection, type.getTypeName()));

			return collections
					.get(collection).stream()
					.map(envelope -> RecordMapper.createRecordFromRecord(type, envelope.document()))
					.toList();
		} catch (ClassNotFoundException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

	public <RESULT extends Record & Document> List<RESULT> query(CollectionQuery<RESULT> query) {
		if (!collections.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		return collections
				.get(query.collection()).stream()
				.map(envelope -> RecordMapper.createRecordFromRecord(query.resultType(), envelope.document()))
				.toList();
	}

	public Optional<? extends FileDocument> getResource(String name) {
		return Optional
				.ofNullable(resources.get(name))
				.map(Envelope::document);
	}

	@Override
	public String toString() {
		return this
				.collections
				.entrySet().stream()
				.map(entry -> " - %s: %d".formatted(entry.getKey(), entry.getValue().size()))
				.collect(joining(", ", "MapStore {", "}"));
	}

}