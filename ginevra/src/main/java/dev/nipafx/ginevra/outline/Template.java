package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Store.Query;

public interface Template<DATA extends Record> {

	Query<DATA> getQuery();

	Document<RenderedDocumentData> render(DATA data);

}
