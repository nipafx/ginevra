package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

public interface CustomSingleQueryElement<DATA extends Record & Data> extends CustomQueryElement<DATA> {

	@Override
	default List<Element> compose(DATA data) {
		return List.of(composeSingle(data));
	}

	Element composeSingle(DATA data);

}
