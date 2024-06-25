package dev.nipafx.ginevra.html;

import java.util.List;

public interface CustomSingleElement extends CustomElement {

	@Override
	default List<Element> compose() {
		return List.of(composeSingle());
	}

	Element composeSingle();

}
