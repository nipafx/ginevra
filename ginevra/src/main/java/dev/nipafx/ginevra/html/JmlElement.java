package dev.nipafx.ginevra.html;

public sealed interface JmlElement extends KnownElement permits
		CodeBlock,
		HtmlLiteral,
		Nothing,
		Text {

	CodeBlock codeBlock = new CodeBlock();
	HtmlLiteral html = new HtmlLiteral();
	Nothing nothing = new Nothing();
	Text text = new Text();

}
