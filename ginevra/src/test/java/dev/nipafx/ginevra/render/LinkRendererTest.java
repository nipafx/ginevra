package dev.nipafx.ginevra.render;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.link;
import static org.assertj.core.api.Assertions.assertThat;

class LinkRendererTest {

	private static final Renderer RENDERER = new Renderer();

	@Nested
	class Attributes {

		@Test
		void withHref() {
			var element = link.href("/path/to/file");
			var rendered = RENDERER.render(element);

			assertThat(rendered).isEqualTo("""
					<link href="/path/to/file" />
					""");
		}

		@Test
		void withRel() {
			var element = link.rel("stylesheet");
			var rendered = RENDERER.render(element);

			assertThat(rendered).isEqualTo("""
					<link rel="stylesheet" />
					""");
		}

		@Test
		void withHrefAndRel() {
			var element = link.href("/path/to/file").rel("stylesheet");
			var rendered = RENDERER.render(element);

			assertThat(rendered).isEqualTo("""
					<link href="/path/to/file" rel="stylesheet" />
					""");
		}

	}

}
