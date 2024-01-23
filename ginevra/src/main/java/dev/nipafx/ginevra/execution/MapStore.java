package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.util.RecordMapper;

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
	public <RESULT extends Record & Data> Document<RESULT> query(RootQuery<RESULT> query) {
		queries.add(query);
		// TODO: evaluate query
		return null;
	}

	@Override
	public <RESULT extends Record & Data> List<Document<RESULT>> query(CollectionQuery<RESULT> query) {
		if (!documents.containsKey(query.collection()))
			throw new IllegalArgumentException("Unknown document collection: " + query.collection());

		queries.add(query);
		return documents
				.get(query.collection()).stream()
				.<Document<RESULT>> map(document -> new GeneralDocument<>(
						document.id().transform("queried-" + query.resultType().getName()),
						RecordMapper.createRecordFromRecord(query.resultType(), document.data())))
				.toList();
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