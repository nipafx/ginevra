package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Store.DocCollection;
import dev.nipafx.ginevra.outline.Store.Query;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.outline.Transformer;

import java.util.Optional;
import java.util.function.Predicate;

sealed interface Step {

	record SourceStep<DATA extends Record & Data>(
			Source<DATA> source) implements Step { }

	record TransformStep<DATA_IN extends Record & Data, DATA_OUT extends Record & Data>(
			Predicate<Document<DATA_IN>> filter, Transformer<DATA_IN, DATA_OUT> transformer) implements Step { }

	record StoreStep<DATA extends Record & Data>(
			Predicate<Document<DATA>> filter, Optional<DocCollection> collection) implements Step { }

	record TemplateStep<DATA extends Record & Data>(
			Query<DATA> query, Predicate<Document<DATA>> filter, Template<DATA> template) implements Step { }

}