package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Store;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class MapStore implements Store {

	private final List<Document<?>> rootDocuments;

	public MapStore() {
		rootDocuments = new ArrayList<>();
	}

	@Override
	public void store(Document<?> doc) {
		rootDocuments.add(doc);
	}

	@Override
	public void commit() {

	}

	@Override
	public String toString() {
		return this
				.rootDocuments.stream()
				.map(doc -> STR." - \{doc.id()}")
				.collect(joining("MapStore:\n\t", "\t", "\n"));
	}

}
