package dev.nipafx.ginevra.html;

import java.util.List;

public record Div(String id, Classes classes, List<? extends Element> children) implements HtmlElement {

	public Div {
		children = List.copyOf(children);
	}

	public Div() {
		this(null, Classes.none(), List.of());
	}

	public Div id(String id) {
		return new Div(id, this.classes, this.children);
	}

	public Div classes(Classes classes) {
		return new Div(this.id, classes, this.children);
	}

	public Div children(List<? extends Element> children) {
		return new Div(this.id, this.classes, children);
	}

	public Div children(Element... children) {
		return new Div(this.id, this.classes, List.of(children));
	}

}
