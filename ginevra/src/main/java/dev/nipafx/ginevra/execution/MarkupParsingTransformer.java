package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.DataString;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Transformer;
import dev.nipafx.ginevra.parse.MarkupDocument;
import dev.nipafx.ginevra.parse.MarkupParser;
import dev.nipafx.ginevra.util.RecordMapper;

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
		var input = doc.data().dataAsString();
		var markupDocument = parser.parse(input);

		var id = doc.id().transform(parser.name());
		var data = extractData(markupDocument);
		return List.of(new GeneralDocument<>(id, data));
	}

	private DATA_OUT extractData(MarkupDocument document) {
		// TODO: include `MarkupDocument::content`
		return RecordMapper.createFromMapToStringList(frontMatterType, document.frontMatter().asMap());
	}

}
