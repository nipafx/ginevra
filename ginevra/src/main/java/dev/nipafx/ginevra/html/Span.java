package dev.nipafx.ginevra.html;

import java.util.List;

public record Span(String id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public Span {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public Span() {
		this(null, Classes.none(), null, List.of());
	}

	public Span id(String id) {
		return new Span(id, this.classes, this.text, this.children);
	}

	public Span classes(Classes classes) {
		return new Span(this.id, classes, this.text, this.children);
	}

	public Span text(String text) {
		return new Span(this.id, this.classes, text, this.children);
	}

	public Span children(List<? extends Element> children) {
		return new Span(this.id, this.classes, this.text, children);
	}

	public Span children(Element... children) {
		return new Span(this.id, this.classes, this.text, List.of(children));
	}

}
