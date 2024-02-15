package dev.nipafx.ginevra.html;

public record Link(String href, String rel) implements HtmlElement {

	public Link() {
		this(null, null);
	}

	public Link href(String href) {
		return new Link(href, this.rel);
	}

	public Link rel(String rel) {
		return new Link(this.href, rel);
	}

}
