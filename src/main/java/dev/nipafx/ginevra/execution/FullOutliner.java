package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.ExecutionStep.FilterStep;
import dev.nipafx.ginevra.execution.ExecutionStep.GenerateResourcesStep;
import dev.nipafx.ginevra.execution.ExecutionStep.MergeSteps;
import dev.nipafx.ginevra.execution.ExecutionStep.SourceStep;
import dev.nipafx.ginevra.execution.ExecutionStep.StoreResourceStep;
import dev.nipafx.ginevra.execution.ExecutionStep.StoreStep;
import dev.nipafx.ginevra.execution.ExecutionStep.TemplateStep;
import dev.nipafx.ginevra.execution.ExecutionStep.TransformStep;
import dev.nipafx.ginevra.outline.BinaryFileData;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Document.StringData;
import dev.nipafx.ginevra.outline.FileDataStep;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Step;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.outline.TextFileData;
import dev.nipafx.ginevra.outline.TextFileDataStep;
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

	private final Map<ExecutionStep, List<ExecutionStep>> stepMap;

	public FullOutliner(Store store, Optional<MarkdownParser> markdownParser, Renderer renderer, Paths paths) {
		this.store = store;
		this.renderer = renderer;
		this.markdownParser = markdownParser;
		this.paths = paths;
		this.stepMap = new HashMap<>();
	}

	// sources

	@Override
	public <DATA_OUT extends Record & Data> Step<DATA_OUT> source(Source<DATA_OUT> source) {
		var step = new SourceStep<>(source);
		createStepListFor(step);
		return new SingleStep<>(this, step);
	}

	@Override
	public <DATA_OUT extends Record & Data> Step<DATA_OUT> source(DATA_OUT source) {
		return source(new RecordSource<>(source));
	}

	@Override
	public TextFileDataStep<TextFileData> sourceTextFiles(String name, Path path) {
		var step = new SourceStep<>(FileSource.forTextFiles(name, path));
		createStepListFor(step);
		return new SingleTextFileDataStep<>(this, step);
	}

	@Override
	public FileDataStep<BinaryFileData> sourceBinaryFiles(String name, Path path) {
		var step = new SourceStep<>(FileSource.forBinaryFiles(name, path));
		createStepListFor(step);
		return new SingleFileDataStep<>(this, step);
	}

	// transformers

	@Override
	public <DATA extends Record & Data> Step<DATA> filter(Step<DATA> previous, Predicate<DATA> filter) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new FilterStep(filter);
		appendStep(previous, next);
		return new SingleStep<>(this, next);
	}

	@Override
	public <DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformToMany(Step<DATA_IN> previous, Transformer<DATA_IN, DATA_OUT> transformer) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new TransformStep(transformer);
		appendStep(previous, next);
		return new SingleStep<>(this, next);
	}

	@Override
	public <DATA_IN extends Record & StringData, DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformMarkdown(Step<DATA_IN> previous, Class<DATA_OUT> frontMatterType) {
		if (markdownParser.isEmpty())
			throw new IllegalStateException("Can't transform Markdown: No Markdown parser was created");

		var markupTransformer = new MarkupParsingTransformer<DATA_IN, DATA_OUT>(markdownParser.get(), frontMatterType);
		return transformToMany(previous, markupTransformer);
	}

	@Override
	public <DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data>
	Step<DATA_OUT> merge(Step<DATA_IN_1> previous1, Step<DATA_IN_2> previous2, Merger<DATA_IN_1, DATA_IN_2, DATA_OUT> merger) {
		var next = MergeSteps.create(merger);
		appendStep(previous1, next.one());
		appendStep(previous2, next.two());
		return new PairStep<>(this, next.one(), next.two());
	}

	// store

	@Override
	public <DATA_IN extends Record & Data> void store(Step<DATA_IN> previous, String collection) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new StoreStep(Optional.of(collection));
		appendStep(previous, next);
	}

	@Override
	public <DATA_IN extends Record & Data> void store(Step<DATA_IN> previous) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		var next = new StoreStep(Optional.empty());
		appendStep(previous, next);
	}

	@Override
	public <DATA_IN extends Record & FileData> void storeResource(Step<DATA_IN> previous, Function<DATA_IN, String> naming) {
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

	@Override
	public void generateStaticResources(Path folder, String... resources) {
		var next = new GenerateResourcesStep(folder, List.of(resources));
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

	private void createStepListFor(GenerateResourcesStep step) {
		var previous = stepMap.put(step, List.of());
		if (previous != null)
			throw new IllegalArgumentException("This resource generation was already registered");
	}

	private void appendStep(Step<?> previous, ExecutionStep step) {
		stepMap.putIfAbsent(step, new ArrayList<>());
		switch (previous) {
			case SingleStep<?> previousSingle -> stepMap.get(previousSingle.step()).add(step);
			case PairStep<?> previousPair -> {
				stepMap.get(previousPair.step1()).add(step);
				stepMap.get(previousPair.step2()).add(step);
			}
			default -> {
				var message = "Unexpected implementation of `%s`: `%s`".formatted(Step.class.getSimpleName(), previous.getClass().getSimpleName());
				throw new IllegalStateException(message);
			}
		};
	}

	private static class SingleStep<DATA extends Record & Data> implements Step<DATA> {

		protected final FullOutliner outliner;
		private final ExecutionStep step;

		private SingleStep(FullOutliner outliner, ExecutionStep step) {
			this.outliner = outliner;
			this.step = step;
		}

		@Override
		public Step<DATA> filter(Predicate<DATA> filter) {
			return outliner.filter(this, filter);
		}

		@Override
		public <DATA_OUT1 extends Record & Data> Step<DATA_OUT1> transformToMany(Transformer<DATA, DATA_OUT1> transformer) {
			return outliner.transformToMany(this, transformer);
		}

		@Override
		public <OTHER_DATA extends Record & Data, DATA_OUT1 extends Record & Data> Step<DATA_OUT1> merge(Step<OTHER_DATA> other, Merger<DATA, OTHER_DATA, DATA_OUT1> merger) {
			return outliner.merge(this, other, merger);
		}

		@Override
		public void store(String collection) {
			outliner.store(this, collection);
		}

		@Override
		public void store() {
			outliner.store(this);
		}

		public ExecutionStep step() {
			return step;
		}

	}

	private static class SingleFileDataStep<DATA extends Record & FileData> extends SingleStep<DATA> implements FileDataStep<DATA> {

		private SingleFileDataStep(FullOutliner outliner, ExecutionStep step) {
			super(outliner, step);
		}

		@Override
		public void storeResource(Function<DATA, String> naming) {
			outliner.storeResource(this, naming);
		}

	}

	private static class SingleTextFileDataStep<DATA extends Record & FileData & StringData> extends SingleFileDataStep<DATA> implements TextFileDataStep<DATA> {

		private SingleTextFileDataStep(FullOutliner outliner, ExecutionStep step) {
			super(outliner, step);
		}


		@Override
		public <DATA_OUT extends Record & Data> Step<DATA_OUT> transformMarkdown(Class<DATA_OUT> frontMatterType) {
			return outliner.transformMarkdown(this, frontMatterType);
		}

	}

	private static class PairStep<DATA extends Record & Data> implements Step<DATA> {

		protected final FullOutliner outliner;
		private final ExecutionStep step1;
		private final ExecutionStep step2;

		private PairStep(FullOutliner outliner, ExecutionStep step1, ExecutionStep step2) {
			this.outliner = outliner;
			this.step1 = step1;
			this.step2 = step2;
		}

		@Override
		public Step<DATA> filter(Predicate<DATA> filter) {
			return outliner.filter(this, filter);
		}

		@Override
		public <DATA_OUT1 extends Record & Data> Step<DATA_OUT1> transformToMany(Transformer<DATA, DATA_OUT1> transformer) {
			return outliner.transformToMany(this, transformer);
		}

		@Override
		public <OTHER_DATA extends Record & Data, DATA_OUT1 extends Record & Data> Step<DATA_OUT1> merge(Step<OTHER_DATA> other, Merger<DATA, OTHER_DATA, DATA_OUT1> merger) {
			return outliner.merge(this, other, merger);
		}

		@Override
		public void store(String collection) {
			outliner.store(this, collection);
		}

		@Override
		public void store() {
			outliner.store(this);
		}

		public ExecutionStep step1() {
			return step1;
		}

		public ExecutionStep step2() {
			return step2;
		}

	}

}
