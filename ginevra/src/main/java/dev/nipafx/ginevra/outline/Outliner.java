package dev.nipafx.ginevra.outline;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Use it to build your {@link Outline}.
 */
public interface Outliner {

	StepKey registerSource(Source source);

	StepKey sourceFileSystem(Path path);

	// transformers
	StepKey transform(StepKey previous, Transformer transformer, Predicate<Document> filter);

	default StepKey transform(StepKey previous, Transformer transformer) {
		return transform(previous, transformer, _ -> true);
	}

	default StepKey transformEach(
			StepKey previous,
			UnaryOperator<Document> transformer,
			Predicate<Document> filter) {
		return transform(previous, transformer::apply, filter);
	}

	default StepKey transformEach(StepKey previous, UnaryOperator<Document> transformer) {
		return transformEach(previous, transformer, _ -> true);
	}

	StepKey transformMarkdown(StepKey previous, Predicate<Document> filter);

	default StepKey transformMarkdown(StepKey previous) {
		return transformMarkdown(previous, _ -> true);
	}


	// store
	void store(StepKey previous, Predicate<Document> filter);

	default void store(StepKey previous) {
		store(previous, _ -> true);
	}

	// build

	Outline build();

	// inner

	interface StepKey { }

}
