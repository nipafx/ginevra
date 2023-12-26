package dev.nipafx.ginevra.parse;

import dev.nipafx.ginevra.html.Element;

import java.util.List;

public interface MarkupParser {

	List<Element> parse(String markup);

}
