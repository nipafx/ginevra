package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.util.RecordMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class OneTimeStore implements StoreFront {

	private final Map<String, Object> root;
	private final Map<String, List<Document>> collections;
	private final Map<String, FileDocument> resources;

	OneTimeStore() {
		root = new HashMap<>();
		collections = new HashMap<>();
		resources = new HashMap<>();
	}

	void storeDocument(Optional<String> collection, Document document) {
		collection.ifPresentOrElse(
				col -> storeDocument(col, document),
				() -> storeDocument(document));
	}

	void storeDocument(String collection, Document document) {
		collections
				.computeIfAbsent(collection, _ -> new ArrayList<>())
				.add(document);
	}

	void storeDocument(Document document) {
		var documentData = RecordMapper.createValueMapFromRecord((Record) document);
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

	void storeResource(String name, FileDocument document) {
		var previous = resources.put(name, document);
		if (previous != null) {
			var message = "Resources must have unique names, but both %s and %s are named '%s'.".formatted(previous, document, name);
			throw new IllegalArgumentException(message);
		}
	}

	@Override
	public <RESULT extends Record & Document> RESULT query(RootQuery<RESULT> query) {
		return StoreUtils.queryRoot(
				query.resultType(),
				rootKey -> Optional.ofNullable(root.get(rootKey)),
				collectionKey -> Optional.ofNullable(collections.get(collectionKey)).map(List::stream));
	}

	@Override
	public <RESULT extends Record & Document> List<RESULT> query(CollectionQuery<RESULT> query) {
		if (!collections.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		return collections
				.get(query.collection()).stream()
				.map(document -> RecordMapper.createRecordFromRecord(query.resultType(), (Record) document))
				.toList();
	}

	@Override
	public Optional<? extends FileDocument> getResource(String name) {
		return Optional.ofNullable(resources.get(name));
	}

	@Override
	public String toString() {
		return "OneTimeStore{%s root entries, %s collections, %s resources}"
				.formatted(root.size(), collections.size(), resources.size());
	}

}