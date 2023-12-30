package dev.nipafx.ginevra.html;

public record HtmlLiteral(String literal) implements JmlElement {

	public HtmlLiteral() {
		this(null);
	}

	public HtmlLiteral literal(String literal) {
		return new HtmlLiteral(literal);
	}

}
