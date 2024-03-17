package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

sealed public interface Query<RESULT extends Record & Data> {

	record RootQuery<RESULT extends Record & Data>(Class<RESULT> resultType)
			implements Query<RESULT> { }

	record CollectionQuery<RESULT extends Record & Data>(String collection, Class<RESULT> resultType)
			implements Query<RESULT> { }

}
