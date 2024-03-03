package dev.nipafx.site.nipafx_dev.components;

import dev.nipafx.ginevra.css.Css;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.site.nipafx_dev.components.ArticleList.Style;
import dev.nipafx.site.nipafx_dev.data.ArticleData;

import java.util.Comparator;
import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.a;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.h2;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static java.util.Comparator.comparing;

public record ArticleList(List<ArticleData.Page> articles) implements CustomElement, CssStyled<Style> {

	public record Style(Classes container, Classes article, Classes title, Classes description, String style) implements CssStyle { }
	private static final Style STYLE = Css.parse(Style.class, """
			.container {
				display: flex;
				flex-direction: column;
				gap: 24px;
			}
			
			.article {
			
			}
			
			#title {
				margin: 0;
				font-size: 1.5em;
				font-weight: bold;
			}
			
			#description {
				margin: 12px 0 0;
			}
			""");

	@Override
	public Element renderSingle() {
		return div.classes(STYLE.container)
				.children(articles.stream()
						.sorted(comparing(ArticleData.Page::date).reversed())
						.map(this::renderArticle)
						.toList());
	}

	private Element renderArticle(ArticleData.Page article) {
		return div.classes(STYLE.article).children(
				h2.classes(STYLE.article).children(
						a.href(article.slug()).text(article.title())),
				p.classes(STYLE.description).text(article.description())
		);
	}

	@Override
	public Style style() {
		return STYLE;
	}

}