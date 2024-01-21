package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.DataString;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Transformer;
import dev.nipafx.ginevra.parse.MarkupDocument;
import dev.nipafx.ginevra.parse.MarkupParser;
import dev.nipafx.ginevra.util.RecordMapper;

import java.util.HashMap;
import java.util.List;

class MarkupParsingTransformer<DATA_IN extends Record & DataString, DATA_OUT extends Record & Data>
		implements Transformer<DATA_IN, DATA_OUT> {

	private final MarkupParser parser;
	private final Class<DATA_OUT> frontMatterType;

	public MarkupParsingTransformer(MarkupParser parser, Class<DATA_OUT> frontMatterType) {
		this.parser = parser;
		this.frontMatterType = frontMatterType;
	}

	@Override
	public List<Document<DATA_OUT>> transform(Document<DATA_IN> doc) {
		var inputData = doc.data();
		var markupDocument = parser.parse(inputData.dataAsString());

		var id = doc.id().transform(parser.name());
		var data = extractData(inputData, markupDocument);
		return List.of(new GeneralDocument<>(id, data));
	}

	private DATA_OUT extractData(DATA_IN inputData, MarkupDocument document) {
		var inputDataAndParsedContent = new HashMap<>(RecordMapper.createValueMapFromRecord(inputData));
		inputDataAndParsedContent.put("contentParsedAsMarkdown", document.content());
		return RecordMapper.createRecordFromMaps(frontMatterType, inputDataAndParsedContent, document.frontMatter().asMap());
	}

}
