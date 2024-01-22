package dev.nipafx.ginevra.html;

import java.util.List;

public record Body(List<Element> content) implements HtmlElement {

	public Body() {
		this(List.of());
	}

	public Body elements(List<Element> elements) {
		return new Body(elements);
	}

}
