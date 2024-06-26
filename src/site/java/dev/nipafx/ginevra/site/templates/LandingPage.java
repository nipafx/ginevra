package dev.nipafx.ginevra.site.templates;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.HtmlDocumentData;
import dev.nipafx.ginevra.outline.Query;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.site.data.LandingPageData;

import java.nio.file.Path;

import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.h1;
import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.ginevra.site.components.Components.layout;

public class LandingPage implements Template<LandingPageData> {

	@Override
	public Query<LandingPageData> query() {
		return new RootQuery<>(LandingPageData.class);
	}

	@Override
	public HtmlDocumentData compose(LandingPageData page) {
		return new HtmlDocumentData(Path.of(""), composePage(page));
	}

	private Element composePage(LandingPageData page) {
		return layout
				.head(head.title(page.title()))
				.children(
						h1.text(page.title()),
						div.children(page
								.landingPageTexts().stream()
								.map(text -> div.children(text.text()))
								.toList()));
	}

}
