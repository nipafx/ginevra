package dev.nipafx.ginevra.html;

public sealed interface HtmlElement extends KnownElement permits
		Anchor,
		Code,
		Div,
		Heading,
		HorizontalRule,
		Paragraph,
		Pre,
		Span {

	Anchor a = new Anchor();
	Code code = new Code();
	Div div = new Div();
	Heading h1 = new Heading(1);
	Heading h2 = new Heading(2);
	Heading h3 = new Heading(3);
	Heading h4 = new Heading(4);
	Heading h5 = new Heading(5);
	Heading h6 = new Heading(6);
	HorizontalRule hr = new HorizontalRule();
	Paragraph p = new Paragraph();
	Pre pre = new Pre();
	Span span = new Span();

}
