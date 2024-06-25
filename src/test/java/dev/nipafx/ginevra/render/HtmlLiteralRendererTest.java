package dev.nipafx.ginevra.render;

import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.JmlElement.html;
import static dev.nipafx.ginevra.html.JmlElement.text;
import static dev.nipafx.ginevra.render.HtmlRendererTest.RENDERER;
import static org.assertj.core.api.Assertions.assertThat;

class HtmlLiteralRendererTest {

	@Test
	void nullLiteral() {
		var element = html.literal(null);
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("");
	}

	@Test
	void emptyLiteral() {
		var element = html.literal("");
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("");
	}

	@Test
	void textLiteral() {
		var element = html.literal("text");
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("text");
	}

	@Test
	void htmlLiteral() {
		var element = html.literal("<p>Paragraph.</p>");
		var rendered = RENDERER.render(element);

		// no trailing newline because HTML literals are not known to be closed
		assertThat(rendered).isEqualTo("<p>Paragraph.</p>");
	}

	@Test
	void nestedHtmlLiteral() {
		var element = html.literal("<p><b>Nested</b> paragraph.</p>");
		var rendered = RENDERER.render(element);

		// no trailing newline because HTML literals are not known to be closed
		assertThat(rendered).isEqualTo("<p><b>Nested</b> paragraph.</p>");
	}

	@Test
	void htmlLiteralAsSiblingToText() {
		var element = p.children(
				text.text("Some "),
				html.literal("<b>bold</b>"),
				text.text(" text"));
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("<p>Some <b>bold</b> text</p>\n");
	}


}