package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static dev.nipafx.ginevra.html.HtmlElement.code;
import static dev.nipafx.ginevra.html.HtmlElement.pre;
import static dev.nipafx.ginevra.html.JmlElement.codeBlock;
import static dev.nipafx.ginevra.html.JmlElement.text;
import static org.assertj.core.api.Assertions.assertThat;

class CodeBlockResolverTest {

	private static final ElementResolver RESOLVER = new ElementResolver(Optional.empty());

	@Test
	void empty() {
		var block = codeBlock;
		var expressed = RESOLVER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code));
	}

	@Test
	void withId() {
		var block = codeBlock.id("the-id");
		var expressed = RESOLVER.express(block);

		assertThat(expressed).isEqualTo(
				pre.id("the-id").children(code));
	}

	@Test
	void withClass() {
		var block = codeBlock.classes(Classes.of("the-id"));
		var expressed = RESOLVER.express(block);

		assertThat(expressed).isEqualTo(
				pre.classes(Classes.of("the-id")).children(code));
	}

	@Test
	void withLanguage() {
		var block = codeBlock.language("java");
		var expressed = RESOLVER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.classes(Classes.of("language-java"))));
	}

	@Test
	void withText() {
		var block = codeBlock.text("void main() { println(\"When?\"); }");
		var expressed = RESOLVER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.text("void main() { println(\"When?\"); }")));
	}

	@Test
	void withTextChild() {
		var block = codeBlock.children(text.text("void main() { println(\"When?\"); }"));
		var expressed = RESOLVER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.text("void main() { println(\"When?\"); }")));
	}

	@Test
	void withChildren() {
		var block = codeBlock.children(
				text.text("void main() {"),
				text.text("\tprintln(\"When?\");"),
				text.text("}"));
		var expressed = RESOLVER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.children(
						text.text("void main() {"),
						text.text("\tprintln(\"When?\");"),
						text.text("}"))));
	}

}