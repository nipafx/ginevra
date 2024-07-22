package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.NodeOutline.Node;
import dev.nipafx.ginevra.execution.NodeOutline.Node.FilterNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.MergeNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.SourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreDocumentNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.StoreResourceNode;
import dev.nipafx.ginevra.execution.NodeOutline.Node.TransformNode;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.SenderId;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.SourceEvent;
import dev.nipafx.ginevra.outline.SourceEvent.Added;
import dev.nipafx.ginevra.outline.SourceEvent.Changed;
import dev.nipafx.ginevra.outline.SourceEvent.Removed;
import dev.nipafx.ginevra.util.StreamUtils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static dev.nipafx.ginevra.util.CollectionUtils.add;
import static dev.nipafx.ginevra.util.StreamUtils.crossProduct;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableMap;

sealed interface LiveNode {

	static Map<LiveNode, List<LiveNode>> buildToStore(NodeOutline outline) {
		var liveNodes = new HashMap<Node, LiveNode>();
		outline
				.streamNodes(SourceNode.class)
				.forEach(node -> liveNodes.put(node, new SourceLiveNode(node.source())));
		outline
				.streamNodes(FilterNode.class)
				.forEach(node -> liveNodes.put(node, new FilterLiveNode(node.filter())));
		outline
				.streamNodes(TransformNode.class)
				.forEach(node -> liveNodes.put(node, new TransformLiveNode(node.transformerName(), node.transformer())));
		// the creation of live nodes has to be done in steps, so by the time we get to merge nodes,
		// there is at least one merge node whose input nodes are non-merge nodes
		// and the respective live nodes have already been created
		var mutableMergeNodes = outline.streamNodes(MergeNode.class).collect(toList());
		while (!mutableMergeNodes.isEmpty()) {
			var completed = mutableMergeNodes.stream()
					.filter(node -> liveNodes.containsKey(node.leftNode()) && liveNodes.containsKey(node.rightNode()))
					.peek(node -> liveNodes.put(node, new MergeLiveNode(liveNodes.get(node.leftNode()), liveNodes.get(node.rightNode()), node.merger())))
					.toList();
			if (completed.isEmpty())
				throw new IllegalStateException("Huh?!");
			mutableMergeNodes.removeAll(completed);
		}
		outline
				.streamNodes(StoreDocumentNode.class)
				.forEach(node -> liveNodes.put(node, new StoreDocumentLiveNode(node.collection())));
		outline
				.streamNodes(StoreResourceNode.class)
				.forEach(node -> liveNodes.put(node, new StoreResourceLiveNode(node.naming())));

		return outline
				.streamNodes(
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
	}

	final class SourceLiveNode implements LiveNode {

		private final Source<?> source;

		SourceLiveNode(Source<?> source) {
			this.source = source;
		}

		List<Envelope<?>> loadAllAndObserve(Consumer<SourceEvent> listener) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			var envelopes = (List<Envelope<?>>) (List) source.loadAll();
			source.onChange(listener);
			return envelopes;
		}

	}

	final class FilterLiveNode implements LiveNode {

		private final Predicate<Document> filter;

		// indicates whether any documents from the sender passed the filter
		private final Map<SenderId, Boolean> ledger;

		FilterLiveNode(Predicate<Document> filter) {
			this.filter = filter;
			this.ledger = new HashMap<>();
		}

		Envelope<?> filter(Envelope<?> envelope) {
			var filtered = applyFilter(envelope);
			ledger.put(envelope.sender(), !filtered.documents().isEmpty());
			return filtered;
		}

		Optional<SourceEvent> update(SourceEvent event) {
			return switch (event) {
				case Added(var added) -> {
					var filtered = applyFilter(added);
					var anyRemain = !filtered.documents().isEmpty();
					ledger.put(added.sender(), anyRemain);
					yield anyRemain
							? Optional.of(new Added(filtered))
							: Optional.empty();
				}
				case Changed(var changed) -> {
					var filtered = applyFilter(changed);
					var anyRemain = !filtered.documents().isEmpty();
					var anyRemained = ledger.put(changed.sender(), anyRemain);
					if (anyRemained == null) {
						var message = "The changed envelope with sender ID '%s' was unknown when it shouldn't have been."
								.formatted(changed.sender());
						throw new IllegalStateException(message);
					}

					if (anyRemained && anyRemain) // ✅ ⇝ ✅
						yield Optional.of(new Changed(filtered));
					else if (anyRemained) // ✅ ⇝ ❌
						yield Optional.of(new Removed(changed.sender().filter()));
					else if (anyRemain) // ❌ ⇝ ✅
						yield Optional.of(new Added(filtered));
					else // ❌ ⇝ ❌
						yield Optional.empty();
				}
				case Removed(var removed) -> {
					// this should be a `switch(ledger.remove(removed))`, but:
					// https://bugs.openjdk.org/browse/JDK-8336781
					var anyRemained = ledger.remove(removed);
					if (anyRemained == null)
						throw new IllegalStateException("No envelope with ID '%s' was registered and thus can't be removed".formatted(removed));
					yield anyRemained
							? Optional.of(new Removed(removed.filter()))
							: Optional.empty();
				}
			};
		}

		private <DOCUMENT extends Record & Document> Envelope<DOCUMENT> applyFilter(Envelope<DOCUMENT> envelope) {
			var filteredDocuments = envelope
					.documents().stream()
					.filter(filter)
					.toList();
			return new SimpleEnvelope<>(envelope.sender().filter(), filteredDocuments);
		}

	}

	final class TransformLiveNode implements LiveNode {

		private final String transformerName;
		private final Function<Document, List<Document>> transformer;

		// indicates whether transforming the sender's documents lead to a non-empty result
		private final Map<SenderId, Boolean> ledger;

		TransformLiveNode(String transformerName, Function<Document, List<Document>> transformer) {
			this.transformerName = transformerName;
			this.transformer = transformer;
			this.ledger = new HashMap<>();
		}

		Envelope<?> transform(Envelope<?> envelope) {
			Envelope<?> transformed = applyTransformation(envelope);
			ledger.put(envelope.sender(), !transformed.documents().isEmpty());
			return transformed;
		}

		Optional<SourceEvent> update(SourceEvent event) {
			return switch (event) {
				case Added(var added) -> {
					var transformed = applyTransformation(added);
					var anyTransform = !transformed.documents().isEmpty();
					ledger.put(added.sender(), anyTransform);
					yield anyTransform
							? Optional.of(new Added(transformed))
							: Optional.empty();
				}
				case Changed(var changed) -> {
					var transformed = applyTransformation(changed);
					var anyTransform = !transformed.documents().isEmpty();
					var anyTransformed = ledger.put(changed.sender(), anyTransform);
					if (anyTransformed == null) {
						var message = "The changed envelope with sender ID '%s' was unknown when it shouldn't have been."
								.formatted(changed.sender());
						throw new IllegalStateException(message);
					}

					if (anyTransformed && anyTransform) // ✅ ⇝ ✅
						yield Optional.of(new Changed(transformed));
					else if (anyTransformed) // ✅ ⇝ ❌
						yield Optional.of(new Removed(transformed.sender()));
					else if (anyTransform) // ❌ ⇝ ✅
						yield Optional.of(new Added(transformed));
					else // ❌ ⇝ ❌
						yield Optional.empty();
				}
				case Removed(var removed) -> {
					// this should be a `switch(ledger.remove(removed))`, but:
					// https://bugs.openjdk.org/browse/JDK-8336781
					var anyTransformed = ledger.remove(removed);
					if (anyTransformed == null)
						throw new IllegalStateException("No envelope with ID '%s' was registered and thus can't be removed".formatted(removed));
					yield anyTransformed
							? Optional.of(new Removed(removed.transform(transformerName)))
							: Optional.empty();
				}
			};
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Envelope<?> applyTransformation(Envelope<?> envelope) {
			var newId = envelope.sender().transform(transformerName);
			var transformedDocuments = envelope
					.documents().stream()
					.flatMap(document -> transformer.apply(document).stream())
					.toList();
			return new SimpleEnvelope(newId, transformedDocuments);
		}

	}

	final class MergeLiveNode implements LiveNode {

		private final LiveNode leftNode;
		private final LiveNode rightNode;
		private final Merger<?, ?, ?> merger;

		// indicates whether merging the senders' documents lead to a non-empty result
		private final Map<Pair<SenderId, SenderId>, Boolean> ledger;
		private List<Envelope<?>> leftInput;
		private List<Envelope<?>> rightInput;

		MergeLiveNode(LiveNode leftNode, LiveNode rightNode, Merger<?, ?, ?> merger) {
			this.leftNode = leftNode;
			this.rightNode = rightNode;
			this.merger = merger;

			this.ledger = new HashMap<>();
		}

		MergeLiveNode setInput(LiveNode previous, List<Envelope<?>> envelopes) {
			if (leftNode == previous)
				leftInput = List.copyOf(envelopes);
			else if (rightNode == previous)
				rightInput = List.copyOf(envelopes);
			else
				throw new IllegalStateException("Unexpected merge parent node: " + previous);

			return this;
		}

		Optional<List<Envelope<?>>> merge() {
			if (leftInput == null || rightInput == null)
				return Optional.empty();

			var merged = crossProduct(leftInput, rightInput)
					.<Envelope<?>>map(pair -> {
						Envelope<?> mergedEnvelope = applyMerge(pair.left(), pair.right());
						ledger.put(
								new Pair<>(pair.left().sender(), pair.right().sender()),
								!mergedEnvelope.documents().isEmpty());
						return mergedEnvelope;
					})
					.filter(envelope -> !envelope.documents().isEmpty())
					.toList();
			return Optional.of(merged);
		}

		List<SourceEvent> update(LiveNode previous, SourceEvent event) {
			return switch (event) {
				case Added(var added) -> {
					if (leftNode == previous) {
						leftInput = add(leftInput, added);
						yield rightInput.stream()
								.map(right -> {
									var addedEnvelope = applyMerge(added, right);
									ledger.put(
											new Pair<>(added.sender(), right.sender()),
											!addedEnvelope.documents().isEmpty());
									return addedEnvelope;
								})
								.filter(envelope -> !envelope.documents().isEmpty())
								.<SourceEvent>map(Added::new)
								.toList();
					} else if (rightNode == previous) {
						rightInput = add(rightInput, added);
						yield leftInput.stream()
								.map(left -> {
									var addedEnvelope = applyMerge(left, added);
									ledger.put(
											new Pair<>(left.sender(), added.sender()),
											!addedEnvelope.documents().isEmpty());
									return addedEnvelope;
								})
								.filter(envelope -> !envelope.documents().isEmpty())
								.<SourceEvent>map(Added::new)
								.toList();
					} else
						throw new IllegalArgumentException("Unexpected merge parent node: " + previous);
				}
				case Changed(var changed) -> {
					if (leftNode == previous) {
						yield rightInput.stream()
								.<Optional<SourceEvent>>map(right -> {
									var merged = applyMerge(changed, right);
									var anyMerge = !merged.documents().isEmpty();
									var anyMerged = ledger.put(new Pair<>(changed.sender(), right.sender()), anyMerge);
									if (anyMerged == null) {
										var message = "The envelope combination with sender IDs '%s' and '%s' was unknown when it shouldn't have been."
												.formatted(changed.sender(), right.sender());
										throw new IllegalStateException(message);
									}

									if (anyMerged && anyMerge) // ✅ ⇝ ✅
										return Optional.of(new Changed(merged));
									else if (anyMerged) // ✅ ⇝ ❌
										return Optional.of(new Removed(changed.sender()));
									else if (anyMerge) // ❌ ⇝ ✅
										return Optional.of(new Added(merged));
									else // ❌ ⇝ ❌
										return Optional.empty();
								})
								.flatMap(Optional::stream)
								.toList();
					} else if (rightNode == previous) {
						yield leftInput.stream()
								.<Optional<SourceEvent>>map(left -> {
									var merged = applyMerge(left, changed);
									var anyMerge = !merged.documents().isEmpty();
									var anyMerged = ledger.put(new Pair<>(left.sender(), changed.sender()), anyMerge);
									if (anyMerged == null) {
										var message = "The envelope combination with sender IDs '%s' and '%s' was unknown when it shouldn't have been."
												.formatted(left.sender(), changed.sender());
										throw new IllegalStateException(message);
									}

									if (anyMerged && anyMerge) // ✅ ⇝ ✅
										return Optional.of(new Changed(merged));
									else if (anyMerged) // ✅ ⇝ ❌
										return Optional.of(new Removed(changed.sender()));
									else if (anyMerge) // ❌ ⇝ ✅
										return Optional.of(new Added(merged));
									else // ❌ ⇝ ❌
										return Optional.empty();
								})
								.flatMap(Optional::stream)
								.toList();
					} else
						throw new IllegalArgumentException("Unexpected merge parent node: " + previous);
				}
				case Removed(var removed) -> {
					if (leftNode == previous) {
						var removals = ledger
								.keySet().stream()
								.filter(pair -> pair.left().equals(removed))
								.map(pair -> pair.left().mergeFrom(pair.right()))
								.<SourceEvent>map(Removed::new)
								.toList();
						ledger.entrySet().removeIf(entry -> entry.getKey().left().equals(removed));
						yield removals;
					} else if (rightNode == previous) {
						var removals = ledger
								.keySet().stream()
								.filter(pair -> pair.right().equals(removed))
								.map(pair -> pair.left().mergeFrom(pair.right()))
								.<SourceEvent>map(Removed::new)
								.toList();
						ledger.entrySet().removeIf(entry -> entry.getKey().right().equals(removed));
						yield removals;
					} else
						throw new IllegalArgumentException("Unexpected merge parent node: " + previous);
				}
			};
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Envelope<?> applyMerge(Envelope<?> left, Envelope<?> right) {
			var mergedId = left.sender().mergeFrom(right.sender());
			var mergedDocuments = crossProduct(left.documents(), right.documents())
					.flatMap(innerPair -> ((Merger) merger).merge(innerPair.left(), innerPair.right()).stream())
					.toList();
			return new SimpleEnvelope(mergedId, mergedDocuments);
		}

	}

	record StoreDocumentLiveNode(Optional<String> collection) implements LiveNode { }

	record StoreResourceLiveNode(Function<Document, String> naming) implements LiveNode { }

}
