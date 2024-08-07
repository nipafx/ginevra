package dev.nipafx.ginevra.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.SenderId;
import dev.nipafx.ginevra.outline.SourceEvent;
import dev.nipafx.ginevra.outline.SourceEvent.Added;
import dev.nipafx.ginevra.outline.SourceEvent.Changed;
import dev.nipafx.ginevra.outline.SourceEvent.Removed;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class LiveStore implements StoreFront {

	private static final ObjectMapper JSON = Json.LIVE_STORE_MAPPER;

	private final Map<SenderId, JsonDocuments> root;
	private final Map<String, Map<SenderId, JsonDocuments>> collections;
	// Resources can be managed as POJOs because:
	//  * they're never passed back to user code and can thus not cause class loader issues
	//  * they're only queried by name and thus don't benefit from transformation to/from JSON
	// TODO: this causes a memory leak because old classes hold references to old class loaders - fix it
	private final Map<String, DocumentWithId<? extends FileDocument>> resources;

	private final StoreCache cache;
	private final StoreQueryTracker tracker;

	public LiveStore() {
		root = new HashMap<>();
		collections = new HashMap<>();
		resources = new HashMap<>();
		cache = new StoreCache();
		tracker = new StoreQueryTracker();
	}

	void storeEnvelope(Optional<String> collection, Envelope<?> envelope) {
		collection.ifPresentOrElse(
				col -> storeEnvelope(col, envelope),
				() -> storeEnvelope(envelope));
	}

	private void storeEnvelope(String collection, Envelope<?> envelope) {
		collections
				.computeIfAbsent(collection, _ -> new HashMap<>())
				.put(envelope.sender(), toJson(envelope));
		cache.invalidateCollection(collection);
		envelope.documents().forEach(doc -> tracker.recordStore(doc.getClass()));
	}

	private void storeEnvelope(Envelope<?> envelope) {
		root.put(envelope.sender(), toJson(envelope));
		cache.invalidateRoot();
		envelope.documents().forEach(doc -> tracker.recordStore(doc.getClass()));
	}

	void updateEnvelope(Optional<String> collection, SourceEvent event) {
		collection.ifPresentOrElse(
				col -> updateEnvelope(col, event),
				() -> updateEnvelope(event));
	}

	private void updateEnvelope(String collection, SourceEvent event) {
		switch (event) {
			case Added(var added) -> storeEnvelope(collection, added);
			case Changed(var changed) -> {
				removeEnvelope(collection, changed.sender());
				storeEnvelope(collection, changed);
			}
			case Removed(var removedId) -> removeEnvelope(collection, removedId);
		}
	}

	private void updateEnvelope(SourceEvent event) {
		switch (event) {
			case Added(var added) -> storeEnvelope(added);
			case Changed(var changed) -> {
				removeEnvelope(changed.sender());
				storeEnvelope(changed);
			}
			case Removed(var removedId) -> removeEnvelope(removedId);
		}
	}

	private void removeEnvelope(String collection, SenderId id) {
		// TODO: remove HtmlContent that was associated with these documents
		collections.get(collection).remove(id);
		cache.invalidateCollection(collection);
	}

	private void removeEnvelope(SenderId id) {
		root.remove(id);
		cache.invalidateRoot();
	}

	void storeResource(Function<Document, String> naming, Envelope<?> envelope) {
		envelope
				.documents()
				.forEach(doc -> {
					var name = naming.apply(doc);
					var previous = resources.put(name, new DocumentWithId<>(envelope.sender(), (Record & FileDocument) doc));
					if (previous != null) {
						var message = "Resources must have unique names, but both %s and %s are named '%s'."
								.formatted(previous.document(), doc, name);
						throw new IllegalArgumentException(message);
					}
				});
	}

	void updateResource(Function<Document, String> naming, SourceEvent event) {
		switch (event) {
			case Added(var added) -> storeResource(naming, added);
			case Changed(var changed) -> {
				removeResource(changed.sender());
				storeResource(naming, changed);
			}
			case Removed(var removedId) -> removeResource(removedId);
		}
	}

	private void removeResource(SenderId id) {
		resources.values().removeIf(doc -> doc.id().equals(id));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <RESULT extends Record & Document> RESULT query(RootQuery<RESULT> query) {
		tracker.recordQuery(query.resultType());
		return (RESULT) cache.queryRoot(query.resultType(), collections::containsKey, this::queryRootField, this::queryCollection);
	}

	private <RESULT> RESULT queryRootField(String fieldName, Class<RESULT> resultType) {
		tracker.recordQuery(resultType);
		try {
			// TODO: duplicate value detection (only one value should be defined for each key)
			var rootNode = JSON.createObjectNode();
			for (var docs : root.values())
				for (JsonNode jsonNode : docs.documents())
					JSON.readerForUpdating(rootNode).readValue(jsonNode);

			return JSON.treeToValue(rootNode.get(fieldName), resultType);
		} catch (IOException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public <RESULT extends Record & Document> Set<RESULT> query(CollectionQuery<RESULT> query) {
		if (!collections.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		tracker.recordQuery(query.resultType());
		return cache.queryCollection(query.collection(), query.resultType(), this::queryCollection);
	}

	private <RESULT> Set<RESULT> queryCollection(String collectionName, Class<RESULT> resultType) {
		tracker.recordQuery(resultType);
		return collections
				.get(collectionName)
				.values().stream()
				.flatMap(envelope -> envelope.documents().stream())
				.map(documentNode -> fromJson(resultType, documentNode))
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Optional<? extends FileDocument> getResource(String name) {
		return Optional
				.ofNullable(resources.get(name))
				.map(DocumentWithId::document);
	}

	/**
	 * The return value can only be trusted if outside code established that no "deeper" changes
	 * than the document types occurred, i.e. if arbitrary types changed, a removal of all stored
	 * data via {@link LiveStore#removeAllData() removeAllData} is always necessary.
	 *
	 * @return whether the changes required a removal of all stored data
	 */
	public boolean updateToNewTypes(List<Class<? extends Document>> changedDocumentTypes) {
		// the cache holds instances of the old types, so it needs to be invalidated
		// regardless of whether the new types are actually different
		cache.invalidateAll();

		var removeAll = !tracker.onlyQueryTypes(changedDocumentTypes);
		if (removeAll)
			removeAllData();
		return removeAll;
	}

	public void removeAllData() {
		root.clear();
		collections.clear();
		resources.clear();
		cache.invalidateAll();
		tracker.reset();
	}

	@Override
	public String toString() {
		return "LiveStore{%s root entries, %s collections, %s resources}"
				.formatted(root.size(), collections.size(), resources.size());
	}

	private record DocumentWithId<DOCUMENT extends Record & Document>(SenderId id, DOCUMENT document) { }

	private static JsonDocuments toJson(Envelope<?> envelope) {
		var documentsAsJson = envelope
				.documents().stream()
				.map(JSON::<JsonNode>valueToTree)
				.toList();
		return new JsonDocuments(documentsAsJson);
	}

	private static <TYPE> TYPE fromJson(Class<TYPE> type, JsonNode jsonNode) {
		try {
			return JSON.treeToValue(jsonNode, type);
		} catch (JsonProcessingException ex) {
			// TODO: handle error
			throw new IllegalStateException(ex);
		}
	}

	private record JsonDocuments(List<JsonNode> documents) { }

}
