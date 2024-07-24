package dev.nipafx.ginevra.outline;

public interface StringStep<DOCUMENT extends Record & StringDocument> extends Step<DOCUMENT> {

	<DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformMarkdown(Class<DOCUMENT_OUT> frontMatterType);

}