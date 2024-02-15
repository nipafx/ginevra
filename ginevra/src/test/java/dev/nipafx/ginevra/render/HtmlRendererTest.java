package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Element;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.nipafx.ginevra.html.HtmlElement.br;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.HtmlElement.span;
import static dev.nipafx.ginevra.html.HtmlElement.strong;
import static dev.nipafx.ginevra.html.JmlElement.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmlRendererTest {

	interface TestBasics {

		Renderer renderer();

		String tag();

	}

	interface IdAndClasses<ELEMENT extends Element> extends TestBasics {

		ELEMENT createWith(String id, List<String> classes);

		default boolean isSelfClosing() {
			return false;
		}

		@Test
		default void neither() {
			var element = createWith(null, List.of());
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withEmptyId() {
			var element = createWith("", List.of());
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withId() {
			var element = createWith("the-id", List.of());
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} id="the-id" />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} id="the-id"></\{tag()}>
					""");
		}

		@Test
		default void withOneEmptyClass() {
			var element = createWith(null, List.of(""));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withOneClass() {
			var element = createWith(null, List.of("the-class"));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} class="the-class" />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} class="the-class"></\{tag()}>
					""");
		}

		@Test
		default void withEmptyClasses() {
			var element = createWith(null, List.of("", ""));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withClasses() {
			var element = createWith(null, List.of("one-class", "another-class"));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} class="one-class another-class" />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} class="one-class another-class"></\{tag()}>
					""");
		}

		@Test
		default void withIdAndClasses() {
			var element = createWith("the-id", List.of("one-class", "another-class"));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} id="the-id" class="one-class another-class" />
					""");
			else
				assertThat(rendered).isEqualTo(STR."""
					<\{tag()} id="the-id" class="one-class another-class"></\{tag()}>
					""");
		}

	}

	interface EmbeddedText<ELEMENT extends Element> extends TestBasics {

		ELEMENT createWith(String text, Element... children);

		@Test
		default void withoutTextAndChildren() {
			var element = createWith(null);
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}></\{tag()}>
					""");
		}

		@Test
		default void withTextAndChildren() {
			assertThatThrownBy(() -> createWith("inline text", text.text("text element")))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		default void withText() {
			var element = createWith("inline text");
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>inline text</\{tag()}>
					""");
		}

		@Test
		default void withSingleTextChild() {
			var element = createWith(null, text.text("text element"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>text element</\{tag()}>
					""");
		}

		@Test
		default void withTextChildren() {
			var element = createWith(
					null,
					text.text("text element 1"),
					text.text("text element 2"),
					text.text("text element 3"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>text element 1text element 2text element 3</\{tag()}>
					""");
		}

		@Test
		default void withMixedChildren() {
			var element = createWith(
					null,
					text.text("text element 1"),
					// an element that must not have spaces before or after
					strong.text("strong element"),
					text.text("text element 2"),
					// a self-closing element that must not have spaces before or after
					br,
					text.text("text element 3"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>text element 1<strong>strong element</strong>text element 2<br />text element 3</\{tag()}>
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
							span.id("child"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<span id="child"></span>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildren() {
			var element =
					createWith(
							span.id("child-1"),
							span.id("child-2"),
							span.id("child-3"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<span id="child-1"></span>
						<span id="child-2"></span>
						<span id="child-3"></span>
					</\{tag()}>
					""");
		}

		@Test
		default void withOneChildAndGrandchild() {
			var element =
					createWith(
							createWith(
									span.id("grandchild")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<\{tag()}>
							<span id="grandchild"></span>
						</\{tag()}>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneGrandchild_first() {
			var element =
					createWith(
							createWith(
									span.id("grandchild")),
							span.id("child-1"),
							span.id("child-2"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<\{tag()}>
							<span id="grandchild"></span>
						</\{tag()}>
						<span id="child-1"></span>
						<span id="child-2"></span>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneGrandchild_middle() {
			var element =
					createWith(
							span.id("child-1"),
							createWith(
									span.id("grandchild")),
							span.id("child-2"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<span id="child-1"></span>
						<\{tag()}>
							<span id="grandchild"></span>
						</\{tag()}>
						<span id="child-2"></span>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneGrandchild_last() {
			var element =
					createWith(
							span.id("child-1"),
							span.id("child-2"),
							createWith(
									span.id("grandchild")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<span id="child-1"></span>
						<span id="child-2"></span>
						<\{tag()}>
							<span id="grandchild"></span>
						</\{tag()}>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneTextGrandchild_first() {
			var element =
					createWith(
							p.text("grandchild"),
							span.id("child-1"),
							span.id("child-2"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<p>grandchild</p>
						<span id="child-1"></span>
						<span id="child-2"></span>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneTextGrandchild_middle() {
			var element =
					createWith(
							span.id("child-1"),
							p.text("grandchild"),
							span.id("child-3"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<span id="child-1"></span>
						<p>grandchild</p>
						<span id="child-3"></span>
					</\{tag()}>
					""");
		}

		@Test
		default void withChildrenAndOneTextGrandchild_last() {
			var element =
					createWith(
							span.id("child-1"),
							span.id("child-2"),
							p.text("grandchild"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<span id="child-1"></span>
						<span id="child-2"></span>
						<p>grandchild</p>
					</\{tag()}>
					""");
		}

		@Test
		default void withManyChildrenAndGrandchildren() {
			var element =
					createWith(
							createWith(
									span.id("grandchild-1"),
									span.id("grandchild-2"),
									span.id("grandchild-3")),
							createWith(
									span.id("grandchild-4"),
									span.id("grandchild-5"),
									span.id("grandchild-6")),
							createWith(
									span.id("grandchild-7"),
									span.id("grandchild-8"),
									span.id("grandchild-9")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo(STR."""
					<\{tag()}>
						<\{tag()}>
							<span id="grandchild-1"></span>
							<span id="grandchild-2"></span>
							<span id="grandchild-3"></span>
						</\{tag()}>
						<\{tag()}>
							<span id="grandchild-4"></span>
							<span id="grandchild-5"></span>
							<span id="grandchild-6"></span>
						</\{tag()}>
						<\{tag()}>
							<span id="grandchild-7"></span>
							<span id="grandchild-8"></span>
							<span id="grandchild-9"></span>
						</\{tag()}>
					</\{tag()}>
					""");
		}

	}

}
