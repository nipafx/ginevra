package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.html.UnorderedList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.li;
import static dev.nipafx.ginevra.html.HtmlElement.ul;
import static org.assertj.core.api.Assertions.assertThat;

class UnorderedListRendererTest {

	private static final String TAG = "ul";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<UnorderedList> {

		@Override
		public UnorderedList createWith(Id id, Classes classes) {
			return ul.id(id).classes( classes);
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
					li.id(Id.of("child")));
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
					li.id(Id.of("child-1")),
					li.id(Id.of("child-2")),
					li.id(Id.of("child-3")));
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