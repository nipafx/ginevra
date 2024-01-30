package dev.nipafx.ginevra.html;

import java.util.List;

public record Anchor(String id, Classes classes, String href, String title, String text, List<Element> children) implements HtmlElement {

	public Anchor {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public Anchor() {
		this(null, Classes.none(), null, null, null, List.of());
	}

	public Anchor id(String id) {
		return new Anchor(id, this.classes, this.href, this.title, this.text, this.children);
	}

	public Anchor classes(Classes classes) {
		return new Anchor(this.id, classes, this.href, this.title, this.text, this.children);
	}

	public Anchor href(String href) {
		return new Anchor(this.id, this.classes, href, this.title, this.text, this.children);
	}

	public Anchor title(String title) {
		return new Anchor(this.id, this.classes, this.href, title, this.text, this.children);
	}

	public Anchor text(String text) {
		return new Anchor(this.id, this.classes, this.href, this.title, text, this.children);
	}

	public Anchor children(List<Element> children) {
		return new Anchor(this.id, this.classes, this.href, this.title, this.text, children);
	}

	public Anchor children(Element... children) {
		return new Anchor(this.id, this.classes, this.href, this.title, this.text, List.of(children));
	}

}
