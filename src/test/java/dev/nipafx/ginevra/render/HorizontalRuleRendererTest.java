package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.HorizontalRule;
import dev.nipafx.ginevra.html.Id;
import org.junit.jupiter.api.Nested;

import static dev.nipafx.ginevra.html.HtmlElement.hr;

class HorizontalRuleRendererTest {

	private static final String TAG = "hr";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<HorizontalRule> {

		@Override
		public HorizontalRule createWith(Id id, Classes classes) {
			return hr.id(id).classes( classes);
		}

		@Override
		public boolean isSelfClosing() {
			return true;
		}

	}

}
