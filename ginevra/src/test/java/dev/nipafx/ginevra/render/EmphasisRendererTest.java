package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Emphasis;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.em;

class EmphasisRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "em";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public Renderer renderer() {
			return RENDERER;
		}

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Emphasis> {

		@Override
		public Emphasis createWith(String id, List<String> classes) {
			return em.id(id).classes(Classes.of(classes));
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