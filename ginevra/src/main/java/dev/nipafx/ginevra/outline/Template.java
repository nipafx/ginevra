package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.Id;
import dev.nipafx.ginevra.outline.Store.Query;

public interface Template<DATA extends Record & Data> {

	RenderedDocumentData render(DATA document);

}
