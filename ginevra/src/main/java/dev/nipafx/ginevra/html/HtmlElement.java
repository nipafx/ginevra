package dev.nipafx.ginevra.html;

public sealed interface HtmlElement extends KnownElement permits
		Anchor,
		BlockQuote,
		Code,
		Div,
		Heading,
		HorizontalRule,
		ListItem,
		OrderedList,
		Paragraph,
		Pre,
		Span,
		UnorderedList {

	Anchor a = new Anchor();
	BlockQuote blockquote = new BlockQuote();
	Code code = new Code();
	Div div = new Div();
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
	UnorderedList ul = new UnorderedList();

}
