package dev.nipafx.ginevra.parse;

import dev.nipafx.ginevra.outline.HtmlContent;

import java.util.List;
import java.util.Map;

public interface MarkupDocument {

	FrontMatter frontMatter();

	HtmlContent content();

	interface FrontMatter {

		Map<String, List<String>> asMap();

	}

}
