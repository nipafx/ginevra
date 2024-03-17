package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

public interface Template<DATA extends Record & Data> {

	HtmlDocumentData compose(DATA document);

}
