package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.StringDocument;
import dev.nipafx.ginevra.parse.DataFormatParser;

import java.util.function.Function;

class DataFormatValueTransformer<DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
		implements Function<DOCUMENT_IN, DOCUMENT_OUT> {

	private final DataFormatParser parser;
	private final Class<DOCUMENT_OUT> documentType;

	DataFormatValueTransformer(DataFormatParser parser, Class<DOCUMENT_OUT> documentType) {
		this.parser = parser;
		this.documentType = documentType;
	}

	@Override
	public DOCUMENT_OUT apply(DOCUMENT_IN doc) {
		return parser.parseValue(doc.documentAsString(), documentType);
	}

}
