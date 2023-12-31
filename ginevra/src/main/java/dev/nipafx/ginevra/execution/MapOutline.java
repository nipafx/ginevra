package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Step;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Transformer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.stream.Collectors.toUnmodifiableMap;

class MapOutline implements Outline {

	private final Map<Step<?>, List<FilteredStep<?, ?>>> stepMap;
	private final List<Source<?>> sources;
	private final Store store;

	MapOutline(Map<Step<?>, List<FilteredStep<?, ?>>> stepMap, Store store) {
		this.stepMap = stepMap
				.entrySet().stream()
				.map(entry -> entry(entry.getKey(), List.copyOf(entry.getValue())))
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
		this.sources = stepMap
				.keySet().stream()
				.<Source<?>>flatMap(step -> step instanceof Source ? Stream.of((Source<?>) step) : Stream.empty())
				.toList();
		this.store = store;
	}

	@Override
	public void run() {
		sources.forEach(source -> source.register(doc -> process(source, doc)));
		sources.forEach(Source::loadAll);
		System.out.println(store);
	}

	private void process(Source<?> source, Document<?> doc) {
		processRecursively(source, doc);
		store.commit();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processRecursively(Step<?> source, Document<?> doc) {
		var steps = stepMap.get(source);
		if (steps == null)
			throw new IllegalStateException("Unknown step triggered document processing");

		steps.forEach(filteredStep -> {
			var applyStep = ((Predicate) filteredStep.filter()).test(doc);
			if (applyStep)
				switch (filteredStep.step()) {
					case Source<?> _ -> throw new IllegalStateException("No step should map to a source");
					case Transformer<?, ?> transformer -> transformer
							.transform((Document) doc)
							.forEach(transformedDoc -> processRecursively(transformer, (Document<?>) transformedDoc));
					case Store store -> store.store(doc);
				}
		});
	}

}
