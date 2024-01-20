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

	<RESULT extends Record> RESULT query(RootQuery<RESULT> query);

	<RESULT extends Record> List<RESULT> query(CollectionQuery<RESULT> query);

	record None() implements Data { }

	record DocCollection(String name) { }

	sealed interface Query<RESULT extends Record> { }

	record RootQuery<RESULT extends Record>(Class<RESULT> resultType)
			implements Query<RESULT> { }

	record CollectionQuery<RESULT extends Record>(String collection, Class<RESULT> resultType)
			implements Query<RESULT> { }

}
