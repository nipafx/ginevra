package dev.nipafx.ginevra.html;

import java.nio.charset.Charset;

public record Head(String title, Charset charset) implements HtmlElement {

	public Head() {
		this(null, null);
	}

	public Head title(String title) {
		return new Head(title, this.charset);
	}

	public Head charset(Charset charset) {
		return new Head(this.title, charset);
	}

}
