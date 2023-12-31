package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Store.None;

/**
 * Stores all {@link Document}s produced by various {@link Source}s and {@link Transformer}s.
 */
public non-sealed interface Store extends Step<None> {

	void store(Document<?> doc);

	void commit();

	record None() implements Data { }

}
