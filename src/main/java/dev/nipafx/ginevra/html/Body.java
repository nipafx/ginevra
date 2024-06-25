package dev.nipafx.ginevra.html;

import java.util.List;

public record Body(Id id, Classes classes, List<? extends Element> children) implements HtmlElement {

	public Body() {
		this(Id.none(), Classes.none(), List.of());
	}

	public Body id(Id id) {
		return new Body(id, this.classes, this.children);
	}

	public Body classes(Classes classes) {
		return new Body(this.id, classes, this.children);
	}

	public Body children(List<? extends Element> children) {
		return new Body(this.id, this.classes, children);
	}

	public Body children(Element... children) {
		return new Body(this.id, this.classes, List.of(children));
	}

}
