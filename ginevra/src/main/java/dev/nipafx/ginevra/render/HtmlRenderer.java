package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Anchor;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Code;
import dev.nipafx.ginevra.html.CodeBlock;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Div;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Heading;
import dev.nipafx.ginevra.html.HorizontalRule;
import dev.nipafx.ginevra.html.HtmlLiteral;
import dev.nipafx.ginevra.html.Nothing;
import dev.nipafx.ginevra.html.Paragraph;
import dev.nipafx.ginevra.html.Pre;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.ginevra.html.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static dev.nipafx.ginevra.html.HtmlElement.code;
import static dev.nipafx.ginevra.html.HtmlElement.pre;

public class HtmlRenderer {

	public String render(Element element) {
		var renderer = new Renderer();
		render(element, renderer);
		return renderer.render();
	}

	private void render(Element element, Renderer renderer) {
		switch (element) {
			case Anchor(var id, var classes, var href, var title, var text, var children) -> {
				renderer.open("a", id, classes, attributes("href", href, "title", title));
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("a");
			}
			case Code(var id, var classes, var text, var children) -> {
				renderer.open("code", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("code");
			}
			case CodeBlock codeBlock -> render(express(codeBlock), renderer);
			case Div(var id, var classes, var children) -> {
				renderer.open("div", id, classes);
				children.forEach(child -> render(child, renderer));
				renderer.close("div");
			}
			case Heading(var level, var id, var classes, var text, var children) -> {
				renderer.open("h" + level, id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("h" + level);
			}
			case HtmlLiteral(var literal) when literal == null || literal.isBlank() -> { }
			case HtmlLiteral(var literal) -> renderer.insertTextElement(literal);
			case HorizontalRule(var id, var classes) -> renderer.selfClosed("hr", id, classes);
			case Nothing _ -> { }
			case Paragraph(var id, var classes, var text, var children) -> {
				renderer.open("p", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("p");
			}
			case Pre(var id, var classes, var text, var children) -> {
				renderer.open("pre", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("pre");
			}
			case Span(var id, var classes, var text, var children) -> {
				renderer.open("span", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("span");
			}
			case Text(var text) -> renderer.insertTextElement(text);
			case CustomElement customElement -> customElement.render().forEach(child -> render(child, renderer));
		}
	}

	private static Map<String, String> attributes(String... namesAndValues) {
		if (namesAndValues.length % 2 != 0)
			throw new IllegalArgumentException();

		var attributes = new LinkedHashMap<String, String>();
		for (int i = 0; i < namesAndValues.length; i += 2)
			attributes.put(namesAndValues[i], namesAndValues[i+1]);
		return attributes;
	}

	// package-visible for tests
	Element express(CodeBlock block) {
		var codeClasses = block.language() == null ? Classes.none() : Classes.of(STR."language-\{block.language()}");
		return pre.id(block.id()).classes(block.classes()).children(
				code.classes(codeClasses).text(block.text()).children(block.children()));
	}

	private static class Renderer {

		private final StringBuilder builder = new StringBuilder();
		/**
		 * To render elements without children on one line (e.g. {@code <div></div>}),
		 * opening tags are not followed by a new line; instead this flag is set to true
		 * and then all follow-up actions must check and potentially add a newline.
		 */
		private boolean addNewLineBeforeNextElement = false;
		private int indentation = 0;

		public void open(String tag, String id, Classes classes) {
			open(tag, id, classes, Map.of());
		}

		public void open(String tag, String id, Classes classes, Map<String, String> attributes) {
			updateNewLine(true);
			builder.repeat("\t", indentation).append("<").append(tag);
			attribute("id", id);
			attribute("class", classes.asCssString());
			attributes.forEach(this::attribute);
			builder.append(">");

			indentation++;
			addNewLineBeforeNextElement = true;
		}

		/**
		 * Inserts a new line if indicated by {@code addNewLineBeforeNextElement} and {@code insertNewLine}
		 * and resets {@code addNewLineBeforeNextElement}.
		 *
		 * @param insertNewLine whether a new line should be inserted
		 * @return whether the renderer stands on a new line after this call
		 */
		private boolean updateNewLine(boolean insertNewLine) {
			if (addNewLineBeforeNextElement && insertNewLine)
				builder.append("\n");
			var onNewLine = !addNewLineBeforeNextElement || insertNewLine;
			addNewLineBeforeNextElement = false;
			return onNewLine;
		}

		private void attribute(String name, String value) {
			if (value == null || value.isBlank())
				return;

			builder.append(" ").append(name).append("=\"").append(value).append("\"");
		}

		public void insertChildren(String text, List<Element> children, Consumer<Element> renderChild) {
			if (text == null)
				children.forEach(renderChild);
			else
				insertText(text);
		}

		public void close(String tag) {
			indentation--;

			var onNewLine = updateNewLine(false);
			if (onNewLine)
				builder.repeat("\t", indentation);
			builder.append("</").append(tag).append(">\n");
		}

		public void selfClosed(String tag, String id, Classes classes) {
			updateNewLine(true);
			builder.repeat("\t", indentation).append("<").append(tag);
			attribute("id", id);
			attribute("class", classes.asCssString());
			builder.append(" />\n");
		}

		private void insertText(String text) {
			updateNewLine(false);
			builder.append(text);
		}

		public void insertTextElement(String text) {
			if (!text.isBlank()) {
				updateNewLine(true);
				builder.repeat("\t", indentation).append(text).append("\n");
			}
		}

		public String render() {
			return builder.toString();
		}

	}

}
