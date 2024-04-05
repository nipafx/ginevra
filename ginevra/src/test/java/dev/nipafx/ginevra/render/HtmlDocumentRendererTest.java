package dev.nipafx.ginevra.render;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.render.HtmlRendererTest.RENDERER;
import static org.assertj.core.api.Assertions.assertThat;

class HtmlDocumentRendererTest {

	@Test
	void empty() {
		var element = document;
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<!doctype html>
					<html></html>
					""");
	}

	@Test
	void withLanguage() {
		var element = document.language(Locale.US);
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<!doctype html>
					<html lang="en"></html>
					""");
	}

	@Test
	void withHead() {
		var element = document.head(head);
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<!doctype html>
					<html>
						<head></head>
					</html>
					""");
	}

	@Test
	void withBody() {
		var element = document.body(body);
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<!doctype html>
					<html>
						<body></body>
					</html>
					""");
	}

	@Test
	void withEverything() {
		var element = document
				.language(Locale.US)
				.head(head
						.charset(StandardCharsets.UTF_8)
						.title("The document title"))
				.body(body
						.children(List.of(
								p.text("Hello"),
								p.text("World"))));
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<!doctype html>
					<html lang="en">
						<head>
							<meta charset="UTF-8" />
							<title>The document title</title>
						</head>
						<body>
							<p>Hello</p>
							<p>World</p>
						</body>
					</html>
					""");
	}

}
