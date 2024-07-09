package dev.nipafx.ginevra.outline;

import java.nio.file.Path;

public record TextFileData(Path file, String content) implements FileDocument, StringDocument {

	@Override
	public String dataAsString() {
		return content;
	}

}
