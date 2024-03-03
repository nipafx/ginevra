package dev.nipafx.ginevra.html;

import java.util.List;

public record Div(Id id, Classes classes, List<? extends Element> children) implements HtmlElement {

	public Div {
		children = List.copyOf(children);
	}

	public Div() {
		 this(Id.none(), Classes.none(), List.of());
	}

	public Div id(Id id) {
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
