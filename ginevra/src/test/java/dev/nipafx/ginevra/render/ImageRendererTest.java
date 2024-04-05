package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.html.Image;
import dev.nipafx.ginevra.html.Src;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.img;
import static org.assertj.core.api.Assertions.assertThat;

class ImageRendererTest {

	private static final String TAG = "img";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Image> {

		@Override
		public Image createWith(Id id, Classes classes) {
			return img.id(id).classes( classes);
		}

		@Override
		public boolean isSelfClosing() {
			return true;
		}

	}

	@Nested
	class SrcAndTitleAndAlt extends AnchorRendererTest.TestBasics {

		@Test
		void neither() {
			var element = img;
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<img />
					""");
		}

		@Test
		void withSrc() {
			var element = img.src(Src.direct("url"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<img src="url" />
					""");
		}

		@Test
		void withTitle() {
			var element = img.title("the title");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<img title="the title" />
					""");
		}

		@Test
		void withAlt() {
			var element = img.alt("alt text");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<img alt="alt text" />
					""");
		}

		@Test
		void withAll() {
			var element = img.src(Src.direct("url")).title("the title").alt("alt text");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<img src="url" title="the title" alt="alt text" />
					""");
		}

	}

}
