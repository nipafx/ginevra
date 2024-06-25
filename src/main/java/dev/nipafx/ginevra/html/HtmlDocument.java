package dev.nipafx.ginevra.html;

import java.util.Locale;

public record HtmlDocument(Locale language, Head head, Body body) implements HtmlElement {

	public HtmlDocument() {
		this(null, null, null);
	}

	public HtmlDocument language(Locale language) {
		return new HtmlDocument(language, this.head, this.body);
	}

	public HtmlDocument head(Head head) {
		return new HtmlDocument(this.language, head, this.body);
	}

	public HtmlDocument body(Body body) {
		return new HtmlDocument(this.language, this.head, body);
	}

}
