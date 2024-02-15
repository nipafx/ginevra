package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.Step.SourceStep;
import dev.nipafx.ginevra.execution.Step.StoreStep;
import dev.nipafx.ginevra.execution.Step.TemplateStep;
import dev.nipafx.ginevra.execution.Step.TransformStep;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.DataString;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Store.DocCollection;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.outline.Transformer;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.render.Renderer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class FullOutliner implements Outliner {

	private final Store store;
	private final Optional<MarkdownParser> markdownParser;
	private final Renderer renderer;
	private final Paths paths;

	private final Map<Step, List<Step>> stepMap;

	public FullOutliner(Store store, Optional<MarkdownParser> markdownParser, Renderer renderer, Paths paths) {
		this.store = store;
		this.renderer = renderer;
		this.markdownParser = markdownParser;
		this.paths = paths;
		this.stepMap = new HashMap<>();
	}

	// sources

	@Override
	public <DATA_OUT extends Record & Data> StepKey<DATA_OUT>
	source(Source<DATA_OUT> source) {
		var step = new SourceStep<>(source);
		createStepListFor(step);
		return new SimpleStepKey<>(step);
	}

	@Override
	public StepKey<FileData> sourceFileSystem(String name, Path path) {
		return source(new FileSource(name, path));
	}

	// transformers

	@Override
	public <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transform(StepKey<DATA_IN> previous, Transformer<DATA_IN, DATA_OUT> transformer, Predicate<Document<DATA_IN>> filter) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var step = new TransformStep(filter, transformer);
		getStepListFor(previous).add(step);
		ensureStepListFor(step);
		return new SimpleStepKey<>(step);
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
	void store(StepKey<DATA_IN> previous, DocCollection collection, Predicate<Document<DATA_IN>> filter) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var step = new StoreStep(filter, Optional.of(collection));
		getStepListFor(previous).add(step);
	}

	@Override
	public <DATA_IN extends Record & Data>
	void store(StepKey<DATA_IN> previous, Predicate<Document<DATA_IN>> filter) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var step = new StoreStep(filter, Optional.empty());
		getStepListFor(previous).add(step);
	}

	// build

	@Override
	public Outline build() {
		return new MapOutline(stepMap, store, renderer, paths);
	}

	// generate

	@Override
	public <DATA extends Record & Data>
	void generate(Store.Query<DATA> query, Predicate<Document<DATA>> filter, Template<DATA> template) {
		var step = new TemplateStep<>(query, filter, template);
		createStepListFor(step);
	}

	// misc

	private void createStepListFor(SourceStep<?> step) {
		var previous = stepMap.put(step, new ArrayList<>());
		if (previous != null)
			throw new IllegalArgumentException("This source was already registered");
	}

	private void createStepListFor(TemplateStep<?> step) {
		var previous = stepMap.put(step, List.of());
		if (previous != null)
			throw new IllegalArgumentException("This template was already registered");
	}

	private void ensureStepListFor(TransformStep<?, ?> transformer) {
		stepMap.putIfAbsent(transformer, new ArrayList<>());
	}

	private List<Step> getStepListFor(StepKey<?> previous) {
		if (!(previous instanceof SimpleStepKey<?>(Step step)))
			throw new IllegalStateException("Unexpected implementation of " + StepKey.class.getSimpleName());

		var stepList = stepMap.get(step);
		if (stepList == null)
			throw new IllegalArgumentException("The specified previous step is unregistered");
		return stepList;
	}

	private record SimpleStepKey<DATA_OUT extends Record & Data>(Step step) implements StepKey<DATA_OUT> { }

}
