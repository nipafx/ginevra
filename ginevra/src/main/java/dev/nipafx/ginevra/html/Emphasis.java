package dev.nipafx.ginevra.html;

import java.util.List;

public record Emphasis(String id, Classes classes, String text, List<Element> children) implements HtmlElement {

	public Emphasis {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		} else
			children = List.copyOf(children);
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

	public Emphasis children(List<Element> children) {
		return new Emphasis(this.id, this.classes, this.text, children);
	}

	public Emphasis children(Element... children) {
		return new Emphasis(this.id, this.classes, this.text, List.of(children));
	}

}
