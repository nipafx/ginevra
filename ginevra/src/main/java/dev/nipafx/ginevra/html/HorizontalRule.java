package dev.nipafx.ginevra.html;

public record HorizontalRule(Id id, Classes classes) implements HtmlElement {

	public HorizontalRule() {
		 this(Id.none(), Classes.none());
	}

	public HorizontalRule id(Id id) {
		return new HorizontalRule(id, this.classes);
	}

	public HorizontalRule classes(Classes classes) {
		return new HorizontalRule(this.id, classes);
	}

}
