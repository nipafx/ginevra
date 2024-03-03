package dev.nipafx.ginevra.html;

import java.util.Objects;

/**
 * A CSS ID.
 */
public class Id {

	private final String id;

	private Id(String id) {
		this.id = id;
	}

	public static Id none() {
		return new Id("");
	}

	public static Id of(String id) {
		return new Id(id);
	}

	public String asString() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		return this == o
			   || o instanceof Id other
				  && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return STR."ID{\{id}}";
	}

}
