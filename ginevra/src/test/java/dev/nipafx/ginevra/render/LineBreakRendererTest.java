package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.html.LineBreak;
import org.junit.jupiter.api.Nested;

import static dev.nipafx.ginevra.html.HtmlElement.br;

class LineBreakRendererTest {

	private static final String TAG = "br";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<LineBreak> {

		@Override
		public LineBreak createWith(Id id, Classes classes) {
			return br.id(id).classes( classes);
		}

		@Override
		public boolean isSelfClosing() {
			return true;
		}

	}

}
