package dev.nipafx.ginevra.outline;

import java.util.function.Function;

public interface FileStep<DOCUMENT extends Record & FileDocument> extends Step<DOCUMENT> {

	default void storeResource() {
		storeResource(fileDoc -> fileDoc.file().getFileName().toString());
	}

	void storeResource(Function<DOCUMENT, String> naming);

}
