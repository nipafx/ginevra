package dev.nipafx.ginevra.execution;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.HtmlContent;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.SenderId;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.SourceEvent;
import dev.nipafx.ginevra.outline.SourceEvent.Added;
import dev.nipafx.ginevra.outline.SourceEvent.Changed;
import dev.nipafx.ginevra.outline.SourceEvent.Removed;
import dev.nipafx.ginevra.util.RecordMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableMap;

class LiveStore implements StoreFront {

	private static final ObjectMapper JSON = new ObjectMapper()
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule())
			.registerModule(new SimpleModule(
					"StoreModule",
					Version.unknownVersion(),
					Map.of(
							Envelope.class, new EnvelopeDeserializer(),
							HtmlContent.class, new ContentDeserializer(),
							Path.class, new PathDeserializer()),
					List.of(
							new EnvelopeSerializer(),
							new ContentSerializer(),
							new PathSerializer())));

	private static final TypeReference<Map<SenderId, Envelope<?>>> ROOT_TYPE = new TypeReference<>() { };
	private static final TypeReference<Map<String, Map<SenderId, Envelope<?>>>> COLLECTIONS_TYPE = new TypeReference<>() { };

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

	void updateEnvelope(Optional<String> collection, SourceEvent event) {
		collection.ifPresentOrElse(
				col -> updateEnvelope(col, event),
				() -> updateEnvelope(event));
	}

	private void updateEnvelope(String collection, SourceEvent event) {
		switch (event) {
			case Added(var added) -> storeEnvelope(collection, added);
			case Changed(var changed) -> {
				removeEnvelope(changed.sender());
				storeEnvelope(collection, changed);
			}
			case Removed(var removedId) -> removeEnvelope(removedId);
		}
	}

	private void updateEnvelope(SourceEvent event) {
		switch (event) {
			case Added(var added) -> storeEnvelope(added);
			case Changed(var changed) -> {
				root.remove(changed.sender());
				storeEnvelope(changed);
			}
			case Removed(var removedId) -> root.remove(removedId);
		}
	}

	private void removeEnvelope(SenderId id) {
		collections.values().forEach(col -> col.remove(id));
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
	public <RESULT extends Record & Document> Set<RESULT> query(CollectionQuery<RESULT> query) {
		if (!collections.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		return collections
				.get(query.collection())
				.values().stream()
				.flatMap(envelope -> envelope.documents().stream())
				.map(document -> RecordMapper.createRecordFromRecord(query.resultType(), document))
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Optional<? extends FileDocument> getResource(String name) {
		return Optional
				.ofNullable(resources.get(name))
				.map(DocumentWithId::document);
	}

	public void removeAll() {
		root.clear();
		collections.clear();
		resources.clear();
	}

	public void updateToNewClassLoader() {
		var rootNode = JSON.valueToTree(root);
		var collectionsNode = JSON.valueToTree(collections);

		JSON.setTypeFactory(JSON.getTypeFactory().withClassLoader(ByteArrayClassLoader.currentOrApp()));

		try {
			root.clear();
			root.putAll(JSON.readValue(JSON.treeAsTokens(rootNode), ROOT_TYPE));
			collections.clear();
			collections.putAll(JSON.readValue(JSON.treeAsTokens(collectionsNode), COLLECTIONS_TYPE));

			if (!ContentSerializer.CONTENT.isEmpty())
				throw new IllegalStateException("Content map should be empty but isn't: " + ContentSerializer.CONTENT);
		} catch (IOException ex) {
			// TODO: handle error
			throw new UncheckedIOException(ex);
		}
	}

	@Override
	public String toString() {
		return "LiveStore{%s root entries, %s collections, %s resources}"
				.formatted(root.size(), collections.size(), resources.size());
	}

	private record DocumentWithId<DOCUMENT extends Record & Document>(SenderId id, DOCUMENT document) { }

	/*
	 * `Envelope` holds a `List<Document>` but Jackson does not have enough information
	 *  to deserialize arbitrary `Document` implementations. This custom serialization stores
	 *  the record name, so it can be used during deserialization.
	 */

	static class EnvelopeSerializer extends StdSerializer<Envelope<?>> {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public EnvelopeSerializer() {
			super((Class) Envelope.class);
		}

		@Override
		public void serialize(Envelope<?> envelope, JsonGenerator json, SerializerProvider provider) throws IOException {
			json.writeStartObject();
			json.writeObjectField("sender", envelope.sender());
			json.writeFieldName("documents");
			json.writeStartArray();
			for (Document doc : envelope.documents()) {
				json.writeStartObject();
				json.writeObjectField("type", doc.getClass().getName());
				json.writeObjectField("value", doc);
				json.writeEndObject();
			}
			json.writeEndArray();
			json.writeEndObject();
		}

	}

	static class EnvelopeDeserializer extends StdDeserializer<Envelope<?>> {

		public EnvelopeDeserializer() {
			super(Envelope.class);
		}

		@Override
		public Envelope<?> deserialize(JsonParser json, DeserializationContext context) throws IOException {
			try {
				var node = json.getCodec().readTree(json);

				var senderNode = node.get("sender");
				var sender = json.getCodec().treeToValue(senderNode, SenderId.class);

				var documents = new ArrayList();
				var docNodes = ((ArrayNode) node.get("documents")).elements();
				while (docNodes.hasNext()) {
					var docNode = docNodes.next();
					var typeName = docNode.get("type").asText();
					var type = ByteArrayClassLoader.currentOrApp().loadClass(typeName);
					var doc = json.getCodec().treeToValue(docNode.get("value"), type);
					documents.add(doc);
				}

				return new SimpleEnvelope<>(sender, documents);
			} catch (ClassNotFoundException ex) {
				throw new IllegalStateException(ex);
			}
		}

	}

	/*
	 * The `HtmlContent` is a potentially large object tree (~> (de-)serialization takes time)
	 * with Ginevra instances (~> doesn't care about new class loader ~> (de-)serialization unnecessary),
	 * so exclude it from (de-)serialization by intermittently storing it in a map.
	 */

	static class ContentSerializer extends StdSerializer<HtmlContent> {

		private static final ConcurrentMap<UUID, HtmlContent> CONTENT = new ConcurrentHashMap<>();

		public ContentSerializer() {
			super(HtmlContent.class);
		}

		@Override
		public void serialize(HtmlContent content, JsonGenerator json, SerializerProvider provider) throws IOException {
			var contentId = UUID.randomUUID();
			CONTENT.put(contentId, content);
			json.writeString(contentId.toString());
		}

	}

	static class ContentDeserializer extends StdDeserializer<HtmlContent> {

		public ContentDeserializer() {
			super(HtmlContent.class);
		}

		@Override
		public HtmlContent deserialize(JsonParser json, DeserializationContext context) throws IOException {
			var node = json.getCodec().readTree(json);
			var contentId = ((TextNode) node).asText();
			return ContentSerializer.CONTENT.remove(UUID.fromString(contentId));
		}

	}

	/*
	 * Jackson makes paths absolute during serialization (https://github.com/FasterXML/jackson-databind/issues/1422),
	 * which breaks all slugs (and probably more). A custom (de-) serialization stores paths as-are.
	 */

	static class PathSerializer extends StdSerializer<Path> {

		private static final ConcurrentMap<UUID, HtmlContent> CONTENT = new ConcurrentHashMap<>();

		public PathSerializer() {
			super(Path.class);
		}

		@Override
		public void serialize(Path path, JsonGenerator json, SerializerProvider provider) throws IOException {
			json.writeString(path.toString());
		}

	}

	static class PathDeserializer extends StdDeserializer<Path> {

		public PathDeserializer() {
			super(Path.class);
		}

		@Override
		public Path deserialize(JsonParser json, DeserializationContext context) throws IOException {
			var node = json.getCodec().readTree(json);
			return Path.of(((TextNode) node).asText());
		}

	}

}
