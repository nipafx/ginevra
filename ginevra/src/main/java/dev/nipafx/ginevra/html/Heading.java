package dev.nipafx.ginevra.html;

import java.util.List;

public record Heading(int level, String id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public Heading {
		if (level < 1 || 6 < level)
			throw new IllegalArgumentException("Heading level must be between 1 and 6 (both inclusive)");
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public Heading(int level) {
		this(level, null, Classes.none(), null, List.of());
	}

	public Heading id(String id) {
		return new Heading(this.level, id, this.classes, this.text, this.children);
	}

	public Heading classes(Classes classes) {
		return new Heading(this.level, this.id, classes, this.text, this.children);
	}

	public Heading text(String text) {
		return new Heading(this.level, this.id, this.classes, text, this.children);
	}

	public Heading children(List<? extends Element> children) {
		return new Heading(this.level, this.id, this.classes, this.text, children);
	}

	public Heading children(Element... children) {
		return new Heading(this.level, this.id, this.classes, this.text, List.of(children));
	}

}
