package dev.nipafx.ginevra.html;

import java.util.List;

public record Strong(Id id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public Strong {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public Strong() {
		 this(Id.none(), Classes.none(), null, List.of());
	}

	public Strong id(Id id) {
		return new Strong(id, this.classes, this.text, this.children);
	}

	public Strong classes(Classes classes) {
		return new Strong(this.id, classes, this.text, this.children);
	}

	public Strong text(String text) {
		return new Strong(this.id, this.classes, text, this.children);
	}

	public Strong children(List<? extends Element> children) {
		return new Strong(this.id, this.classes, this.text, children);
	}

	public Strong children(Element... children) {
		return new Strong(this.id, this.classes, this.text, List.of(children));
	}

}
