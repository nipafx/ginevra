package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class MapStore implements Store {

	private final Map<DocCollection, List<Document<?>>> documents;
	private final List<Document<?>> rootDocuments;

	public MapStore() {
		documents = new HashMap<>();
		rootDocuments = new ArrayList<>();
	}

	@Override
	public void store(Document<?> doc) {
		rootDocuments.add(doc);
	}

	@Override
	public void store(DocCollection collection, Document<?> doc) {
		documents
				.computeIfAbsent(collection, _ -> new ArrayList<>())
				.add(doc);
	}

	@Override
	public void commit() {

	}

	@Override
	public String toString() {
		return this
				.documents
				.entrySet().stream()
				.map(entry -> STR." - \{entry.getKey().name()}: \{entry.getValue().size()}")
				.collect(joining("MapStore:\n\t", "\t", "\n"));
	}

}