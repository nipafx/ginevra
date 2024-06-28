package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.Id;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Step<DATA extends Record & Data> {

	// transformers

	Step<DATA> filter(Predicate<DATA> filter);

	default <DATA_OUT extends Record & Data>
	Step<DATA_OUT> transform(
			String transformerName,
			Function<DATA, DATA_OUT> transformer) {
		return transformToMany(in -> List.of(new GeneralDocument<>(
				in.id().transform(transformerName),
				transformer.apply(in.data()))));
	}

	default <DATA_OUT extends Record & Data>
	Step<DATA_OUT> transform(
			Function<Id, Id> idTransformer,
			Function<DATA, DATA_OUT> transformer) {
		return transformToMany(in -> List.of(new GeneralDocument<>(
				idTransformer.apply(in.id()),
				transformer.apply(in.data()))));
	}

	default <DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformToMany(
			String transformerName,
			Function<DATA, List<DATA_OUT>> transformer) {
		return transformToMany(in -> transformer
				.apply(in.data()).stream()
				.<Document<DATA_OUT>> map(out -> new GeneralDocument<>(in.id().transform(transformerName), out))
				.toList());
	}

	<DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformToMany(Transformer<DATA, DATA_OUT> transformer);

	<OTHER_DATA extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> merge(Step<OTHER_DATA> other, Merger<DATA, OTHER_DATA, DATA_OUT> merger);

	// store

	void store(String collection);

	void store();

}
