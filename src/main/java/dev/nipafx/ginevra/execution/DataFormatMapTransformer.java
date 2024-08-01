package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.StringDocument;
import dev.nipafx.ginevra.parse.DataFormatParser;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

class DataFormatMapTransformer<DOCUMENT_IN extends Record & StringDocument, DOCUMENT extends Record & Document, DOCUMENT_OUT extends Record & Document>
		implements Function<DOCUMENT_IN, List<DOCUMENT_OUT>> {

	private final DataFormatParser parser;
	private final Class<DOCUMENT> documentType;
	private final BiFunction<String, DOCUMENT, DOCUMENT_OUT> entryMapper;

	DataFormatMapTransformer(DataFormatParser parser, Class<DOCUMENT> documentType, BiFunction<String, DOCUMENT, DOCUMENT_OUT> entryMapper) {
		this.parser = parser;
		this.documentType = documentType;
		this.entryMapper = entryMapper;
	}

	@Override
	public List<DOCUMENT_OUT> apply(DOCUMENT_IN document) {
		return parser.parseMap(document.documentAsString(), documentType, entryMapper);
	}

}
