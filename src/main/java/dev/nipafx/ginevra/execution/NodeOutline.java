package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Template;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.StreamUtils.keepOnly;

class NodeOutline implements Outline {

	private final Map<Node, List<Node>> nodes;

	NodeOutline(Map<Node, List<Node>> nodes) {
		this.nodes = nodes;
	}

	<NODE extends Node> Stream<NODE> streamNodes(Class<NODE> type) {
		return nodes
				.keySet().stream()
				.mapMulti(keepOnly(type));
	}

	List<Node> getChildrenOf(Node node) {
		return nodes.get(node);
	}

	sealed interface Node {

		record SourceNode(Source<?> source) implements Node { }
		record FilterNode(Predicate<Document> filter) implements Node { }
		record TransformNode(String transformerName, Function<Document, List<Document>> transformer) implements Node { }
		record MergeNode(Node left, Node right, Merger<?, ?, ?> merger) implements Node { }
		record StoreDocumentNode(Optional<String> collection) implements Node { }
		record StoreResourceNode(Function<Document, String> naming) implements Node { }
		record GenerateTemplateNode(Template<?> template) implements Node { }
		record GenerateResourcesNode(Path folder, List<String> resourceNames) implements Node { }

	}

}
