package dev.nipafx.ginevra.parse;

import dev.nipafx.ginevra.html.Element;

import java.util.List;

public sealed interface MarkupParser permits MarkdownParser {

	List<Element> parse(String markup);

}
