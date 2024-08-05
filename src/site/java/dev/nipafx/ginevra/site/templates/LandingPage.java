package dev.nipafx.ginevra.site.templates;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.HtmlPage;
import dev.nipafx.ginevra.outline.Query;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.site.documents.LandingPageDoc;

import java.nio.file.Path;

import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.h1;
import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.ginevra.site.components.Components.layout;

public class LandingPage implements Template<LandingPageDoc> {

	@Override
	public Query<LandingPageDoc> query() {
		return new RootQuery<>(LandingPageDoc.class);
	}

	@Override
	public HtmlPage compose(LandingPageDoc document) {
		return new HtmlPage(Path.of(""), composePage(document));
	}

	private Element composePage(LandingPageDoc page) {
		return layout
				.head(head.title(page.title()))
				.children(
						h1.text(page.title()),
						div.children(page
								.landingPageTexts().stream()
								.map(text -> div.children(text.text().elements()))
								.toList())
				);
	}

}
