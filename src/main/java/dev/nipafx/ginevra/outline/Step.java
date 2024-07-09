package dev.nipafx.ginevra.outline;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Step<DOCUMENT extends Record & Document> {

	// transformers

	Step<DOCUMENT> filter(Predicate<DOCUMENT> filter);

	<DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transform(String transformerName, Function<DOCUMENT, DOCUMENT_OUT> transformer);

	<DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformToMany(
			String transformerName,
			Function<DOCUMENT, List<DOCUMENT_OUT>> transformer);

	<DOCUMENT_OTHER extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> merge(Step<DOCUMENT_OTHER> other, Merger<DOCUMENT, DOCUMENT_OTHER, DOCUMENT_OUT> merger);

	// store

	void store(String collection);

	void store();

}
