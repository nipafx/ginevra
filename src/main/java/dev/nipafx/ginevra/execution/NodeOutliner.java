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
import dev.nipafx.ginevra.outline.BinaryFileDocument;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.FileStep;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Step;
import dev.nipafx.ginevra.outline.StringDocument;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.outline.TextFileDocument;
import dev.nipafx.ginevra.outline.TextFileStep;
import dev.nipafx.ginevra.parse.MarkdownParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NodeOutliner implements Outliner {

	private final Optional<MarkdownParser> markdownParser;
	private final Map<Node, List<Node>> nodes;

	public NodeOutliner(Optional<MarkdownParser> markdownParser) {
		this.markdownParser = markdownParser;
		this.nodes = new HashMap<>();
	}

	// sources

	@Override
	public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> source(Source<DOCUMENT_OUT> source) {
		var node = createNewNode(() -> new SourceNode(source));
		return new NodeStep<>(this, node);
	}

	@Override
	public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> source(DOCUMENT_OUT source) {
		var node = createNewNode(() -> new SourceNode(new RecordSource<>(source)));
		return new NodeStep<>(this, node);
	}

	@Override
	public TextFileStep<TextFileDocument> sourceTextFiles(String name, Path path) {
		var node = createNewNode(() -> new SourceNode(FileSource.forTextFiles(name, path)));
		return new TextFileNodeStep<>(this, node);
	}

	@Override
	public FileStep<BinaryFileDocument> sourceBinaryFiles(String name, Path path) {
		var node = createNewNode(() -> new SourceNode(FileSource.forBinaryFiles(name, path)));
		return new FileNodeStep<>(this, node);
	}

	// transformers

	@Override
	public <DOCUMENT extends Record & Document> Step<DOCUMENT> filter(Step<DOCUMENT> previous, Predicate<DOCUMENT> filter) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var node = appendNewNode(previous, () -> new FilterNode((Predicate) filter));
		return new NodeStep<>(this, node);
	}

	@Override
	public <DOCUMENT_IN extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transform(Step<DOCUMENT_IN> previous, String transformerName, Function<DOCUMENT_IN, DOCUMENT_OUT> transformer) {
		return transformToMany(previous, transformerName, doc -> List.of(transformer.apply(doc)));
	}

	@Override
	public <DOCUMENT_IN extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformToMany(Step<DOCUMENT_IN> previous, String transformerName, Function<DOCUMENT_IN, List<DOCUMENT_OUT>> transformer) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var node = appendNewNode(previous, () -> new TransformNode(transformerName, (Function) transformer));
		return new NodeStep<>(this, node);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformMarkdown(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> frontMatterType) {
		if (markdownParser.isEmpty())
			throw new IllegalStateException("Can't transform Markdown: No Markdown parser was created");

		var markupTransformer = new MarkupParsingTransformer<DOCUMENT_IN, DOCUMENT_OUT>(markdownParser.get(), frontMatterType);
		return transform(previous, "markdown", markupTransformer);
	}

	@Override
	public <DOCUMENT_IN_1 extends Record & Document, DOCUMENT_IN_2 extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> merge(Step<DOCUMENT_IN_1> left, Step<DOCUMENT_IN_2> right, Merger<DOCUMENT_IN_1, DOCUMENT_IN_2, DOCUMENT_OUT> merger) {
		var leftNode = getNodeFromStep(left);
		var rightNode = getNodeFromStep(right);
		var mergeNode = new MergeNode(leftNode, rightNode, merger);
		appendNode(leftNode, mergeNode);
		appendNode(rightNode, mergeNode);
		return new NodeStep<>(this, mergeNode);
	}

	// store

	@Override
	public <DOCUMENT_IN extends Record & Document> void store(Step<DOCUMENT_IN> previous, String collection) {
		appendNewNode(previous, () -> new StoreDocumentNode(Optional.of(collection)));
	}

	@Override
	public <DOCUMENT_IN extends Record & Document> void store(Step<DOCUMENT_IN> previous) {
		appendNewNode(previous, () -> new StoreDocumentNode(Optional.empty()));
	}

	@Override
	public <DOCUMENT_IN extends Record & FileDocument> void storeResource(Step<DOCUMENT_IN> previous, Function<DOCUMENT_IN, String> naming) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var _ = appendNewNode(previous, () -> new StoreResourceNode((Function) naming));
	}

	@Override
	public <DOCUMENT_IN extends Record & FileDocument> void storeResource(Step<DOCUMENT_IN> previous) {
		storeResource(previous, fileDoc -> fileDoc.file().getFileName().toString());
	}

	// build

	@Override
	public Outline build() {
		return new NodeOutline(nodes);
	}

	// generate

	@Override
	public <DOCUMENT extends Record & Document>
	void generate(Template<DOCUMENT> template) {
		createNewNode(() -> new GenerateTemplateNode(template));
	}

	@Override
	public void generateStaticResources(Path targetFolder, String... resources) {
		createNewNode(() -> new GenerateResourcesNode(targetFolder, List.of(resources)));
	}

	// misc

	private Node createNewNode(Supplier<Node> nodeFactory) {
		var node = nodeFactory.get();
		var previous = nodes.put(node, new ArrayList<>());
		if (previous != null)
			throw new IllegalArgumentException("This node was already registered");
		return node;
	}

	private Node appendNewNode(Step<?> previous, Supplier<Node> nodeFactory) {
		var nextNode = createNewNode(nodeFactory);
		var previousNode = getNodeFromStep(previous);
		appendNode(previousNode, nextNode);
		return nextNode;
	}

	private static Node getNodeFromStep(Step<?> previous) {
		return switch(previous) {
			case NodeStep<?> previousNodeStep -> previousNodeStep.node();
			default -> {
				var message = "Unexpected implementation of `%s`: `%s`".formatted(Step.class.getSimpleName(), previous.getClass().getSimpleName());
				throw new IllegalStateException(message);
			}
		};
	}

	private void appendNode(Node previous, Node next) {
		nodes.get(previous).add(next);
	}

	private static class NodeStep<DOCUMENT extends Record & Document> implements Step<DOCUMENT> {

		protected final NodeOutliner outliner;
		private final Node node;

		private NodeStep(NodeOutliner outliner, Node node) {
			this.outliner = outliner;
			this.node = node;
		}

		@Override
		public Step<DOCUMENT> filter(Predicate<DOCUMENT> filter) {
			return outliner.filter(this, filter);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transform(String transformerName, Function<DOCUMENT, DOCUMENT_OUT> transformer) {
			return outliner.transform(this, transformerName, transformer);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformToMany(String transformerName, Function<DOCUMENT, List<DOCUMENT_OUT>> transformer) {
			return outliner.transformToMany(this, transformerName, transformer);
		}

		@Override
		public <OTHER_DOCUMENT extends Record & Document, DOCUMENT_OUT1 extends Record & Document> Step<DOCUMENT_OUT1> merge(Step<OTHER_DOCUMENT> other, Merger<DOCUMENT, OTHER_DOCUMENT, DOCUMENT_OUT1> merger) {
			return outliner.merge(this, other, merger);
		}

		@Override
		public void store(String collection) {
			outliner.store(this, collection);
		}

		@Override
		public void store() {
			outliner.store(this);
		}

		public Node node() {
			return node;
		}

	}

	private static class FileNodeStep<DOCUMENT extends Record & FileDocument> extends NodeStep<DOCUMENT> implements FileStep<DOCUMENT> {

		private FileNodeStep(NodeOutliner outliner, Node node) {
			super(outliner, node);
		}

		@Override
		public void storeResource(Function<DOCUMENT, String> naming) {
			outliner.storeResource(this, naming);
		}

	}

	private static class TextFileNodeStep<DOCUMENT extends Record & FileDocument & StringDocument> extends FileNodeStep<DOCUMENT> implements TextFileStep<DOCUMENT> {

		private TextFileNodeStep(NodeOutliner outliner, Node node) {
			super(outliner, node);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformMarkdown(Class<DOCUMENT_OUT> frontMatterType) {
			return outliner.transformMarkdown(this, frontMatterType);
		}

		@Override
		public void storeResource(Function<DOCUMENT, String> naming) {
			outliner.storeResource(this, naming);
		}

	}

}
