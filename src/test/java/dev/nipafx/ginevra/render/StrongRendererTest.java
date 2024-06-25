package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.html.Strong;
import org.junit.jupiter.api.Nested;

import static dev.nipafx.ginevra.html.HtmlElement.strong;

class StrongRendererTest {

	private static final String TAG = "strong";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Strong> {

		@Override
		public Strong createWith(Id id, Classes classes) {
			return strong.id(id).classes( classes);
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