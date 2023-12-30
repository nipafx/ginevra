package dev.nipafx.ginevra.parse.commonmark;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Heading;
import dev.nipafx.ginevra.html.HtmlElement;
import dev.nipafx.ginevra.html.JmlElement;
import dev.nipafx.ginevra.html.ListItem;
import dev.nipafx.ginevra.html.Nothing;
import dev.nipafx.ginevra.parse.MarkupParser;
import org.commonmark.node.Document;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.html.JmlElement.text;

public class CommonmarkParser implements MarkupParser {

	private final Parser parser;

	public CommonmarkParser(Parser parser) {
		this.parser = parser;
	}

	@Override
	public List<Element> parse(String markup) {
		var root = parser.parse(markup);
		if (!(root instanceof Document document))
			throw new IllegalStateException("Root element is supposed to be of type 'Document'");

		return streamChildren(document)
				.map(this::parse)
				.filter(element -> !(element instanceof Nothing))
				.toList();
	}

	private Element parse(Node node) {
		var children = streamChildren(node)
				.map(this::parse)
				.filter(element -> !(element instanceof Nothing))
				.toList();
		return switch (node) {
			case org.commonmark.node.BlockQuote _ -> HtmlElement.blockquote.children(children);
			case org.commonmark.node.BulletList _ -> {
				if (children.stream().anyMatch(child -> ! (child instanceof ListItem)))
					throw new IllegalStateException("List contains children that aren't list items: " + children);
				@SuppressWarnings({ "unchecked", "rawtypes" })
				var listItems = (List<ListItem>) (List) children;
				yield HtmlElement.ul.children(listItems);
			}
			case org.commonmark.node.Code code -> HtmlElement.code.text(code.getLiteral()).children(children);
			case org.commonmark.node.FencedCodeBlock cb -> JmlElement
					.codeBlock
					.language(nullIfBlank(cb.getInfo()))
					.text(nullIfBlank(cb.getLiteral()))
					.children(children);
			case org.commonmark.node.HtmlBlock html -> JmlElement.html.literal(html.getLiteral());
			case org.commonmark.node.Link a -> HtmlElement
					.a
					.href(nullIfBlank(a.getDestination()))
					.title(nullIfBlank(a.getTitle()))
					.children(children);
			case org.commonmark.node.LinkReferenceDefinition _ -> JmlElement.nothing;
			case org.commonmark.node.ListItem _ -> HtmlElement.li.children(children);
			case org.commonmark.node.Heading h -> new Heading(h.getLevel()).children(children);
			case org.commonmark.node.OrderedList ol -> {
				var start = ol.getStartNumber() == 1 ? null : ol.getStartNumber();
				if (children.stream().anyMatch(child -> ! (child instanceof ListItem)))
					throw new IllegalStateException("List contains children that aren't list items: " + children);
				@SuppressWarnings({ "unchecked", "rawtypes" })
				var listItems = (List<ListItem>) (List) children;
				yield HtmlElement.ol.start(start).children(listItems);
			}
			case org.commonmark.node.Paragraph _ -> HtmlElement.p.children(children);
			case org.commonmark.node.Text t -> text.text(nullIfBlank(t.getLiteral()));
			case org.commonmark.node.ThematicBreak _ -> HtmlElement.hr;
			default -> throw new IllegalArgumentException(
					STR."The node type '\{node.getClass().getSimpleName()}' is unsupported");
		};
	}

	private static Stream<Node> streamChildren(Node parent) {
		var child = parent.getFirstChild();
		if (child == null)
			return Stream.of();

		var children = new ArrayList<Node>();
		while (child != null) {
			children.add(child);
			child = child.getNext();
		}

		return children.stream();
	}

	private static String nullIfBlank(String value) {
		return value == null || value.isBlank() ? null : value;
	}

}
