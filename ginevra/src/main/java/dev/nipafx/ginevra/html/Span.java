package dev.nipafx.ginevra.html;

import java.util.List;

public record Span(String id, Classes classes, String text, List<Element> children) implements HtmlElement {

	public Span {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		} else
			children = List.copyOf(children);
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

	public Span children(List<Element> children) {
		return new Span(this.id, this.classes, this.text, children);
	}

	public Span children(Element... children) {
		return new Span(this.id, this.classes, this.text, List.of(children));
	}

}
