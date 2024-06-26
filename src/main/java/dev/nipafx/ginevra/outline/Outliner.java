package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Document.Id;
import dev.nipafx.ginevra.outline.Document.StringData;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Use it to build your {@link Outline}.
 */
public interface Outliner {

	// sources

	<DATA_OUT extends Record & Data>
	Step<DATA_OUT> source(Source<DATA_OUT> source);

	<DATA_OUT extends Record & Data>
	Step<DATA_OUT> source(DATA_OUT source);

	TextFileDataStep<TextFileData> sourceTextFiles(String name, Path path);

	FileDataStep<BinaryFileData> sourceBinaryFiles(String name, Path path);

	// transformers

	<DATA extends Record & Data>
	Step<DATA> filter(Step<DATA> previous, Predicate<DATA> filter);

	default <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> transform(
			Step<DATA_IN> previous,
			String transformerName,
			Function<DATA_IN, DATA_OUT> transformer) {
		return transformToMany(previous, in -> List.of(new GeneralDocument<>(
				in.id().transform(transformerName),
				transformer.apply(in.data()))));
	}

	default <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> transform(
			Step<DATA_IN> previous,
			Function<Id, Id> idTransformer,
			Function<DATA_IN, DATA_OUT> transformer) {
		return transformToMany(previous, in -> List.of(new GeneralDocument<>(
				idTransformer.apply(in.id()),
				transformer.apply(in.data()))));
	}

	default <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformToMany(
			Step<DATA_IN> previous,
			String transformerName,
			Function<DATA_IN, List<DATA_OUT>> transformer) {
		return transformToMany(previous, in -> transformer
				.apply(in.data()).stream()
				.<Document<DATA_OUT>> map(out -> new GeneralDocument<>(in.id().transform(transformerName), out))
				.toList());
	}

	<DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformToMany(
			Step<DATA_IN> previous,
			Transformer<DATA_IN, DATA_OUT> transformer);

	<DATA_IN extends Record & StringData, DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformMarkdown(Step<DATA_IN> previous, Class<DATA_OUT> frontMatterType);

	<DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> merge(
			Step<DATA_IN_1> previous1, Step<DATA_IN_2> previous2,
			Merger<DATA_IN_1, DATA_IN_2, DATA_OUT> merger);

	// store

	<DATA_IN extends Record & Data>
	void store(Step<DATA_IN> previous, String collection);

	<DATA_IN extends Record & Data>
	void store(Step<DATA_IN> previous);

	<DATA_IN extends Record & FileData>
	void storeResource(Step<DATA_IN> previous, Function<DATA_IN, String> naming);

	default <DATA_IN extends Record & FileData>
	void storeResource(Step<DATA_IN> previous) {
		storeResource(previous, fileData -> fileData.file().getFileName().toString());
	}

	// generate

	<DATA extends Record & Data>
	void generate(Template<DATA> template);

	void generateStaticResources(Path folder, String... resources);

	// build

	Outline build();

}
