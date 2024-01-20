package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

/**
 * Transforms {@link Document}s, for example when parsing them as Markdown.
 */
public interface Transformer<DATA_IN extends Record & Data, DATA_OUT extends Record & Data> {

	List<Document<DATA_OUT>> transform(Document<DATA_IN> doc);

}
