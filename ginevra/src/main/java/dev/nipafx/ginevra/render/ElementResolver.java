package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.Anchor;
import dev.nipafx.ginevra.html.BlockQuote;
import dev.nipafx.ginevra.html.Body;
import dev.nipafx.ginevra.html.Classes;
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
import dev.nipafx.ginevra.html.KnownElement;
import dev.nipafx.ginevra.html.LineBreak;
import dev.nipafx.ginevra.html.Link;
import dev.nipafx.ginevra.html.ListItem;
import dev.nipafx.ginevra.html.Nothing;
import dev.nipafx.ginevra.html.OrderedList;
import dev.nipafx.ginevra.html.Paragraph;
import dev.nipafx.ginevra.html.Pre;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.ginevra.html.Strong;
import dev.nipafx.ginevra.html.Text;
import dev.nipafx.ginevra.html.UnorderedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.code;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.pre;

class ElementResolver {

	private final Optional<CssRenderer> cssRenderer;

	public ElementResolver(Optional<CssRenderer> cssRenderer) {
		this.cssRenderer = cssRenderer;
	}

	public HtmlDocument resolveToDocument(Element element, Optional<?> maybeStyledTemplate) {
		var sideData = new SideData();
		maybeStyledTemplate.ifPresent(maybeStyled -> addStyleToSideData(sideData, maybeStyled));
		var elementResolution = resolve(element, sideData);

		if (elementResolution.size() == 1 && elementResolution.getFirst() instanceof HtmlDocument htmlDoc)
			return htmlDoc.head(mergeLinksIntoHead(htmlDoc.head(), sideData.links()));
		else
			return document.body(body.children(elementResolution));
	}

	private Head mergeLinksIntoHead(Head head, List<Link> styles) {
		if (head == null && styles.isEmpty())
			return null;
		else if (head == null)
			return HtmlElement.head.children(styles);

		var newHeadChildren = new ArrayList<Element>(head.children());
		newHeadChildren.addAll(styles);
		return head.children(newHeadChildren);
	}

	public List<KnownElement> resolve(Element element) {
		return resolve(element, new SideData());
	}

	private List<KnownElement> resolve(Element element, SideData sideData) {
		return switch (element) {
			case HtmlElement htmlElement -> {
				var resolvedElement = switch (htmlElement) {
					case Anchor el -> el.children(resolveChildren(el.children(), sideData));
					case BlockQuote el -> el.children(resolveChildren(el.children(), sideData));
					case Body el -> el.children(resolveChildren(el.children(), sideData));
					case Code el -> el.children(resolveChildren(el.children(), sideData));
					case Div el -> el.children(resolveChildren(el.children(), sideData));
					case Emphasis el -> el.children(resolveChildren(el.children(), sideData));
					case Head el -> el.children(resolveChildren(el.children(), sideData));
					case Heading el -> el.children(resolveChildren(el.children(), sideData));
					case HorizontalRule el -> el;
					case HtmlDocument doc -> {
						var head = doc.head() == null
								? null
								: doc.head().children(resolveChildren(doc.head().children(), sideData));
						var body = doc.body() == null
								? null
								: doc.body().children(resolveChildren(doc.body().children(), sideData));
						yield doc.head(head).body(body);
					}
					case Image el -> el;
					case LineBreak el -> el;
					case Link el -> el;
					case ListItem el -> el.children(resolveChildren(el.children(), sideData));
					case OrderedList list -> list.children(list
							.children().stream()
							.map(listItem -> listItem.children(listItem
									.children().stream()
									.flatMap(listItemChild -> resolve(listItemChild, sideData).stream())
									.toList()))
							.toList());
					case Paragraph el -> el.children(resolveChildren(el.children(), sideData));
					case Pre el -> el.children(resolveChildren(el.children(), sideData));
					case Span el -> el.children(resolveChildren(el.children(), sideData));
					case Strong el -> el.children(resolveChildren(el.children(), sideData));
					case UnorderedList list -> list.children(list
							.children().stream()
							.map(listItem -> listItem.children(listItem
									.children().stream()
									.flatMap(listItemChild -> resolve(listItemChild, sideData).stream())
									.toList()))
							.toList());
				};
				yield List.of(resolvedElement);
			}
			case JmlElement jmlElement -> switch (jmlElement) {
				case CodeBlock codeBlock -> List.of(express(codeBlock));
				case HtmlLiteral(var literal) when literal == null || literal.isBlank() -> List.of();
				case HtmlLiteral el -> List.of(el);
				case Nothing _ -> List.of();
				case Text(var text) when text == null || text.isBlank() -> List.of();
				case Text text -> List.of(text);
			};
			case CustomElement customElement -> {
				addStyleToSideData(sideData, customElement);
				yield customElement
						.compose().stream()
						// a custom element may return a new custom element, so keep resolving
						.flatMap(resolved -> resolve(resolved, sideData).stream())
						.toList();
			}
		};
	}

	private void addStyleToSideData(SideData sideData, Object maybeStyled) {
		if (maybeStyled instanceof CssStyled<?> styled && cssRenderer.isPresent()) {
			var link = cssRenderer.get().processStyle(styled);
			sideData.links().add(link);
		}
	}

	private List<KnownElement> resolveChildren(List<? extends Element> children, SideData sideData) {
		return children.stream()
				.flatMap(element -> resolve(element, sideData).stream())
				.toList();
	}

	// package-visible for tests
	HtmlElement express(CodeBlock block) {
		var codeClasses = block.language() == null ? Classes.none() : Classes.of(STR."language-\{block.language()}");
		return pre.id(block.id()).classes(block.classes()).children(
				code.classes(codeClasses).text(block.text()).children(block.children()));
	}

	private record SideData(List<Link> links) {

		public SideData() {
			this(new ArrayList<>());
		}

	}

}
