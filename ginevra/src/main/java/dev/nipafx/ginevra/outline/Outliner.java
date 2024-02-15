package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.DataString;
import dev.nipafx.ginevra.outline.Store.DocCollection;
import dev.nipafx.ginevra.outline.Store.Query;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Use it to build your {@link Outline}.
 */
public interface Outliner {

	// sources

	<DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> source(Source<DATA_OUT> source);

	StepKey<FileData> sourceFileSystem(String name, Path path);

	// transformers

	<DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transform(
			StepKey<DATA_IN> previous,
			Transformer<DATA_IN, DATA_OUT> transformer,
			Predicate<Document<DATA_IN>> filter);

	default <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transform(StepKey<DATA_IN> previous, Transformer<DATA_IN, DATA_OUT> transformer) {
		return transform(previous, transformer, _ -> true);
	}

	<DATA_IN extends Record & DataString, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transformMarkdown(StepKey<DATA_IN> previous, Class<DATA_OUT> frontMatterType, Predicate<Document<DATA_IN>> filter);

	default <DATA_IN extends Record & DataString, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transformMarkdown(StepKey<DATA_IN> previous, Class<DATA_OUT> frontMatterType) {
		return transformMarkdown(previous, frontMatterType, _ -> true);
	}

	// store

	<DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous, DocCollection collection, Predicate<Document<DATA_IN>> filter);

	default <DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous, DocCollection collection) {
		store(previous, collection, _ -> true);
	}

	<DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous, Predicate<Document<DATA_IN>> filter);

	default <DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous) {
		store(previous, _ -> true);
	}

	// generate

	<DATA extends Record & Data>
	void generate(Query<DATA> query, Predicate<Document<DATA>> filter, Template<DATA> template);

	default <DATA extends Record & Data>
	void generate(Query<DATA> query, Template<DATA> template) {
		generate(query, _ -> true, template);
	}

	// build

	Outline build();

	// inner

	interface StepKey<DATA_OUT extends Record & Data> { }

}
