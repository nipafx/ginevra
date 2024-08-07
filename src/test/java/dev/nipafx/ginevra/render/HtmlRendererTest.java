package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.execution.StoreFront;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static dev.nipafx.ginevra.html.GmlElement.text;
import static dev.nipafx.ginevra.html.HtmlElement.br;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.HtmlElement.span;
import static dev.nipafx.ginevra.html.HtmlElement.strong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmlRendererTest {

	public static final Renderer RENDERER = new Renderer(new EmptyStore(), Path.of(""), Path.of(""));
	public static final ElementResolver RESOLVER = new ElementResolver(new EmptyStore(), Path.of(""), Path.of(""));

	interface TestBasics {

		default Renderer renderer() {
			return RENDERER;
		}

		String tag();

	}

	interface IdAndClasses<ELEMENT extends Element> extends TestBasics {

		ELEMENT createWith(Id id, Classes classes);

		default boolean isSelfClosing() {
			return false;
		}

		@Test
		default void neither() {
			var element = createWith(Id.none(), Classes.none());
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withEmptyId() {
			var element = createWith(Id.none(), Classes.none());
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withId() {
			var element = createWith(Id.of("the-id"), Classes.none());
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s id="the-id" />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s id="the-id"></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withOneEmptyClass() {
			var element = createWith(Id.none(), Classes.of(""));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withOneClass() {
			var element = createWith(Id.none(), Classes.of("the-class"));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s class="the-class" />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s class="the-class"></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withEmptyClasses() {
			var element = createWith(Id.none(), Classes.of("", ""));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withClasses() {
			var element = createWith(Id.none(), Classes.of("one-class", "another-class"));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s class="one-class another-class" />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s class="one-class another-class"></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withIdAndClasses() {
			var element = createWith(Id.of("the-id"), Classes.of("one-class", "another-class"));
			var rendered = renderer().render(element);

			if (isSelfClosing())
				assertThat(rendered).isEqualTo("""
					<%s id="the-id" class="one-class another-class" />
					""".formatted(tag()));
			else
				assertThat(rendered).isEqualTo("""
					<%s id="the-id" class="one-class another-class"></%s>
					""".formatted(tag(), tag()));
		}

	}

	interface EmbeddedText<ELEMENT extends Element> extends TestBasics {

		ELEMENT createWith(String text, Element... children);

		@Test
		default void withoutTextAndChildren() {
			var element = createWith(null);
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s></%s>
					""".formatted(tag(), tag()));
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

			assertThat(rendered).isEqualTo("""
					<%s>inline text</%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withSingleTextChild() {
			var element = createWith(null, text.text("text element"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>text element</%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withTextChildren() {
			var element = createWith(
					null,
					text.text("text element 1"),
					text.text("text element 2"),
					text.text("text element 3"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>text element 1text element 2text element 3</%s>
					""".formatted(tag(), tag()));
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

			assertThat(rendered).isEqualTo("""
					<%s>text element 1<strong>strong element</strong>text element 2<br />text element 3</%s>
					""".formatted(tag(), tag()));
		}

	}

	interface Children<ELEMENT extends Element> extends TestBasics {

		ELEMENT createWith(Element... children);

		@Test
		default void withoutChildren() {
			var element =
					createWith();
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s></%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withOneChild() {
			var element =
					createWith(
							span.id(Id.of("child")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<span id="child"></span>
					</%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withChildren() {
			var element =
					createWith(
							span.id(Id.of("child-1")),
							span.id(Id.of("child-2")),
							span.id(Id.of("child-3")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<span id="child-1"></span>
						<span id="child-2"></span>
						<span id="child-3"></span>
					</%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withOneChildAndGrandchild() {
			var element =
					createWith(
							createWith(
									span.id(Id.of("grandchild"))));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<%s>
							<span id="grandchild"></span>
						</%s>
					</%s>
					""".formatted(tag(), tag(), tag(), tag()));
		}

		@Test
		default void withChildrenAndOneGrandchild_first() {
			var element =
					createWith(
							createWith(
									span.id(Id.of("grandchild"))),
							span.id(Id.of("child-1")),
							span.id(Id.of("child-2")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<%s>
							<span id="grandchild"></span>
						</%s>
						<span id="child-1"></span>
						<span id="child-2"></span>
					</%s>
					""".formatted(tag(), tag(), tag(), tag()));
		}

		@Test
		default void withChildrenAndOneGrandchild_middle() {
			var element =
					createWith(
							span.id(Id.of("child-1")),
							createWith(
									span.id(Id.of("grandchild"))),
							span.id(Id.of("child-2")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<span id="child-1"></span>
						<%s>
							<span id="grandchild"></span>
						</%s>
						<span id="child-2"></span>
					</%s>
					""".formatted(tag(), tag(), tag(), tag()));
		}

		@Test
		default void withChildrenAndOneGrandchild_last() {
			var element =
					createWith(
							span.id(Id.of("child-1")),
							span.id(Id.of("child-2")),
							createWith(
									span.id(Id.of("grandchild"))));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<span id="child-1"></span>
						<span id="child-2"></span>
						<%s>
							<span id="grandchild"></span>
						</%s>
					</%s>
					""".formatted(tag(), tag(), tag(), tag()));
		}

		@Test
		default void withChildrenAndOneTextGrandchild_first() {
			var element =
					createWith(
							p.text("grandchild"),
							span.id(Id.of("child-1")),
							span.id(Id.of("child-2")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<p>grandchild</p>
						<span id="child-1"></span>
						<span id="child-2"></span>
					</%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withChildrenAndOneTextGrandchild_middle() {
			var element =
					createWith(
							span.id(Id.of("child-1")),
							p.text("grandchild"),
							span.id(Id.of("child-3")));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<span id="child-1"></span>
						<p>grandchild</p>
						<span id="child-3"></span>
					</%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withChildrenAndOneTextGrandchild_last() {
			var element =
					createWith(
							span.id(Id.of("child-1")),
							span.id(Id.of("child-2")),
							p.text("grandchild"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<span id="child-1"></span>
						<span id="child-2"></span>
						<p>grandchild</p>
					</%s>
					""".formatted(tag(), tag()));
		}

		@Test
		default void withManyChildrenAndGrandchildren() {
			var element =
					createWith(
							createWith(
									span.id(Id.of("grandchild-1")),
									span.id(Id.of("grandchild-2")),
									span.id(Id.of("grandchild-3"))),
							createWith(
									span.id(Id.of("grandchild-4")),
									span.id(Id.of("grandchild-5")),
									span.id(Id.of("grandchild-6"))),
							createWith(
									span.id(Id.of("grandchild-7")),
									span.id(Id.of("grandchild-8")),
									span.id(Id.of("grandchild-9"))));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<%s>
						<%s>
							<span id="grandchild-1"></span>
							<span id="grandchild-2"></span>
							<span id="grandchild-3"></span>
						</%s>
						<%s>
							<span id="grandchild-4"></span>
							<span id="grandchild-5"></span>
							<span id="grandchild-6"></span>
						</%s>
						<%s>
							<span id="grandchild-7"></span>
							<span id="grandchild-8"></span>
							<span id="grandchild-9"></span>
						</%s>
					</%s>
					""".formatted(tag(), tag(), tag(), tag(), tag(), tag(), tag(), tag()));
		}

	}

	private static class EmptyStore implements StoreFront {

		@Override
		public <RESULT extends Record & Document> RESULT query(RootQuery<RESULT> query) {
			throw new IllegalStateException("The empty store can't answer queries");
		}

		@Override
		public <RESULT extends Record & Document> Set<RESULT> query(CollectionQuery<RESULT> query) {
			throw new IllegalStateException("The empty store can't answer queries");
		}

		@Override
		public Optional<? extends FileDocument> getResource(String name) {
			throw new IllegalStateException("The empty store can't answer queries");
		}

	}

}
