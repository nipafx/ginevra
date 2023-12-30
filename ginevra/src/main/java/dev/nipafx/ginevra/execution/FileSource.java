package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Source;

import java.nio.file.Path;
import java.util.function.Consumer;

class FileSource implements Source {

	private final Path path;

	public FileSource(Path path) {
		this.path = path;
	}

	@Override
	public void register(Consumer<Document> consumer) {

	}

	@Override
	public void loadAll() {

	}

}
