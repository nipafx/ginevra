package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Anchor;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Id;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.a;
import static org.assertj.core.api.Assertions.assertThat;

class AnchorRendererTest {

	private static final String TAG = "a";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Anchor> {

		@Override
		public Anchor createWith(Id id, Classes classes) {
			return a.id(id).classes( classes);
		}

	}

	@Nested
	class HrefAndTitle extends TestBasics {

		@Test
		void neither() {
			var element = a;
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<a></a>
					""");
		}

		@Test
		void withHref() {
			var element = a.href("url");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<a href="url"></a>
					""");
		}

		@Test
		void withTitle() {
			var element = a.title("the title");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<a title="the title"></a>
					""");
		}

		@Test
		void withHrefAndTitle() {
			var element = a.href("url").title("the title");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<a href="url" title="the title"></a>
					""");
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<Anchor> {

		@Override
		public Anchor createWith(String text, Element... children) {
			return a.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Anchor> {

		@Override
		public Anchor createWith(Element... children) {
			return a.children(children);
		}

	}

}