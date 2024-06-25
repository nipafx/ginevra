package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.execution.StoreFront;
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
import dev.nipafx.ginevra.html.Meta;
import dev.nipafx.ginevra.html.Nothing;
import dev.nipafx.ginevra.html.OrderedList;
import dev.nipafx.ginevra.html.Paragraph;
import dev.nipafx.ginevra.html.Pre;
import dev.nipafx.ginevra.html.Source;
import dev.nipafx.ginevra.html.Span;
import dev.nipafx.ginevra.html.Strong;
import dev.nipafx.ginevra.html.Text;
import dev.nipafx.ginevra.html.UnorderedList;
import dev.nipafx.ginevra.html.Video;
import dev.nipafx.ginevra.outline.CustomQueryElement;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.Template;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.html.HtmlElement.body;
import static dev.nipafx.ginevra.html.HtmlElement.code;
import static dev.nipafx.ginevra.html.HtmlElement.document;
import static dev.nipafx.ginevra.html.HtmlElement.pre;

class ElementResolver {

	private final StoreFront store;
	private final Path resourceFolder;
	private final Path cssFolder;

	public ElementResolver(StoreFront store, Path resourceFolder, Path cssFolder) {
		this.store = store;
		this.resourceFolder = resourceFolder;
		this.cssFolder = cssFolder;
	}

	public HtmlDocumentWithResources resolveToDocument(Element element, Optional<Template<?>> maybeTemplateWithResources) {
		var resources = new ResourceGatherer(store, resourceFolder, cssFolder);
		maybeTemplateWithResources.ifPresent(resources::includeStyle);
		var elementResolution = resolve(element, resources);

		var htmlDocument = (elementResolution.size() == 1 && elementResolution.getFirst() instanceof HtmlDocument htmlDoc)
				? htmlDoc.head(mergeLinksIntoHead(htmlDoc.head(), resources.cssHeadLinks().toList()))
				: document.body(body.children(elementResolution));
		return new HtmlDocumentWithResources(htmlDocument, resources.resources().collect(Collectors.toSet()));
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

	// package visible for tests
	List<KnownElement> resolve(Element element) {
		return resolve(element, new ResourceGatherer(store, resourceFolder, cssFolder));
	}

	private List<KnownElement> resolve(Element element, ResourceGatherer resources) {
		return switch (element) {
			case HtmlElement htmlElement -> {
				var resolvedElement = switch (htmlElement) {
					case Anchor el -> el.children(resolveChildren(el.children(), resources));
					case BlockQuote el -> el.children(resolveChildren(el.children(), resources));
					case Body el -> el.children(resolveChildren(el.children(), resources));
					case Code el -> el.children(resolveChildren(el.children(), resources));
					case Div el -> el.children(resolveChildren(el.children(), resources));
					case Emphasis el -> el.children(resolveChildren(el.children(), resources));
					case Head el -> el.children(resolveChildren(el.children(), resources));
					case Heading el -> el.children(resolveChildren(el.children(), resources));
					case HorizontalRule el -> el;
					case HtmlDocument doc -> {
						var head = doc.head() == null
								? null
								: doc.head().children(resolveChildren(doc.head().children(), resources));
						var body = doc.body() == null
								? null
								: doc.body().children(resolveChildren(doc.body().children(), resources));
						yield doc.head(head).body(body);
					}
					case Image el -> el.src(resources.includeResource(el.src()));
					case LineBreak el -> el;
					case Link el -> el;
					case ListItem el -> el.children(resolveChildren(el.children(), resources));
					case Meta el -> el;
					case OrderedList list -> list.children(list
							.children().stream()
							.map(listItem -> listItem.children(listItem
									.children().stream()
									.flatMap(listItemChild -> resolve(listItemChild, resources).stream())
									.toList()))
							.toList());
					case Paragraph el -> el.children(resolveChildren(el.children(), resources));
					case Pre el -> el.children(resolveChildren(el.children(), resources));
					case Source el -> el.src(resources.includeResource(el.src()));
					case Span el -> el.children(resolveChildren(el.children(), resources));
					case Strong el -> el.children(resolveChildren(el.children(), resources));
					case UnorderedList list -> list.children(list
							.children().stream()
							.map(listItem -> listItem.children(listItem
									.children().stream()
									.flatMap(listItemChild -> resolve(listItemChild, resources).stream())
									.toList()))
							.toList());
					case Video el -> el
							.src(resources.includeResource(el.src()))
							.poster(resources.includeResource(el.poster()))
							.children(resolveChildren(el.children(), resources));
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
				resources.includeStyle(customElement);
				yield composeChildren(customElement)
						// a custom element may return a new custom element, so keep resolving
						.flatMap(resolved -> resolve(resolved, resources).stream())
						.toList();
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Stream<Element> composeChildren(CustomElement customElement) {
		return switch (customElement) {
			case CustomQueryElement queryElement -> {
				var results = switch (queryElement.query()) {
					case CollectionQuery<?> collectionQuery -> store
							.query(collectionQuery).stream()
							.filter(result -> ((Predicate) collectionQuery.filter()).test(result.data()));
					case RootQuery<?> rootQuery -> Stream.of(store.query(rootQuery));
				};
				yield results
						.flatMap(result -> queryElement.compose(result.data()).stream());
			}
			case CustomElement _ -> customElement.compose().stream();
		};
	}

	private List<KnownElement> resolveChildren(List<? extends Element> children, ResourceGatherer resources) {
		return children.stream()
				.flatMap(element -> resolve(element, resources).stream())
				.toList();
	}

	// package-visible for tests
	HtmlElement express(CodeBlock block) {
		var codeClasses = block.language() == null ? Classes.none() : Classes.of(STR."language-\{block.language()}");
		return pre.id(block.id()).classes(block.classes()).children(
				code.classes(codeClasses).text(block.text()).children(block.children()));
	}

}
