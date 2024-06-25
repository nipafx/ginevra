package dev.nipafx.ginevra.html;

import java.nio.charset.Charset;
import java.util.List;

public record Head(String title, Charset charset, List<? extends Element> children) implements HtmlElement {

	public Head() {
		this(null, null, List.of());
	}

	public Head title(String title) {
		return new Head(title, this.charset, this.children);
	}

	public Head charset(Charset charset) {
		return new Head(this.title, charset, this.children);
	}

	public Head children(List<? extends Element> children) {
		return new Head(this.title, this.charset, children);
	}

	public Head children(Element... children) {
		return new Head(this.title, this.charset, List.of(children));
	}

}
