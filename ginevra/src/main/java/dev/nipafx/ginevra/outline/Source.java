package dev.nipafx.ginevra.outline;

import java.util.function.Consumer;

/**
 * A source will load files (e.g. Markdown or JSON), query external services, etc. to push
 * to {@link Source#register(Consumer) registered} {@link Document} consumers.
 */
public non-sealed interface Source extends Step {

	void register(Consumer<Document> consumer);

	void loadAll();

}
