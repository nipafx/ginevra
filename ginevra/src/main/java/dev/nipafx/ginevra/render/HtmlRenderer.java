package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Anchor;
import dev.nipafx.ginevra.html.BlockQuote;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Code;
import dev.nipafx.ginevra.html.CodeBlock;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Div;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Emphasis;
import dev.nipafx.ginevra.html.Heading;
import dev.nipafx.ginevra.html.HorizontalRule;
import dev.nipafx.ginevra.html.HtmlLiteral;
import dev.nipafx.ginevra.html.LineBreak;
import dev.nipafx.ginevra.html.ListItem;
import dev.nipafx.ginevra.html.Nothing;
import dev.nipafx.ginevra.html.OrderedList;
import dev.nipafx.ginevra.html.Paragraph;
import dev.nipafx.ginevra.html.Pre;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.ginevra.html.Strong;
import dev.nipafx.ginevra.html.Text;
import dev.nipafx.ginevra.html.UnorderedList;

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
			case BlockQuote(var id, var classes, var text, var children) -> {
				renderer.open("blockquote", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("blockquote");
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
			case Emphasis(var id, var classes, var text, var children) -> {
				renderer.open("em", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("em");
			}
			case Heading(var level, var id, var classes, var text, var children) -> {
				renderer.open("h" + level, id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("h" + level);
			}
			case HtmlLiteral(var literal) when literal == null || literal.isBlank() -> { }
			case HtmlLiteral(var literal) -> renderer.insertTextElement(literal);
			case HorizontalRule(var id, var classes) -> renderer.selfClosed("hr", id, classes);
			case LineBreak(var id, var classes) -> renderer.selfClosed("br", id, classes);
			case ListItem(String id, Classes classes, String text, List<Element> children) -> {
				renderer.open("li", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("li");
			}
			case Nothing _ -> { }
			case OrderedList(var id, var classes, var start, var children) -> {
				renderer.open("ol", id, classes,
						attributes("start", start == null ? null : String.valueOf(start)));
				children.forEach(child -> render(child, renderer));
				renderer.close("ol");
			}
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
			case Strong(var id, var classes, var text, var children) -> {
				renderer.open("strong", id, classes);
				renderer.insertChildren(text, children, child -> render(child, renderer));
				renderer.close("strong");
			}
			case Text(var text) when text == null || text.isBlank() -> { }
			case Text(var text) -> renderer.insertTextElement(text);
			case UnorderedList(var id, var classes, var children) -> {
				renderer.open("ul", id, classes);
				children.forEach(child -> render(child, renderer));
				renderer.close("ul");
			}
			case CustomElement customElement -> customElement.render().forEach(child -> render(child, renderer));
		}
	}

	private static Map<String, String> attributes(String... namesAndValues) {
		if (namesAndValues.length % 2 != 0)
			throw new IllegalArgumentException();

		var attributes = new LinkedHashMap<String, String>();
		for (int i = 0; i < namesAndValues.length; i += 2)
			attributes.put(namesAndValues[i], namesAndValues[i + 1]);
		return attributes;
	}

	// package-visible for tests
	Element express(CodeBlock block) {
		var codeClasses = block.language() == null ? Classes.none() : Classes.of(STR."language-\{block.language()}");
		return pre.id(block.id()).classes(block.classes()).children(
				code.classes(codeClasses).text(block.text()).children(block.children()));
	}

	private static class Renderer {

		private enum State { EMPTY, OPENED, INLINE, CLOSED }

		private final StringBuilder builder = new StringBuilder();
		/**
		 * To render elements without children on one line (e.g. {@code <div></div>}),
		 * tags are not always followed by a new line; instead this state indicates
		 * what was previously rendered and then all follow-up actions must check and
		 * add a newline and indentation accordingly.
		 */
		private State state = State.EMPTY;
		private int indentation = 0;

		public void open(String tag) {
			open(tag, null, Classes.none(), attributes());
		}

		public void open(String tag, String id, Classes classes) {
			open(tag, id, classes, attributes());
		}

		public void open(String tag, Map<String, String> attributes) {
			open(tag, null, Classes.none(), attributes);
		}

		public void open(String tag, String id, Classes classes, Map<String, String> attributes) {
			switch (state)  {
				case EMPTY, CLOSED  -> builder.repeat("\t", indentation);
				case OPENED-> builder.append("\n").repeat("\t", indentation);
				case INLINE -> throw new IllegalStateException();
			}

			builder.append("<").append(tag);
			attribute("id", id);
			attribute("class", classes.asCssString());
			attributes.forEach(this::attribute);
			builder.append(">");

			indentation++;
			state = State.OPENED;
		}

		private void attribute(String name, String value) {
			if (value == null || value.isBlank())
				return;

			builder.append(" ").append(name).append("=\"").append(value).append("\"");
		}

		public void insertChildren(List<? extends Element> children, Consumer<Element> renderChild) {
			insertChildren(null, children, renderChild);
		}

		public void insertChildren(String text, List<? extends Element> children, Consumer<Element> renderChild) {
			if (text == null)
				children.forEach(renderChild);
			else
				insertText(text);
		}

		public void close(String tag) {
			indentation--;
			switch (state)  {
				case EMPTY -> throw new IllegalStateException();
				case OPENED, INLINE -> { }
				case CLOSED -> builder.repeat("\t", indentation);
			}

			builder.append("</").append(tag).append(">\n");
			state = State.CLOSED;
		}

		public void selfClosed(String tag, String id, Classes classes) {
			selfClosed(tag, id, classes, attributes());
		}

		public void selfClosed(String tag, Map<String, String> attributes) {
			selfClosed(tag, null, Classes.none(), attributes);
		}

		public void selfClosed(String tag, String id, Classes classes, Map<String, String> attributes) {
			switch (state)  {
				case EMPTY -> builder.repeat("\t", indentation);
				case OPENED, CLOSED -> builder.append("\n").repeat("\t", indentation);
				case INLINE -> throw new IllegalStateException();
			}

			builder.append("<").append(tag);
			attribute("id", id);
			attribute("class", classes.asCssString());
			attributes.forEach(this::attribute);
			builder.append(" />\n");

			state = State.CLOSED;
		}

		private void insertText(String text) {
			switch (state)  {
				case EMPTY, INLINE, CLOSED -> throw new IllegalStateException();
				case OPENED -> { }
			}
			builder.append(text);
			state = State.INLINE;
		}

		public void insertTextElement(String text) {
			switch (state)  {
				case EMPTY, CLOSED -> builder.repeat("\t", indentation);
				case OPENED -> builder.append("\n").repeat("\t", indentation);
				case INLINE -> throw new IllegalStateException();
			}
			builder.append(text).append("\n");
			state = State.CLOSED;
		}

		public String render() {
			return builder.toString();
		}

	}

}
