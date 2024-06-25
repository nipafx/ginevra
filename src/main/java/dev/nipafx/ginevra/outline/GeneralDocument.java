package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

import java.util.Objects;

public record GeneralDocument<DATA extends Record & Data>(Id id, DATA data) implements Document<DATA> {

	public GeneralDocument {
		Objects.requireNonNull(id);
		Objects.requireNonNull(data);
	}

}
