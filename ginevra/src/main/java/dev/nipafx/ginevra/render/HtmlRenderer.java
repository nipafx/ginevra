package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Div;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Heading;
import dev.nipafx.ginevra.html.HorizontalRule;
import dev.nipafx.ginevra.html.Paragraph;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.ginevra.html.Text;

public class HtmlRenderer {

	public String render(Element element) {
		var renderer = new Renderer();
		render(element, renderer);
		return renderer.render();
	}

	private void render(Element element, Renderer renderer) {
		switch (element) {
			case Div(var id, var classes, var children) -> {
				renderer.open("div", id, classes);
				children.forEach(child -> render(child, renderer));
				renderer.close("div");
			}
			case Heading(var level, var id, var classes, var text, var children) -> {
				renderer.open("h" + level, id, classes);
				if (text == null)
					children.forEach(child -> render(child, renderer));
				else
					renderer.insertText(text);
				renderer.close("h" + level);
			}
			case HorizontalRule(var id, var classes) -> renderer.selfClosed("hr", id, classes);
			case Paragraph(var id, var classes, var text, var children) -> {
				renderer.open("p", id, classes);
				if (text == null)
					children.forEach(child -> render(child, renderer));
				else
					renderer.insertText(text);
				renderer.close("p");
			}
			case Span(var id, var classes, var text, var children) -> {
				renderer.open("span", id, classes);
				if (text == null)
					children.forEach(child -> render(child, renderer));
				else
					renderer.insertText(text);
				renderer.close("span");
			}
			case Text(var text) -> renderer.insertTextElement(text);
			case CustomElement customElement -> customElement.render().forEach(child -> render(child, renderer));
		}
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
			updateNewLine(true);
			builder.repeat("\t", indentation).append("<").append(tag);
			attribute("id", id);
			attribute("class", classes.asCssString());
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

		public void insertText(String text) {
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
