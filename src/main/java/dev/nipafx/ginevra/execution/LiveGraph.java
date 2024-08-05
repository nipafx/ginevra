package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.LiveNode.FilterLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.MergeLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.SourceLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.StoreDocumentLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.StoreResourceLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.TransformLiveNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node;
import dev.nipafx.ginevra.execution.NodeOutline.Node.FilterNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateResourcesNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateTemplateNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.MergeNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.SourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreDocumentNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreResourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.TransformNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.StreamUtils.only;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableMap;

class LiveGraph {

	private final Map<LiveNode, List<LiveNode>> nodes;
	private final Map<String, LiveNode> nodesById;

	public LiveGraph(Map<LiveNode, List<LiveNode>> nodes, Map<String, LiveNode> nodesById) {
		this.nodes = nodes;
		this.nodesById = nodesById;
	}

	static LiveGraph buildToStore(NodeOutline outline) {
		var liveNodes = new HashMap<Node, LiveNode>();

		outline
				.nodes(SourceNode.class)
				.forEach(node -> liveNodes.put(node, new SourceLiveNode(node.source())));
		outline
				.nodes(FilterNode.class)
				.forEach(node -> liveNodes.put(node, new FilterLiveNode(node.filter())));
		outline
				.nodes(TransformNode.class)
				.forEach(node -> liveNodes.put(node, new TransformLiveNode(node.transformerName(), node.transformer())));
		// the creation of live nodes has to be done in steps, so by the time we get to merge nodes,
		// there is at least one merge node whose input nodes are non-merge nodes
		// and the respective live nodes have already been created
		var mutableMergeNodes = outline.nodes(MergeNode.class).collect(toList());
		while (!mutableMergeNodes.isEmpty()) {
			var completed = mutableMergeNodes.stream()
					.filter(node -> liveNodes.containsKey(node.leftNode()) && liveNodes.containsKey(node.rightNode()))
					.peek(node -> liveNodes.put(
							node,
							new MergeLiveNode(
									liveNodes.get(node.leftNode()), liveNodes.get(node.rightNode()), node.merger())))
					.toList();
			if (completed.isEmpty())
				throw new IllegalStateException("Huh?!");
			mutableMergeNodes.removeAll(completed);
		}
		outline
				.nodes(StoreDocumentNode.class)
				.forEach(node -> liveNodes.put(node, new StoreDocumentLiveNode(node.collection())));
		outline
				.nodes(StoreResourceNode.class)
				.forEach(node -> liveNodes.put(node, new StoreResourceLiveNode(node.naming())));

		var nodes = outline
				.nodes(
						SourceNode.class, FilterNode.class, TransformNode.class, MergeNode.class,
						StoreDocumentNode.class, StoreResourceNode.class)
				.map(node -> {
					var liveNode = liveNodes.get(node);
					var children = outline
							.getChildrenOf(node)
							.filter(liveNodes::containsKey)
							.map(liveNodes::get)
							.toList();
					return Map.entry(liveNode, children);
				})
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
		var nodesById = liveNodes
				.entrySet().stream()
				.collect(toUnmodifiableMap(entry -> entry.getKey().id(), Entry::getValue));

		return new LiveGraph(nodes, nodesById);
	}

	void updateToNewClassLoader(NodeOutline outline) {
		outline
				.allNodes()
				.forEach(node -> {
					switch (node) {
						case SourceNode source -> getNode(source.id(), SourceLiveNode.class).updateSource(source.source());
						case FilterNode filter -> getNode(filter.id(), FilterLiveNode.class).updateFilter(filter.filter());
						case TransformNode transform -> getNode(transform.id(), TransformLiveNode.class)
								.updateTransformer(transform.transformer());
						case MergeNode merge -> getNode(merge.id(), MergeLiveNode.class).updateMerger(merge.merger());
						case StoreResourceNode storeResource -> getNode(storeResource.id(), StoreResourceLiveNode.class)
								.updateNaming(storeResource.naming());
						case StoreDocumentNode _, GenerateTemplateNode _, GenerateResourcesNode _ -> { }
					}
				});
	}

	private <NODE extends LiveNode> NODE getNode(String id, Class<NODE> type) {
		return type.cast(nodesById.get(id));
	}

	@SafeVarargs
	final <NODE extends LiveNode> Stream<NODE> nodes(Class<? extends NODE>... types) {
		return nodes
				.keySet().stream()
				.gather(only(types));
	}

	Stream<LiveNode> getChildrenOf(LiveNode node) {
		return nodes.get(node).stream();
	}

}
