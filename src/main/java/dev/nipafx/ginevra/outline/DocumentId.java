package dev.nipafx.ginevra.outline;

import java.net.URI;
import java.util.Objects;

public final class DocumentId {

	private final String id;

	private DocumentId(String id) { this.id = id; }

	public static DocumentId sourcedFrom(String name, URI location) {
		return new DocumentId("Source[%s; %s]".formatted(name, location));
	}

	public DocumentId transformedBy(String transformerName) {
		return new DocumentId("%s >>> Transformer[%s]".formatted(this, transformerName));
	}

	public DocumentId mergedWith(DocumentId other) {
		return new DocumentId("%s >>> Merged[%s]".formatted(this, other));
	}

	public String id() { return id; }

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (DocumentId) obj;
		return Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "DocumentId[" +
			   "id=" + id + ']';
	}


}
