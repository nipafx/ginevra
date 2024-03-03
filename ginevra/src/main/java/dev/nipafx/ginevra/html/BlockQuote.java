package dev.nipafx.ginevra.html;

import java.util.List;

public record BlockQuote(Id id, Classes classes, String text, List<? extends Element> children) implements HtmlElement {

	public BlockQuote {
		var textChildren = new TextChildren(text, children);
		text = textChildren.text();
		children = textChildren.children();
	}

	public BlockQuote() {
		 this(Id.none(), Classes.none(), null, List.of());
	}

	public BlockQuote id(Id id) {
		return new BlockQuote(id, this.classes, this.text, this.children);
	}

	public BlockQuote classes(Classes classes) {
		return new BlockQuote(this.id, classes, this.text, this.children);
	}

	public BlockQuote text(String text) {
		return new BlockQuote(this.id, this.classes, text, this.children);
	}

	public BlockQuote children(List<? extends Element> children) {
		return new BlockQuote(this.id, this.classes, this.text, children);
	}

	public BlockQuote children(Element... children) {
		return new BlockQuote(this.id, this.classes, this.text, List.of(children));
	}

}
