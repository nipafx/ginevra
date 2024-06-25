package dev.nipafx.ginevra.parse;

import dev.nipafx.ginevra.html.Element;

import java.util.List;
import java.util.Map;

public interface MarkupDocument {

	FrontMatter frontMatter();

	List<Element> content();

	interface FrontMatter {

		Map<String, List<String>> asMap();

	}

}
