package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.util.RecordMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class MapStore implements Store {

	private final Map<String, Object> root;
	private final Map<String, List<Document<?>>> collections;
	private final Set<Query<?>> queries;

	public MapStore() {
		root = new HashMap<>();
		collections = new HashMap<>();
		queries = new HashSet<>();
	}

	@Override
	public void store(Document<?> doc) {
		var documentData = RecordMapper.createValueMapFromRecord(doc.data());
		mergeData(root, documentData);
		// TODO: detect changes and mark impacted queries as dirty
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

	@Override
	public void store(String collection, Document<?> doc) {
		collections
				.computeIfAbsent(collection, _ -> new ArrayList<>())
				.add(doc);
	}

	@Override
	public List<Query<?>> commit() {
		// TODO: return dirty queries
		return List.of();
	}

	@Override
	public <RESULT extends Record & Data> Document<RESULT> query(RootQuery<RESULT> query) {
		queries.add(query);
		var type = query.resultType();
		var data = Stream
				.of(type.getRecordComponents())
				.collect(toMap(RecordComponent::getName, this::queryData));
		var instance = RecordMapper.createRecordFromValueMap(type, data);
		return new GeneralDocument<>(new Document.StoreId("MapStore"), instance);
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
			throw new IllegalArgumentException(STR."""
					A root query's component that queries a collection needs to be of type 'java.util.List' \
					but '\{collection}' is of type '\{genericType.getTypeName()}'.""");

		try {
			@SuppressWarnings("unchecked")
			var type = (Class<Record>) Class.forName(parameterizedType.getActualTypeArguments()[0].getTypeName());
			if (!type.isRecord())
				throw new IllegalArgumentException(STR."""
					A root query's component that queries a collection needs to be a list of some record type \
					but '\{collection}' is a list of '\{type.getTypeName()}'.""");

			return collections
					.get(collection).stream()
					.map(data -> RecordMapper.createRecordFromRecord(type, data.data()))
					.toList();
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public <RESULT extends Record & Data> List<Document<RESULT>> query(CollectionQuery<RESULT> query) {
		if (!collections.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		queries.add(query);
		return collections
				.get(query.collection()).stream()
				.<Document<RESULT>> map(document -> new GeneralDocument<>(
						document.id().transform("queried-" + query.resultType().getName()),
						RecordMapper.createRecordFromRecord(query.resultType(), document.data())))
				.toList();
	}

	@Override
	public String toString() {
		return this
				.collections
				.entrySet().stream()
				.map(entry -> STR." - \{entry.getKey()}: \{entry.getValue().size()}")
				.collect(joining(", ", "MapStore {", "}"));
	}

}