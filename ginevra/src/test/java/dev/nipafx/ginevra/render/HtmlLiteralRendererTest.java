package dev.nipafx.ginevra.render;

import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.JmlElement.html;
import static org.assertj.core.api.Assertions.assertThat;

class HtmlLiteralRendererTest {

	private static final HtmlRenderer RENDERER = new HtmlRenderer();

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

		assertThat(rendered).isEqualTo("text\n");
	}

	@Test
	void htmlLiteral() {
		var element = html.literal("<p>Paragraph.</p>");
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("<p>Paragraph.</p>\n");
	}

	@Test
	void nestedHtmlLiteral() {
		var element = html.literal("<p><b>Nested</b> paragraph.</p>");
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("<p><b>Nested</b> paragraph.</p>\n");
	}

}