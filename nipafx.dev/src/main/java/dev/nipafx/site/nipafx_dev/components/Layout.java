package dev.nipafx.site.nipafx_dev.components;

import dev.nipafx.ginevra.css.Css;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Element;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.div;

public record Layout(List<Element> children) implements CustomElement, CssStyled<Layout.Style> {

	public record Style(Classes layout, Classes content, String style) implements CssStyle { }
	private static final Style STYLE = Css.parse(Style.class, """
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
	public Element renderSingle() {
		return div.classes(STYLE.layout).children(div.classes(STYLE.content).children(children));
	}

}
