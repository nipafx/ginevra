package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Emphasis;
import dev.nipafx.ginevra.html.Id;
import org.junit.jupiter.api.Nested;

import static dev.nipafx.ginevra.html.HtmlElement.em;

class EmphasisRendererTest {

	private static final String TAG = "em";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Emphasis> {

		@Override
		public Emphasis createWith(Id id, Classes classes) {
			return em.id(id).classes( classes);
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<Emphasis> {

		@Override
		public Emphasis createWith(String text, Element... children) {
			return em.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Emphasis> {

		@Override
		public Emphasis createWith(Element... children) {
			return em.children(children);
		}

	}

}