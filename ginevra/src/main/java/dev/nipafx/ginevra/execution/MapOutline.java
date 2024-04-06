package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.Step.FilterStep;
import dev.nipafx.ginevra.execution.Step.MergeStepOne;
import dev.nipafx.ginevra.execution.Step.MergeStepTwo;
import dev.nipafx.ginevra.execution.Step.SourceStep;
import dev.nipafx.ginevra.execution.Step.StoreResourceStep;
import dev.nipafx.ginevra.execution.Step.StoreStep;
import dev.nipafx.ginevra.execution.Step.TemplateStep;
import dev.nipafx.ginevra.execution.Step.TransformStep;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Query.CollectionQuery;
import dev.nipafx.ginevra.outline.Query.RootQuery;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.render.Renderer;
import dev.nipafx.ginevra.render.ResourceFile;
import dev.nipafx.ginevra.render.ResourceFile.CopiedFile;
import dev.nipafx.ginevra.render.ResourceFile.CssFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.util.StreamUtils.keepOnly;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toUnmodifiableMap;

class MapOutline implements Outline {

	private final Map<Step, List<Step>> stepMap;
	private final Store store;
	private final Renderer renderer;
	private final Paths paths;

	MapOutline(Map<Step, List<Step>> stepMap, Store store, Renderer renderer, Paths paths) {
		this.stepMap = stepMap
				.entrySet().stream()
				.map(entry -> entry(entry.getKey(), List.copyOf(entry.getValue())))
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
		this.store = store;
		this.renderer = renderer;
		this.paths = paths;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		runOutlineUntilStorage();
		renderTemplates();
	}

	private void runOutlineUntilStorage() {
		var sourceSteps = stepMap
				.keySet().stream()
				.mapMulti(keepOnly(SourceStep.class))
				.toList();
		sourceSteps.forEach(step -> step.source().register(doc -> processRecursively(step, (Document<?>) doc)));
		sourceSteps.forEach(step -> step.source().loadAll());
	}

	private void renderTemplates() {
		try {
			paths.createFolders();
			stepMap
					.keySet().stream()
					.mapMulti(keepOnly(TemplateStep.class))
					.flatMap(this::generateFromTemplate)
					// TODO: find out why this cast is needed
					.forEach((Consumer<TemplatedFile>) this::writeTemplatedFile);
		} catch (IOException | UncheckedIOException ex) {
			// TODO: handle error
			ex.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processRecursively(Step origin, Document<?> doc) {
		var steps = stepMap.get(origin);
		if (steps == null)
			throw new IllegalStateException("Unknown step triggered document processing");

		steps.forEach(step -> {
			switch (step) {
				case SourceStep _ -> throw new IllegalStateException("No step should map to a source");
				case FilterStep filterStep -> {
					if (filterStep.filter().test(doc.data()))
						processRecursively(filterStep, doc);
				}
				case TransformStep transformStep -> transformStep
						.transformer()
						.transform(doc)
						.forEach(transformedDoc -> processRecursively(transformStep, (Document<?>) transformedDoc));
				case MergeStepOne mergeStepOne -> mergeStepOne
						.merge(doc)
						.forEach(mergedDoc -> processRecursively(mergeStepOne, (Document<?>) mergedDoc));
				case MergeStepTwo mergeStepTwo -> mergeStepTwo
						.merge(doc)
						.forEach(mergedDoc -> processRecursively(mergeStepTwo, (Document<?>) mergedDoc));
				case StoreStep(var collection) -> collection.ifPresentOrElse(
						col -> store.store(col, doc),
						() -> store.store(doc)
				);
				case StoreResourceStep(var naming) -> {
					var name = (String) ((Function) naming).apply(doc.data());
					var fileDoc = (Document<? extends FileData>) doc;
					store.storeResource(name, fileDoc);
				}
				case TemplateStep _ -> throw new IllegalStateException("No step should map to a template");
			}
		});
	}

	private <DATA extends Record & Data> Stream<TemplatedFile> generateFromTemplate(TemplateStep<DATA> templateStep) {
		var template = templateStep.template();
		var results = switch (template.query()) {
			case CollectionQuery<DATA> collectionQuery -> store
					.query(collectionQuery).stream()
					.filter(result -> collectionQuery.filter().test(result.data()));
			case RootQuery<DATA> rootQuery -> Stream.of(store.query(rootQuery));
		};
		return results
				.map(document -> generateFromTemplate(template, document));
	}

	private <DATA extends Record & Data> TemplatedFile generateFromTemplate(Template<DATA> template, Document<DATA> document) {
		var id = document.id().transform("template-" + template.getClass().getName());

		var composedDocument = template.compose(document.data());
		var fileContent = renderer.renderAsDocument(composedDocument.html(), template);
		var filePath = paths.siteFolder().resolve(composedDocument.slug()).resolve("index.html").toAbsolutePath();

		return new TemplatedFile(filePath, fileContent.html(), fileContent.referencedResources());
	}

	private void writeTemplatedFile(TemplatedFile file) {
		writeToFile(file.file(), file.content());
		file
				.referencedResources()
				.forEach(res -> {
					switch (res) {
						case CopiedFile copiedFile -> copyFile(copiedFile);
						case CssFile cssFile -> writeCssFile(cssFile);
					}
				});
	}

	private void copyFile(CopiedFile copiedFile) {
		var target = paths.siteFolder().resolve(copiedFile.target());
		try {
			if (!Files.exists(target))
				Files.copy(copiedFile.source(), target);
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
		}
	}

	private void writeCssFile(CssFile cssFile) {
		var file = paths.siteFolder().resolve(cssFile.file()).toAbsolutePath();
		writeToFile(file, cssFile.content());
	}

	private static void writeToFile(Path filePath, String fileContent) {
		try {
			Files.createDirectories(filePath.getParent());
			Files.deleteIfExists(filePath);
			Files.writeString(filePath, fileContent);
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
		}
	}

	private record TemplatedFile(Path file, String content, Set<ResourceFile> referencedResources) { }

}
