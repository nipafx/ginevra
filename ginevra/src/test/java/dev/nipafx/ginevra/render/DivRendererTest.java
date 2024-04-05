package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Div;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Id;
import org.junit.jupiter.api.Nested;

import static dev.nipafx.ginevra.html.HtmlElement.div;

class DivRendererTest {

	private static final String TAG = "div";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Div> {

		@Override
		public Div createWith(Id id, Classes classes) {
			return div.id(id).classes( classes);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Div> {

		@Override
		public Div createWith(Element... children) {
			return div.children(children);
		}

	}

}