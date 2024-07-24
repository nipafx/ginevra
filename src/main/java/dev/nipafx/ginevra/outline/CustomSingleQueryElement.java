package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.Element;

import java.util.List;

public interface CustomSingleQueryElement<DOCUMENT extends Record & Document> extends CustomQueryElement<DOCUMENT> {

	@Override
	default List<Element> compose(DOCUMENT document) {
		return List.of(composeSingle(document));
	}

	Element composeSingle(DOCUMENT document);

}
