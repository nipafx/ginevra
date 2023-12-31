package dev.nipafx.ginevra.outline;

/**
 * A step in the {@link Outline}.
 */
public sealed interface Step<DATA_OUT extends Record> permits Source, Transformer, Store { }
