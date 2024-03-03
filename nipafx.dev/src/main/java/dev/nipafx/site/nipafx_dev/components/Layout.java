package dev.nipafx.site.nipafx_dev.components;

import dev.nipafx.ginevra.css.Css;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.site.nipafx_dev.components.Header.Style;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.h1;
import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.HtmlElement.span;

public record Layout(List<Element> children) implements CustomElement, CssStyled<Layout.Style> {

	public record Style(Classes layout, Classes content, String style) implements CssStyle { }
	private static final Style STYLE = Css.parse(Style.class, """
			body {
				margin: 0;
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
			
			a {
				color: white;
			}
			
			a:visited {
				color: #aaa;
			}
			""");

	@Override
	public Element renderSingle() {
		return div.classes(STYLE.layout).children(div.classes(STYLE.content).children(children));
	}

	@Override
	public Style style() {
		return STYLE;
	}

}
