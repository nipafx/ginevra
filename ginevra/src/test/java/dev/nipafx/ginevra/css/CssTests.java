package dev.nipafx.ginevra.css;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CssTests {

	public record Broken(Object noString, String style) implements CssStyle { }

	@Test
	void throwsOnBrokenRecord() {
		assertThatThrownBy(() -> Css.parse(Broken.class, ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("string components");
	}

	public record JustStyle(String style) implements CssStyle { }

	@Test
	void justStyle_emptyCss() {
		var css = "";
		var style = Css.parse(JustStyle.class, css);

		assertThat(style.style()).isEqualTo(css);
	}

	@Test
	void justStyle_nonEmptyCss() {
		var css = """
				div {
					margin: 1em;
				}
				""";
		var style = Css.parse(JustStyle.class, css);

		assertThat(style.style()).isEqualTo(css);
	}

	public record OneReference(String ref, String style) implements CssStyle { }

	@Test
	void oneReference_referencedAsClass() {
		var css = """
				.ref {
					margin: 1em;
				}
				""";
		var style = Css.parse(OneReference.class, css);

		assertThat(style.ref()).matches("dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref");
		assertThat(style.style()).matches("""
				.dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref \\{
					margin: 1em;
				}
				""");
	}

	@Test
	void oneReference_referencedAsId() {
		var css = """
				#ref {
					margin: 1em;
				}
				""";
		var style = Css.parse(OneReference.class, css);

		assertThat(style.ref()).matches("dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref");
		assertThat(style.style()).matches("""
				#dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref \\{
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
		var style = Css.parse(OneReference.class, css);

		assertThat(style.ref()).matches("dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref");
		assertThat(style.style()).matches("""
				div.dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref \\{
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
		var style = Css.parse(OneReference.class, css);

		assertThat(style.ref()).matches("dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref");
		assertThat(style.style()).matches("""
				.dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref.other \\{
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
		var style = Css.parse(OneReference.class, css);

		assertThat(style.ref()).matches("dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref");
		assertThat(style.style()).matches("""
				.other.dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref \\{
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
		var style = Css.parse(OneReference.class, css);

		assertThat(style.ref()).matches("dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref");
		assertThat(style.style()).matches("""
				#dev-nipafx-ginevra-css-CssTests-OneReference--.*--ref
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
		var style = Css.parse(OneReference.class, css);

		assertThat(style.style()).isEqualTo(css);
	}

	@Test
	void oneReference_prefixOfReference() {
		var css = """
				.refWithSamePrefix {
					margin: 1em;
				}
				""";
		var style = Css.parse(OneReference.class, css);

		assertThat(style.style()).isEqualTo(css);
	}

	public record TwoReferences(String ref1, String ref2, String style) implements CssStyle { }

	@Test
	void twoReferences_referencedAsClassAndId() {
		var css = """
				.ref1 {
					margin: 1em;
				}
				#ref2 {
					margin: 2em;
				}
				""";
		var style = Css.parse(TwoReferences.class, css);

		assertThat(style.ref1()).matches("dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref1");
		assertThat(style.ref2()).matches("dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref2");
		assertThat(style.style()).matches("""
				.dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref1 \\{
					margin: 1em;
				}
				#dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref2 \\{
					margin: 2em;
				}
				""");
	}

	@Test
	void twoReferences_referencedAsClassAndIdOnSameElement() {
		var css = """
				#ref1.ref2 {
					margin: 1em;
				}
				""";
		var style = Css.parse(TwoReferences.class, css);

		assertThat(style.ref1()).matches("dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref1");
		assertThat(style.ref2()).matches("dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref2");
		assertThat(style.style()).matches("""
				#dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref1.dev-nipafx-ginevra-css-CssTests-TwoReferences--.*--ref2 \\{
					margin: 1em;
				}
				""");
	}

}
