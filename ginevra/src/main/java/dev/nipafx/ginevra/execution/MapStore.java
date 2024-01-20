package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

public class MapStore implements Store {

	private final Map<DocCollection, List<Document<?>>> documents;
	private final List<Document<?>> rootDocuments;
	private final Set<Query<?>> queries;

	public MapStore() {
		documents = new HashMap<>();
		rootDocuments = new ArrayList<>();
		queries = new HashSet<>();
	}

	@Override
	public void store(Document<?> doc) {
		rootDocuments.add(doc);
		// TODO: detect changes and mark impacted queries as dirty
	}

	@Override
	public void store(DocCollection collection, Document<?> doc) {
		documents
				.computeIfAbsent(collection, _ -> new ArrayList<>())
				.add(doc);
	}

	@Override
	public List<Query<?>> commit() {
		// TODO: return dirty queries
		return List.of();
	}

	@Override
	public <RESULT extends Record> RESULT query(RootQuery<RESULT> query) {
		queries.add(query);
		// TODO: evaluate query
		return null;
	}

	@Override
	public <RESULT extends Record> List<RESULT> query(CollectionQuery<RESULT> query) {
		queries.add(query);
		// TODO: evaluate query
		return null;
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