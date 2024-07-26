package dev.nipafx.ginevra.html;

public record Text(String text) implements GmlElement {

	public Text() {
		this(null);
	}

	public Text text(String text) {
		return new Text(text);
	}

}
