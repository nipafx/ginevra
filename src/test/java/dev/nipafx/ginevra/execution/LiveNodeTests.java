package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.LiveNode.FilterLiveNode;
import dev.nipafx.ginevra.execution.LiveNode.TransformLiveNode;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.SenderId;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.SourceEvent.Added;
import dev.nipafx.ginevra.outline.SourceEvent.Changed;
import dev.nipafx.ginevra.outline.SourceEvent.Removed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiveNodeTests {

	@Nested
	class FilterLiveNodeTests {

		private final FilterLiveNode node = new FilterLiveNode(doc -> asDoc(doc).value() < 10);

		private void assertIsRegistered(String id, boolean anyRemained) {
			// the specific event used for the update doesn't matter;
			// what matters is that the response is an event that shows
			// that an envelope with the specified ID was correctly registered
			var remove = new Removed(id(id));
			var removal = node.update(remove);
			if (anyRemained) {
				var expected = new Removed(id(id).filter());
				assertThat(removal).contains(expected);
			} else {
				assertThat(removal).isEmpty();
			}
		}

		private void assertIsNotRegistered(String id) {
			// the specific event used for the update doesn't matter;
			// what matters is that the response is an event that shows
			// that an envelope with the specified ID was correctly registered
			var remove = new Removed(id(id));
			assertThatThrownBy(() -> node.update(remove)).isInstanceOf(IllegalStateException.class);
		}

		@Nested
		class Filter {

			@Test
			void keepSmallValue() {
				var filtered = node.filter(envelop("4", new Doc(4)));
				assertThat(filtered.documents()).isEqualTo(List.of(new Doc(4)));
			}

			@Test
			void filterLargeValue() {
				var filtered = node.filter(envelop("42", new Doc(42)));
				assertThat(filtered.documents()).isEmpty();
			}

			@Test
			void filterAllLargeValues() {
				var filtered = node.filter(envelop("255", new Doc(4), new Doc(42), new Doc(2), new Doc(255)));
				assertThat(filtered.documents()).isEqualTo(List.of(new Doc(4), new Doc(2)));
			}

		}

		@Nested
		class Update {

			@BeforeEach
			void prefillNode() {
				node.filter(envelop("4", new Doc(4)));
				node.filter(envelop("42", new Doc(42)));
			}

			@Nested
			class Addition {

				@Test
				void addPassingEnvelope_addedEvent() {
					var addition = node.update(new Added(envelop("2", new Doc(2))));

					var expected = new Added(envelop(
							id("2").filter(),
							new Doc(2)));
					assertThat(addition).contains(expected);

					assertIsRegistered("2", true);
				}

				@Test
				void addNonPassingEnvelope_noEvent() {
					var addition = node.update(new Added(envelop("42", new Doc(42))));

					assertThat(addition).isEmpty();

					assertIsRegistered("42", false);
				}

			}

			@Nested
			class Change {

				@Test
				void changeUnknownValue_exception() {
					var changed = new Changed(envelop("63", new Doc(63)));
					assertThatThrownBy(() -> node.update(changed)).isInstanceOf(IllegalStateException.class);
				}

				@Test
				void changePassingToPassingValue_changedEvent() {
					var change = node.update(new Changed(envelop("4", new Doc(8))));

					var expected = new Changed(envelop(
							id("4").filter(),
							new Doc(8)));
					assertThat(change).contains(expected);

					assertIsRegistered("4", true);
				}

				@Test
				void changePassingToNonPassingValue_removedEvent() {
					var removal = node.update(new Changed(envelop("4", new Doc(63))));

					var expected = new Removed(id("4").filter());
					assertThat(removal).contains(expected);

					assertIsRegistered("4", false);
				}

				@Test
				void changeNonPassingToPassingValue_addedEvent() {
					var added = node.update(new Changed(envelop("42", new Doc(4))));

					var expected = new Added(envelop(
							id("42").filter(),
							new Doc(4)));
					assertThat(added).contains(expected);

					assertIsRegistered("4", true);
				}

				@Test
				void changeNonPassingToNonPassingValue_noEvent() {
					var event = node.update(new Changed(envelop("42", new Doc(63))));

					assertThat(event).isEmpty();

					assertIsRegistered("42", false);
				}

			}

			@Nested
			class Remove {

				@Test
				void removeUnknownValue_exception() {
					var removed = new Removed(id("63"));
					assertThatThrownBy(() -> node.update(removed)).isInstanceOf(IllegalStateException.class);
				}

				@Test
				void removePassingEnvelope_removedEvent() {
					var removal = node.update(new Removed(id("4")));

					var expected = new Removed(id("4").filter());
					assertThat(removal).contains(expected);

					assertIsNotRegistered("4");
				}

				@Test
				void removeNonPassingEnvelope_noEvent() {
					var removal = node.update(new Removed(id("42")));

					assertThat(removal).isEmpty();

					assertIsNotRegistered("42");
				}

			}

		}

	}

	@Nested
	class TransformLiveNodeTests {

		private static final String NAME = "🏳️‍🌈";
		private static final Function<Document, List<Document>> FUNCTION = doc -> asDoc(doc).value() <= 1
				? List.of()
				: List.of(new Doc(asDoc(doc).value() / 10 * 10), new Doc(asDoc(doc).value() % 10));

		private final TransformLiveNode node = new TransformLiveNode(NAME, FUNCTION);

		private void assertIsRegistered(String id, boolean anyTransformed) {
			// the specific event used for the update doesn't matter;
			// what matters is that the response is an event that shows
			// that an envelope with the specified ID was correctly registered
			var remove = new Removed(id(id));
			var removal = node.update(remove);
			if (anyTransformed) {
				var expected = new Removed(id(id).transform(NAME));
				assertThat(removal).contains(expected);
			} else {
				assertThat(removal).isEmpty();
			}
		}

		private void assertIsNotRegistered(String id) {
			// the specific event used for the update doesn't matter;
			// what matters is that the response is an event that shows
			// that an envelope with the specified ID was correctly registered
			var remove = new Removed(id(id));
			assertThatThrownBy(() -> node.update(remove)).isInstanceOf(IllegalStateException.class);
		}

		@Test
		void transform() {
			var transformed = node.transform(envelop("42", new Doc(42)));
			assertThat(transformed.documents()).isEqualTo(List.of(new Doc(40), new Doc(2)));
		}

		@Nested
		class Update {

			@BeforeEach
			void prefillNode() {
				node.transform(envelop("0", new Doc(0)));
				node.transform(envelop("42", new Doc(42)));
			}

			@Nested
			class Addition {

				@Test
				void addEnvelopeMappedToNonEmpty_addedEvent() {
					var addition = node.update(new Added(envelop("63", new Doc(63))));

					var expected = new Added(envelop(
							id("63").transform(NAME),
							new Doc(60), new Doc(3)));
					assertThat(addition).contains(expected);

					assertIsRegistered("63", true);
				}

				@Test
				void addEnvelopeMappedToEmpty_noEvent() {
					var event = node.update(new Added(envelop("0", new Doc(0))));

					assertThat(event).isEmpty();

					assertIsRegistered("0", false);
				}

			}

			@Nested
			class Change {

				@Test
				void changeUnknownValue_exception() {
					var changed = new Changed(envelop("63", new Doc(63)));
					assertThatThrownBy(() -> node.update(changed)).isInstanceOf(IllegalStateException.class);
				}

				@Test
				void changeNonEmptyToNonEmptyValue_changedEvent() {
					var change = node.update(new Changed(envelop("42", new Doc(63))));

					var expected = new Changed(envelop(
							id("42").transform(NAME),
							new Doc(60), new Doc(3)));
					assertThat(change).contains(expected);

					assertIsRegistered("42", true);
				}

				@Test
				void changeNonEmptyToEmptyValue_removedEvent() {
					var removal = node.update(new Changed(envelop("42", new Doc(0))));

					var expected = new Removed(id("42").transform(NAME));
					assertThat(removal).contains(expected);

					assertIsRegistered("42", false);
				}

				@Test
				void changeEmptyToNonEmptyValue_addedEvent() {
					var added = node.update(new Changed(envelop("0", new Doc(63))));

					var expected = new Added(envelop(
							id("0").transform(NAME),
							new Doc(60), new Doc(3)));
					assertThat(added).contains(expected);

					assertIsRegistered("0", true);
				}

				@Test
				void changeEmptyToEmptyValue_noEvent() {
					var event = node.update(new Changed(envelop("0", new Doc(1))));

					assertThat(event).isEmpty();

					assertIsRegistered("0", false);
				}

			}

			@Nested
			class Remove {

				@Test
				void removeUnknownValue_exception() {
					var removed = new Removed(id("63"));
					assertThatThrownBy(() -> node.update(removed)).isInstanceOf(IllegalStateException.class);
				}

				@Test
				void removeNonEmptyEnvelope_removedEvent() {
					var removal = node.update(new Removed(id("42")));

					var expected = new Removed(id("42").transform(NAME));
					assertThat(removal).contains(expected);

					assertIsNotRegistered("42");
				}

				@Test
				void removeEmptyEnvelope_noEvent() {
					var removal = node.update(new Removed(id("0")));

					assertThat(removal).isEmpty();

					assertIsNotRegistered("0");
				}

			}

		}

	}

	// helper

	private static Envelope<Doc> envelop(String senderId, Doc... docs) {
		return envelop(id(senderId), docs);
	}

	private static Envelope<Doc> envelop(SenderId senderId, Doc... docs) {
		return new SimpleEnvelope<>(senderId, List.of(docs));
	}

	private static SenderId id(String name) {
		return SenderId.source("test", URI.create(name));
	}

	private static Doc asDoc(Document doc) {
		return (Doc) doc;
	}

	record Doc(int value) implements Document { }

}
