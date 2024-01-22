package dev.nipafx.ginevra.html;

public sealed interface HtmlElement extends KnownElement permits
		Anchor,
		BlockQuote,
		Body,
		Code,
		Div,
		Emphasis,
		Head,
		Heading,
		HtmlDocument,
		HorizontalRule,
		LineBreak,
		ListItem,
		OrderedList,
		Paragraph,
		Pre,
		Span,
		Strong,
		UnorderedList {

	Anchor a = new Anchor();
	BlockQuote blockquote = new BlockQuote();
	Body body = new Body();
	LineBreak br = new LineBreak();
	Code code = new Code();
	Div div = new Div();
	Head head = new Head();
	HtmlDocument document = new HtmlDocument();
	Emphasis em = new Emphasis();
	Heading h1 = new Heading(1);
	Heading h2 = new Heading(2);
	Heading h3 = new Heading(3);
	Heading h4 = new Heading(4);
	Heading h5 = new Heading(5);
	Heading h6 = new Heading(6);
	HorizontalRule hr = new HorizontalRule();
	ListItem li = new ListItem();
	OrderedList ol = new OrderedList();
	Paragraph p = new Paragraph();
	Pre pre = new Pre();
	Span span = new Span();
	Strong strong = new Strong();
	UnorderedList ul = new UnorderedList();

}
