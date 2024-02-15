package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Paragraph;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.p;

class ParagraphRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "p";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Paragraph> {

		@Override
		public Paragraph createWith(String id, List<String> classes) {
			return p.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<Paragraph> {

		@Override
		public Paragraph createWith(String text, Element... children) {
			return p.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Paragraph> {

		@Override
		public Paragraph createWith(Element... children) {
			return p.children(children);
		}

	}

}