package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.Step.FilterStep;
import dev.nipafx.ginevra.execution.Step.MergeSteps;
import dev.nipafx.ginevra.execution.Step.SourceStep;
import dev.nipafx.ginevra.execution.Step.StoreResourceStep;
import dev.nipafx.ginevra.execution.Step.StoreStep;
import dev.nipafx.ginevra.execution.Step.TemplateStep;
import dev.nipafx.ginevra.execution.Step.TransformStep;
import dev.nipafx.ginevra.outline.BinaryFileData;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Document.StringData;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.outline.TextFileData;
import dev.nipafx.ginevra.outline.Transformer;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.render.Renderer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
	public <DATA_OUT extends Record & Data> StepKey<DATA_OUT> source(Source<DATA_OUT> source) {
		var step = new SourceStep<>(source);
		createStepListFor(step);
		return new SingleStepKey<>(step);
	}

	@Override
	public <DATA_OUT extends Record & Data> StepKey<DATA_OUT> source(DATA_OUT source) {
		return source(new RecordSource<>(source));
	}

	@Override
	public StepKey<TextFileData> sourceTextFiles(String name, Path path) {
		return source(FileSource.forTextFiles(name, path));
	}

	@Override
	public StepKey<BinaryFileData> sourceBinaryFiles(String name, Path path) {
		return source(FileSource.forBinaryFiles(name, path));
	}

	// transformers

	@Override
	public <DATA extends Record & Data> StepKey<DATA> filter(StepKey<DATA> previous, Predicate<DATA> filter) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new FilterStep(filter);
		appendStep(previous, next);
		return new SingleStepKey<>(next);
	}

	@Override
	public <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transform(StepKey<DATA_IN> previous, Transformer<DATA_IN, DATA_OUT> transformer) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new TransformStep(transformer);
		appendStep(previous, next);
		return new SingleStepKey<>(next);
	}

	@Override
	public <DATA_IN extends Record & StringData, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> transformMarkdown(StepKey<DATA_IN> previous, Class<DATA_OUT> frontMatterType) {
		if (markdownParser.isEmpty())
			throw new IllegalStateException("Can't transform Markdown: No Markdown parser was created");

		var markupTransformer = new MarkupParsingTransformer<DATA_IN, DATA_OUT>(markdownParser.get(), frontMatterType);
		return transform(previous, markupTransformer);
	}

	@Override
	public <DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data>
	StepKey<DATA_OUT> merge(StepKey<DATA_IN_1> previous1, StepKey<DATA_IN_2> previous2, Merger<DATA_IN_1, DATA_IN_2, DATA_OUT> merger) {
		var next = MergeSteps.create(merger);
		appendStep(previous1, next.one());
		appendStep(previous2, next.two());
		return new PairStepKey<>(next.one(), next.two());
	}

	// store

	@Override
	public <DATA_IN extends Record & Data> void store(StepKey<DATA_IN> previous, String collection) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new StoreStep(Optional.of(collection));
		appendStep(previous, next);
	}

	@Override
	public <DATA_IN extends Record & Data> void store(StepKey<DATA_IN> previous) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new StoreStep(Optional.empty());
		appendStep(previous, next);
	}

	@Override
	public <DATA_IN extends Record & FileData> void storeResource(StepKey<DATA_IN> previous, Function<DATA_IN, String> naming) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new StoreResourceStep(naming);
		appendStep(previous, next);
	}

	// build

	@Override
	public Outline build() {
		return new MapOutline(stepMap, store, renderer, paths);
	}

	// generate

	@Override
	public <DATA extends Record & Data>
	void generate(Template<DATA> template) {
		var next = new TemplateStep<>(template);
		createStepListFor(next);
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

	private void appendStep(StepKey<?> previous, Step step) {
		stepMap.putIfAbsent(step, new ArrayList<>());
		switch (previous) {
			case SingleStepKey<?>(Step prev) -> stepMap.get(prev).add(step);
			case PairStepKey<?>(Step prev1, Step prev2) -> {
				stepMap.get(prev1).add(step);
				stepMap.get(prev2).add(step);
			}
			default -> {
				var message = STR."Unexpected implementation of `\{StepKey.class.getSimpleName()}`: `\{previous.getClass().getSimpleName()}`";
				throw new IllegalStateException(message );
			}
		};
	}

	private record SingleStepKey<DATA_OUT extends Record & Data>(Step step) implements StepKey<DATA_OUT> { }
	private record PairStepKey<DATA_OUT extends Record & Data>(Step step1, Step step2) implements StepKey<DATA_OUT> { }

}
