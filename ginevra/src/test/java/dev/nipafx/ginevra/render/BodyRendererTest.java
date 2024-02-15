package dev.nipafx.ginevra.render;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static org.assertj.core.api.Assertions.assertThat;

class BodyRendererTest {

	private static final Renderer RENDERER = new Renderer();

	@Test
	void empty() {
		var element = body;
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<body></body>
					""");
	}

	@Test
	void withDiv() {
		var element = body.children(List.of(div));
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<body>
						<div></div>
					</body>
					""");
	}

	@Test
	void withParagraphs() {
		var element = body.children(List.of(
				p.text("Hello"),
				p.text("World")));
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<body>
						<p>Hello</p>
						<p>World</p>
					</body>
					""");
	}

}