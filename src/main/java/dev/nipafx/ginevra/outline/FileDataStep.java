package dev.nipafx.ginevra.outline;

import java.util.function.Function;

public interface FileDataStep<DOCUMENT extends Record & FileDocument> extends Step<DOCUMENT> {

	default void storeResource() {
		storeResource(fileData -> fileData.file().getFileName().toString());
	}

	void storeResource(Function<DOCUMENT, String> naming);

}
