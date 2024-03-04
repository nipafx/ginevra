package dev.nipafx.site.nipafx_dev.templates;

import dev.nipafx.ginevra.css.Css;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.outline.RenderedDocumentData;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.site.nipafx_dev.components.Header;
import dev.nipafx.site.nipafx_dev.components.Header.ChannelTags;
import dev.nipafx.site.nipafx_dev.components.Layout;
import dev.nipafx.site.nipafx_dev.data.ArticleData;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.head;

public class ArticlePage implements Template<ArticleData.Page>, CssStyled<ArticlePage.Style> {

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public record Style(String style) implements CssStyle { }
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
			""");

	@Override
	public Style style() {
		return STYLE;
	}

	@Override
	public RenderedDocumentData render(ArticleData.Page article) {
		return new RenderedDocumentData(Path.of(article.slug()), renderArticle(article));
	}

	private HtmlDocument renderArticle(ArticleData.Page article) {
		return document
				.language(Locale.US)
				.head(head
						.charset(StandardCharsets.UTF_8)
						.title(article.title()))
				.body(body.children(new Layout(List.of(
						new Header(
								article.title(),
								article.description(),
								DATE.format(article.date()),
								Optional.of(new ChannelTags("post", article.tags()))),
						div.children(article.contentParsedAsMarkdown())))));
	}

}
