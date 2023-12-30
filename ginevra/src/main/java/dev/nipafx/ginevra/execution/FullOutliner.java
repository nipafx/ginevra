package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.MapOutline.FilteredStep;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Step;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Transformer;
import dev.nipafx.ginevra.parse.MarkdownParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class FullOutliner implements Outliner {

	private static final Predicate<Document> ALWAYS = _ -> true;

	private final Store store;
	private final Optional<MarkdownParser> markdownParser;

	private final Map<Step, List<FilteredStep>> stepMap;

	public FullOutliner(Store store, Optional<MarkdownParser> markdownParser) {
		this.store = store;
		this.markdownParser = markdownParser;
		this.stepMap = new HashMap<>();
	}

	// sources

	@Override
	public StepKey registerSource(Source source) {
		createStepListFor(source);
		return new SimpleStepKey(source);
	}

	@Override
	public StepKey sourceFileSystem(Path path) {
		return registerSource(new FileSource(path));
	}

	// transformers
	@Override
	public StepKey transform(StepKey previous, Transformer transformer, Predicate<Document> filter) {
		getStepListFor(previous).add(new FilteredStep(filter, transformer));
		ensureStepListFor(transformer);
		return new SimpleStepKey(transformer);
	}

	@Override
	public StepKey transformMarkdown(StepKey previous, Predicate<Document> filter) {
		if (markdownParser.isEmpty())
			throw new IllegalStateException("Can't transform Markdown: No Markdown parser was created");

		return transform(previous, new MarkupParsingTransformer(markdownParser.get()), filter);
	}

	// store
	@Override
	public void store(StepKey previous, Predicate<Document> filter) {
		getStepListFor(previous).add(new FilteredStep(filter, store));
	}

	// build

	@Override
	public Outline build() {
		return new MapOutline(stepMap, store);
	}

	// misc

	private void createStepListFor(Source source) {
		var previous = stepMap.put(source, new ArrayList<>());
		if (previous != null)
			throw new IllegalStateException("This source was already registered");
	}

	private void ensureStepListFor(Transformer transformer) {
		stepMap.putIfAbsent(transformer, new ArrayList<>());
	}

	private List<FilteredStep> getStepListFor(StepKey previous) {
		if (!(previous instanceof SimpleStepKey(var step)))
			throw new IllegalStateException("Unexpected implementation of " + StepKey.class.getSimpleName());

		var stepList = stepMap.get(step);
		if (stepList == null)
			throw new IllegalStateException("The specified previous step is unregistered");
		return stepList;
	}

	private record SimpleStepKey(Step step) implements Outliner.StepKey { }

}
