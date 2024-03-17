package dev.nipafx.site.nipafx_dev.templates;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.HtmlDocumentData;
import dev.nipafx.ginevra.outline.Query;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.site.nipafx_dev.components.Header.ChannelTags;
import dev.nipafx.site.nipafx_dev.data.ArticleData;
import dev.nipafx.site.nipafx_dev.data.ArticleData.Page;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.site.nipafx_dev.components.Components.header;
import static dev.nipafx.site.nipafx_dev.components.Components.layout;

public class ArticlePage implements Template<ArticleData.Page> {

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	public Query<Page> query() {
		return new CollectionQuery<>("articles", ArticleData.Page.class);
	}

	@Override
	public HtmlDocumentData compose(ArticleData.Page article) {
		return new HtmlDocumentData(Path.of(article.slug()), composeArticle(article));
	}

	private Element composeArticle(ArticleData.Page article) {
		return layout
				.head(head.title(article.title()))
				.children(
						header(
								article.title(),
								article.description(),
								DATE.format(article.date()),
								Optional.of(new ChannelTags("post", article.tags()))),
						div.children(article.contentParsedAsMarkdown()));
	}

}
