package dev.nipafx.ginevra.outline;

import java.util.function.Predicate;

sealed public interface Query<RESULT extends Record & Document> {

	record RootQuery<RESULT extends Record & Document>(Class<RESULT> resultType)
			implements Query<RESULT> { }

	record CollectionQuery<RESULT extends Record & Document>(String collection, Class<RESULT> resultType, Predicate<RESULT> filter)
			implements Query<RESULT> {

		public CollectionQuery(String collection, Class<RESULT> resultType) {
			this(collection, resultType, _ -> true);
		}

	}

}
