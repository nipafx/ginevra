package dev.nipafx.ginevra.html;

public sealed interface JmlElement extends KnownElement permits
		CodeBlock,
		Nothing,
		Text {

	CodeBlock codeBlock = new CodeBlock();
	Nothing nothing = new Nothing();
	Text text = new Text();

}
