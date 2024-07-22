package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.SenderId;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.SourceEvent;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

class RecordSource<DOCUMENT extends Record & Document> implements Source<DOCUMENT> {

	private final Envelope<DOCUMENT> data;

	RecordSource(DOCUMENT data) {
		this.data = new SimpleEnvelope<>(
				SenderId.source("record-instance", URI.create(data.getClass().getName())),
				List.of(data));
	}

	@Override
	public void onChange(Consumer<SourceEvent> listener) {
		// this source is immutable and thus no changes can be observed
	}

	@Override
	public List<Envelope<DOCUMENT>> loadAll() {
		return List.of(data);
	}

}
