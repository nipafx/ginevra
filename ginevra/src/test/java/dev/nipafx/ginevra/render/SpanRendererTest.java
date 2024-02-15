package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Span;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.span;

class SpanRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "span";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Span> {

		@Override
		public Span createWith(String id, List<String> classes) {
			return span.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<Span> {

		@Override
		public Span createWith(String text, Element... children) {
			return span.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Span> {

		@Override
		public Span createWith(Element... children) {
			return span.children(children);
		}

	}

}