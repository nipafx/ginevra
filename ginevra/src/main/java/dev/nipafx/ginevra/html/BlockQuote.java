package dev.nipafx.ginevra.html;

import java.util.List;

public record BlockQuote(String id, Classes classes, String text, List<Element> children) implements HtmlElement {

	public BlockQuote {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		} else
			children = List.copyOf(children);
	}

	public BlockQuote() {
		this(null, Classes.none(), null, List.of());
	}

	public BlockQuote id(String id) {
		return new BlockQuote(id, this.classes, this.text, this.children);
	}

	public BlockQuote classes(Classes classes) {
		return new BlockQuote(this.id, classes, this.text, this.children);
	}

	public BlockQuote text(String text) {
		return new BlockQuote(this.id, this.classes, text, this.children);
	}

	public BlockQuote children(List<Element> children) {
		return new BlockQuote(this.id, this.classes, this.text, children);
	}

	public BlockQuote children(Element... children) {
		return new BlockQuote(this.id, this.classes, this.text, List.of(children));
	}

}
