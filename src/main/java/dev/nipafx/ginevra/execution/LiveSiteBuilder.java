package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.LiveNode.FilterLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.MergeLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.SourceLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.StoreDocumentLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.StoreResourceLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.TransformLiveNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.GenerateResourcesNode;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.SourceEvent;
import dev.nipafx.ginevra.render.Renderer;
import dev.nipafx.ginevra.util.MultiplexingQueue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static dev.nipafx.ginevra.util.StreamUtils.only;
import static java.util.stream.Collectors.toUnmodifiableMap;

class LiveSiteBuilder {

	private final LiveStore store;
	private final Renderer renderer;
	private final LiveServer server;
	private final MultiplexingQueue<SourcedEvent> sourceEvents;

	private Optional<BuildState> buildState;

	LiveSiteBuilder(LiveStore store, Renderer renderer, LiveServer server) {
		this.store = store;
		this.renderer = renderer;
		this.server = server;
		this.sourceEvents = new MultiplexingQueue<>(this::handleSourceEvent, "source-event-watcher");
		this.buildState = Optional.empty();
	}

	// build

	public void build(NodeOutline outline, int port) {
		if (buildState.isPresent())
			throw new IllegalStateException("Can't build after a past build - trigger a rebuild instead");
		buildState = Optional.of(buildSite(outline));

		server.launch(port, this::serve);
	}

	private BuildState buildSite(NodeOutline outline) {
		var liveNodes = createLiveNodesAndFillStore(outline);
		var templating = LiveTemplating.initializeTemplates(outline, store, renderer);
		var staticResources = createResourceMap(outline);
		return new BuildState(liveNodes, templating, staticResources);
	}

	public void rebuild(NodeOutline outline) {
		buildState
				.orElseThrow(() -> new IllegalStateException("Can't rebuild before a build"))
				.liveNodes
				.keySet().stream()
				.gather(only(SourceLiveNode.class))
				.forEach(SourceLiveNode::stopObservation);
		store.removeAll();

		buildState = Optional.of(buildSite(outline));
		server.refresh();
	}

	private Map<LiveNode, List<LiveNode>> createLiveNodesAndFillStore(NodeOutline outline) {
		var liveNodes = LiveNode.buildToStore(outline);
		liveNodes
				.keySet().stream()
				.gather(only(SourceLiveNode.class))
				.forEach(sourceNode -> processEnvelopesRecursively(liveNodes, Optional.empty(), sourceNode, List.of()));
		return liveNodes;
	}

	private void processEnvelopesRecursively(
			Map<LiveNode, List<LiveNode>> liveNodes, Optional<LiveNode> parent, LiveNode node, List<Envelope<?>> envelopes) {
		List<Envelope<?>> nextDocuments = switch (node) {
			case SourceLiveNode source -> source
					.loadAllAndObserve(event -> sourceEvents.add(new SourcedEvent(source, event)));
			case FilterLiveNode filter -> envelopes.stream()
					.<Envelope<?>>map(filter::filter)
					.toList();
			case TransformLiveNode transform -> envelopes.stream()
					.<Envelope<?>>map(transform::transform)
					.toList();
			case MergeLiveNode merge -> merge
					.setInput(parent.orElseThrow(IllegalStateException::new), envelopes)
					.merge()
					.orElse(List.of());
			case StoreDocumentLiveNode(var collection) -> {
				envelopes.forEach(envelope -> store.storeEnvelope(collection, envelope));
				yield List.of();
			}
			case StoreResourceLiveNode(var naming) -> {
				envelopes.forEach(envelope -> store.storeResource(naming, envelope));
				yield List.of();
			}
		};

		if (!nextDocuments.isEmpty())
			liveNodes
					.get(node)
					.forEach(nextNode -> processEnvelopesRecursively(liveNodes, Optional.of(node), nextNode, nextDocuments));
	}

	private Map<Path, String> createResourceMap(NodeOutline outline) {
		return outline
				.streamNodes(GenerateResourcesNode.class)
				.flatMap(node -> node
						.resourceNames().stream()
						.map(resourceName -> {
							if (store.getResource(resourceName).isEmpty())
								throw new IllegalArgumentException("No resource with name '%s'.".formatted(resourceName));
							return Map.entry(node.targetFolder().resolve(resourceName), resourceName);
						}))
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
	}

	// observe

	private void handleSourceEvent(SourcedEvent event) {
		processEventsRecursively(Optional.empty(), event.sourceNode(), List.of(event.event()));
		buildState
				.orElseThrow(IllegalStateException::new)
				.liveTemplating()
				.queryDataChanged();
		if (sourceEvents.isEmpty())
			server.refresh();
	}

	private void processEventsRecursively(Optional<LiveNode> parent, LiveNode node, List<SourceEvent> events) {
		List<SourceEvent> nextEvents = switch (node) {
			// the source node already did its job by creating the event in the first place
			case SourceLiveNode _ -> events;
			case FilterLiveNode filter -> events.stream()
					.map(filter::update)
					.flatMap(Optional::stream)
					.toList();
			case TransformLiveNode transform -> events.stream()
					.map(transform::update)
					.flatMap(Optional::stream)
					.toList();
			case MergeLiveNode merge -> events.stream()
					.map(event -> merge.update(parent.orElseThrow(IllegalStateException::new), event))
					.flatMap(List::stream)
					.toList();
			case StoreDocumentLiveNode(var collection) -> {
				events.forEach(event -> store.updateEnvelope(collection, event));
				yield List.of();
			}
			case StoreResourceLiveNode(var naming) -> {
				events.forEach(event -> store.updateResource(naming, event));
				yield List.of();
			}
		};

		if (!events.isEmpty())
			buildState
					.orElseThrow(IllegalStateException::new)
					.liveNodes()
					.get(node)
					.forEach(nextNode -> processEventsRecursively(Optional.of(node), nextNode, nextEvents));
	}

	private record SourcedEvent(SourceLiveNode sourceNode, SourceEvent event) { }

	// serve

	private byte[] serve(Path path) {
		var state = buildState.orElseThrow(IllegalStateException::new);

		// slugs have no leading slash but paths do, so remove it
		var slug = Path.of("/").relativize(path);
		if (state.staticResources().containsKey(slug))
			return serveStaticResource(state.staticResources().get(slug));
		else
			return state.liveTemplating().serve(slug);
	}

	private byte[] serveStaticResource(String resourceName) {
		FileDocument fileDocument = store
				.getResource(resourceName)
				// this should've been checked during the initial build
				.orElseThrow(IllegalStateException::new);

		try {
			return Files.readAllBytes(fileDocument.file());
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
			return new byte[0];
		}
	}

	// misc

	private record BuildState(Map<LiveNode, List<LiveNode>> liveNodes, LiveTemplating liveTemplating, Map<Path, String> staticResources) { }

}
