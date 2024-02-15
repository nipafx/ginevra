package dev.nipafx.ginevra.html;

import java.util.List;

public record Emphasis(String id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public Emphasis {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public Emphasis() {
		this(null, Classes.none(), null, List.of());
	}

	public Emphasis id(String id) {
		return new Emphasis(id, this.classes, this.text, this.children);
	}

	public Emphasis classes(Classes classes) {
		return new Emphasis(this.id, classes, this.text, this.children);
	}

	public Emphasis text(String text) {
		return new Emphasis(this.id, this.classes, text, this.children);
	}

	public Emphasis children(List<? extends Element> children) {
		return new Emphasis(this.id, this.classes, this.text, children);
	}

	public Emphasis children(Element... children) {
		return new Emphasis(this.id, this.classes, this.text, List.of(children));
	}

}
