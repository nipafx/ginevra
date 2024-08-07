package dev.nipafx.ginevra.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class OneTimeStore implements StoreFront {

	private static final ObjectMapper JSON = Json.ONE_TIME_STORE_MAPPER;

	private final JsonNode root;
	private final Map<String, ArrayNode> collections;
	private final Map<String, FileDocument> resources;

	private final StoreCache cache;

	OneTimeStore() {
		root = JSON.createObjectNode();
		collections = new HashMap<>();
		resources = new HashMap<>();
		cache = new StoreCache();
	}

	void storeDocument(Optional<String> collection, Document document) {
		collection.ifPresentOrElse(
				col -> storeDocument(col, document),
				() -> storeDocument(document));
	}

	void storeDocument(String collection, Document document) {
		collections
				.computeIfAbsent(collection, _ -> JSON.createArrayNode())
				.add(JSON.valueToTree(document));
	}

	void storeDocument(Document document) {
		try {
			// TODO: duplicate value detection (only one value should be defined for each key)
			JSON
					.readerForUpdating(root)
					.readValue(JSON.<JsonNode> valueToTree(document));
		} catch (IOException ex) {
			// TODO: handle error
			throw new IllegalStateException(ex);
		}
	}

	void storeResource(String name, FileDocument document) {
		var previous = resources.put(name, document);
		if (previous != null) {
			var message = "Resources must have unique names, but both %s and %s are named '%s'.".formatted(previous, document, name);
			throw new IllegalArgumentException(message);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <RESULT extends Record & Document> RESULT query(RootQuery<RESULT> query) {
		return (RESULT) cache.queryRoot(query.resultType(), collections::containsKey, this::queryRoot, this::queryCollection);
	}

	private <RESULT> RESULT queryRoot(String fieldName, Class<RESULT> resultType) {
		try {
			return JSON.treeToValue(root.get(fieldName), resultType);
		} catch (IOException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public <RESULT extends Record & Document> Set<RESULT> query(CollectionQuery<RESULT> query) {
		if (!collections.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		return cache.queryCollection(query.collection(), query.resultType(), this::queryCollection);
	}

	private <RESULT> Set<RESULT> queryCollection(String collectionName, Class<RESULT> resultType) {
		try {
			var setType = JSON.getTypeFactory().constructCollectionType(Set.class, resultType);
			return JSON.readerFor(setType).readValue(collections.get(collectionName));
		} catch (IOException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
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