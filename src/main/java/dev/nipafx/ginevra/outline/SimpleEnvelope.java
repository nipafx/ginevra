package dev.nipafx.ginevra.outline;

import java.util.List;
import java.util.Objects;

public record SimpleEnvelope<DOCUMENT extends Record & Document>(SenderId sender, List<DOCUMENT> documents) implements Envelope<DOCUMENT> {

	public SimpleEnvelope {
		Objects.requireNonNull(sender);
		Objects.requireNonNull(documents);
	}

}
