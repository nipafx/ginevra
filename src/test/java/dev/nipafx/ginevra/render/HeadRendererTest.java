package dev.nipafx.ginevra.render;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static dev.nipafx.ginevra.html.HtmlElement.head;
import static dev.nipafx.ginevra.render.HtmlRendererTest.RENDERER;
import static org.assertj.core.api.Assertions.assertThat;

class HeadRendererTest {

	@Test
	void empty() {
		var element = head;
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<head></head>
					""");
	}

	@Test
	void withTitle() {
		var element = head.title("The document title");
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<head>
						<title>The document title</title>
					</head>
					""");
	}

	@Test
	void withCharset() {
		var element = head.charset(StandardCharsets.UTF_8);
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<head>
						<meta charset="UTF-8" />
					</head>
					""");
	}

	@Test
	void withAll() {
		var element = head
				.title("The document title")
				.charset(StandardCharsets.UTF_8);
		var rendered = RENDERER.render(element);

		assertThat(rendered).isEqualTo("""
					<head>
						<meta charset="UTF-8" />
						<title>The document title</title>
					</head>
					""");
	}

}