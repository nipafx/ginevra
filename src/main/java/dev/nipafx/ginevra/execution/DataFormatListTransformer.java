package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.StringDocument;
import dev.nipafx.ginevra.parse.DataFormatParser;

import java.util.List;
import java.util.function.Function;

class DataFormatListTransformer<DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
		implements Function<DOCUMENT_IN, List<DOCUMENT_OUT>> {

	private final DataFormatParser parser;
	private final Class<DOCUMENT_OUT> documentType;

	DataFormatListTransformer(DataFormatParser parser, Class<DOCUMENT_OUT> documentType) {
		this.parser = parser;
		this.documentType = documentType;
	}

	@Override
	public List<DOCUMENT_OUT> apply(DOCUMENT_IN document) {
		return parser.parseList(document.documentAsString(), documentType);
	}

}
