package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

import java.util.function.Consumer;

/**
 * A source will load files (e.g. Markdown or JSON), query external services, etc. to push
 * to {@link Source#register(Consumer) registered} {@link Document} consumers.
 */
public interface Source<DATA_OUT extends Record & Data> {

	void register(Consumer<Document<DATA_OUT>> consumer);

	void loadAll();

}
