package dev.nipafx.ginevra.execution;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.nipafx.ginevra.outline.HtmlContent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

class Json {

	static final ObjectMapper LIVE_STORE_MAPPER = new ObjectMapper()
			.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule())
			.registerModule(new SimpleModule(
					"StoreModule",
					Version.unknownVersion(),
					Map.of(
							HtmlContent.class, new ContentDeserializer(),
							Path.class, new PathDeserializer()),
					List.of(
							new ContentSerializer(),
							new PathSerializer())));

	static final ObjectMapper ONE_TIME_STORE_MAPPER = new ObjectMapper()
			.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
			.registerModule(new Jdk8Module())
			.registerModule(new JavaTimeModule())
			.registerModule(new SimpleModule(
					"StoreModule",
					Version.unknownVersion(),
					Map.of(
							HtmlContent.class, new ContentDeserializer(),
							Path.class, new PathDeserializer()),
					List.of(
							new ContentSerializer(),
							new PathSerializer())));

	/*
	 * The `HtmlContent` is a potentially large object tree (~> (de-)serialization takes time)
	 * of Ginevra instances (~> doesn't care about new class loader ~> (de-)serialization unnecessary),
	 * so exclude it from (de-)serialization by storing it in a map.
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
			return ContentSerializer.CONTENT.get(UUID.fromString(contentId));
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
