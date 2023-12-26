package dev.nipafx.ginevra.html;

import java.util.List;

public record Paragraph(String id, Classes classes, String text, List<Element> children) implements HtmlElement {

	public Paragraph {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		} else
			children = List.copyOf(children);
	}

	public Paragraph() {
		this(null, Classes.none(), null, List.of());
	}

	public Paragraph id(String id) {
		return new Paragraph(id, this.classes, this.text, this.children);
	}

	public Paragraph classes(Classes classes) {
		return new Paragraph(this.id, classes, this.text, this.children);
	}

	public Paragraph text(String text) {
		return new Paragraph(this.id, this.classes, text, this.children);
	}

	public Paragraph children(List<Element> children) {
		return new Paragraph(this.id, this.classes, this.text, children);
	}

	public Paragraph children(Element... children) {
		return new Paragraph(this.id, this.classes, this.text, List.of(children));
	}

}
