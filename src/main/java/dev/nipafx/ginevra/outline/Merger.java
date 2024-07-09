package dev.nipafx.ginevra.outline;

import java.util.List;

/**
 * Merges two {@link Envelope}s.
 */
public interface Merger<DOCUMENT_IN_1 extends Record & Document, DOCUMENT_IN_2 extends Record & Document, DOCUMENT_OUT extends Record & Document> {

	List<DOCUMENT_OUT> merge(DOCUMENT_IN_1 doc1, DOCUMENT_IN_2 doc2);

}
