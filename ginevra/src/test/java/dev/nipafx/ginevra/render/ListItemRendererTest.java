package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.ListItem;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.li;

class ListItemRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "li";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<ListItem> {

		@Override
		public ListItem createWith(String id, List<String> classes) {
			return li.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class EmbeddedText extends TestBasics implements HtmlRendererTest.EmbeddedText<ListItem> {

		@Override
		public ListItem createWith(String text, Element... children) {
			return li.text(text).children(children);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<ListItem> {

		@Override
		public ListItem createWith(Element... children) {
			return li.children(children);
		}

	}

}