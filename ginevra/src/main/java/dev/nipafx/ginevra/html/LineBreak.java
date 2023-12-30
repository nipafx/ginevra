package dev.nipafx.ginevra.html;

public record LineBreak(String id, Classes classes) implements HtmlElement {

	public LineBreak() {
		this(null, Classes.none());
	}

	public LineBreak id(String id) {
		return new LineBreak(id, this.classes);
	}

	public LineBreak classes(Classes classes) {
		return new LineBreak(this.id, classes);
	}

}
