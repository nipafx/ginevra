package dev.nipafx.ginevra.render;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.meta;
import static dev.nipafx.ginevra.render.HtmlRendererTest.RENDERER;
import static org.assertj.core.api.Assertions.assertThat;

class MetaRendererTest {

	@Nested
	class Attributes {

		@Test
		void withName() {
			var element = meta.name("a:name");
			var rendered = RENDERER.render(element);

			assertThat(rendered).isEqualTo("""
					<meta name="a:name" />
					""");
		}

		@Test
		void withContent() {
			var element = meta.content("-the-content-");
			var rendered = RENDERER.render(element);

			assertThat(rendered).isEqualTo("""
					<meta content="-the-content-" />
					""");
		}

		@Test
		void withNameAndContent() {
			var element = meta.name("a:name").content("-the-content-");
			var rendered = RENDERER.render(element);

			assertThat(rendered).isEqualTo("""
					<meta name="a:name" content="-the-content-" />
					""");
		}

	}

}
