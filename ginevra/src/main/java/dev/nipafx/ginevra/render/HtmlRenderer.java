package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Div;
import dev.nipafx.ginevra.html.Element;
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
			case Text(var text) -> renderer.text(text);
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

		private void updateNewLine(boolean maybeInsert) {
			if (addNewLineBeforeNextElement) {
				if (maybeInsert)
					builder.append("\n");
				addNewLineBeforeNextElement = false;
			}
		}

		private void attribute(String name, String value) {
			if (value == null || value.isBlank())
				return;

			builder.append(" ").append(name).append("=\"").append(value).append("\"");
		}

		public void close(String tag) {
			indentation--;

			updateNewLine(false);
			builder.repeat("\t", indentation).append("</").append(tag).append(">\n");
		}

		public void text(String text) {
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
