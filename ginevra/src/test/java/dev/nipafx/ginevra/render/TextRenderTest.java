package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Text;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextRenderTest {

	private static final HtmlRenderer RENDERER = new HtmlRenderer();

	@Test
	void emptyText() {
		var text = new Text("");
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("");
	}

	@Test
	void nonEmptyText() {
		var text = new Text("This text is not empty");
		var rendered = RENDERER.render(text);

		assertThat(rendered).isEqualTo("This text is not empty\n");
	}

}
