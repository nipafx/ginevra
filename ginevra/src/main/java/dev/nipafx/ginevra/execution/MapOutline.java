package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.Step.SourceStep;
import dev.nipafx.ginevra.execution.Step.StoreStep;
import dev.nipafx.ginevra.execution.Step.TransformStep;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Store;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Map.entry;
import static java.util.stream.Collectors.toUnmodifiableMap;

class MapOutline implements Outline {

	private final Map<Step, List<Step>> stepMap;
	private final Store store;

	MapOutline(Map<Step, List<Step>> stepMap, Store store) {
		this.stepMap = stepMap
				.entrySet().stream()
				.map(entry -> entry(entry.getKey(), List.copyOf(entry.getValue())))
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
		this.store = store;
	}

	@Override
	public void run() {
		var sourceSteps = stepMap
				.keySet().stream()
				.filter(SourceStep.class::isInstance)
				.map(SourceStep.class::cast)
				.toList();

		sourceSteps.forEach(step -> step.source().register(doc -> process(step, doc)));
		sourceSteps.forEach(step -> step.source().loadAll());
		System.out.println(store);
	}

	private void process(SourceStep sourceStep, Document<?> doc) {
		processRecursively(sourceStep, doc);
		store.commit();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processRecursively(Step origin, Document<?> doc) {
		var steps = stepMap.get(origin);
		if (steps == null)
			throw new IllegalStateException("Unknown step triggered document processing");

		steps.forEach(step -> {
			switch (step) {
				case SourceStep _ -> throw new IllegalStateException("No step should map to a source");
				case TransformStep transformStep -> {
					if (transformStep.filter().test(doc))
						transformStep.transformer()
								.transform((Document) doc)
								.forEach(transformedDoc -> processRecursively(transformStep, (Document<?>) transformedDoc));
				}
				case StoreStep(var filter, var collection) -> {
					if (filter.test(doc))
						collection.ifPresentOrElse(
								col -> store.store(col, doc),
								() -> store.store(doc)
						);
				}
			}
		});
	}

}