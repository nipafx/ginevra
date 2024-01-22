package dev.nipafx.site;

import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Id;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.RenderedDocumentData;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Store.CollectionQuery;
import dev.nipafx.ginevra.outline.Store.DocCollection;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.site.data.ArticlePage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.head;

public class ArticleTemplate implements Template<ArticlePage> {

	private final DocCollection articles;

	public ArticleTemplate(DocCollection articles) {
		this.articles = articles;
	}

	@Override
	public Store.Query<ArticlePage> getQuery() {
		return new CollectionQuery<>(articles, ArticlePage.class);
	}

	@Override
	public Document<RenderedDocumentData> render(Id dataId, ArticlePage articlePage) {
		return new GeneralDocument<>(
				dataId.transform("article-template"),
				new RenderedDocumentData(Path.of(articlePage.slug()), renderArticle(articlePage)));
	}

	private HtmlDocument renderArticle(ArticlePage article) {
		return document
				.language(Locale.US)
				.head(head
						.charset(StandardCharsets.UTF_8)
						.title(article.title()))
				.body(body.elements(article.contentParsedAsMarkdown()));
	}

}
