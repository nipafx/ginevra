package dev.nipafx.ginevra.outline;

import java.util.stream.Stream;

public interface Template<DOCUMENT extends Record & Document> {

	Query<DOCUMENT> query();

	default HtmlPage compose(DOCUMENT document) {
		throw new UnsupportedOperationException();
	}

	default Stream<HtmlPage> composeMany(DOCUMENT document) {
		return Stream.of(compose(document));
	}

}
