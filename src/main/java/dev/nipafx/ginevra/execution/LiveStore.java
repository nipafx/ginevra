package dev.nipafx.ginevra.execution;

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
import dev.nipafx.ginevra.util.RecordMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class LiveStore implements StoreFront {

	private final Map<SenderId, Envelope<?>> root;
	private final Map<String, Map<SenderId, Envelope<?>>> collections;
	private final Map<String, DocumentWithId<? extends FileDocument>> resources;

	public LiveStore() {
		root = new HashMap<>();
		collections = new HashMap<>();
		resources = new HashMap<>();
	}

	void storeEnvelope(Optional<String> collection, Envelope<?> envelope) {
		collection.ifPresentOrElse(
				col -> storeEnvelope(col, envelope),
				() -> storeEnvelope(envelope));
	}

	private void storeEnvelope(String collection, Envelope<?> envelope) {
		collections
				.computeIfAbsent(collection, _ -> new HashMap<>())
				.put(envelope.sender(), envelope);
	}

	private void storeEnvelope(Envelope<?> envelope) {
		root.put(envelope.sender(), envelope);
	}

	void updateDocument(Optional<String> collection, SourceEvent event) {
		collection.ifPresentOrElse(
				col -> updateDocument(col, event),
				() -> updateDocument(event));
	}

	private void updateDocument(String collection, SourceEvent event) {
		switch (event) {
			case Added(var added) -> storeEnvelope(collection, added);
			case Changed(var changed) -> {
				collections.values().forEach(col -> col.remove(changed.sender()));
				storeEnvelope(collection, changed);
			}
			case Removed(var removedId) -> collections.values().forEach(col -> col.remove(removedId));
		}
	}

	private void updateDocument(SourceEvent event) {
		switch (event) {
			case Added(var added) -> storeEnvelope(added);
			case Changed(var changed) -> {
				root.remove(changed.sender());
				storeEnvelope(changed);
			}
			case Removed(var removedId) -> root.remove(removedId);
		}
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
	public <RESULT extends Record & Document> RESULT query(RootQuery<RESULT> rootQuery) {
		var combinedValueMap = root
				.values().stream()
				.flatMap(envelope -> envelope.documents().stream())
				.map(RecordMapper::createValueMapFromRecord)
				.flatMap(valueMap -> valueMap.entrySet().stream())
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));

		return StoreUtils.queryRoot(
				rootQuery.resultType(),
				rootKey -> Optional.ofNullable(combinedValueMap.get(rootKey)),
				collectionKey -> Optional
						.ofNullable(collections.get(collectionKey))
						.map(collection -> collection
								.values().stream()
								.flatMap(envelope -> envelope.documents().stream())));
	}

	@Override
	public <RESULT extends Record & Document> List<RESULT> query(CollectionQuery<RESULT> query) {
		if (!collections.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		return collections
				.get(query.collection())
				.values().stream()
				.flatMap(envelope -> envelope.documents().stream())
				.map(document -> RecordMapper.createRecordFromRecord(query.resultType(), document))
				.toList();
	}

	@Override
	public Optional<? extends FileDocument> getResource(String name) {
		return Optional
				.ofNullable(resources.get(name))
				.map(DocumentWithId::document);
	}

	@Override
	public String toString() {
		return "LiveStore{%s root entries, %s collections, %s resources}"
				.formatted(root.size(), collections.size(), resources.size());
	}

	private record DocumentWithId<DOCUMENT extends Record & Document>(SenderId id, DOCUMENT document) { }

}
