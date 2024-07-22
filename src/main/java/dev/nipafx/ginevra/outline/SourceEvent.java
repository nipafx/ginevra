package dev.nipafx.ginevra.outline;

public sealed interface SourceEvent {

	record Added(Envelope<?> envelope) implements SourceEvent { }

	record Changed(Envelope<?> envelope) implements SourceEvent { }

	record Removed(SenderId id) implements SourceEvent { }

}
