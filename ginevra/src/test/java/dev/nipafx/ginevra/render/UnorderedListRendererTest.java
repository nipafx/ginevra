package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.UnorderedList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.li;
import static dev.nipafx.ginevra.html.HtmlElement.ul;
import static org.assertj.core.api.Assertions.assertThat;

class UnorderedListRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "ul";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<UnorderedList> {

		@Override
		public UnorderedList createWith(String id, List<String> classes) {
			return ul.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class Children extends TestBasics {

		@Test
		void withoutChildren() {
			var element = ul;
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		void withOneChild() {
			var element = ul.children(
					li.id("child"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<li id="child"></li>
					</\{tag()}>
					""");
		}

		@Test
		void withChildren() {
			var element = ul.children(
					li.id("child-1"),
					li.id("child-2"),
					li.id("child-3"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<li id="child-1"></li>
						<li id="child-2"></li>
						<li id="child-3"></li>
					</\{tag()}>
					""");
		}

	}

}