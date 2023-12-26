package dev.nipafx.ginevra.html;

import java.util.List;

public non-sealed interface CustomElement extends Element {

	default List<Element> render() {
		return List.of(renderSingle());
	}

	default Element renderSingle() {
		throw new IllegalStateException("No `render...` method implemented");
	}

}
