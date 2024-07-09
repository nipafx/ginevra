package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.DocumentId;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.SourceEvent;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

class RecordSource<DOCUMENT extends Record & Document> implements Source<DOCUMENT> {

	private final Envelope<DOCUMENT> data;

	public RecordSource(DOCUMENT data) {
		this.data = new SimpleEnvelope<>(
				DocumentId.sourcedFrom("record-instance", URI.create(data.getClass().getName())),
				data);
	}

	@Override
	public void onChange(Consumer<SourceEvent<DOCUMENT>> listener) {
		// this source is immutable and thus no changes can be observed
	}

	@Override
	public List<Envelope<DOCUMENT>> loadAll() {
		return List.of(data);
	}

}
