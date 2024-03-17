package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Merger;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.outline.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

sealed interface Step {

	record SourceStep<DATA extends Record & Data>(
			Source<DATA> source) implements Step { }

	record TransformStep<DATA_IN extends Record & Data, DATA_OUT extends Record & Data>(
			Predicate<Document<DATA_IN>> filter, Transformer<DATA_IN, DATA_OUT> transformer) implements Step { }

	final class MergeSteps<DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data> {

		private final BiPredicate<Document<DATA_IN_1>, Document<DATA_IN_2>> filter;
		private final Merger<DATA_IN_1, DATA_IN_2, DATA_OUT> merger;
		private final List<Document<DATA_IN_1>> documents1;
		private final List<Document<DATA_IN_2>> documents2;
		private MergeStepOne<DATA_IN_1, DATA_IN_2, DATA_OUT> one;
		private MergeStepTwo<DATA_IN_1, DATA_IN_2, DATA_OUT> two;

		private MergeSteps(BiPredicate<Document<DATA_IN_1>, Document<DATA_IN_2>> filter, Merger<DATA_IN_1, DATA_IN_2, DATA_OUT> merger) {
			this.filter = filter;
			this.merger = merger;
			this.documents1 = new ArrayList<>();
			this.documents2 = new ArrayList<>();
		}

		public static <DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data>
		MergeSteps<DATA_IN_1, DATA_IN_2, DATA_OUT> create(
				BiPredicate<Document<DATA_IN_1>, Document<DATA_IN_2>> filter, Merger<DATA_IN_1, DATA_IN_2, DATA_OUT> merger) {
			var mergeStep = new MergeSteps<>(filter, merger);
			mergeStep.one = new MergeStepOne<>(mergeStep);
			mergeStep.two = new MergeStepTwo<>(mergeStep);
			return mergeStep;
		}

		private record Docs<DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data>(
				Document<DATA_IN_1> data1, Document<DATA_IN_2> data2) { }

		public MergeStepOne<DATA_IN_1, DATA_IN_2, DATA_OUT> one() {
			return one;
		}

		public MergeStepTwo<DATA_IN_1, DATA_IN_2, DATA_OUT> two() {
			return two;
		}

	}

	final class MergeStepOne<DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data> implements Step {

		private final MergeSteps<DATA_IN_1, DATA_IN_2, DATA_OUT> merge;

		public MergeStepOne(MergeSteps<DATA_IN_1, DATA_IN_2, DATA_OUT> merge) {
			this.merge = merge;
		}

		public Stream<Document<DATA_OUT>> merge(Document<DATA_IN_1> data1) {
			merge.documents1.add(data1);
			return merge.documents2.stream()
					.map(data2 -> new MergeSteps.Docs<>(data1, data2))
					.filter(docs -> merge.filter.test(docs.data1, docs.data2))
					.flatMap(docs -> merge.merger.merge(docs.data1, docs.data2).stream());
		}

	}

	final class MergeStepTwo<DATA_IN_1 extends Record & Data, DATA_IN_2 extends Record & Data, DATA_OUT extends Record & Data>
			implements Step {

		private final MergeSteps<DATA_IN_1, DATA_IN_2, DATA_OUT> merge;

		public MergeStepTwo(MergeSteps<DATA_IN_1, DATA_IN_2, DATA_OUT> merge) {
			this.merge = merge;
		}

		public Stream<Document<DATA_OUT>> merge(Document<DATA_IN_2> data2) {
			merge.documents2.add(data2);
			return merge.documents1.stream()
					.map(data1 -> new MergeSteps.Docs<>(data1, data2))
					.filter(docs ->merge.filter.test(docs.data1, docs.data2))
					.flatMap(docs ->merge.merger.merge(docs.data1, docs.data2).stream());
		}

	}

	record StoreStep<DATA extends Record & Data>(
			Predicate<Document<DATA>> filter, Optional<String> collection) implements Step { }

	record TemplateStep<DATA extends Record & Data>(
			Template<DATA> template) implements Step { }

}