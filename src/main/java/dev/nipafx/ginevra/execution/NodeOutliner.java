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
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.FileStep;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Step;
import dev.nipafx.ginevra.outline.StringDocument;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.outline.TextFileDocument;
import dev.nipafx.ginevra.outline.TextFileStep;
import dev.nipafx.ginevra.parse.JsonParser;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.YamlParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NodeOutliner implements Outliner {

	private final Optional<MarkdownParser> markdownParser;
	private final Optional<JsonParser> jsonParser;
	private final Optional<YamlParser> yamlParser;
	private final Map<Node, List<Node>> nodes;

	private final AtomicInteger nextNodeId = new AtomicInteger();

	public NodeOutliner(Optional<MarkdownParser> markdownParser, Optional<JsonParser> jsonParser, Optional<YamlParser> yamlParser) {
		this.markdownParser = markdownParser;
		this.jsonParser = jsonParser;
		this.yamlParser = yamlParser;
		this.nodes = new HashMap<>();
	}

	private String nextId() {
		return String.valueOf(nextNodeId.getAndIncrement());
	}

	// sources

	@Override
	public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> source(Source<DOCUMENT_OUT> source) {
		var node = createNewNode(() -> new SourceNode(nextId(), source));
		return new NodeStep<>(this, node);
	}

	@Override
	public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> source(DOCUMENT_OUT source) {
		var node = createNewNode(() -> new SourceNode(nextId(), new RecordSource<>(source)));
		return new NodeStep<>(this, node);
	}

	@Override
	public TextFileStep<TextFileDocument> sourceTextFiles(String name, Path path) {
		var node = createNewNode(() -> new SourceNode(nextId(), FileSource.forTextFiles(name, path)));
		return new TextFileNodeStep<>(this, node);
	}

	@Override
	public FileStep<BinaryFileDocument> sourceBinaryFiles(String name, Path path) {
		var node = createNewNode(() -> new SourceNode(nextId(), FileSource.forBinaryFiles(name, path)));
		return new FileNodeStep<>(this, node);
	}

	// transformers

	@Override
	public <DOCUMENT extends Record & Document> Step<DOCUMENT> filter(Step<DOCUMENT> previous, Predicate<DOCUMENT> filter) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var node = appendNewNode(previous, () -> new FilterNode(nextId(), (Predicate) filter));
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
		var node = appendNewNode(previous, () -> new TransformNode(nextId(), transformerName, (Function) transformer));
		return new NodeStep<>(this, node);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformMarkdown(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> frontMatterType) {
		if (markdownParser.isEmpty())
			throw new IllegalStateException("Can't transform Markdown: No Markdown parser was created");

		var markupTransformer = new MarkupTransformer<DOCUMENT_IN, DOCUMENT_OUT>(markdownParser.get(), frontMatterType);
		return transform(previous, markdownParser.get().name(), markupTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformJsonValue(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> jsonType) {
		if (jsonParser.isEmpty())
			throw new IllegalStateException("Can't transform JSON: No JSON parser was created");

		var jsonTransformer = new DataFormatValueTransformer<DOCUMENT_IN, DOCUMENT_OUT>(jsonParser.get(), jsonType);
		return transform(previous, jsonParser.get().name(), jsonTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformJsonList(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> jsonType) {
		if (jsonParser.isEmpty())
			throw new IllegalStateException("Can't transform JSON: No JSON parser was created");

		var jsonTransformer = new DataFormatListTransformer<DOCUMENT_IN, DOCUMENT_OUT>(jsonParser.get(), jsonType);
		return transformToMany(previous, jsonParser.get().name(), jsonTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformJsonMap(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> jsonType) {
		if (jsonParser.isEmpty())
			throw new IllegalStateException("Can't transform JSON: No JSON parser was created");

		var jsonTransformer = new DataFormatMapTransformer<DOCUMENT_IN, DOCUMENT_OUT, DOCUMENT_OUT>(
				jsonParser.get(), jsonType, (_, doc) -> doc);
		return transformToMany(previous, jsonParser.get().name(), jsonTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, VALUE extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformJsonMap(Step<DOCUMENT_IN> previous, Class<VALUE> jsonType, BiFunction<String, VALUE, DOCUMENT_OUT> entryMapper) {
		if (jsonParser.isEmpty())
			throw new IllegalStateException("Can't transform JSON: No JSON parser was created");

		var jsonTransformer = new DataFormatMapTransformer<DOCUMENT_IN, VALUE, DOCUMENT_OUT>(
				jsonParser.get(), jsonType, entryMapper);
		return transformToMany(previous, jsonParser.get().name(), jsonTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlValue(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> yamlType) {
		if (yamlParser.isEmpty())
			throw new IllegalStateException("Can't transform YAML: No YAML parser was created");

		var yamlTransformer = new DataFormatValueTransformer<DOCUMENT_IN, DOCUMENT_OUT>(yamlParser.get(), yamlType);
		return transform(previous, yamlParser.get().name(), yamlTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlList(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> yamlType) {
		if (yamlParser.isEmpty())
			throw new IllegalStateException("Can't transform YAML: No YAML parser was created");

		var yamlTransformer = new DataFormatListTransformer<DOCUMENT_IN, DOCUMENT_OUT>(yamlParser.get(), yamlType);
		return transformToMany(previous, yamlParser.get().name(), yamlTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlMap(Step<DOCUMENT_IN> previous, Class<DOCUMENT_OUT> yamlType) {
		if (yamlParser.isEmpty())
			throw new IllegalStateException("Can't transform YAML: No YAML parser was created");

		var yamlTransformer = new DataFormatMapTransformer<DOCUMENT_IN, DOCUMENT_OUT, DOCUMENT_OUT>(
				yamlParser.get(), yamlType, (_, doc) -> doc);
		return transformToMany(previous, yamlParser.get().name(), yamlTransformer);
	}

	@Override
	public <DOCUMENT_IN extends Record & StringDocument, VALUE extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> transformYamlMap(Step<DOCUMENT_IN> previous, Class<VALUE> yamlType, BiFunction<String, VALUE, DOCUMENT_OUT> entryMapper) {
		if (yamlParser.isEmpty())
			throw new IllegalStateException("Can't transform YAML: No YAML parser was created");

		var yamlTransformer = new DataFormatMapTransformer<DOCUMENT_IN, VALUE, DOCUMENT_OUT>(
				yamlParser.get(), yamlType, entryMapper);
		return transformToMany(previous, yamlParser.get().name(), yamlTransformer);
	}

	@Override
	public <DOCUMENT_IN_1 extends Record & Document, DOCUMENT_IN_2 extends Record & Document, DOCUMENT_OUT extends Record & Document>
	Step<DOCUMENT_OUT> merge(Step<DOCUMENT_IN_1> left, Step<DOCUMENT_IN_2> right, Merger<DOCUMENT_IN_1, DOCUMENT_IN_2, DOCUMENT_OUT> merger) {
		var leftNode = getNodeFromStep(left);
		var rightNode = getNodeFromStep(right);
		var mergeNode = new MergeNode(nextId(), leftNode, rightNode, merger);
		appendNode(leftNode, mergeNode);
		appendNode(rightNode, mergeNode);
		return new NodeStep<>(this, mergeNode);
	}

	// store

	@Override
	public <DOCUMENT_IN extends Record & Document> void store(Step<DOCUMENT_IN> previous, String collection) {
		appendNewNode(previous, () -> new StoreDocumentNode(nextId(), Optional.of(collection)));
	}

	@Override
	public <DOCUMENT_IN extends Record & Document> void store(Step<DOCUMENT_IN> previous) {
		appendNewNode(previous, () -> new StoreDocumentNode(nextId(), Optional.empty()));
	}

	@Override
	public <DOCUMENT_IN extends Record & FileDocument> void storeResource(Step<DOCUMENT_IN> previous, Function<DOCUMENT_IN, String> naming) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var _ = appendNewNode(previous, () -> new StoreResourceNode(nextId(), (Function) naming));
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
		createNewNode(() -> new GenerateTemplateNode(nextId(), template));
	}

	@Override
	public void generateStaticResources(Path targetFolder, String... resources) {
		createNewNode(() -> new GenerateResourcesNode(nextId(), targetFolder, List.of(resources)));
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
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformJsonValue(Class<DOCUMENT_OUT> yamlType) {
			return outliner.transformJsonValue(this, yamlType);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformJsonList(Class<DOCUMENT_OUT> yamlType) {
			return outliner.transformJsonList(this, yamlType);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformJsonMap(Class<DOCUMENT_OUT> yamlType) {
			return outliner.transformJsonMap(this, yamlType);
		}

		@Override
		public <VALUE extends Record & Document, DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT>
		transformJsonMap(Class<VALUE> yamlType, BiFunction<String, VALUE, DOCUMENT_OUT> entryMapper) {
			return outliner.transformJsonMap(this, yamlType, entryMapper);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformYamlValue(Class<DOCUMENT_OUT> yamlType) {
			return outliner.transformYamlValue(this, yamlType);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformYamlList(Class<DOCUMENT_OUT> yamlType) {
			return outliner.transformYamlList(this, yamlType);
		}

		@Override
		public <DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT> transformYamlMap(Class<DOCUMENT_OUT> yamlType) {
			return outliner.transformYamlMap(this, yamlType);
		}

		@Override
		public <VALUE extends Record & Document, DOCUMENT_OUT extends Record & Document> Step<DOCUMENT_OUT>
		transformYamlMap(Class<VALUE> yamlType, BiFunction<String, VALUE, DOCUMENT_OUT> entryMapper) {
			return outliner.transformYamlMap(this, yamlType, entryMapper);
		}

		@Override
		public void storeResource(Function<DOCUMENT, String> naming) {
			outliner.storeResource(this, naming);
		}

	}

}
