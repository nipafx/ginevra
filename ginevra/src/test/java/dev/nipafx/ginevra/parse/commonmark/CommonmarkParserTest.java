package dev.nipafx.ginevra.parse.commonmark;

import dev.nipafx.ginevra.html.Element;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.a;
import static dev.nipafx.ginevra.html.HtmlElement.h1;
import static dev.nipafx.ginevra.html.HtmlElement.h2;
import static dev.nipafx.ginevra.html.HtmlElement.h3;
import static dev.nipafx.ginevra.html.HtmlElement.h4;
import static dev.nipafx.ginevra.html.HtmlElement.h5;
import static dev.nipafx.ginevra.html.HtmlElement.h6;
import static dev.nipafx.ginevra.html.HtmlElement.hr;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.JmlElement.codeBlock;
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

	// --- section 4 - Leaf blocks

	// it is necessary to test all header levels in one go because the CommonMark parser corrects the heading level
	// if the Markdown gets it wrong, e.g. the standalone string "## header" gets parsed to an h1 node
	@Test
	void headers() {
		parseAndAssert(
				"""
				# Header 1
				## Header 2
				### Header 3
				#### Header 4
				##### Header 5
				###### Header 6
				""",
				h1.text("Header 1"),
				h2.text("Header 2"),
				h3.text("Header 3"),
				h4.text("Header 4"),
				h5.text("Header 5"),
				h6.text("Header 6"));
	}

	@Test
	void horizontalRule() {
		parseAndAssert(
				"---",
				hr);
	}

	@Test
	void codeBlockWithoutLanguage() {
		parseAndAssert(
				"""
				```
				void main() { println("When?"); }
				```
				""",
				codeBlock.text("void main() { println(\"When?\"); }\n"));
	}

	@Test
	void codeBlockWithLanguage() {
		parseAndAssert(
				"""
				```java
				void main() { println("When?"); }
				```
				""",
				codeBlock.language("java").text("void main() { println(\"When?\"); }\n"));
	}

	@Test
	void linkReference() {
		parseAndAssert(
				"""
				[nipafx.dev]: <>

				[nipafx.dev]
				""",
				p.children(a.text("nipafx.dev")));
	}

	@Test
	void linkReferenceWithHrefAndTitle() {
		parseAndAssert(
				"""
				[nipafx.dev]: https://nipafx.dev "nipafx"

				[nipafx.dev]
				""",
				p.children(a.href("https://nipafx.dev").title("nipafx").text("nipafx.dev")));
	}

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
