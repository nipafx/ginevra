package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.DataString;
import dev.nipafx.ginevra.parse.MarkupDocument.FrontMatter;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Use it to build your {@link Outline}.
 */
public interface Outliner {

	// sources

	<DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> registerSource(Source<DATA_OUT> source);

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
	void store(StepKey<DATA_IN> previous, Predicate<Document<DATA_IN>> filter);

	default <DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous) {
		store(previous, _ -> true);
	}

	// build

	Outline build();

	// inner

	interface StepKey<DATA_OUT extends Record & Data> { }

}
