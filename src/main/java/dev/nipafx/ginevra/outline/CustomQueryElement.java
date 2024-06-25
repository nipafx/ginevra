package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

public interface CustomQueryElement<DATA extends Record & Data> extends CustomElement {

	@Override
	default List<Element> compose() {
		throw new IllegalArgumentException("This custom element can't be composed without passing a query result");
	}

	Query<DATA> query();

	List<Element> compose(DATA data);

}
