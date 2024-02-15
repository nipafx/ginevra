package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Pre;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.pre;

class PreRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "pre";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Pre> {

		@Override
		public Pre createWith(String id, List<String> classes) {
			return pre.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<Pre> {

		@Override
		public Pre createWith(String text, Element... children) {
			return pre.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Pre> {

		@Override
		public Pre createWith(Element... children) {
			return pre.children(children);
		}

	}

}