package dev.nipafx.ginevra.html;

import java.util.List;

public record Code(String id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public Code {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
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

	public Code children(List<? extends Element> children) {
		return new Code(this.id, this.classes, this.text, children);
	}

	public Code children(Element... children) {
		return new Code(this.id, this.classes, this.text, List.of(children));
	}

}
