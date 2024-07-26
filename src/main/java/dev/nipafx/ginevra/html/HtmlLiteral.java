package dev.nipafx.ginevra.html;

public record HtmlLiteral(String literal) implements GmlElement {

	public HtmlLiteral() {
		this(null);
	}

	public HtmlLiteral literal(String literal) {
		return new HtmlLiteral(literal);
	}

}
