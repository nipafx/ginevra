package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Text;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.code;
import static dev.nipafx.ginevra.html.HtmlElement.pre;
import static dev.nipafx.ginevra.html.JmlElement.codeBlock;
import static org.assertj.core.api.Assertions.assertThat;

class CodeBlockRendererTest {

	private static final HtmlRenderer RENDERER = new HtmlRenderer();

	@Test
	void empty() {
		var block = codeBlock;
		var expressed = RENDERER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code));
	}

	@Test
	void withId() {
		var block = codeBlock.id("the-id");
		var expressed = RENDERER.express(block);

		assertThat(expressed).isEqualTo(
				pre.id("the-id").children(code));
	}

	@Test
	void withClass() {
		var block = codeBlock.classes(Classes.of("the-id"));
		var expressed = RENDERER.express(block);

		assertThat(expressed).isEqualTo(
				pre.classes(Classes.of("the-id")).children(code));
	}

	@Test
	void withLanguage() {
		var block = codeBlock.language("java");
		var expressed = RENDERER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.classes(Classes.of("language-java"))));
	}

	@Test
	void withText() {
		var block = codeBlock.text("void main() { println(\"When?\"); }");
		var expressed = RENDERER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.text("void main() { println(\"When?\"); }")));
	}

	@Test
	void withTextChild() {
		var block = codeBlock.children(new Text("void main() { println(\"When?\"); }"));
		var expressed = RENDERER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.text("void main() { println(\"When?\"); }")));
	}

	@Test
	void withChildren() {
		var block = codeBlock.children(
				new Text("void main() {"),
				new Text("\tprintln(\"When?\");"),
				new Text("}"));
		var expressed = RENDERER.express(block);

		assertThat(expressed).isEqualTo(
				pre.children(code.children(
						new Text("void main() {"),
						new Text("\tprintln(\"When?\");"),
						new Text("}"))));
	}

}