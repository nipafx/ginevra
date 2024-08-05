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

import static dev.nipafx.ginevra.util.StreamUtils.only;

class NodeOutline implements Outline {

	private final Map<Node, List<Node>> nodes;

	NodeOutline(Map<Node, List<Node>> nodes) {
		this.nodes = nodes;
	}

	Stream<Node> allNodes() {
		return nodes.keySet().stream();
	}

	@SafeVarargs
	final <NODE extends Node> Stream<NODE> nodes(Class<? extends NODE>... types) {
		return nodes
				.keySet().stream()
				.gather(only(types));
	}

	Stream<Node> getChildrenOf(Node node) {
		return nodes.get(node).stream();
	}

	sealed interface Node {

		String id();
		
		record SourceNode(String id, Source<?> source) implements Node { }
		record FilterNode(String id, Predicate<Document> filter) implements Node { }
		record TransformNode(String id, String transformerName, Function<Document, List<Document>> transformer) implements Node { }
		record MergeNode(String id, Node leftNode, Node rightNode, Merger<?, ?, ?> merger) implements Node { }
		record StoreDocumentNode(String id, Optional<String> collection) implements Node { }
		record StoreResourceNode(String id, Function<Document, String> naming) implements Node { }
		record GenerateTemplateNode(String id, Template<?> template) implements Node { }
		record GenerateResourcesNode(String id, Path targetFolder, List<String> resourceNames) implements Node { }

	}

}
