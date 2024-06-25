package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

/**
 * Merges two {@link Document}s.
 */
public interface Merger<DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data> {

	List<Document<DATA_OUT>> merge(Document<DATA_IN_1> doc1, Document<DATA_IN_2> doc2);

}
