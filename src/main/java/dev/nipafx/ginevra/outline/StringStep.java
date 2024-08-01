package dev.nipafx.ginevra.outline;

import java.util.function.BiFunction;

public interface StringStep<DOCUMENT extends Record & StringDocument> extends Step<DOCUMENT> {

	<DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformMarkdown(Class<DOCUMENT_OUT> frontMatterType);

	<DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlValue(Class<DOCUMENT_OUT> frontMatterType);

	<DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformYamlList(Class<DOCUMENT_OUT> yamlType);

	<DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformYamlMap(Class<DOCUMENT_OUT> yamlType);

	<VALUE extends Record & Document, DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT>
	transformYamlMap(Class<VALUE> yamlType, BiFunction<String, VALUE, DOCUMENT_OUT> entryMapper);

}
