package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateTemplateNode;
import dev.nipafx.ginevra.html.GmlElement;
import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.HtmlPage;
import dev.nipafx.ginevra.outline.Query;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.render.Renderer;
import dev.nipafx.ginevra.render.ResourceFile;
import dev.nipafx.ginevra.render.ResourceFile.CopiedFile;
import dev.nipafx.ginevra.render.ResourceFile.CssFile;
import dev.nipafx.ginevra.util.FileSystemUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.CollectionUtils.add;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toMap;

class LiveTemplating {

	private final Map<String, TemplateCache> cache;
	private final LiveStore store;
	private final Renderer renderer;

	private LiveTemplating(Map<String, TemplateCache> cache, LiveStore store, Renderer renderer) {
		this.store = store;
		this.renderer = renderer;
		this.cache = cache;
	}

	static LiveTemplating initializeTemplates(NodeOutline outline, LiveStore store, Renderer renderer) {
		var cache = outline
				.nodes(GenerateTemplateNode.class)
				.map(node -> Map.entry(
						node.id(),
						TemplateCache.createFor(node.template(), store, renderer))
				)
				.collect(toMap(Entry::getKey, Entry::getValue));
		return new LiveTemplating(cache, store, renderer);
	}

	void queryDataChanged() {
		cache.values().forEach(TemplateCache::reset);
	}

	void updateToNewClassLoader(NodeOutline outline) {
		outline
				.nodes(GenerateTemplateNode.class)
				// replace the cache for that node with a fresh one (~> content will have to be rendered again)
				.forEach(node -> cache.put(node.id(), TemplateCache.createFor(node.template(), store, renderer)));
	}

	public void updateToNewClassLoaderWithChangedTemplates(NodeOutline outline, List<Class<? extends Template<?>>> changedTemplates) {
		outline
				.nodes(GenerateTemplateNode.class)
				.forEach(node -> {
					var templateChanged = changedTemplates.stream().anyMatch(type -> type.isAssignableFrom(node.template().getClass()));
					if (templateChanged)
						// replace the cache for that node with a fresh one (~> content will have to be rendered again)
						cache.put(node.id(), TemplateCache.createFor(node.template(), store, renderer));
					else
						// update the cache to the new but unchanged template instance (~> rendered content remains valid)
						cache.get(node.id()).updateTemplate(node.template());
				});
	}

	byte[] serve(Path slug) {
		return cache
				.values().stream()
				.map(template -> template.readinessToServe(slug))
				.filter(template -> template.readiness() != Readiness.NEVER)
				.max(comparing(TemplateReadiness::readiness))
				.flatMap(template -> template.cache().tryToServe(slug, store, renderer))
				// TODO: better handling of missing content
				.orElse("404".getBytes());
	}

	private record TemplateReadiness(Readiness readiness, TemplateCache cache) { }
	private enum Readiness {
		NEVER, RESET, RESOLVED, RENDERED
	}

	private static class TemplateCache {

		private Template<?> template;
		private TemplateState state;

		private TemplateCache(Template<?> template, TemplateState state) {
			this.template = template;
			this.state = state;
		}

		static <DOCUMENT extends Record & Document> TemplateCache createFor(
				Template<DOCUMENT> template, StoreFront store, Renderer renderer) {
			return new TemplateCache(template, applyTemplate(template, store, renderer));
		}

		void reset() {
			state = switch (state) {
				case Reset reset -> reset;
				case Templated(var content) -> new Reset(Set.copyOf(content.keySet()));
			};
		}

		TemplateReadiness readinessToServe(Path slug) {
			var readiness =  switch (state) {
				case Reset(var paths) -> paths.contains(slug) ? Readiness.RESET : Readiness.NEVER;
				case Templated(var content) -> switch (content.get(slug)) {
					case null -> Readiness.NEVER;
					case ResolvedDoc _, ResolvedFile _ -> Readiness.RESOLVED;
					case Rendered _ -> Readiness.RENDERED;
				};
			};
			return new TemplateReadiness(readiness, this);
		}

		// this method changes internal state but may be called from multiple threads
		// (if there are multiple clients observing the site) ~> synchronize to prevent races
		synchronized Optional<byte[]> tryToServe(Path slug, StoreFront store, Renderer renderer) {
			if (state instanceof Reset)
				state = applyTemplate(template, store, renderer);
			if (state instanceof Templated(var content) && content.containsKey(slug))
				return Optional.of(renderContent(slug, renderer));

			return Optional.empty();
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private static <DOCUMENT extends Record & Document> TemplateState applyTemplate(Template<DOCUMENT> template, StoreFront store, Renderer renderer) {
			Stream<DOCUMENT> queryResults = switch (template.query()) {
				case Query.CollectionQuery collectionQuery -> store
						.<DOCUMENT> query(collectionQuery).stream()
						.filter(result -> collectionQuery.filter().test(result));
				case Query.RootQuery rootQuery -> Stream
						.of((DOCUMENT) store.query(rootQuery));
			};

			var resolved = queryResults
					.flatMap(template::composeMany)
					.flatMap(htmlPage -> createContent(htmlPage, template, renderer))
					.collect(toConcurrentMap(Entry::getKey, Entry::getValue, (content, _) -> content));
			return new Templated(resolved);
		}

		private static <DOCUMENT extends Record & Document> Stream<Entry<Path, Content>> createContent(
				HtmlPage htmlPage, Template<DOCUMENT> template, Renderer renderer) {
			var htmlDoc = renderer.resolveToDocument(htmlPage.html(), template);
			return Stream.concat(
					Stream.of(Map.entry(
							htmlPage.slug(),
							new ResolvedDoc(htmlDoc.document(), htmlPage.slug(), htmlDoc.referencedResources()))),
					htmlDoc
							.referencedResources().stream()
							.map(resourceFile -> switch (resourceFile) {
								case CopiedFile(var source, var target) -> Map.entry(target, new ResolvedFile(source));
								case CssFile(var file, var css) -> Map.entry(file, new Rendered(css.getBytes()));
							}));
		}

		private byte[] renderContent(Path slug, Renderer renderer) {
			if (!(state instanceof Templated(var templated)))
				throw new IllegalStateException("Rendering can only be called in a templated state");

			templated.computeIfPresent(slug, (_, content) -> switch (content) {
				case ResolvedDoc(var doc, _, var resources) -> renderDocument(renderer, doc, resources);
				case ResolvedFile(var file) -> new Rendered(FileSystemUtils.readAllBytes(file));
				case Rendered rd -> rd;
			});

			return switch (templated.get(slug)) {
				case Rendered(var bytes) -> bytes;
				case ResolvedDoc _, ResolvedFile _ -> throw new IllegalStateException("Content should've just been rendered");
				case null -> throw new IllegalStateException("Rendering can only be called when it is confirmed that the path belongs to the correct document");
			};
		}

		private Rendered renderDocument(Renderer renderer, HtmlDocument document, Set<ResourceFile> referencedResources) {
			var injectedDocument = injectSseRequest(document);
			var page = renderer.renderAsHtml(injectedDocument, referencedResources);
			return new Rendered(page.html().getBytes());
		}

		private static HtmlDocument injectSseRequest(HtmlDocument document) {
			var headChildren = add(
					document.head().children(),
					GmlElement.html.literal("<script>%s</script>".formatted(LiveServer.REFRESH_JS_CODE)));
			return document.head(document.head().children(headChildren));
		}

		public void updateTemplate(Template<?> template) {
			this.template = template;
		}

	}

	private sealed interface TemplateState { }
	private record Reset(Set<Path> paths) implements TemplateState { }
	private record Templated(ConcurrentMap<Path, Content> templated) implements TemplateState { }

	private sealed interface Content { }
	private record ResolvedDoc(HtmlDocument document, Path documentSlug, Set<ResourceFile> referencedResources) implements Content { }
	private record ResolvedFile(Path file) implements Content { }
	private record Rendered(byte[] bytes) implements Content { }

}
