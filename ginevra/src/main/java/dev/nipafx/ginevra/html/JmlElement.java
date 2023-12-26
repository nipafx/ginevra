package dev.nipafx.ginevra.html;

public sealed interface JmlElement extends KnownElement permits Text {

	Text text = new Text();

}
