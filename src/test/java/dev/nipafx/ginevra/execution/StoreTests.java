package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.DocumentId;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoreTests {

	private final Store store = new Store();

	@Nested
	class Root {

		@Test
		void queryRootDocument() {
			var testDocument = new TestDocument("content");
			store.store(inEnvelope(testDocument));

			var result = store.query(new RootQuery<>(TestDocument.class));
			assertThat(result).isEqualTo(testDocument);
		}

		@Test
		void storeTwice_differentDocument_succeeds() {
			var testDocument1 = new TestDocument("content");
			store.store(inEnvelope(testDocument1));
			var testDocument2 = new NonCollidingTestDocument("more content");
			store.store(inEnvelope(testDocument2));

			var result1 = store.query(new RootQuery<>(TestDocument.class));
			assertThat(result1).isEqualTo(testDocument1);
			var result2 = store.query(new RootQuery<>(NonCollidingTestDocument.class));
			assertThat(result2).isEqualTo(testDocument2);
		}

		@Test
		void storeTwice_differentDocument_merged() {
			store.store(inEnvelope(new TestDocument("content")));
			store.store(inEnvelope(new NonCollidingTestDocument("more content")));

			var result = store.query(new RootQuery<>(MergedTestDocument.class));
			assertThat(result).isEqualTo(new MergedTestDocument("content", "more content"));
		}

		@Test
		void storeTwice_sameDocument_fails() {
			store.store(inEnvelope(new TestDocument("content")));

			assertThatThrownBy(() -> store.store(inEnvelope(new TransformedTestDocument("more content"))))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void queryCollection() {
			var testDocument1 = new TestDocument("content #1");
			var testDocument2 = new TestDocument("content #2");
			var testDocument3 = new TestDocument("content #3");
			store.store("collection", inEnvelope(testDocument1));
			store.store("collection", inEnvelope(testDocument2));
			store.store("collection", inEnvelope(testDocument3));

			var result = store.query(new RootQuery<>(RootCollectionDocument.class));
			assertThat(result.collection())
					.containsExactlyInAnyOrder(testDocument1, testDocument2, testDocument3);
		}

		@Test
		void mixedQuery() {
			var testDocument1 = new TestDocument("content #1");
			var testDocument2 = new TestDocument("content #2");
			var testDocument3 = new TestDocument("content #3");
			store.store("collection", inEnvelope(testDocument1));
			store.store("collection", inEnvelope(testDocument2));
			store.store("collection", inEnvelope(testDocument3));
			store.store(inEnvelope(new TestDocument("content")));
			store.store(inEnvelope(new NonCollidingTestDocument("more content")));

			var result = store.query(new RootQuery<>(RootMixedDocument.class));
			assertThat(result.content()).isEqualTo("content");
			assertThat(result.moreContent()).isEqualTo("more content");
			assertThat(result.collection())
					.containsExactlyInAnyOrder(testDocument1, testDocument2, testDocument3);
		}

	}

	@Nested
	class Collections {

		@Test
		void querySingle() {
			var testDocument = new TestDocument("content");
			store.store("collection", inEnvelope(testDocument));

			var result = store.query(new CollectionQuery<>("collection", TestDocument.class));
			assertThat(result).containsExactly(testDocument);
		}

		@Test
		void queryMultiple() {
			var testDocument1 = new TestDocument("content #1");
			var testDocument2 = new TestDocument("content #2");
			var testDocument3 = new TestDocument("content #3");
			store.store("collection", inEnvelope(testDocument1));
			store.store("collection", inEnvelope(testDocument2));
			store.store("collection", inEnvelope(testDocument3));

			var result = store.query(new CollectionQuery<>("collection", TestDocument.class));
			assertThat(result).containsExactlyInAnyOrder(testDocument1, testDocument2, testDocument3);
		}

		@Test
		void queryMultipleTransformed() {
			store.store("collection", inEnvelope(new TestDocument("content #1")));
			store.store("collection", inEnvelope(new TestDocument("content #2")));
			store.store("collection", inEnvelope(new TestDocument("content #3")));

			var result = store.query(new CollectionQuery<>("collection", TransformedTestDocument.class));
			assertThat(result).containsExactlyInAnyOrder(
					new TransformedTestDocument("content #1"),
					new TransformedTestDocument("content #2"),
					new TransformedTestDocument("content #3"));
		}

	}

	private static <T extends Record & Document> Envelope<T> inEnvelope(T document) {
		return new SimpleEnvelope<>(DocumentId.sourcedFrom("test", URI.create("test")), document);
	}

	public record TestDocument(String content) implements Document { }
	public record NonCollidingTestDocument(String moreContent) implements Document { }
	public record MergedTestDocument(String content, String moreContent) implements Document { }

	public record RootCollectionDocument(List<TestDocument> collection) implements Document { }
	public record RootMixedDocument(String content, String moreContent, List<TestDocument> collection) implements Document { }

	// This "transformed" document has the same shape as the test document, but
	// is nonetheless of a different type, so record mapping is engaged.
	// For tests of record mapping itself, see `RecordMapperTests`.
	public record TransformedTestDocument(String content) implements Document { }

}
