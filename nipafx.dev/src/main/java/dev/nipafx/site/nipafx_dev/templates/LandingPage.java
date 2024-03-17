package dev.nipafx.site.nipafx_dev.templates;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.HtmlDocumentData;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.site.nipafx_dev.components.ArticleList;
import dev.nipafx.site.nipafx_dev.components.Header;
import dev.nipafx.site.nipafx_dev.data.LandingPageData;

import java.nio.file.Path;
import java.util.Optional;

import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.site.nipafx_dev.components.Components.articleList;
import static dev.nipafx.site.nipafx_dev.components.Components.header;
import static dev.nipafx.site.nipafx_dev.components.Components.layout;

public class LandingPage implements Template<LandingPageData> {

	@Override
	public HtmlDocumentData compose(LandingPageData page) {
		return new HtmlDocumentData(Path.of(""), composePage(page));
	}

	private Element composePage(LandingPageData page) {
		return layout
				.head(head.title(page.title()))
				.children(
						header(
								"You. Me. Java.",
								"Nice of you to stop by. I'm nipafx, but you can call me Nicolai \uD83D\uDE09, a Java enthusiast with a passion for learning and sharing, online and offline. If you want to sharpen your Java skills, you've come to the right place.",
								"Welcome to nipafx.dev. On here, it's:",
								Optional.empty()),
						articleList(page.articles()));
	}

}
