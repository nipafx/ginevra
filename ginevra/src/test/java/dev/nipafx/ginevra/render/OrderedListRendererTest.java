package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.OrderedList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.li;
import static dev.nipafx.ginevra.html.HtmlElement.ol;
import static org.assertj.core.api.Assertions.assertThat;

class OrderedListRendererTest {

	private static final Renderer RENDERER = new Renderer();
	private static final String TAG = "ol";

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
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<OrderedList> {

		@Override
		public OrderedList createWith(String id, List<String> classes) {
			return ol.id(id).classes(Classes.of(classes));
		}

	}

	@Nested
	class Start extends TestBasics {

		@Test
		void withoutStart() {
			var element = ol;
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<ol></ol>
					""");
		}

		@Test
		void withStart() {
			var element = ol.start(2);
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<ol start="2"></ol>
					""");
		}

	}

	@Nested
	class Children extends TestBasics {

		@Test
		void withoutChildren() {
			var element = ol;
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		void withOneChild() {
			var element = ol.children(
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
			var element = ol.children(
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