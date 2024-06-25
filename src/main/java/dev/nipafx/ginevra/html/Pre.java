package dev.nipafx.ginevra.html;

import java.util.List;

public record Pre(Id id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public Pre {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public Pre() {
		 this(Id.none(), Classes.none(), null, List.of());
	}

	public Pre id(Id id) {
		return new Pre(id, this.classes, this.text, this.children);
	}

	public Pre classes(Classes classes) {
		return new Pre(this.id, classes, this.text, this.children);
	}

	public Pre text(String text) {
		return new Pre(this.id, this.classes, text, this.children);
	}

	public Pre children(List<? extends Element> children) {
		return new Pre(this.id, this.classes, this.text, children);
	}

	public Pre children(Element... children) {
		return new Pre(this.id, this.classes, this.text, List.of(children));
	}

}
