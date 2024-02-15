package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.LineBreak;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.br;

class LineBreakRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "br";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<LineBreak> {

		@Override
		public LineBreak createWith(String id, List<String> classes) {
			return br.id(id).classes(Classes.of(classes));
		}

		@Override
		public boolean isSelfClosing() {
			return true;
		}

	}

}
