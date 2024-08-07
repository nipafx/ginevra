package dev.nipafx.ginevra.parse.commonmark;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.GmlElement;
import dev.nipafx.ginevra.html.Heading;
import dev.nipafx.ginevra.html.HtmlElement;
import dev.nipafx.ginevra.html.ListItem;
import dev.nipafx.ginevra.html.Nothing;
import dev.nipafx.ginevra.html.Src;
import dev.nipafx.ginevra.outline.HtmlContent;
import dev.nipafx.ginevra.outline.Resources;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.MarkupDocument;
import dev.nipafx.ginevra.parse.MarkupDocument.FrontMatter;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Document;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.StreamUtils.only;
import static java.util.stream.Collectors.joining;

public class CommonmarkParser implements MarkdownParser {

	private final Parser parser;

	public CommonmarkParser(Parser parser) {
		this.parser = parser;
	}

	@Override
	public String name() {
		return "Commonmark";
	}

	@Override
	public MarkupDocument parse(String markup) {
		var root = parser.parse(markup);
		if (!(root instanceof Document document))
			throw new IllegalStateException("Root element is supposed to be of type 'Document'");

		var frontMatterVisitor = new YamlFrontMatterVisitor();
		document.accept(frontMatterVisitor);
		var frontMatter = frontMatterVisitor.getData();

		var elements = streamChildren(document)
				.map(this::parse)
				.filter(element -> !(element instanceof Nothing))
				.toList();
		return new CommonmarkDocument(new CommonmarkFrontMatter(frontMatter), new HtmlContent(elements));
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
			case org.commonmark.node.Emphasis _ -> HtmlElement.em.children(children);
			case org.commonmark.node.FencedCodeBlock cb -> GmlElement
					.codeBlock
					.language(nullIfBlank(cb.getInfo()))
					.text(nullIfBlank(cb.getLiteral()))
					.children(children);
			case org.commonmark.node.HardLineBreak _ -> HtmlElement.br;
			case org.commonmark.node.Heading h -> new Heading(h.getLevel()).children(children);
			case org.commonmark.node.HtmlBlock html -> GmlElement.html.literal(html.getLiteral());
			case org.commonmark.node.HtmlInline html -> GmlElement.html.literal(html.getLiteral());
			case org.commonmark.node.Image img -> {
				var alt = streamChildren(img)
						.gather(only(org.commonmark.node.Text.class))
						.map(child -> child instanceof org.commonmark.node.Text t
								? t.getLiteral()
								: " ")
						.collect(joining());
				yield HtmlElement.img
						.src(img.getDestination() == null ? Src.none() : Resources.includeIfLocal(img.getDestination()))
						.title(nullIfBlank(img.getTitle()))
						.alt(alt);
			}
			case org.commonmark.node.IndentedCodeBlock cb -> GmlElement
					.codeBlock
					.text(nullIfBlank(cb.getLiteral()))
					.children(children);
			case org.commonmark.node.Link a -> HtmlElement
					.a
					.href(nullIfBlank(a.getDestination()))
					.title(nullIfBlank(a.getTitle()))
					.children(children);
			case org.commonmark.node.LinkReferenceDefinition _ -> GmlElement.nothing;
			case org.commonmark.node.ListItem _ -> HtmlElement.li.children(children);
			case org.commonmark.node.OrderedList ol -> {
				var start = ol.getMarkerStartNumber();
				if (children.stream().anyMatch(child -> ! (child instanceof ListItem)))
					throw new IllegalStateException("List contains children that aren't list items: " + children);
				@SuppressWarnings({ "unchecked", "rawtypes" })
				var listItems = (List<ListItem>) (List) children;
				yield HtmlElement.ol.start(start == 1 ? null : start).children(listItems);
			}
			case org.commonmark.node.Paragraph _ -> HtmlElement.p.children(children);
			case org.commonmark.node.SoftLineBreak _ -> GmlElement.text.text(" ");
			case org.commonmark.node.StrongEmphasis _ -> HtmlElement.strong.children(children);
			case org.commonmark.node.Text t -> GmlElement.text.text(nullIfBlank(t.getLiteral()));
			case org.commonmark.node.ThematicBreak _ -> HtmlElement.hr;
			case org.commonmark.ext.front.matter.YamlFrontMatterBlock _ -> GmlElement.nothing;
			case org.commonmark.ext.front.matter.YamlFrontMatterNode _ -> GmlElement.nothing;
			default -> throw new IllegalArgumentException("The node type '%s' is unsupported".formatted(node.getClass().getSimpleName()));
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

	private record CommonmarkDocument(FrontMatter frontMatter, HtmlContent content) implements MarkupDocument { }

	private record CommonmarkFrontMatter(Map<String, List<String>> asMap) implements FrontMatter { }

}
