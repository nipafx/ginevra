package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Store;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class MapStore implements Store {

	private final List<Document<?>> documents;

	public MapStore() {
		documents = new ArrayList<>();
	}

	@Override
	public void store(Document<?> doc) {
		documents.add(doc);
	}

	@Override
	public void commit() {

	}

	@Override
	public String toString() {
		var documents = this.documents.stream()
				.map(Document::toString)
				.collect(joining("\n\t", "\t", "\n"));
		return "MapStore:\n" + documents;
	}

}
