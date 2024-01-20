package dev.nipafx.ginevra.parse.commonmark;

import dev.nipafx.ginevra.html.Element;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.a;
import static dev.nipafx.ginevra.html.HtmlElement.blockquote;
import static dev.nipafx.ginevra.html.HtmlElement.br;
import static dev.nipafx.ginevra.html.HtmlElement.code;
import static dev.nipafx.ginevra.html.HtmlElement.em;
import static dev.nipafx.ginevra.html.HtmlElement.h1;
import static dev.nipafx.ginevra.html.HtmlElement.h2;
import static dev.nipafx.ginevra.html.HtmlElement.h3;
import static dev.nipafx.ginevra.html.HtmlElement.h4;
import static dev.nipafx.ginevra.html.HtmlElement.h5;
import static dev.nipafx.ginevra.html.HtmlElement.h6;
import static dev.nipafx.ginevra.html.HtmlElement.hr;
import static dev.nipafx.ginevra.html.HtmlElement.li;
import static dev.nipafx.ginevra.html.HtmlElement.ol;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.HtmlElement.strong;
import static dev.nipafx.ginevra.html.HtmlElement.ul;
import static dev.nipafx.ginevra.html.JmlElement.codeBlock;
import static dev.nipafx.ginevra.html.JmlElement.html;
import static dev.nipafx.ginevra.html.JmlElement.text;
import static org.assertj.core.api.Assertions.assertThat;

class CommonmarkParserTest {

	private final CommonmarkParser parser = new CommonmarkParser(Parser.builder().build());

	private void parseAndAssert(String markdown, Element... elements) {
		var parsed = parser.parse(markdown);
		assertThat(parsed.content()).containsExactly(elements);
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
	void htmlBlock_p() {
		parseAndAssert(
				"""
				<p>This is an HTML paragraph.</p>
				""",
				html.literal("<p>This is an HTML paragraph.</p>"));
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

	// --- section 5 - Container blocks

	@Test
	void blockQuoteWithText() {
		parseAndAssert(
				"> A line of text",
				blockquote.children(p.text("A line of text"))
		);
	}

	@Test
	void blockQuoteWithHeaderAndText() {
		parseAndAssert(
				"""
				> # A Header
				>
				> A line of text
				""",
				blockquote.children(
						h1.text("A Header"),
						p.text("A line of text"))
		);
	}

	@Test
	void unorderedList() {
		parseAndAssert(
				"""
				* one
				* two
				* three
				""",
				ul.children(
						li.children(p.text("one")),
						li.children(p.text("two")),
						li.children(p.text("three")))
		);
	}

	@Test
	void orderedList() {
		parseAndAssert(
				"""
				1. one
				2. two
				3. three
				""",
				ol.children(
						li.children(p.text("one")),
						li.children(p.text("two")),
						li.children(p.text("three")))
		);
	}

	@Test
	void orderedListWithStart() {
		parseAndAssert(
				"""
				2. two
				3. three
				4. four
				""",
				ol.start(2).children(
						li.children(p.text("two")),
						li.children(p.text("three")),
						li.children(p.text("four")))
		);
	}

	// --- section 6 - Inlines

	@Test
	void codeSpan() {
		parseAndAssert("`this.is().code()`",
				p.children(
						code.text("this.is().code()"))
		);
	}

	@Test
	void codeSpanInText() {
		parseAndAssert("There is some `code()` in the middle of this paragraph.",
				p.children(
						text.text("There is some "),
						code.text("code()"),
						text.text(" in the middle of this paragraph."))
		);
	}

	@Test
	void emphasis() {
		parseAndAssert("*emphasized*",
				p.children(
						em.text("emphasized"))
		);
	}

	@Test
	void emphasisInText() {
		parseAndAssert("There is some *emphasized text* in the middle of this paragraph.",
				p.children(
						text.text("There is some "),
						em.text("emphasized text"),
						text.text(" in the middle of this paragraph."))
		);
	}

	@Test
	void strong() {
		parseAndAssert("**emphasized**",
				p.children(
						strong.text("emphasized"))
		);
	}

	@Test
	void strongInText() {
		parseAndAssert("There is some **emphasized text** in the middle of this paragraph.",
				p.children(
						text.text("There is some "),
						strong.text("emphasized text"),
						text.text(" in the middle of this paragraph."))
		);
	}

	@Test
	void link() {
		parseAndAssert("[nipafx](https://nipafx.dev \"nipafx site\")",
				p.children(
						a.text("nipafx").title("nipafx site").href("https://nipafx.dev"))
		);
	}

	@Test
	void linkInText() {
		parseAndAssert("There is [a link](https://nipafx.dev \"nipafx site\") in the middle of this paragraph.",
				p.children(
						text.text("There is "),
						a.text("a link").title("nipafx site").href("https://nipafx.dev"),
						text.text(" in the middle of this paragraph."))
		);
	}

	@Test
	void autolink() {
		parseAndAssert("<https://nipafx.dev>",
				p.children(
						a.text("https://nipafx.dev").href("https://nipafx.dev"))
		);
	}

	@Test
	void autolinkInText() {
		parseAndAssert("There is a link to <https://nipafx.dev> in the middle of this paragraph.",
				p.children(
						text.text("There is a link to "),
						a.text("https://nipafx.dev").href("https://nipafx.dev"),
						text.text(" in the middle of this paragraph."))
		);
	}

	// TODO: improve raw HTML parsing, so that there's one `html` element with children
	//       instead of an opening and a closing `html` element with their content as siblings

	@Test
	void rawHtml() {
		parseAndAssert("<my-tag>some text</my-tag>",
				p.children(
						html.literal("<my-tag>"),
						text.text("some text"),
						html.literal("</my-tag>"))
		);
	}

	@Test
	void rawHtmlInText() {
		parseAndAssert("There is <my-tag>my tag</my-tag> in the middle of this paragraph.",
				p.children(
						text.text("There is "),
						html.literal("<my-tag>"),
						text.text("my tag"),
						html.literal("</my-tag>"),
						text.text(" in the middle of this paragraph."))
		);
	}

	@Test
	void lineBreakInText() {
		parseAndAssert(
				"""
				This is the first\\
				of two lines.
				""",
				p.children(
						text.text("This is the first"),
						br,
						text.text("of two lines."))
		);
	}

}
