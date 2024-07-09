package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.Element;

import java.util.List;

public interface CustomSingleQueryElement<DOCUMENT extends Record & Document> extends CustomQueryElement<DOCUMENT> {

	@Override
	default List<Element> compose(DOCUMENT data) {
		return List.of(composeSingle(data));
	}

	Element composeSingle(DOCUMENT data);

}
