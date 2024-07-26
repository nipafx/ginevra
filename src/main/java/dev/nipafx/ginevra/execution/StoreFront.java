package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;

import java.util.Optional;
import java.util.Set;

public interface StoreFront {

	<RESULT extends Record & Document> RESULT query(RootQuery<RESULT> query);

	<RESULT extends Record & Document> Set<RESULT> query(CollectionQuery<RESULT> query);

	Optional<? extends FileDocument> getResource(String name);

}
