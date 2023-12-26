package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlRendererTest {

	interface TestBasics {

		HtmlRenderer renderer();

		String tag();

	}

	interface IdAndClasses<ELEMENT extends Element> extends TestBasics {

		ELEMENT createWith(String id, List<String> classes);

		@Test
		default void neither() {
			var element = createWith(null, List.of());
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withEmptyId() {
			var element = createWith("", List.of());
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withId() {
			var element = createWith("the-id", List.of());
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()} id="the-id"></\{tag()}>
					""");
		}

		@Test
		default void withOneEmptyClass() {
			var element = createWith(null, List.of(""));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withOneClass() {
			var element = createWith(null, List.of("the-class"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()} class="the-class"></\{tag()}>
					""");
		}

		@Test
		default void withEmptyClasses() {
			var element = createWith(null, List.of("", ""));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withClasses() {
			var element = createWith(null, List.of("one-class", "another-class"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()} class="one-class another-class"></\{tag()}>
					""");
		}

		@Test
		default void withIdAndClasses() {
			var element = createWith("the-id", List.of("one-class", "another-class"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()} id="the-id" class="one-class another-class"></\{tag()}>
					""");
		}

	}

	interface Children<ELEMENT extends Element> extends TestBasics {

		ELEMENT createWith(Element... children);

		@Test
		default void withoutChildren() {
			var element =
					createWith();
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withOneChild() {
			var element =
					createWith(
							new Text("child"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						child
					</\{tag()}>
					""");
		}

		@Test
		default void withChildren() {
			var element =
					createWith(
							new Text("child 1"),
							new Text("child 2"),
							new Text("child 3"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						child 1
						child 2
						child 3
					</\{tag()}>
					""");
		}

		@Test
		default void withOneChildAndGrandchild() {
			var element =
					createWith(
							createWith(
									new Text("grandchild")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<\{tag()}>
							grandchild
						</\{tag()}>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneGrandchild_first() {
			var element =
					createWith(
							createWith(
									new Text("grandchild")),
							new Text("child 1"),
							new Text("child 2"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<\{tag()}>
							grandchild
						</\{tag()}>
						child 1
						child 2
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneGrandchild_middle() {
			var element =
					createWith(
							new Text("child 1"),
							createWith(
									new Text("grandchild")),
							new Text("child 2"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						child 1
						<\{tag()}>
							grandchild
						</\{tag()}>
						child 2
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneGrandchild_last() {
			var element =
					createWith(
							new Text("child 1"),
							new Text("child 2"),
							createWith(
									new Text("grandchild")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						child 1
						child 2
						<\{tag()}>
							grandchild
						</\{tag()}>
					</\{tag()}>
					""");
		}

		@Test
		default void withManyChildrenAndGrandchildren() {
			var element =
					createWith(
							createWith(
									new Text("grandchild 1"),
									new Text("grandchild 2"),
									new Text("grandchild 3")),
							createWith(
									new Text("grandchild 4"),
									new Text("grandchild 5"),
									new Text("grandchild 6")),
							createWith(
									new Text("grandchild 7"),
									new Text("grandchild 8"),
									new Text("grandchild 9")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<\{tag()}>
							grandchild 1
							grandchild 2
							grandchild 3
						</\{tag()}>
						<\{tag()}>
							grandchild 4
							grandchild 5
							grandchild 6
						</\{tag()}>
						<\{tag()}>
							grandchild 7
							grandchild 8
							grandchild 9
						</\{tag()}>
					</\{tag()}>
					""");
		}

	}

}
