package dev.nipafx.ginevra.html;

import java.util.List;

public record Paragraph(Id id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public Paragraph {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public Paragraph() {
		 this(Id.none(), Classes.none(), null, List.of());
	}

	public Paragraph id(Id id) {
		return new Paragraph(id, this.classes, this.text, this.children);
	}

	public Paragraph classes(Classes classes) {
		return new Paragraph(this.id, classes, this.text, this.children);
	}

	public Paragraph text(String text) {
		return new Paragraph(this.id, this.classes, text, this.children);
	}

	public Paragraph children(List<? extends Element> children) {
		return new Paragraph(this.id, this.classes, this.text, children);
	}

	public Paragraph children(Element... children) {
		return new Paragraph(this.id, this.classes, this.text, List.of(children));
	}

}
