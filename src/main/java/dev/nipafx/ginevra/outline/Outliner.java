package dev.nipafx.ginevra.outline;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Use it to build your {@link Outline}.
 */
public interface Outliner {

	// sources

	<DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> source(Source<DOCUMENT_OUT> source);

	<DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> source(DOCUMENT_OUT source);

	TextFileStep<TextFileDocument> sourceTextFiles(String name, Path path);

	FileStep<BinaryFileDocument> sourceBinaryFiles(String name, Path path);

	// transformers

	<DOCUMENT extends Record & Document>
	Step<DOCUMENT> filter(Step<DOCUMENT> previous, Predicate<DOCUMENT> filter);

	<DOCUMENT_IN extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transform(
			Step<DOCUMENT_IN> previous,
			String transformerName,
			Function<DOCUMENT_IN, DOCUMENT_OUT> transformer);

	<DOCUMENT_IN extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformToMany(
			Step<DOCUMENT_IN> previous,
			String transformerName,
			Function<DOCUMENT_IN, List<DOCUMENT_OUT>> transformer);

	<DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformMarkdown(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> frontMatterType);

	<DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlValue(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> frontMatterType);

	<DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlList(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> yamlType);

	<DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlMap(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> yamlType);

	<DOCUMENT_IN extends Record & StringDocument, VALUE extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlMap(Step<DOCUMENT_IN> previous, Class<VALUE> yamlType, BiFunction<String, VALUE, DOCUMENT_OUT> entryMapper);

	<DOCUMENT_IN_1 extends Record & Document, DOCUMENT_IN_2 extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> merge(
			Step<DOCUMENT_IN_1> left, Step<DOCUMENT_IN_2> right,
			Merger<DOCUMENT_IN_1, DOCUMENT_IN_2, DOCUMENT_OUT> merger);

	// store

	<DOCUMENT_IN extends Record & Document>
	void store(Step<DOCUMENT_IN> previous, String collection);

	<DOCUMENT_IN extends Record & Document>
	void store(Step<DOCUMENT_IN> previous);

	<DOCUMENT_IN extends Record & FileDocument>
	void storeResource(Step<DOCUMENT_IN> previous, Function<DOCUMENT_IN, String> naming);

	<DOCUMENT_IN extends Record & FileDocument>
	void storeResource(Step<DOCUMENT_IN> previous);

	// generate

	<DOCUMENT extends Record & Document>
	void generate(Template<DOCUMENT> template);

	void generateStaticResources(Path targetFolder, String... resources);

	// build

	Outline build();

}
