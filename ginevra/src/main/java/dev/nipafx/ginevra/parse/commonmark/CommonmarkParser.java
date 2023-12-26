package dev.nipafx.ginevra.parse.commonmark;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.HtmlElement;
import dev.nipafx.ginevra.html.Text;
import dev.nipafx.ginevra.parse.MarkupParser;
import org.commonmark.node.Document;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
				.toList();
	}

	private Element parse(Node node) {
		var children = streamChildren(node)
				.map(this::parse)
				.toArray(Element[]::new);
		return switch (node) {
			case org.commonmark.node.Paragraph _ -> HtmlElement.p.children(children);
			case org.commonmark.node.Text t -> new Text(t.getLiteral());
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

}
