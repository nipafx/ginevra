package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.FileData;

import java.util.function.Function;

public interface FileDataStep<DATA extends Record & FileData> extends Step<DATA> {

	default void storeResource() {
		storeResource(fileData -> fileData.file().getFileName().toString());
	}

	void storeResource(Function<DATA, String> naming);

}
