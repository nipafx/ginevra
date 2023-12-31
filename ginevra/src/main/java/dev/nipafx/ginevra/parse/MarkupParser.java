package dev.nipafx.ginevra.parse;

public sealed interface MarkupParser permits MarkdownParser {

	String name();

	MarkupDocument parse(String markup);

}
