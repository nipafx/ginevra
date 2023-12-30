package dev.nipafx.ginevra.outline;

/**
 * Stores all {@link Document}s produced by various {@link Source}s and {@link Transformer}s.
 */
public non-sealed interface Store extends Step {

	void store(Document doc);

	void commit();

}
