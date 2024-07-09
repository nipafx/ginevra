package dev.nipafx.ginevra.outline;

/**
 * An envelope wraps an ID and a document.
 */
public interface Envelope<DOCUMENT extends Record & Document> {

	DocumentId id();

	DOCUMENT document();

}
