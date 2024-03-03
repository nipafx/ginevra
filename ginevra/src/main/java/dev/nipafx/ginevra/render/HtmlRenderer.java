package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Id;

import java.util.Map;

class HtmlRenderer {

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
		open(tag, Id.none(), Classes.none(), Map.of());
	}

	public void open(String tag, Id id, Classes classes) {
		open(tag, id, classes, Map.of());
	}

	public void open(String tag, Map<String, String> attributes) {
		open(tag, Id.none(), Classes.none(), attributes);
	}

	public void open(String tag, Id id, Classes classes, Map<String, String> attributes) {
		switch (state) {
			case EMPTY -> builder.repeat("\t", indentation);
			case OPENED, CLOSED -> builder.append("\n").repeat("\t", indentation);
			case INLINE -> {
			}
		}

		builder.append("<").append(tag);
		attribute("id", id.asString());
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

	public void close(String tag) {
		indentation--;
		switch (state) {
			case EMPTY -> throw new IllegalStateException();
			case OPENED, INLINE -> {
			}
			case CLOSED -> builder.append("\n").repeat("\t", indentation);
		}

		builder.append("</").append(tag).append(">");
		state = State.CLOSED;
	}

	public void selfClosed(String tag, Id id, Classes classes) {
		selfClosed(tag, id, classes, Map.of());
	}

	public void selfClosed(String tag, Map<String, String> attributes) {
		selfClosed(tag, Id.none(), Classes.none(), attributes);
	}

	public void selfClosed(String tag, Id id, Classes classes, Map<String, String> attributes) {
		switch (state) {
			case EMPTY -> builder.repeat("\t", indentation);
			case OPENED, CLOSED -> builder.append("\n").repeat("\t", indentation);
			case INLINE -> {
			}
		}

		builder.append("<").append(tag);
		attribute("id", id.asString());
		attribute("class", classes.asCssString());
		attributes.forEach(this::attribute);
		builder.append(" />");

		state = State.CLOSED;
	}

	public void insertText(String text) {
		switch (state) {
			case EMPTY -> builder.repeat("\t", indentation);
			case OPENED, INLINE, CLOSED -> {
			}
		}
		builder.append(text);
		state = State.INLINE;
	}

	public String render() {
		// closing tags don't end with a newline, so
		// add one final newline before assembling the result
		if (state == State.CLOSED)
			builder.append("\n");
		return builder.toString();
	}

}
