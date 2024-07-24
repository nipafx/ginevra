package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Element;

import java.util.List;

public interface CustomQueryElement<DOCUMENT extends Record & Document> extends CustomElement {

	@Override
	default List<Element> compose() {
		throw new IllegalArgumentException("This custom element can't be composed without passing a query result");
	}

	Query<DOCUMENT> query();

	List<Element> compose(DOCUMENT document);

}
