package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.GmlElement;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.render.HtmlRendererTest.RENDERER;
import static org.assertj.core.api.Assertions.assertThat;

class TextRenderTest {

	@Test
	void nullText() {
		var text = GmlElement.text;
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("");
	}

	@Test
	void emptyText() {
		var text = GmlElement.text.text("");
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("");
	}

	@Test
	void nonEmptyText() {
		var text = GmlElement.text.text("This text is not empty");
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("This text is not empty");
	}

}
