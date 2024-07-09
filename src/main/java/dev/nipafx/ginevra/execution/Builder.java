package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.FileSystem.TemplatedFile;
import dev.nipafx.ginevra.execution.NodeOutline.Node;
import dev.nipafx.ginevra.execution.NodeOutline.Node.FilterNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateResourcesNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateTemplateNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.MergeNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.SourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreDocumentNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreResourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.TransformNode;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.render.Renderer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.StreamUtils.crossProduct;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

class Builder {

	private final NodeOutline outline;
	private final Store store;
	private final Renderer renderer;
	private final FileSystem fileSystem;
	private final Map<MergeNode, MergeCache> mergeCaches;

	Builder(NodeOutline outline, Store store, Renderer renderer, FileSystem fileSystem) {
		this.outline = outline;
		this.store = store;
		this.renderer = renderer;
		this.fileSystem = fileSystem;
		this.mergeCaches = outline
				.streamNodes(MergeNode.class)
				.collect(toUnmodifiableMap(identity(), MergeCache::new));
	}

	public void build() {
		runUntilStorage();

		fileSystem.initialize();
		renderTemplates();
		generateResources();
	}

	private void runUntilStorage() {
		outline
				.streamNodes(SourceNode.class)
				.forEach(this::runFromSource);
	}

	private void runFromSource(SourceNode sourceNode) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var envelopes = (List<Envelope<?>>) (List) sourceNode.source().loadAll();
		processRecursively(sourceNode, envelopes);
	}

	private void processRecursively(Node previous, List<Envelope<?>> envelopes) {
		var children = outline.getChildrenOf(previous);
		if (children == null)
			throw new IllegalStateException("Unknown step triggered document processing");
		if (envelopes.isEmpty())
			return;

		children.forEach(next -> {
			switch (next) {
				case SourceNode _ -> throw new IllegalStateException("No step should map to a source");
				case FilterNode nextFilter -> {
					var filteredEnvelopes = envelopes.stream()
							.filter(envelope -> nextFilter.filter().test(envelope.document()))
							.toList();
					processRecursively(nextFilter, filteredEnvelopes);
				}
				case TransformNode nextTransform -> {
					var transformedEnvelopes = envelopes.stream()
							.flatMap(envelope -> {
								var id = envelope.id().transformedBy(nextTransform.transformerName());
								return nextTransform
										.transformer()
										.apply(envelope.document()).stream()
										.<Envelope<?>> map(doc -> new SimpleEnvelope<>(id, (Record & Document) doc));
							})
							.toList();
					processRecursively(nextTransform, transformedEnvelopes);
				}
				case MergeNode nextMerge -> mergeCaches
						.get(nextMerge)
						.update(previous, envelopes)
						.merge()
						.ifPresent(mergedEnvelopes -> processRecursively(nextMerge, mergedEnvelopes));
				case StoreDocumentNode(var collection) -> collection.ifPresentOrElse(
						col -> envelopes.forEach(envelope -> store.store(col, envelope)),
						() -> envelopes.forEach(store::store)
				);
				case StoreResourceNode(var naming) -> envelopes.forEach(envelope -> {
					var name = naming.apply(envelope.document());
					@SuppressWarnings("unchecked")
					var fileDoc = (Envelope<? extends FileDocument>) envelope;
					store.storeResource(name, fileDoc);
				});
				case GenerateTemplateNode _ -> throw new IllegalStateException("No step should map to a template");
				case GenerateResourcesNode _ -> throw new IllegalStateException("No step should map to resource generation");
			}
		});
	}

	private void renderTemplates() {
		outline
				.streamNodes(GenerateTemplateNode.class)
				.flatMap(this::generateFromTemplate)
				.forEach(fileSystem::writeTemplatedFile);
	}

	private <DOCUMENT extends Record & Document> Stream<TemplatedFile> generateFromTemplate(GenerateTemplateNode templateNode) {
		@SuppressWarnings("unchecked")
		var template = (Template<DOCUMENT>) templateNode.template();
		var results = switch (template.query()) {
			case CollectionQuery<DOCUMENT> collectionQuery -> store
					.query(collectionQuery).stream()
					.filter(result -> collectionQuery.filter().test(result));
			case RootQuery<DOCUMENT> rootQuery -> Stream.of(store.query(rootQuery));
		};
		return results
				.map(document -> generateFromTemplate(template, document));
	}

	private <DOCUMENT extends Record & Document> TemplatedFile generateFromTemplate(Template<DOCUMENT> template, DOCUMENT document) {
		var composedDocument = template.compose(document);
		var fileContent = renderer.renderAsDocument(composedDocument.html(), template);
		return new TemplatedFile(composedDocument.slug(), fileContent.html(), fileContent.referencedResources());
	}

	private void generateResources() {
		outline
				.streamNodes(GenerateResourcesNode.class)
				.forEach(this::generateResources);
	}

	private void generateResources(GenerateResourcesNode node) {
		var resources = node
				.resourceNames().stream()
				.map(resourceName -> store
						.getResource(resourceName)
						.orElseThrow(() -> new IllegalArgumentException("No resource with name '%s'.".formatted(resourceName)))
						.file())
				.toList();
		fileSystem.copyStaticFiles(node.folder(), resources);
	}

	private static class MergeCache {

		private final MergeNode merge;

		// will be null until update was called
		private List<Envelope<?>> leftInput;
		private List<Envelope<?>> rightInput;

		private MergeCache(MergeNode merge) {
			this.merge = merge;
		}

		public MergeCache update(Node previous, List<Envelope<?>> envelopes) {
			if (merge.left() == previous)
				leftInput = List.copyOf(envelopes);
			else if (merge.right() == previous)
				rightInput = List.copyOf(envelopes);
			else
				throw new IllegalArgumentException("Unexpected merge parent node: " + previous);

			return this;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Optional<List<Envelope<?>>> merge() {
			if (leftInput == null || rightInput == null)
				return Optional.empty();

			List<Envelope<?>> mergedEnvelopes = crossProduct(leftInput, rightInput)
					.flatMap(pair -> {
						var mergedId = pair.left().id().mergedWith(pair.right().id());
						var merger = (Merger) merge.merger();
						var mergedDocuments = (List<Record>) merger.merge(pair.left().document(), pair.right().document());
						return mergedDocuments.stream().<Envelope<?>>map(mergedDoc -> new SimpleEnvelope(mergedId, mergedDoc));
					})
					.toList();

			return Optional.of(mergedEnvelopes);
		}

	}

}
