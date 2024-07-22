package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateTemplateNode;
import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.html.JmlElement;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Query;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.render.Renderer;
import dev.nipafx.ginevra.render.ResourceFile;
import dev.nipafx.ginevra.render.ResourceFile.CopiedFile;
import dev.nipafx.ginevra.render.ResourceFile.CssFile;
import dev.nipafx.ginevra.util.CollectionUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.CollectionUtils.add;
import static java.util.stream.Collectors.toUnmodifiableMap;

class LiveTemplating {

	private final List<TemplateCache> cache;
	private final LiveStore store;
	private final Renderer renderer;

	private LiveTemplating(List<TemplateCache> cache, LiveStore store, Renderer renderer) {
		this.store = store;
		this.renderer = renderer;
		this.cache = cache;
	}

	static LiveTemplating initializeTemplates(NodeOutline outline, LiveStore store, Renderer renderer) {
		var cache = outline
				.streamNodes(GenerateTemplateNode.class)
				.map(GenerateTemplateNode::template)
				.map(template -> TemplateCache.createFor(template, store, renderer))
				.toList();
		return new LiveTemplating(cache, store, renderer);
	}

	void queryDataChanged() {
		cache.forEach(TemplateCache::reset);
	}

	byte[] serve(Path slug) {
		// try the most likely candidates first
		var content = cache.stream()
				.filter(templateCache -> templateCache.mayServe(slug))
				.flatMap(templateCache -> templateCache.tryToServe(slug, store, renderer).stream())
				.findFirst();
		if (content.isPresent())
			return content.get();

		return cache.stream()
				.flatMap(templateCache -> templateCache.tryToServe(slug, store, renderer).stream())
				.findFirst()
				// TODO: better handling of missing content
				.orElse("404".getBytes());
	}

	private static class TemplateCache {

		private final Template<?> template;
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

		boolean mayServe(Path slug) {
			return switch (state) {
				case Reset(var paths) -> paths.contains(slug);
				case Templated(var content) -> content.containsKey(slug);
			};
		}

		Optional<byte[]> tryToServe(Path slug, StoreFront store, Renderer renderer) {
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
					.map(template::compose)
					.flatMap(htmlPage -> {
						var htmlDoc = renderer.resolveToDocument(htmlPage.html(), template);
						// The `AtomicReference` is not used for thread safety but as a simple mutable container.
						// The page path as well as all its resource paths (see below) will map to the content.
						// These mappings are easiest to update if they map to a mutable container that can update
						//  internally. To ensure that works, all mappings must map to the same `AtomicReference`.
						var content = new AtomicReference<Content>(
								new Resolved(htmlDoc.document(), htmlPage.slug(), htmlDoc.referencedResources()));

						var pagePathToDoc = Stream.of(Map.entry(htmlPage.slug(), content));
						var resourcePathsToDoc = htmlDoc
								.referencedResources().stream()
								// extract the paths under which these resources will be served
								.map(resourceFile -> switch (resourceFile) {
									case CopiedFile(_, var target) -> target;
									case CssFile(var file, _) -> file;
								})
								.map(path -> Map.entry(path, content));
						return Stream.concat(pagePathToDoc, resourcePathsToDoc);
					})
					.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
			return new Templated(resolved);
		}

		private byte[] renderContent(Path slug, Renderer renderer) {
			if (!(state instanceof Templated(var templated)))
				throw new IllegalStateException("Rendering can only be called in a templated state");

			if (templated.get(slug).get() instanceof Resolved resolved)
				templated.get(slug).set(render(resolved, renderer));
			if (templated.get(slug).get() instanceof Rendered(var rendered))
				return rendered.get(slug);

			throw new IllegalStateException("Rendering can only be called when it is confirmed that the path belongs to the correct document");
		}

		private Rendered render(Resolved resolved, Renderer renderer) {
			var injectedDocument = injectSseRequest(resolved.document());
			var page = renderer.renderAsHtml(injectedDocument, resolved.referencedResources());

			var pageEntry = Stream.of(Map.entry(resolved.documentSlug, page.html().getBytes()));
			var resourceEntries = page
					.referencedResources().stream()
					.map(resourceFile -> switch (resourceFile) {
						case CopiedFile(var source, var target) -> {
							try {
								yield Map.entry(target, Files.readAllBytes(source));
							} catch (IOException ex) {
								// TODO: handle error
								throw new UncheckedIOException(ex);
							}
						}
						case CssFile(var file, var content) -> Map.entry(file, content.getBytes());
					});
			var rendered = Stream
					.concat(pageEntry, resourceEntries)
					.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
			return new Rendered(rendered);
		}

		private static HtmlDocument injectSseRequest(HtmlDocument document) {
			var headChildren = add(
					document.head().children(),
					JmlElement.html.literal("<script>%s</script>".formatted(LiveServer.REFRESH_JS_CODE)));
			return document.head(document.head().children(headChildren));
		}

	}

	private sealed interface TemplateState { }
	private record Reset(Set<Path> paths) implements TemplateState { }
	private record Templated(Map<Path, AtomicReference<Content>> templated) implements TemplateState { }

	private sealed interface Content { }
	private record Resolved(HtmlDocument document, Path documentSlug, Set<ResourceFile> referencedResources) implements Content { }
	private record Rendered(Map<Path, byte[]> rendered) implements Content { }

}
