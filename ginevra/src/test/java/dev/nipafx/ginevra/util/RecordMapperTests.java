package dev.nipafx.ginevra.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.nipafx.ginevra.util.RecordMapper.createFromMapToStringList;
import static dev.nipafx.ginevra.util.RecordMapper.createFromMapToValues;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordMapperTests {

	public record None() { }
	public record OneString(String stringValue) { }
	public record OneOptional(Optional<String> stringValue) { }
	public record OneSet(Set<String> stringValues) { }
	public record OneList(List<String> stringValues) { }

	@Nested
	class FromMapToValues {

		@Test
		void noComponents() {
			var instance = createFromMapToValues(None.class, Map.of());
			assertThat(instance).isNotNull();
		}

		@Test
		void oneComponent_present() {
			var instance = createFromMapToValues(
					OneString.class,
					Map.of(
							"stringValue", "the string value"
					));

			assertThat(instance.stringValue).isEqualTo("the string value");
		}

		@Test
		void oneComponent_absent() {
			assertThatThrownBy(() -> createFromMapToValues(OneString.class, Map.of()))
					.hasMessageStartingWith("No values are defined for the component 'stringValue'")
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void optionalComponent_empty() {
			var instance = createFromMapToValues(
					OneOptional.class,
					Map.of(
							"stringValue", Optional.empty()
					));

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void optionalComponent_nonEmpty() {
			var instance = createFromMapToValues(
					OneOptional.class,
					Map.of(
							"stringValue", Optional.of("the string value")
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_present() {
			var instance = createFromMapToValues(
					OneOptional.class,
					Map.of(
							"stringValue", "the string value"
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_absent() {
			var instance = createFromMapToValues(
					OneOptional.class,
					Map.of());

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void listComponent_empty() {
			var instance = createFromMapToValues(
					OneList.class,
					Map.of(
							"stringValues", List.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void listComponent_nonEmpty() {
			var instance = createFromMapToValues(
					OneList.class,
					Map.of(
							"stringValues", List.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactly("value #1", "value #2", "value #3");
		}

		@Test
		void listComponent_present() {
			var instance = createFromMapToValues(
					OneList.class,
					Map.of(
							"stringValues", "value"
					));

			assertThat(instance.stringValues).containsExactly("value");
		}

		@Test
		void listComponent_absent() {
			var instance = createFromMapToValues(
					OneList.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_empty() {
			var instance = createFromMapToValues(
					OneSet.class,
					Map.of(
							"stringValues", Set.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_nonEmpty() {
			var instance = createFromMapToValues(
					OneSet.class,
					Map.of(
							"stringValues", Set.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value #1", "value #2", "value #3");
		}

		@Test
		void setComponent_present() {
			var instance = createFromMapToValues(
					OneSet.class,
					Map.of(
							"stringValues", "value"
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value");
		}

		@Test
		void setComponent_absent() {
			var instance = createFromMapToValues(
					OneSet.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

	}

	@Nested
	class FromMapToStringList {

		@Test
		void noComponents() {
			var instance = createFromMapToStringList(None.class, Map.of());
			assertThat(instance).isNotNull();
		}

		@Test
		void oneComponent_present() {
			var instance = createFromMapToStringList(
					OneString.class,
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).isEqualTo("the string value");
		}

		@Test
		void oneComponent_absent() {
			assertThatThrownBy(() -> createFromMapToStringList(OneString.class, Map.of()))
					.hasMessageStartingWith("No values are defined for the component 'stringValue'")
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void optionalComponent_empty() {
			var instance = createFromMapToStringList(
					OneOptional.class,
					Map.of(
							"stringValue", List.of()
					));

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void optionalComponent_nonEmpty() {
			var instance = createFromMapToStringList(
					OneOptional.class,
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_present() {
			var instance = createFromMapToStringList(
					OneOptional.class,
					Map.of(
							"stringValue", List.of("the string value")
					));

			assertThat(instance.stringValue).hasValue("the string value");
		}

		@Test
		void optionalComponent_absent() {
			var instance = createFromMapToStringList(
					OneOptional.class,
					Map.of());

			assertThat(instance.stringValue).isEmpty();
		}

		@Test
		void listComponent_empty() {
			var instance = createFromMapToStringList(
					OneList.class,
					Map.of(
							"stringValues", List.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void listComponent_nonEmpty() {
			var instance = createFromMapToStringList(
					OneList.class,
					Map.of(
							"stringValues", List.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactly("value #1", "value #2", "value #3");
		}

		@Test
		void listComponent_present() {
			var instance = createFromMapToStringList(
					OneList.class,
					Map.of(
							"stringValues", List.of("value")
					));

			assertThat(instance.stringValues).containsExactly("value");
		}

		@Test
		void listComponent_absent() {
			var instance = createFromMapToStringList(
					OneList.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_empty() {
			var instance = createFromMapToStringList(
					OneSet.class,
					Map.of(
							"stringValues", List.of()
					));

			assertThat(instance.stringValues).isEmpty();
		}

		@Test
		void setComponent_nonEmpty() {
			var instance = createFromMapToStringList(
					OneSet.class,
					Map.of(
							"stringValues", List.of("value #1", "value #2", "value #3")
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value #1", "value #2", "value #3");
		}

		@Test
		void setComponent_present() {
			var instance = createFromMapToStringList(
					OneSet.class,
					Map.of(
							"stringValues", List.of("value")
					));

			assertThat(instance.stringValues).containsExactlyInAnyOrder("value");
		}

		@Test
		void setComponent_absent() {
			var instance = createFromMapToStringList(
					OneSet.class,
					Map.of());

			assertThat(instance.stringValues).isEmpty();
		}

	}

}
