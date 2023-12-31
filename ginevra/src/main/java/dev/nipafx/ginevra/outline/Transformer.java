package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

/**
 * Transforms {@link Document}s, for example when parsing them as Markdown.
 */
public non-sealed interface Transformer<DATA_IN extends Record & Data, DATA_OUT extends Record & Data> extends Step<DATA_OUT> {

	List<Document<DATA_OUT>> transform(Document<DATA_IN> doc);

}
