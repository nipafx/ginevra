package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Code;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Id;
import org.junit.jupiter.api.Nested;

import static dev.nipafx.ginevra.html.HtmlElement.code;

class CodeRendererTest {

	private static final String TAG = "code";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Code> {

		@Override
		public Code createWith(Id id, Classes classes) {
			return code.id(id).classes( classes);
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<Code> {

		@Override
		public Code createWith(String text, Element... children) {
			return code.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Code> {

		@Override
		public Code createWith(Element... children) {
			return code.children(children);
		}

	}

}