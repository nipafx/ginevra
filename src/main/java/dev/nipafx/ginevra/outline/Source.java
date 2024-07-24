package dev.nipafx.ginevra.outline;

import java.util.List;
import java.util.function.Consumer;

/**
 * A source will load files (e.g. Markdown or JSON), query external services, etc.
 * and provide them as {@link Document documents}.
 */
public interface Source<DOCUMENT_OUT extends Record & Document> {

	List<Envelope<DOCUMENT_OUT>> loadAll();

	void observeChanges(Consumer<SourceEvent> listener);

	void stopObservation();

}
