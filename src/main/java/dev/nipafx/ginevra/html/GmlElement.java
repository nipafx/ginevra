package dev.nipafx.ginevra.html;

public sealed interface GmlElement extends KnownElement permits
		CodeBlock,
		HtmlLiteral,
		Nothing,
		Text {

	CodeBlock codeBlock = new CodeBlock();
	HtmlLiteral html = new HtmlLiteral();
	Nothing nothing = new Nothing();
	Text text = new Text();

}
