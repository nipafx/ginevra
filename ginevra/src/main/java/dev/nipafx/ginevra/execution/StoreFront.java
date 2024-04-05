package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;

import java.util.List;
import java.util.Optional;

public interface StoreFront {

	<RESULT extends Record & Data> Document<RESULT> query(RootQuery<RESULT> query);

	<RESULT extends Record & Data> List<Document<RESULT>> query(CollectionQuery<RESULT> query);

	Optional<Document<? extends FileData>> getResource(String name);

}
