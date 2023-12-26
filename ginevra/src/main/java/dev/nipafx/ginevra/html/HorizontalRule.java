package dev.nipafx.ginevra.html;

public record HorizontalRule(String id, Classes classes) implements HtmlElement {

	public HorizontalRule() {
		this(null, Classes.none());
	}

	public HorizontalRule id(String id) {
		return new HorizontalRule(id, this.classes);
	}

	public HorizontalRule classes(Classes classes) {
		return new HorizontalRule(this.id, classes);
	}

}
