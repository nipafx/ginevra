package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.execution.Step.SourceStep;
import dev.nipafx.ginevra.execution.Step.StoreStep;
import dev.nipafx.ginevra.execution.Step.TemplateStep;
import dev.nipafx.ginevra.execution.Step.TransformStep;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Store.CollectionQuery;
import dev.nipafx.ginevra.outline.Store.RootQuery;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.render.CssFile;
import dev.nipafx.ginevra.render.Renderer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
		var sourceSteps = stepMap
				.keySet().stream()
				.filter(SourceStep.class::isInstance)
				.map(SourceStep.class::cast)
				.toList();

		sourceSteps.forEach(step -> step.source().register(doc -> process(step, (Document<?>) doc)));
		sourceSteps.forEach(step -> step.source().loadAll());

		try {
			paths.createFolders();
			stepMap
					.keySet().stream()
					.flatMap(keepOnly(TemplateStep.class))
					.flatMap(this::generateFromTemplate)
					// TODO: find out why this cast is needed
					.forEach((Consumer<TemplatedFile>) this::writeContentFile);
			renderer
					.cssFiles()
					.forEach(this::writeCssFile);
		} catch (IOException | UncheckedIOException ex) {
		}
	}

	private void process(SourceStep<?> sourceStep, Document<?> doc) {
		processRecursively(sourceStep, doc);
		store.commit();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
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
								.transform(doc)
								.forEach(transformedDoc -> processRecursively(transformStep, (Document<?>) transformedDoc));
				}
				case StoreStep(var filter, var collection) -> {
					if (((Predicate) filter).test(doc))
						collection.ifPresentOrElse(
								col -> store.store(col, doc),
								() -> store.store(doc)
						);
				}
				case TemplateStep _ -> throw new IllegalStateException("No step should map to a template");
			}
		});
	}

	private <DATA extends Record & Data> Stream<TemplatedFile> generateFromTemplate(TemplateStep<DATA> templateStep) {
		return switch (templateStep.query()) {
			case CollectionQuery<DATA> collectionQuery -> store
					.query(collectionQuery).stream()
					.filter(result -> templateStep.filter().test(result))
					.map(document -> generateFromTemplate(templateStep.template(), document));
			case RootQuery<DATA> rootQuery -> {
				var result = store.query(rootQuery);
				yield templateStep.filter().test(result)
						? Stream.of(generateFromTemplate(templateStep.template(), result))
						: Stream.empty();
			}
		};
	}

	private <DATA extends Record & Data> TemplatedFile generateFromTemplate(Template<DATA> template, Document<DATA> document) {
		var renderedId = document.id().transform("template-" + template.getClass().getName());

		var renderedDocument = template.render(document.data());
		var fileContent = renderer.render(renderedDocument.html());
		var filePath = paths.siteFolder().resolve(renderedDocument.slug() + ".html").toAbsolutePath();

		return new TemplatedFile(filePath, fileContent);
	}

	private void writeContentFile(TemplatedFile file) {
		writeToFile(file.file(), file.content());
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
			ex.printStackTrace();
		}
	}

	private record TemplatedFile(Path file, String content) { }

}
