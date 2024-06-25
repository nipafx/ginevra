package dev.nipafx.ginevra.html;

public record LineBreak(Id id, Classes classes) implements HtmlElement {

	public LineBreak() {
		 this(Id.none(), Classes.none());
	}

	public LineBreak id(Id id) {
		return new LineBreak(id, this.classes);
	}

	public LineBreak classes(Classes classes) {
		return new LineBreak(this.id, classes);
	}

}
