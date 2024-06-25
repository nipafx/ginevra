package dev.nipafx.ginevra.html;

public record Image(Id id, Classes classes, Src src, String title, String alt) implements HtmlElement {

	public Image() {
		 this(Id.none(), Classes.none(), Src.none(), null, null);
	}

	public Image id(Id id) {
		return new Image(id, this.classes, this.src, this.title, this.alt);
	}

	public Image classes(Classes classes) {
		return new Image(this.id, classes, this.src, this.title, this.alt);
	}

	public Image src(Src src) {
		return new Image(this.id, this.classes, src, this.title, this.alt);
	}

	public Image title(String title) {
		return new Image(this.id, this.classes, this.src, title, this.alt);
	}

	public Image alt(String alt) {
		return new Image(this.id, this.classes, this.src, this.title, alt);
	}

}
