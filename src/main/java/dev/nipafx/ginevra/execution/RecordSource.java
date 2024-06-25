package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.SourceId;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Source;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class RecordSource<DATA extends Record & Data> implements Source<DATA> {

	private final Document<DATA> data;
	private final List<Consumer<Document<DATA>>> consumers;

	public RecordSource(DATA data) {
		this.data = new GeneralDocument<>(
				new SourceId("record-instance", URI.create(data.getClass().getName())),
				data);
		this.consumers = new ArrayList<>();
	}

	@Override
	public void register(Consumer<Document<DATA>> consumer) {
		consumers.add(consumer);
	}

	@Override
	public void loadAll() {
		consumers.forEach(consumer -> consumer.accept(data));
	}

}
