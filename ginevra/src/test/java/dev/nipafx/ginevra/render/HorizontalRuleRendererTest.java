package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.HorizontalRule;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.hr;

class HorizontalRuleRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "hr";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<HorizontalRule> {

		@Override
		public HorizontalRule createWith(String id, List<String> classes) {
			return hr.id(id).classes(Classes.of(classes));
		}

		@Override
		public boolean isSelfClosing() {
			return true;
		}

	}

}
