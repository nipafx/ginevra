package dev.nipafx.ginevra.html;

public record Meta(String name, String content) implements HtmlElement {

	public Meta() {
		this(null, null);
	}

	public Meta name(String name) {
		return new Meta(name, this.content);
	}

	public Meta content(String content) {
		return new Meta(this.name, content);
	}

}
