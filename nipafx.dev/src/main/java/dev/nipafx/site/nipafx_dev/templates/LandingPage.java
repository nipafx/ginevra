package dev.nipafx.site.nipafx_dev.templates;

import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.outline.RenderedDocumentData;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.site.nipafx_dev.components.ArticleList;
import dev.nipafx.site.nipafx_dev.components.Header;
import dev.nipafx.site.nipafx_dev.components.Layout;
import dev.nipafx.site.nipafx_dev.data.ArticleData;
import dev.nipafx.site.nipafx_dev.data.LandingPageData;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.head;

public class LandingPage implements Template<LandingPageData> {

	@Override
	public RenderedDocumentData render(LandingPageData page) {
		return new RenderedDocumentData(Path.of(""), renderPage(page));
	}

	private HtmlDocument renderPage(LandingPageData page) {
		return document
				.language(Locale.US)
				.head(head
						.charset(StandardCharsets.UTF_8)
						.title(page.title()))
				.body(body.children(new Layout(List.of(
						new Header(
								"You. Me. Java.",
								"Nice of you to stop by. I'm nipafx, but you can call me Nicolai \uD83D\uDE09, a Java enthusiast with a passion for learning and sharing, online and offline. If you want to sharpen your Java skills, you've come to the right place.",
								"Welcome to nipafx.dev. On here, it's:",
								Optional.empty()),
						new ArticleList(page.articles())))));
	}

}
