package dev.nipafx.ginevra.html;

public sealed interface HtmlElement extends KnownElement permits
		Div,
		Paragraph,
		Span {

	Div div = new Div();
	Paragraph p = new Paragraph();
	Span span = new Span();

}
