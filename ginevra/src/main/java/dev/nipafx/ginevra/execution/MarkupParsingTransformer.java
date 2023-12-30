package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Transformer;
import dev.nipafx.ginevra.parse.MarkupParser;

class MarkupParsingTransformer implements Transformer {

	private final MarkupParser parser;

	public MarkupParsingTransformer(MarkupParser parser) {
		this.parser = parser;
	}

	@Override
	public Document transform(Document doc) {
		return doc;
	}

}
