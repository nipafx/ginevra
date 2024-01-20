package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Store.DocCollection;
import dev.nipafx.ginevra.outline.Transformer;

import java.util.Optional;
import java.util.function.Predicate;

sealed interface Step {

	record SourceStep(Source<?> source) implements Step { }

	record TransformStep(Predicate<Document<?>> filter, Transformer<?, ?> transformer) implements Step { }

	record StoreStep(Predicate<Document<?>> filter, Optional<DocCollection> collection) implements Step { }

}