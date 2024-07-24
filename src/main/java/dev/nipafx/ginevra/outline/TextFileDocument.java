package dev.nipafx.ginevra.outline;

import java.nio.file.Path;

public record TextFileDocument(Path file, String content) implements FileDocument, StringDocument {

	@Override
	public String documentAsString() {
		return content;
	}

}
