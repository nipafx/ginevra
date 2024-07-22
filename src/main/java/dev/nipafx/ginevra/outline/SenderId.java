package dev.nipafx.ginevra.outline;

import java.net.URI;
import java.util.Objects;

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


}
