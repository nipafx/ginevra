package dev.nipafx.ginevra.html;

public record Source(Src src, String type) implements HtmlElement {

	public Source() {
		this(Src.none(), null);
	}

	public Source src(Src src) {
		return new Source(src, this.type);
	}

	public Source type(String type) {
		return new Source(this.src, type);
	}

}
