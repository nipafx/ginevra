package dev.nipafx.ginevra.outline;

public interface Template<DOCUMENT extends Record & Document> {

	Query<DOCUMENT> query();

	HtmlPage compose(DOCUMENT document);

}
