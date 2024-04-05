package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.StoreId;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoreTests {

	private final Store store = new Store();

	@Nested
	class Root {

		@Test
		void queryRootData() {
			var testData = new TestData("content");
			store.store(asDocument(testData));

			var result = store.query(new RootQuery<>(TestData.class));
			assertThat(result.id()).isEqualTo(new StoreId("MapStore"));
			assertThat(result.data()).isEqualTo(testData);
		}

		@Test
		void storeTwice_differentData_succeeds() {
			var testData1 = new TestData("content");
			store.store(asDocument(testData1));
			var testData2 = new NonCollidingTestData("more content");
			store.store(asDocument(testData2));

			var result1 = store.query(new RootQuery<>(TestData.class));
			assertThat(result1.data()).isEqualTo(testData1);
			var result2 = store.query(new RootQuery<>(NonCollidingTestData.class));
			assertThat(result2.data()).isEqualTo(testData2);
		}

		@Test
		void storeTwice_differentData_merged() {
			store.store(asDocument(new TestData("content")));
			store.store(asDocument(new NonCollidingTestData("more content")));

			var result = store.query(new RootQuery<>(MergedTestData.class));
			assertThat(result.data()).isEqualTo(new MergedTestData("content", "more content"));
		}

		@Test
		void storeTwice_sameData_fails() {
			store.store(asDocument(new TestData("content")));

			assertThatThrownBy(() -> store.store(asDocument(new TransformedTestData("more content"))))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void queryCollection() {
			var testData1 = new TestData("content #1");
			var testData2 = new TestData("content #2");
			var testData3 = new TestData("content #3");
			store.store("collection", asDocument(testData1));
			store.store("collection", asDocument(testData2));
			store.store("collection", asDocument(testData3));

			var result = store.query(new RootQuery<>(RootCollectionData.class));
			assertThat(result.data().collection())
					.containsExactlyInAnyOrder(testData1, testData2, testData3);
		}

		@Test
		void mixedQuery() {
			var testData1 = new TestData("content #1");
			var testData2 = new TestData("content #2");
			var testData3 = new TestData("content #3");
			store.store("collection", asDocument(testData1));
			store.store("collection", asDocument(testData2));
			store.store("collection", asDocument(testData3));
			store.store(asDocument(new TestData("content")));
			store.store(asDocument(new NonCollidingTestData("more content")));

			var result = store.query(new RootQuery<>(RootMixedData.class));
			assertThat(result.data().content()).isEqualTo("content");
			assertThat(result.data().moreContent()).isEqualTo("more content");
			assertThat(result.data().collection())
					.containsExactlyInAnyOrder(testData1, testData2, testData3);
		}

	}

	@Nested
	class Collections {

		@Test
		void querySingle() {
			var testData = new TestData("content");
			store.store("collection", asDocument(testData));

			var result = store.query(new CollectionQuery<>("collection", TestData.class));
			assertThat(result)
					.map(Document::data)
					.containsExactly(testData);
		}

		@Test
		void queryMultiple() {
			var testData1 = new TestData("content #1");
			var testData2 = new TestData("content #2");
			var testData3 = new TestData("content #3");
			store.store("collection", asDocument(testData1));
			store.store("collection", asDocument(testData2));
			store.store("collection", asDocument(testData3));

			var result = store.query(new CollectionQuery<>("collection", TestData.class));
			assertThat(result)
					.map(Document::data)
					.containsExactlyInAnyOrder(testData1, testData2, testData3);
		}

		@Test
		void queryMultipleTransformed() {
			store.store("collection", asDocument(new TestData("content #1")));
			store.store("collection", asDocument(new TestData("content #2")));
			store.store("collection", asDocument(new TestData("content #3")));

			var result = store.query(new CollectionQuery<>("collection", TransformedTestData.class));
			assertThat(result)
					.map(Document::data)
					.containsExactlyInAnyOrder(
							new TransformedTestData("content #1"),
							new TransformedTestData("content #2"),
							new TransformedTestData("content #3"));
		}

	}

	private static <T extends Record & Data> Document<T> asDocument(T data) {
		return new GeneralDocument<>(new StoreId("test"), data);
	}
	
	public record TestData(String content) implements Data { }
	public record NonCollidingTestData(String moreContent) implements  Data { }
	public record MergedTestData(String content, String moreContent) implements  Data { }

	public record RootCollectionData(List<TestData> collection) implements Data { }
	public record RootMixedData(String content, String moreContent, List<TestData> collection) implements Data { }

	// This "transformed" data has the same shape as the test data, but
	// is nonetheless of a different type, so record mapping is engaged.
	// For tests of record mapping itself, see `RecordMapperTests`.
	public record TransformedTestData(String content) implements Data { }

}
