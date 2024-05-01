package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Document.StringData;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Use it to build your {@link Outline}.
 */
public interface Outliner {

	// sources

	<DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> source(Source<DATA_OUT> source);

	<DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> source(DATA_OUT source);

	StepKey<TextFileData> sourceTextFiles(String name, Path path);

	StepKey<BinaryFileData> sourceBinaryFiles(String name, Path path);

	// transformers

	<DATA extends Record & Data>
	StepKey<DATA> filter(StepKey<DATA> previous, Predicate<DATA> filter);

	<DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transform(
			StepKey<DATA_IN> previous,
			Transformer<DATA_IN, DATA_OUT> transformer);

	<DATA_IN extends Record & StringData, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transformMarkdown(StepKey<DATA_IN> previous, Class<DATA_OUT> frontMatterType);

	<DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> merge(
			StepKey<DATA_IN_1> previous1, StepKey<DATA_IN_2> previous2,
			Merger<DATA_IN_1, DATA_IN_2, DATA_OUT> merger);

	// store

	<DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous, String collection);

	<DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous);

	<DATA_IN extends Record & FileData>
	void storeResource(StepKey<DATA_IN> previous, Function<DATA_IN, String> naming);

	default <DATA_IN extends Record & FileData>
	void storeResource(StepKey<DATA_IN> previous) {
		storeResource(previous, fileData -> fileData.file().getFileName().toString());
	}

	// generate

	<DATA extends Record & Data>
	void generate(Template<DATA> template);

	void generateStaticResources(Path folder, String... resources);

	// build

	Outline build();

	// inner

	interface StepKey<DATA_OUT extends Record & Data> { }

}
