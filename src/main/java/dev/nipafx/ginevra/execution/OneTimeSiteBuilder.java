package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.NodeOutline.Node;
import dev.nipafx.ginevra.execution.NodeOutline.Node.FilterNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateResourcesNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateTemplateNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.MergeNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.SourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreDocumentNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreResourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.TransformNode;
import dev.nipafx.ginevra.execution.SiteFileSystem.TemplatedFile;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.render.Renderer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.StreamUtils.crossProduct;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

class OneTimeSiteBuilder {

	private final OneTimeStore store;
	private final Renderer renderer;
	private final SiteFileSystem siteFileSystem;

	private NodeOutline outline;
	private Map<MergeNode, MergeCache> mergeCaches;

	OneTimeSiteBuilder(OneTimeStore store, Renderer renderer, SiteFileSystem siteFileSystem) {
		this.store = store;
		this.renderer = renderer;
		this.siteFileSystem = siteFileSystem;
	}

	public void build(NodeOutline outline) throws InterruptedException {
		this.outline = outline;
		this.mergeCaches = outline
				.nodes(MergeNode.class)
				.collect(toUnmodifiableMap(identity(), MergeCache::new));

		fillStore();

		siteFileSystem.initialize();
		renderTemplates();
		generateResources();
		siteFileSystem.awaitCompletion();
	}

	private void fillStore() {
		outline
				.nodes(SourceNode.class)
				.forEach(this::runFromSource);
	}

	@SuppressWarnings("unchecked")
	private void runFromSource(SourceNode sourceNode) {
		sourceNode
				.source()
				.loadAll().stream()
				.map(envelope -> (List<Document>) envelope.documents())
				.forEach(documents -> processRecursively(sourceNode, documents));
	}

	private void processRecursively(Node previous, List<Document> documents) {
		if (documents.isEmpty())
			return;
		var children = outline.getChildrenOf(previous);
		if (children == null)
			throw new IllegalStateException("Unknown step triggered document processing");

		children.forEach(next -> {
			switch (next) {
				case SourceNode _ -> throw new IllegalStateException("No step should map to a source");
				case FilterNode nextFilter -> {
					var filteredDocuments = documents.stream()
							.filter(nextFilter.filter())
							.toList();
					processRecursively(nextFilter, filteredDocuments);
				}
				case TransformNode nextTransform -> {
					var transformedDocuments = documents.stream()
							.flatMap(document -> nextTransform.transformer().apply(document).stream())
							.toList();
					processRecursively(nextTransform, transformedDocuments);
				}
				case MergeNode nextMerge -> mergeCaches
						.get(nextMerge)
						.setInput(previous, documents)
						.merge()
						.ifPresent(mergedDocuments -> processRecursively(nextMerge, mergedDocuments));
				case StoreDocumentNode(_, var collection) -> documents.forEach(doc -> store.storeDocument(collection, doc));
				case StoreResourceNode(_, var naming) -> documents.forEach(doc -> store.storeResource(naming.apply(doc), (FileDocument) doc));
				case GenerateTemplateNode _ -> throw new IllegalStateException("No step should map to a template");
				case GenerateResourcesNode _ -> throw new IllegalStateException("No step should map to resource generation");
			}
		});
	}

	private void renderTemplates() throws InterruptedException {
		try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
			outline
					.nodes(GenerateTemplateNode.class)
					.forEach(templateNode ->  generateFromTemplate(templateNode, executor));
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);
		}
	}

	private <DOCUMENT extends Record & Document> void generateFromTemplate(GenerateTemplateNode templateNode, ExecutorService executor) {
		@SuppressWarnings("unchecked")
		var template = (Template<DOCUMENT>) templateNode.template();
		switch (template.query()) {
			case CollectionQuery<DOCUMENT> collectionQuery -> store
					.query(collectionQuery).stream()
					.filter(result -> collectionQuery.filter().test(result))
					.forEach(document -> executor.submit(() ->
							generateFromTemplate(template, document)
									.forEach(siteFileSystem::writeTemplatedFile)));
			case RootQuery<DOCUMENT> rootQuery -> executor.submit(() ->
					generateFromTemplate(template, store.query(rootQuery))
							.forEach(siteFileSystem::writeTemplatedFile));
		}
	}

	private <DOCUMENT extends Record & Document> Stream<TemplatedFile> generateFromTemplate(Template<DOCUMENT> template, DOCUMENT document) {
		return template
				.composeMany(document)
				.map(page -> {
					var rendered = renderer.renderAsHtml(page.html(), template);
					return new TemplatedFile(page.slug(), rendered.html(), rendered.referencedResources());
				});
	}

	private void generateResources() {
		outline
				.nodes(GenerateResourcesNode.class)
				.forEach(this::generateResources);
	}

	private void generateResources(GenerateResourcesNode node) {
		node
				.resourceNames().stream()
				.map(resourceName -> store
						.getResource(resourceName)
						.orElseThrow(() -> new IllegalArgumentException("No resource with name '%s'.".formatted(resourceName))))
				.map(FileDocument::file)
				.forEach(resource -> siteFileSystem.copyStaticFile(resource, node.targetFolder()));
	}

	private static class MergeCache {

		private final MergeNode merge;

		// will be null until update was called
		private List<Document> leftInput;
		private List<Document> rightInput;

		private MergeCache(MergeNode merge) {
			this.merge = merge;
		}

		MergeCache setInput(Node previous, List<Document> documents) {
			if (merge.leftNode() == previous)
				leftInput = List.copyOf(documents);
			else if (merge.rightNode() == previous)
				rightInput = List.copyOf(documents);
			else
				throw new IllegalArgumentException("Unexpected merge parent node: " + previous);

			return this;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Optional<List<Document>> merge() {
			if (leftInput == null || rightInput == null)
				return Optional.empty();

			var mergedDocuments = crossProduct(leftInput, rightInput)
					.<Document> flatMap(pair -> {
						var merger = (Merger) merge.merger();
						var left = (Record & Document) pair.left();
						var right = (Record & Document) pair.right();
						return merger.merge(left, right).stream();
					})
					.toList();

			// get rid of the inputs - they won't be needed again
			leftInput = null;
			rightInput = null;

			return Optional.of(mergedDocuments);
		}

	}

}
