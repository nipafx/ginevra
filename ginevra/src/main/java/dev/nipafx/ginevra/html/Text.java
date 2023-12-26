package dev.nipafx.ginevra.html;

public record Text(String text) implements JmlElement {

	Text() {
		this(null);
	}

}
