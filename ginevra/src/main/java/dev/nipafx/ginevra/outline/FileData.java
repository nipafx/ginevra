package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.DataString;

import java.nio.file.Path;

public record FileData(Path file, String content) implements DataString {

	@Override
	public String dataAsString() {
		return content;
	}

}
