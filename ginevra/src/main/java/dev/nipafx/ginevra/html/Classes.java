package dev.nipafx.ginevra.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

/**
 * A list of CSS class names.
 */
public class Classes {

	private final List<String> names;

	private Classes(List<String> names) {
		this.names = names;
	}

	public static Classes none() {
		return new Classes(List.of());
	}

	public static Classes of(String... names) {
		return new Classes(List.of(names));
	}

	public static Classes of(List<String> names) {
		return new Classes(List.copyOf(names));
	}

	public Classes plus(String... names) {
		var mergedNames = Stream
				.concat(this.names.stream(), Stream.of(names))
				.peek(name -> { if (name == null) throw new NullPointerException(); })
				.toList();
		return new Classes(mergedNames);
	}

	public Classes minus(String... names) {
		var remainder = new ArrayList<>(this.names);
		remainder.removeAll(List.of(names));
		return new Classes(List.copyOf(remainder));
	}

	public List<String> asList() {
		return names;
	}

	public String asCssString() {
		return names.stream()
				.filter(not(String::isBlank))
				.collect(joining(" "));
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Classes classList
				&& Objects.equals(names, classList.names);
	}

	@Override
	public int hashCode() {
		return Objects.hash(names);
	}

	@Override
	public String toString() {
		return STR."ClassList{\{names}}";
	}

}
