package dev.nipafx.ginevra.html;

import java.util.List;

public record Body(List<? extends Element> children) implements HtmlElement {

	public Body() {
		this(List.of());
	}

	public Body children(List<? extends Element> children) {
		return new Body(children);
	}

	public Body children(Element... children) {
		return new Body(List.of(children));
	}

}
