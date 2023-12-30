package dev.nipafx.ginevra.html;

import java.util.List;

public record Strong(String id, Classes classes, String text, List<Element> children) implements HtmlElement {

	public Strong {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		} else
			children = List.copyOf(children);
	}

	public Strong() {
		this(null, Classes.none(), null, List.of());
	}

	public Strong id(String id) {
		return new Strong(id, this.classes, this.text, this.children);
	}

	public Strong classes(Classes classes) {
		return new Strong(this.id, classes, this.text, this.children);
	}

	public Strong text(String text) {
		return new Strong(this.id, this.classes, text, this.children);
	}

	public Strong children(List<Element> children) {
		return new Strong(this.id, this.classes, this.text, children);
	}

	public Strong children(Element... children) {
		return new Strong(this.id, this.classes, this.text, List.of(children));
	}

}
