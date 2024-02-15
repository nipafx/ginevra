package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.JmlElement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextRenderTest {

	private static final Renderer RENDERER = new Renderer();

	@Test
	void nullText() {
		var text = JmlElement.text;
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("");
	}

	@Test
	void emptyText() {
		var text = JmlElement.text.text("");
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("");
	}

	@Test
	void nonEmptyText() {
		var text = JmlElement.text.text("This text is not empty");
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("This text is not empty");
	}

}
