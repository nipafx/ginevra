package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

/**
 * Stores all {@link Document}s produced by various {@link Source}s and {@link Transformer}s.
 */
public interface Store {

	void store(Document<?> doc);

	void store(DocCollection collection, Document<?> doc);

	List<Query<?>> commit();

	<RESULT extends Record & Data> Document<RESULT> query(RootQuery<RESULT> query);

	<RESULT extends Record & Data> List<Document<RESULT>> query(CollectionQuery<RESULT> query);

	record DocCollection(String name) { }

	sealed interface Query<RESULT extends Record & Data> { }

	record RootQuery<RESULT extends Record & Data>(Class<RESULT> resultType)
			implements Query<RESULT> { }

	record CollectionQuery<RESULT extends Record & Data>(DocCollection collection, Class<RESULT> resultType)
			implements Query<RESULT> { }

}
