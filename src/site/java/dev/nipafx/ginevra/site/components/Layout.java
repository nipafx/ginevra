package dev.nipafx.ginevra.site.components;

import dev.nipafx.ginevra.css.Css;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomSingleElement;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Head;
import dev.nipafx.ginevra.html.HtmlElement;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.outline.Resources;
import dev.nipafx.ginevra.site.components.Layout.Style;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.document;

public record Layout(Head head, List<? extends Element> children) implements CustomSingleElement, CssStyled<Style> {

	public Layout {
		if (head == null)
			head = HtmlElement.head;
		if (head.charset() == null)
			head.charset(StandardCharsets.UTF_8);
	}

	public record Style(Classes layout, Classes content, Css css) implements CssStyle { }
	private static final Style STYLE = Css.parse(Style.class, """
			body {
				margin: 0;
				min-width: 320px;

				--fg-color: white;
				--fg-color-muted: #aaa;
				--bg-color: #262429;
				--alt-color: #A83EA6;

				color: var(--fg-color);
			}

			a {
				color: var(--fg-color);
				text-decoration: none;
			}

			a:visited {
				color: var(--fg-color-muted);
				text-decoration: none;
			}

			a:hover {
				text-decoration: underline;
			}

			.layout {
				display: grid;
				grid-template-columns: 10px 1fr 10px;
				grid-template-areas:
					". content .";

				background-color: var(--bg-color);
				color: white;
			}

			.content {
				grid-area: content;
			}

			@media all and (min-width: 620px) {
				.layout {
					grid-template-columns: 1fr 600px 1fr;
				}
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
						div.classes(STYLE.content).children(children)
				));
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
