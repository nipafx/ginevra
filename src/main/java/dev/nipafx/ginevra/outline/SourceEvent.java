package dev.nipafx.ginevra.outline;

public sealed interface SourceEvent<DOCUMENT extends Record & Document> {

	record NewDocument<DOCUMENT extends Record & Document>(Envelope<DOCUMENT> envelope) implements SourceEvent<DOCUMENT> { }

	record DocumentChanged<DOCUMENT extends Record & Document>(Envelope<DOCUMENT> envelope) implements SourceEvent<DOCUMENT> { }

	record DocumentDeleted<DOCUMENT extends Record & Document>(DocumentId id) implements SourceEvent<DOCUMENT> { }

}
