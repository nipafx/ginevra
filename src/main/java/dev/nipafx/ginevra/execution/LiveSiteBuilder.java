package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.LiveCodeUpdate.Changes;
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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableMap;

class LiveSiteBuilder {

	private final LiveStore store;
	private final Renderer renderer;
	private final LiveServer server;
	private final boolean pureTemplates;
	private final MultiplexingQueue<SourcedEvent> sourceEvents;

	private Optional<BuildState> buildState;

	LiveSiteBuilder(LiveStore store, Renderer renderer, LiveServer server, boolean pureTemplates) {
		this.store = store;
		this.renderer = renderer;
		this.server = server;
		this.pureTemplates = pureTemplates;
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
		var liveGraph = createLiveGraphAndFillStore(outline);
		var templating = LiveTemplating.initializeTemplates(outline, store, renderer);
		var staticResources = createResourceMap(outline);
		return new BuildState(liveGraph, templating, staticResources);
	}

	private LiveGraph createLiveGraphAndFillStore(NodeOutline outline) {
		var graph = LiveGraph.buildToStore(outline);
		graph
				.nodes(SourceLiveNode.class)
				.forEach(sourceNode -> processEnvelopesRecursively(graph, Optional.empty(), sourceNode, List.of()));
		return graph;
	}

	private void processEnvelopesRecursively(
			LiveGraph graph, Optional<LiveNode> parent, LiveNode node, List<Envelope<?>> envelopes) {
		List<Envelope<?>> nextDocuments = switch (node) {
			case SourceLiveNode source -> source
					.loadAllAndObserve(event -> sourceEvents.add(new SourcedEvent(source, event)));
			case FilterLiveNode filter -> envelopes.stream()
					.<Envelope<?>> map(filter::filter)
					.filter(envelope -> !envelope.documents().isEmpty())
					.toList();
			case TransformLiveNode transform -> envelopes.stream()
					.<Envelope<?>> map(transform::transform)
					.filter(envelope -> !envelope.documents().isEmpty())
					.toList();
			case MergeLiveNode merge -> merge
					.setInput(parent.orElseThrow(IllegalStateException::new), envelopes)
					.merge()
					.orElse(List.of());
			case StoreDocumentLiveNode(var collection) -> {
				envelopes.forEach(envelope -> store.storeEnvelope(collection, envelope));
				yield List.of();
			}
			case StoreResourceLiveNode resource -> {
				envelopes.forEach(envelope -> store.storeResource(resource.naming(), envelope));
				yield List.of();
			}
		};

		if (!nextDocuments.isEmpty())
			graph
					.getChildrenOf(node)
					.forEach(nextNode -> processEnvelopesRecursively(graph, Optional.of(node), nextNode, nextDocuments));
	}

	private Map<Path, String> createResourceMap(NodeOutline outline) {
		return outline
				.nodes(GenerateResourcesNode.class)
				.flatMap(node -> node
						.resourceNames().stream()
						.map(resourceName -> {
							if (store.getResource(resourceName).isEmpty())
								throw new IllegalArgumentException("No resource with name '%s'.".formatted(resourceName));
							return Map.entry(node.targetFolder().resolve(resourceName), resourceName);
						}))
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
	}

	public void rebuild(NodeOutline outline, Changes changes) {
		var state = buildState.orElseThrow(() -> new IllegalStateException("Can't rebuild before a build"));

		switch (determineRebuild(changes)) {
			case NOTHING -> System.out.println("REBUILD NOTHING");
			case COMPONENTS -> rebuildComponents(state, changes, outline);
			case TEMPLATES -> rebuildTemplates(state, false, changes, outline);
			case DOCUMENTS -> {
				// This one is a bit tricky. If only document types that are used for queries (as opposed to
				// those used in intermediate steps of the outline) change, we only need to rerun templating;
				// otherwise we need a full rebuild. The store knows which case we're in.
				var onlyQueriesChanged = !store.updateToNewTypes(changes.documents());
				if (onlyQueriesChanged)
					rebuildTemplates(state, true, changes, outline);
				else
					rebuildAll(state, outline);
			}
			case FULL -> rebuildAll(state, outline);
		}

		server.refresh();
	}

	private void rebuildComponents(BuildState state, Changes changes, NodeOutline outline) {
		System.out.println("REBUILD COMPONENTS");

		state.graph().updateToNewClassLoader(outline);
		store.updateToNewTypes(changes.documents());
		state.templating().updateToNewClassLoader(outline);
	}

	private void rebuildTemplates(BuildState state, boolean triggeredByQueryChange, Changes changes, NodeOutline outline) {
		var messageDetail = changes.templates().isEmpty()
				? ""
				: changes.templates().stream()
						.map(Class::getSimpleName)
						.collect(joining(", ", " (", ")"));
		System.out.println("REBUILD TEMPLATES" + messageDetail);

		state.graph().updateToNewClassLoader(outline);
		if (triggeredByQueryChange)
			state.templating().queryDataChanged();
		else
			store.updateToNewTypes(changes.documents());
		state.templating().updateToNewClassLoaderWithChangedTemplates(outline, changes.templates());
	}

	private void rebuildAll(BuildState state, NodeOutline outline) {
		System.out.println("REBUILD ALL");

		state.stopObservation();
		store.removeAllData();
		var buildState = buildSite(outline);
		this.buildState = Optional.of(buildState);
	}

	private Rebuild determineRebuild(Changes changes) {
		// A full rebuild is unnecessary when only HTML templating changes but it's difficult to precisely
		// determine whether that is the case. In order to err on the side of too many full rebuilds, one is
		// triggered unless very narrow requirements are met:
		//
		//  (a) the user guarantees that implementations of `Template`, `CustomElement`, and `CssStyle`
		//      as well as of `Document` that are involved in querying for templating are "pure"
		//      (in the sense that they're not involved in anything but templating) and
		//  (b) only classes that implement those interfaces were modified
		if (!pureTemplates)
			return Rebuild.FULL;

		if (!changes.others().isEmpty())
			return Rebuild.FULL;
		if (!changes.documents().isEmpty())
			return Rebuild.DOCUMENTS;
		if (!changes.templates().isEmpty())
			return Rebuild.TEMPLATES;
		if (!changes.components().isEmpty() || !changes.cssStyles().isEmpty())
			return Rebuild.COMPONENTS;

		return Rebuild.NOTHING;
	}

	// observe

	private void handleSourceEvent(SourcedEvent event) {
		processEventsRecursively(Optional.empty(), event.sourceNode(), List.of(event.event()));
		buildState
				.orElseThrow(IllegalStateException::new)
				.templating()
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
			case StoreResourceLiveNode storeResource -> {
				events.forEach(event -> store.updateResource(storeResource.naming(), event));
				yield List.of();
			}
		};

		if (!events.isEmpty())
			buildState
					.orElseThrow(IllegalStateException::new)
					.graph()
					.getChildrenOf(node)
					.forEach(nextNode -> processEventsRecursively(Optional.of(node), nextNode, nextEvents));
	}

	// serve

	private byte[] serve(Path path) {
		var state = buildState.orElseThrow(IllegalStateException::new);

		// slugs have no leading slash but paths do, so remove it
		var slug = Path.of("/").relativize(path);
		if (state.staticResources().containsKey(slug))
			return serveStaticResource(state.staticResources().get(slug));
		else
			return state.templating().serve(slug);
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

	private record BuildState(LiveGraph graph, LiveTemplating templating, Map<Path, String> staticResources) {

		void stopObservation() {
			graph.nodes(SourceLiveNode.class).forEach(SourceLiveNode::stopObservation);
		}

	}

	private enum Rebuild {
		NOTHING, COMPONENTS, TEMPLATES, DOCUMENTS, FULL
	}

	private record SourcedEvent(SourceLiveNode sourceNode, SourceEvent event) { }

}
