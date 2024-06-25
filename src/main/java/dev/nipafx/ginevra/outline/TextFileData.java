package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Document.StringData;

import java.nio.file.Path;

public record TextFileData(Path file, String content) implements FileData, StringData {

	@Override
	public String dataAsString() {
		return content;
	}

}
