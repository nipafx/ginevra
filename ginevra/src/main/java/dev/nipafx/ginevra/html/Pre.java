package dev.nipafx.ginevra.html;

import java.util.List;

public record Pre(String id, Classes classes, String text, List<Element> children) implements HtmlElement {

	public Pre {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		} else
			children = List.copyOf(children);
	}

	public Pre() {
		this(null, Classes.none(), null, List.of());
	}

	public Pre id(String id) {
		return new Pre(id, this.classes, this.text, this.children);
	}

	public Pre classes(Classes classes) {
		return new Pre(this.id, classes, this.text, this.children);
	}

	public Pre text(String text) {
		return new Pre(this.id, this.classes, text, this.children);
	}

	public Pre children(List<Element> children) {
		return new Pre(this.id, this.classes, this.text, children);
	}

	public Pre children(Element... children) {
		return new Pre(this.id, this.classes, this.text, List.of(children));
	}

}
