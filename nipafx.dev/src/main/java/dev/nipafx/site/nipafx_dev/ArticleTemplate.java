package dev.nipafx.site.nipafx_dev;

import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.ginevra.outline.RenderedDocumentData;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.site.nipafx_dev.data.ArticlePage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.h1;
import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.HtmlElement.span;

public class ArticleTemplate implements Template<ArticlePage> {

	@Override
	public RenderedDocumentData render(ArticlePage article) {
		return new RenderedDocumentData(Path.of(article.slug()), renderArticle(article));
	}

	private HtmlDocument renderArticle(ArticlePage article) {
		return document
				.language(Locale.US)
				.head(head
						.charset(StandardCharsets.UTF_8)
						.title(article.title()))
				.body(body.children(List.of(
						h1.text(article.title()),
						p.text(article.description()),
						div.children(article
								.tags().stream()
								.map(tag -> span.text("#" + tag))
								.toArray(Span[]::new)),
						div.children(article.contentParsedAsMarkdown()))));
	}

}
