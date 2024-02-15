package dev.nipafx.ginevra.html;

import java.util.List;

public record CodeBlock(String id, Classes classes, String language, String text, List<? extends Element> children) implements JmlElement {

	public CodeBlock {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public CodeBlock() {
		this(null, Classes.none(), null, null, List.of());
	}

	public CodeBlock id(String id) {
		return new CodeBlock(id, this.classes, this.language, this.text, this.children);
	}

	public CodeBlock classes(Classes classes) {
		return new CodeBlock(this.id, classes, this.language, this.text, this.children);
	}

	public CodeBlock language(String language) {
		return new CodeBlock(this.id, this.classes, language, this.text, this.children);
	}

	public CodeBlock text(String text) {
		return new CodeBlock(this.id, this.classes, this.language, text, this.children);
	}

	public CodeBlock children(List<? extends Element> children) {
		return new CodeBlock(this.id, this.classes, this.language, this.text, children);
	}

	public CodeBlock children(Element... children) {
		return new CodeBlock(this.id, this.classes, this.language, this.text, List.of(children));
	}

}
