package dev.nipafx.ginevra.html;

public record Image(String id, Classes classes, String src, String title, String alt) implements HtmlElement {

	public Image() {
		this(null, Classes.none(), null, null, null);
	}

	public Image id(String id) {
		return new Image(id, this.classes, this.src, this.title, this.alt);
	}

	public Image classes(Classes classes) {
		return new Image(this.id, classes, this.src, this.title, this.alt);
	}

	public Image src(String src) {
		return new Image(this.id, this.classes, src, this.title, this.alt);
	}

	public Image title(String title) {
		return new Image(this.id, this.classes, this.src, title, this.alt);
	}

	public Image alt(String alt) {
		return new Image(this.id, this.classes, this.src, this.title, alt);
	}

}
