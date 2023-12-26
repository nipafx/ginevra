package dev.nipafx.ginevra.html;

public record Text(String text) implements JmlElement {

	public Text() {
		this(null);
	}

}
