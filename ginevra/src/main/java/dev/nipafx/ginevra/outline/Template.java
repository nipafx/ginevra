package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

public interface Template<DATA extends Record & Data> {

	Query<DATA> query();

	default boolean filter(Document<DATA> document) {
		return true;
	}

	HtmlDocumentData compose(DATA document);

}
