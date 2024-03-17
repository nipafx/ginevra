package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;

import java.util.List;

/**
 * Stores all {@link Document}s produced by various {@link Source}s and {@link Transformer}s.
 */
public interface Store {

	void store(Document<?> doc);

	void store(String collection, Document<?> doc);

	List<Query<?>> commit();

	<RESULT extends Record & Data> Document<RESULT> query(RootQuery<RESULT> query);

	<RESULT extends Record & Data> List<Document<RESULT>> query(CollectionQuery<RESULT> query);

}
