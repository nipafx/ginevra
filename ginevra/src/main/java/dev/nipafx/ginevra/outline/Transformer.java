package dev.nipafx.ginevra.outline;

/**
 * Transforms {@link Document}s, for example when parsing them as Markdown.
 */
public non-sealed interface Transformer extends Step {

	Document transform(Document doc);

}
