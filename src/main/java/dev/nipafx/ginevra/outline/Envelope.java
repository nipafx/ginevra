package dev.nipafx.ginevra.outline;

import java.util.List;

/**
 * An envelope wraps an ID and a document.
 */
public interface Envelope<DOCUMENT extends Record & Document> {

	SenderId sender();

	List<DOCUMENT> documents();

}
