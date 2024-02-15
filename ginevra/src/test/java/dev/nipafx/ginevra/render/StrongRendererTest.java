package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Strong;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.strong;

class StrongRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "strong";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Strong> {

		@Override
		public Strong createWith(String id, List<String> classes) {
			return strong.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<Strong> {

		@Override
		public Strong createWith(String text, Element... children) {
			return strong.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Strong> {

		@Override
		public Strong createWith(Element... children) {
			return strong.children(children);
		}

	}

}