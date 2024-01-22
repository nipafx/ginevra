package dev.nipafx.ginevra.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.nipafx.ginevra.util.RecordMapper.createRecordFromMaps;
import static dev.nipafx.ginevra.util.RecordMapper.createRecordFromStringListMap;
import static dev.nipafx.ginevra.util.RecordMapper.createRecordFromValueMap;
import static dev.nipafx.ginevra.util.RecordMapper.createValueMapFromRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordMapperTests {

	// TODO: test error cases

	public record None() { }
	public record OneString(String stringValue) { }
	public record OneOptional(Optional<String> stringValue) { }
	public record OneSet(Set<String> stringValues) { }
	public record OneList(List<String> stringValues) { }

	@Nested
	class FromInstanceToValueMap {

		@Test
		void noComponents() {
			var instance = new None();
			var map = createValueMapFromRecord(instance);

			assertThat(map).isEmpty();
		}

		@Test
		void oneComponent() {
			var instance = new OneString("value");
			var map = createValueMapFromRecord(instance);

			assertThat(map).containsExactlyInAnyOrderEntriesOf(Map.of("stringValue", instance.stringValue()));
		}

		@Test
		void oneOptionalComponent() {
			var instance = new OneOptional(Optional.of("value"));
			var map = createValueMapFromRecord(instance);

			assertThat(map).containsExactlyInAnyOrderEntriesOf(Map.of("stringValue", instance.stringValue()));
		}

		@Test
		void oneListComponent() {
			var instance = new OneList(List.of("value #1", "value #2", "value #3"));
			var map = createValueMapFromRecord(instance);

			assertThat(map).containsExactlyInAnyOrderEntriesOf(Map.of("stringValues", instance.stringValues()));
		}

		@Test
		void oneSetComponent() {
			var instance = new OneSet(Set.of("value #1", "value #2", "value #3"));
			var map = createValueMapFromRecord(instance);

			assertThat(map).containsExactlyInAnyOrderEntriesOf(Map.of("stringValues", instance.stringValues()));
		}

	}

	@Nested
	class FromValueMapToInstance {

		@Test
		void noComponents() {
			var instance = createRecordFromValueMap(None.class, Map.of());
			assertThat(instance).isNotNull();
		}

		@Test
		void oneComponent_present() {
			var instance = createRecordFromValueMap(
					OneString.class,
					Map.of(
							"stringValue", "the string value"
					));

			assertThat(instance.stringValue).isEqualTo("the string value");
		}

		@Test
		void oneComponent_absent() {
			assertThatThrownBy(() -> createRecordFromValueMap(OneString.class, Map.of()))
					.hasMessageStartingWith("No values are defined for the component 'stringValue'")
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void optionalComponent_empty() {
			var instance = createRecordFromValueMap(
					OneOptional.class,
					Map.of(
							"stringValue", Optional.empty()
					));

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void optionalComponent_nonEmpty() {
			var instance = createRecordFromValueMap(
					OneOptional.class,
					Map.of(
							"stringValue", Optional.of("the string value")
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_present() {
			var instance = createRecordFromValueMap(
					OneOptional.class,
					Map.of(
							"stringValue", "the string value"
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_absent() {
			var instance = createRecordFromValueMap(
					OneOptional.class,
					Map.of());

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void listComponent_empty() {
			var instance = createRecordFromValueMap(
					OneList.class,
					Map.of(
							"stringValues", List.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void listComponent_nonEmpty() {
			var instance = createRecordFromValueMap(
					OneList.class,
					Map.of(
							"stringValues", List.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactly("value #1", "value #2", "value #3");
		}

		@Test
		void listComponent_present() {
			var instance = createRecordFromValueMap(
					OneList.class,
					Map.of(
							"stringValues", "value"
					));

			assertThat(instance.stringValues).containsExactly("value");
		}

		@Test
		void listComponent_absent() {
			var instance = createRecordFromValueMap(
					OneList.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_empty() {
			var instance = createRecordFromValueMap(
					OneSet.class,
					Map.of(
							"stringValues", Set.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_nonEmpty() {
			var instance = createRecordFromValueMap(
					OneSet.class,
					Map.of(
							"stringValues", Set.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value #1", "value #2", "value #3");
		}

		@Test
		void setComponent_present() {
			var instance = createRecordFromValueMap(
					OneSet.class,
					Map.of(
							"stringValues", "value"
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value");
		}

		@Test
		void setComponent_absent() {
			var instance = createRecordFromValueMap(
					OneSet.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

	}

	@Nested
	class FromStringListMapToInstance {

		@Test
		void noComponents() {
			var instance = createRecordFromStringListMap(None.class, Map.of());
			assertThat(instance).isNotNull();
		}

		@Test
		void oneComponent_present() {
			var instance = createRecordFromStringListMap(
					OneString.class,
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).isEqualTo("the string value");
		}

		@Test
		void oneComponent_absent() {
			assertThatThrownBy(() -> createRecordFromStringListMap(OneString.class, Map.of()))
					.hasMessageStartingWith("No values are defined for the component 'stringValue'")
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void optionalComponent_empty() {
			var instance = createRecordFromStringListMap(
					OneOptional.class,
					Map.of(
							"stringValue", List.of()
					));

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void optionalComponent_nonEmpty() {
			var instance = createRecordFromStringListMap(
					OneOptional.class,
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_present() {
			var instance = createRecordFromStringListMap(
					OneOptional.class,
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_absent() {
			var instance = createRecordFromStringListMap(
					OneOptional.class,
					Map.of());

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void listComponent_empty() {
			var instance = createRecordFromStringListMap(
					OneList.class,
					Map.of(
							"stringValues", List.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void listComponent_nonEmpty() {
			var instance = createRecordFromStringListMap(
					OneList.class,
					Map.of(
							"stringValues", List.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactly("value #1", "value #2", "value #3");
		}

		@Test
		void listComponent_present() {
			var instance = createRecordFromStringListMap(
					OneList.class,
					Map.of(
							"stringValues", List.of("value")
					));

			assertThat(instance.stringValues).containsExactly("value");
		}

		@Test
		void listComponent_absent() {
			var instance = createRecordFromStringListMap(
					OneList.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_empty() {
			var instance = createRecordFromStringListMap(
					OneSet.class,
					Map.of(
							"stringValues", List.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_nonEmpty() {
			var instance = createRecordFromStringListMap(
					OneSet.class,
					Map.of(
							"stringValues", List.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value #1", "value #2", "value #3");
		}

		@Test
		void setComponent_present() {
			var instance = createRecordFromStringListMap(
					OneSet.class,
					Map.of(
							"stringValues", List.of("value")
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value");
		}

		@Test
		void setComponent_absent() {
			var instance = createRecordFromStringListMap(
					OneSet.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

	}

	@Nested
	class FromMapsToInstance {

		@Test
		void noComponents() {
			var instance = createRecordFromMaps(None.class, Map.of(), Map.of());
			assertThat(instance).isNotNull();
		}

		@Test
		void oneComponent_presentAsValue() {
			var instance = createRecordFromMaps(
					OneString.class,
					Map.of(
							"stringValue", "the string value"
					),
					Map.of());

			assertThat(instance.stringValue).isEqualTo("the string value");
		}

		@Test
		void oneComponent_presentAsString() {
			var instance = createRecordFromMaps(
					OneString.class,
					Map.of(),
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).isEqualTo("the string value");
		}

		@Test
		void oneComponent_absent() {
			assertThatThrownBy(() -> createRecordFromMaps(OneString.class, Map.of(), Map.of()))
					.hasMessageStartingWith("No values are defined for the component 'stringValue'")
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void optionalComponent_emptyValue() {
			var instance = createRecordFromMaps(
					OneOptional.class,
					Map.of(
							"stringValue", Optional.empty()
					),
					Map.of());

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void optionalComponent_emptyList() {
			var instance = createRecordFromMaps(
					OneOptional.class,
					Map.of(),
					Map.of(
							"stringValue", List.of()
					));

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void optionalComponent_nonEmptyValue() {
			var instance = createRecordFromMaps(
					OneOptional.class,
					Map.of(
							"stringValue", Optional.of("the string value")
					),
					Map.of());

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_presentValue() {
			var instance = createRecordFromMaps(
					OneOptional.class,
					Map.of(
							"stringValue", "the string value"
					),
					Map.of());

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_presentString() {
			var instance = createRecordFromMaps(
					OneOptional.class,
					Map.of(),
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_absent() {
			var instance = createRecordFromMaps(
					OneOptional.class,
					Map.of(),
					Map.of());

			assertThat(instance.stringValue).isEmpty();
		}

	}

}
