package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Src;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.source;
import static org.assertj.core.api.Assertions.assertThat;

class SourceRendererTest {

	private static final String TAG = "source";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class SrcAndTitleAndAlt extends TestBasics {

		@Test
		void neither() {
			var element = source;
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<source />
					""");
		}

		@Test
		void withSrc() {
			var element = source.src(Src.direct("url"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<source src="url" />
					""");
		}

		@Test
		void withTitle() {
			var element = source.type("the-type");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<source type="the-type" />
					""");
		}

		@Test
		void withAll() {
			var element = source.src(Src.direct("url")).type("the-type");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<source src="url" type="the-type" />
					""");
		}

	}

}
