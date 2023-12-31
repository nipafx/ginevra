package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.DataString;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Step;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Transformer;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.MarkupDocument.FrontMatter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class FullOutliner implements Outliner {

	private static final Predicate<Document> ALWAYS = _ -> true;

	private final Store store;
	private final Optional<MarkdownParser> markdownParser;

	private final Map<Step<?>, List<FilteredStep<?, ?>>> stepMap;

	public FullOutliner(Store store, Optional<MarkdownParser> markdownParser) {
		this.store = store;
		this.markdownParser = markdownParser;
		this.stepMap = new HashMap<>();
	}

	// sources

	@Override
	public <DATA_OUT extends Record & Data> StepKey<DATA_OUT>
	registerSource(Source<DATA_OUT> source) {
		createStepListFor(source);
		return new SimpleStepKey<>(source);
	}

	@Override
	public StepKey<FileData> sourceFileSystem(String name, Path path) {
		return registerSource(new FileSource(name, path));
	}

	// transformers

	@Override
	public <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transform(StepKey<DATA_IN> previous, Transformer<DATA_IN, DATA_OUT> transformer, Predicate<Document<DATA_IN>> filter) {
		getStepListFor(previous).add(new FilteredStep<>(filter, transformer));
		ensureStepListFor(transformer);
		return new SimpleStepKey<>(transformer);
	}

	@Override
	public <DATA_IN extends Record & DataString, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transformMarkdown(StepKey<DATA_IN> previous, Class<DATA_OUT> frontMatterType, Predicate<Document<DATA_IN>> filter) {
		if (markdownParser.isEmpty())
			throw new IllegalStateException("Can't transform Markdown: No Markdown parser was created");

		var markupTransformer = new MarkupParsingTransformer<DATA_IN, DATA_OUT>(markdownParser.get(), frontMatterType);
		return transform(previous, markupTransformer, filter);
	}

	// store

	@Override
	public <DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous, Predicate<Document<DATA_IN>> filter) {
		var filteredStep = new FilteredStep<>(filter, store);
		getStepListFor(previous).add(filteredStep);
	}

	// build

	@Override
	public Outline build() {
		return new MapOutline(stepMap, store);
	}

	// misc

	private void createStepListFor(Source<?> source) {
		var previous = stepMap.put(source, new ArrayList<>());
		if (previous != null)
			throw new IllegalStateException("This source was already registered");
	}

	private void ensureStepListFor(Transformer<?, ?> transformer) {
		stepMap.putIfAbsent(transformer, new ArrayList<>());
	}

	private List<FilteredStep<?, ?>> getStepListFor(StepKey<?> previous) {
		if (!(previous instanceof SimpleStepKey<?>(Step<?> step)))
			throw new IllegalStateException("Unexpected implementation of " + StepKey.class.getSimpleName());

		var stepList = stepMap.get(step);
		if (stepList == null)
			throw new IllegalStateException("The specified previous step is unregistered");
		return stepList;
	}

	private record SimpleStepKey<DATA_OUT extends Record & Data>(Step<DATA_OUT> step) implements StepKey<DATA_OUT> { }

}
