package dev.nipafx.ginevra.html;

import java.util.List;

public record ListItem(String id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public ListItem {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public ListItem() {
		this(null, Classes.none(), null, List.of());
	}

	public ListItem id(String id) {
		return new ListItem(id, this.classes, this.text, this.children);
	}

	public ListItem classes(Classes classes) {
		return new ListItem(this.id, classes, this.text, this.children);
	}

	public ListItem text(String text) {
		return new ListItem(this.id, this.classes, text, this.children);
	}

	public ListItem children(List<? extends Element> children) {
		return new ListItem(this.id, this.classes, this.text, children);
	}

	public ListItem children(Element... children) {
		return new ListItem(this.id, this.classes, this.text, List.of(children));
	}

}
