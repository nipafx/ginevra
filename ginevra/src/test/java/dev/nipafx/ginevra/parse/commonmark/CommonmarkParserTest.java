package dev.nipafx.ginevra.parse.commonmark;

import dev.nipafx.ginevra.html.Element;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.p;
import static org.assertj.core.api.Assertions.assertThat;

class CommonmarkParserTest {

	private final CommonmarkParser parser = new CommonmarkParser(Parser.builder().build());

	private void parseAndAssert(String markdown, Element... elements) {
		var parsed = parser.parse(markdown);
		assertThat(parsed).containsExactly(elements);
	}

	/*
	 * This order of tests follows the CommonMark Spec 0.3.0
	 * (https://spec.commonmark.org/0.30/)
	 */

	@Test
	void oneParagraph() {
		parseAndAssert(
				"A line of text",
				p.text("A line of text")
		);
	}

	@Test
	void twoParagraphs() {
		parseAndAssert(
				"""
				A paragraph of text.

				Another paragraph of text.
				""",
				p.text("A paragraph of text."),
				p.text("Another paragraph of text.")
		);
	}

}
