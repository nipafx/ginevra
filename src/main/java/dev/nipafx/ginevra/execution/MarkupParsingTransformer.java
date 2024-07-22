package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.StringDocument;
import dev.nipafx.ginevra.parse.MarkupDocument;
import dev.nipafx.ginevra.parse.MarkupParser;
import dev.nipafx.ginevra.util.RecordMapper;

import java.util.HashMap;
import java.util.function.Function;

class MarkupParsingTransformer<DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
		implements Function<DOCUMENT_IN, DOCUMENT_OUT> {

	private final MarkupParser parser;
	private final Class<DOCUMENT_OUT> frontMatterType;

	MarkupParsingTransformer(MarkupParser parser, Class<DOCUMENT_OUT> frontMatterType) {
		this.parser = parser;
		this.frontMatterType = frontMatterType;
	}

	@Override
	public DOCUMENT_OUT apply(DOCUMENT_IN document) {
		var markupDocument = parser.parse(document.dataAsString());
		return extractData(document, markupDocument);
	}

	private DOCUMENT_OUT extractData(DOCUMENT_IN inputData, MarkupDocument document) {
		var inputDataAndParsedContent = new HashMap<>(RecordMapper.createValueMapFromRecord(inputData));
		inputDataAndParsedContent.put("contentParsedAsMarkdown", document.content());
		return RecordMapper.createRecordFromMaps(frontMatterType, inputDataAndParsedContent, document.frontMatter().asMap());
	}

}
