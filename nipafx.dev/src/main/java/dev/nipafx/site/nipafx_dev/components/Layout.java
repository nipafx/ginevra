package dev.nipafx.site.nipafx_dev.components;

import dev.nipafx.ginevra.css.Css;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.CustomSingleElement;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Head;
import dev.nipafx.ginevra.html.HtmlElement;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.head;

public record Layout(Head head, List<? extends Element> children) implements CustomSingleElement, CssStyled<Layout.Style> {

	public Layout {
		if (head == null)
			head = HtmlElement.head;
		if (head.charset() == null)
			head.charset(StandardCharsets.UTF_8);
	}

	public record Style(Classes layout, Classes content, String style) implements CssStyle { }
	private static final Style STYLE = Css.parse(Style.class, """
			body {
				margin: 0;
			}
			
			a {
				color: white;
			}
			
			a:visited {
				color: #aaa;
			}
			
			.layout {
				display: grid;
				grid-template-columns: 1fr 600px 1fr;
				grid-template-areas:
					". content .";
			
				background-color: #262429;
				color: white;
			}
			
			.content {
				grid-area: content;
			}
			""");

	@Override
	public Style style() {
		return STYLE;
	}

	@Override
	public Element composeSingle() {
		return document
				.language(Locale.US)
				.head(head)
				.body(body.classes(STYLE.layout).children(
						div.classes(STYLE.content).children(children)));
	}

	public Layout head(Head head) {
		return new Layout(head, this.children);
	}

	public Layout children(List<? extends Element> children) {
		return new Layout(this.head, children);
	}

	public Layout children(Element... children) {
		return new Layout(this.head, List.of(children));
	}

}
