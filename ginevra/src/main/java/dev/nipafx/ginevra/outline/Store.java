package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

/**
 * Stores all {@link Document}s produced by various {@link Source}s and {@link Transformer}s.
 */
public interface Store {

	void store(Document<?> doc);

	void store(DocCollection collection, Document<?> doc);

	void commit();

	record None() implements Data { }

	record DocCollection(String name) { }

}
