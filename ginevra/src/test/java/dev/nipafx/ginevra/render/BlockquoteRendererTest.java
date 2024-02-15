package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.BlockQuote;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.blockquote;

class BlockquoteRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "blockquote";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<BlockQuote> {

		@Override
		public BlockQuote createWith(String id, List<String> classes) {
			return blockquote.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<BlockQuote> {

		@Override
		public BlockQuote createWith(String text, Element... children) {
			return blockquote.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<BlockQuote> {

		@Override
		public BlockQuote createWith(Element... children) {
			return blockquote.children(children);
		}

	}

}