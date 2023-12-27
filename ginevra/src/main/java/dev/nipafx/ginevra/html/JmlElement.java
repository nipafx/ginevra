package dev.nipafx.ginevra.html;

public sealed interface JmlElement extends KnownElement permits
		CodeBlock,
		Text {

	CodeBlock codeBlock = new CodeBlock();
	Text text = new Text();

}
