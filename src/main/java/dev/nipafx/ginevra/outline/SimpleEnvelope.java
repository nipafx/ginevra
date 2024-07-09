package dev.nipafx.ginevra.outline;

import java.util.Objects;

public record SimpleEnvelope<DOCUMENT extends Record & Document>(DocumentId id, DOCUMENT document) implements Envelope<DOCUMENT> {

	public SimpleEnvelope {
		Objects.requireNonNull(id);
		Objects.requireNonNull(document);
	}

}
