package dev.nipafx.ginevra.outline;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

@JsonSerialize(using = SenderId.Serializer.class)
@JsonDeserialize(using = SenderId.Deserializer.class)
public final class SenderId {

	private final String id;

	private SenderId(String id) {
		this.id = id;
	}

	public static SenderId source(String name, URI location) {
		return new SenderId("Source[%s; %s]".formatted(name, location));
	}

	public SenderId transform(String transformerName) {
		return new SenderId("%s >>> Transformer[%s]".formatted(id, transformerName));
	}

	public SenderId filter() {
		return new SenderId("%s >>> Filter".formatted(id));
	}

	public SenderId mergeFrom(SenderId other) {
		return new SenderId("%s >>> Merged[%s]".formatted(id, other));
	}

	public String id() { return id; }

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (SenderId) obj;
		return Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SenderId[id=" + id + ']';
	}

	static class Serializer extends StdSerializer<SenderId> {

		public Serializer() {
			super(SenderId.class);
		}

		@Override
		public void serialize(SenderId sender, JsonGenerator json, SerializerProvider provider) throws IOException {
			json.writeString(sender.id());
		}

	}

	static class Deserializer extends StdDeserializer<SenderId> {

		public Deserializer() {
			super(SenderId.class);
		}

		@Override
		public SenderId deserialize(JsonParser json, DeserializationContext context) throws IOException {
			JsonNode node = json.getCodec().readTree(json);
			return new SenderId(node.asText());
		}

	}

}
