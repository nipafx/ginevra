package dev.nipafx.ginevra.css;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Id;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CssTests {

	@Nested
	class EdgeCases {

		public record Broken(Object incorrectType, Css css) implements CssStyle { }

		@Test
		void throwsOnBrokenRecord() {
			assertThatThrownBy(() -> Css.parse(Broken.class, ""))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageStartingWith("CSS record components must be of types");
		}

		public record JustStyle(Css css) implements CssStyle { }

		@Test
		void justStyle_emptyCss() {
			var css = "";
			var style = Css.parse(JustStyle.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

		@Test
		void justStyle_nonEmptyCss() {
			var css = """
					div {
						margin: 1em;
					}
					""";
			var style = Css.parse(JustStyle.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

	}

	@Nested
	class CssClasses {

		public record OneClass(Classes ref, Css css) implements CssStyle { }

		@Test
		void oneReference_referencedAsClass() {
			var css = """
					.ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThat(style.ref().asCssString()).matches("dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref");
			assertThatCssOf(style).matches("""
					\\.dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_classOnElement() {
			var css = """
					div.ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThat(style.ref().asCssString()).matches("dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref");
			assertThatCssOf(style).matches("""
					div\\.dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_multipleClassesFirst() {
			var css = """
					.ref.other {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThat(style.ref().asCssString()).matches("dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref");
			assertThatCssOf(style).matches("""
					\\.dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref\\.other \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_multipleClassesLast() {
			var css = """
					.other.ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThat(style.ref().asCssString()).matches("dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref");
			assertThatCssOf(style).matches("""
					\\.other\\.dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_referencedAtEndOfLine() {
			var css = """
					.ref
					{
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThat(style.ref().asCssString()).matches("dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref");
			assertThatCssOf(style).matches("""
					\\.dev-nipafx-ginevra-css-CssTests-CssClasses-OneClass--.*--ref
					\\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_notReferenced() {
			var css = """
					.noref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

		@Test
		void oneReference_referencedAsId() {
			var css = """
					#ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

		@Test
		void oneReference_prefixOfReference() {
			var css = """
					.refWithSamePrefix {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneClass.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

	}

	@Nested
	class CssIds {

		public record OneId(Id ref, Css css) implements CssStyle { }

		@Test
		void oneReference_referencedAsId() {
			var css = """
					#ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThat(style.ref().asString()).matches("dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref");
			assertThatCssOf(style).matches("""
					#dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_idOnElement() {
			var css = """
					div#ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThat(style.ref().asString()).matches("dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref");
			assertThatCssOf(style).matches("""
					div#dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_withClassesFirst() {
			var css = """
					#ref.other {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThat(style.ref().asString()).matches("dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref");
			assertThatCssOf(style).matches("""
					#dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref\\.other \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_withClassesLast() {
			var css = """
					.other#ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThat(style.ref().asString()).matches("dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref");
			assertThatCssOf(style).matches("""
					\\.other#dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref \\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_referencedAtEndOfLine() {
			var css = """
					#ref
					{
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThat(style.ref().asString()).matches("dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref");
			assertThatCssOf(style).matches("""
					#dev-nipafx-ginevra-css-CssTests-CssIds-OneId--.*--ref
					\\{
						margin: 1em;
					}
					""");
		}

		@Test
		void oneReference_notReferenced() {
			var css = """
					.noref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

		@Test
		void oneReference_referencedAsClass() {
			var css = """
					.ref {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

		@Test
		void oneReference_prefixOfReference() {
			var css = """
					#refWithSamePrefix {
						margin: 1em;
					}
					""";
			var style = Css.parse(OneId.class, css);

			assertThatCssOf(style).isEqualTo(css);
		}

	}

	@Nested
	class CssClassesAndIds {

		public record TwoReferences(Id id, Classes classes, Css css) implements CssStyle { }

		@Test
		void twoReferences_referencedAsIdAndClass() {
			var css = """
					#id {
						margin: 1em;
					}
					.classes {
						margin: 2em;
					}
					""";
			var style = Css.parse(TwoReferences.class, css);

			assertThat(style.id().asString()).matches("dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--id");
			assertThat(style.classes().asCssString()).matches("dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--classes");
			assertThatCssOf(style).matches("""
					#dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--id \\{
						margin: 1em;
					}
					\\.dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--classes \\{
						margin: 2em;
					}
					""");
		}

		@Test
		void twoReferences_referencedAsIdAndClassOnSameElement() {
			var css = """
					#id.classes {
						margin: 1em;
					}
					""";
			var style = Css.parse(TwoReferences.class, css);

			assertThat(style.id().asString()).matches("dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--id");
			assertThat(style.classes().asCssString()).matches("dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--classes");
			assertThatCssOf(style).matches("""
					#dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--id\\.dev-nipafx-ginevra-css-CssTests-CssClassesAndIds-TwoReferences--.*--classes \\{
						margin: 1em;
					}
					""");
		}

	}

	private static AbstractStringAssert<?> assertThatCssOf(CssStyle style) {
		return assertThat(style.css().interpolateSources());
	}

}
