package dev.nipafx.ginevra.html;

import java.util.List;

public record Code(String id, Classes classes, String text, List<Element> children) implements HtmlElement {

	public Code {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		} else
			children = List.copyOf(children);
	}

	public Code() {
		this(null, Classes.none(), null, List.of());
	}

	public Code id(String id) {
		return new Code(id, this.classes, this.text, this.children);
	}

	public Code classes(Classes classes) {
		return new Code(this.id, classes, this.text, this.children);
	}

	public Code text(String text) {
		return new Code(this.id, this.classes, text, this.children);
	}

	public Code children(List<Element> children) {
		return new Code(this.id, this.classes, this.text, children);
	}

	public Code children(Element... children) {
		return new Code(this.id, this.classes, this.text, List.of(children));
	}

}
