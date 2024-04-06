package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.execution.StoreFront;
import dev.nipafx.ginevra.html.Anchor;
import dev.nipafx.ginevra.html.BlockQuote;
import dev.nipafx.ginevra.html.Body;
import dev.nipafx.ginevra.html.Code;
import dev.nipafx.ginevra.html.CodeBlock;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Div;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Emphasis;
import dev.nipafx.ginevra.html.Head;
import dev.nipafx.ginevra.html.Heading;
import dev.nipafx.ginevra.html.HorizontalRule;
import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.html.HtmlElement;
import dev.nipafx.ginevra.html.HtmlLiteral;
import dev.nipafx.ginevra.html.Image;
import dev.nipafx.ginevra.html.JmlElement;
import dev.nipafx.ginevra.html.LineBreak;
import dev.nipafx.ginevra.html.Link;
import dev.nipafx.ginevra.html.ListItem;
import dev.nipafx.ginevra.html.Meta;
import dev.nipafx.ginevra.html.Nothing;
import dev.nipafx.ginevra.html.OrderedList;
import dev.nipafx.ginevra.html.Paragraph;
import dev.nipafx.ginevra.html.Pre;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.ginevra.html.Strong;
import dev.nipafx.ginevra.html.Text;
import dev.nipafx.ginevra.html.UnorderedList;
import dev.nipafx.ginevra.outline.Template;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class Renderer {

	private final ElementResolver resolver;

	public Renderer(StoreFront store, Path resourceFolder, Path cssFolder) {
		this.resolver = new ElementResolver(store, resourceFolder, cssFolder);
	}

	public HtmlWithResources renderAsDocument(Element element, Template<?> maybeStyledTemplate) {
		var resolvedDocument = resolver.resolveToDocument(element, Optional.of(maybeStyledTemplate));
		var html = new HtmlRenderer();
		writeToRenderer(resolvedDocument.document(), html);
		return new HtmlWithResources(html.render(), resolvedDocument.referencedResources());
	}

	// package visible for tests
	String render(Element element) {
		var html = new HtmlRenderer();
		resolver
				.resolve(element)
				.forEach(resolvedElement -> writeToRenderer(resolvedElement, html));
		return html.render();
	}

	private void writeToRenderer(Element element, HtmlRenderer html) {
		switch (element) {
			case HtmlElement htmlElement -> {
				switch (htmlElement) {
					case Anchor(var id, var classes, var href, var title, var text, var children) -> {
						html.open("a", id, classes, attributes("href", href, "title", title));
						renderChildren(text, children, html);
						html.close("a");
					}
					case BlockQuote(var id, var classes, var text, var children) -> {
						html.open("blockquote", id, classes);
						renderChildren(text, children, html);
						html.close("blockquote");
					}
					case Body(var id, var classes, var content) -> {
						html.open("body", id, classes);
						renderChildren(content, html);
						html.close("body");
					}
					case Code(var id, var classes, var text, var children) -> {
						html.open("code", id, classes);
						renderChildren(text, children, html);
						html.close("code");
					}
					case Div(var id, var classes, var children) -> {
						html.open("div", id, classes);
						renderChildren(children, html);
						html.close("div");
					}
					case Emphasis(var id, var classes, var text, var children) -> {
						html.open("em", id, classes);
						renderChildren(text, children, html);
						html.close("em");
					}
					case Head(var title, var charset, var children) -> {
						html.open("head");
						if (charset != null)
							html.selfClosed("meta", attributes("charset", charset.name()));
						if (title != null) {
							html.open("title");
							html.insertText(title);
							html.close("title");
						}
						renderChildren(children, html);
						html.close("head");
					}
					case Heading(var level, var id, var classes, var text, var children) -> {
						html.open("h" + level, id, classes);
						renderChildren(text, children, html);
						html.close("h" + level);
					}
					case HtmlDocument(var language, var head, var body) -> {
						html.insertText("<!doctype html>\n");
						var lang = language == null
								? attributes()
								: attributes("lang", language.getLanguage());
						html.open("html", lang);
						var children = Stream.of(head, body).filter(not(Objects::isNull)).toList();
						renderChildren(children, html);
						html.close("html");
					}
					case HorizontalRule(var id, var classes) -> html.selfClosed("hr", id, classes);
					case Image(var id, var classes, var src, var title, var alt) -> html
							.selfClosed("img", id, classes,
									attributes("src", src.path(), "title", title, "alt", alt));
					case LineBreak(var id, var classes) -> html.selfClosed("br", id, classes);
					case Link(var href, var rel) -> html.selfClosed("link", attributes("href", href, "rel", rel));
					case ListItem(var id, var classes, var text, var children) -> {
						html.open("li", id, classes);
						renderChildren(text, children, html);
						html.close("li");
					}
					case Meta(var name, var content) -> html.selfClosed("meta", attributes("name", name, "content", content));
					case OrderedList(var id, var classes, var start, var children) -> {
						html.open("ol", id, classes,
								attributes("start", start == null ? null : String.valueOf(start)));
						renderChildren(children, html);
						html.close("ol");
					}
					case Paragraph(var id, var classes, var text, var children) -> {
						html.open("p", id, classes);
						renderChildren(text, children, html);
						html.close("p");
					}
					case Pre(var id, var classes, var text, var children) -> {
						html.open("pre", id, classes);
						renderChildren(text, children, html);
						html.close("pre");
					}
					case Span(var id, var classes, var text, var children) -> {
						html.open("span", id, classes);
						renderChildren(text, children, html);
						html.close("span");
					}
					case Strong(var id, var classes, var text, var children) -> {
						html.open("strong", id, classes);
						renderChildren(text, children, html);
						html.close("strong");
					}
					case UnorderedList(var id, var classes, var children) -> {
						html.open("ul", id, classes);
						renderChildren(children, html);
						html.close("ul");
					}
				}
			}
			case JmlElement jmlElement -> {
				switch (jmlElement) {
					case CodeBlock _ ->
							throw new IllegalArgumentException("CodeBlock elements should've been resolved");
					case HtmlLiteral(var literal) -> html.insertText(literal);
					case Nothing _ -> throw new IllegalArgumentException("Nothing elements should've been resolved");
					case Text(var text) -> html.insertText(text);
				}
			}
			case CustomElement _ -> throw new IllegalArgumentException("Custom elements should've been resolved");
		}
	}

	private void renderChildren(String text, List<? extends Element> children, HtmlRenderer renderer) {
		if (text == null)
			renderChildren(children, renderer);
		else
			renderer.insertText(text);
	}

	private void renderChildren(List<? extends Element> children, HtmlRenderer renderer) {
		children.forEach(child -> writeToRenderer(child, renderer));
	}

	private static Map<String, String> attributes(String... namesAndValues) {
		if (namesAndValues.length % 2 != 0)
			throw new IllegalArgumentException();

		var attributes = new LinkedHashMap<String, String>();
		for (int i = 0; i < namesAndValues.length; i += 2)
			attributes.put(namesAndValues[i], namesAndValues[i + 1]);
		return attributes;
	}

}
