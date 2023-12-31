package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.SourceId;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;

class FileSource implements Source<FileData> {

	private final String name;
	private final Path path;
	private final List<Consumer<Document<FileData>>> consumers;

	public FileSource(String name, Path path) {
		this.name = name;
		this.path = path;
		this.consumers = new ArrayList<>();
	}

	@Override
	public void register(Consumer<Document<FileData>> consumer) {
		consumers.add(consumer);
	}

	@Override
	public void loadAll() {
		var files = Files.isDirectory(path)
				? loadAllFromDirectory(path)
				: List.of(loadFile(path));
		files.forEach(file -> consumers.forEach(consumer -> consumer.accept(file)));
	}

	private List<Document<FileData>> loadAllFromDirectory(Path directory) {
		try (var files = Files.walk(directory, 1)) {
			return files
					.filter(not(Files::isDirectory))
					.map(this::loadFile)
					.toList();
		} catch (IOException ex) {
			// do nothing
			return List.of();
		}
	}

	private Document<FileData> loadFile(Path file) {
		var id = new SourceId(STR."FileSystem: '\{name}'", file.toUri());
		try {
			var data = new FileData(file, Files.readString(file));
			return new GeneralDocument<>(id, data);
		} catch (IOException ex) {
			var data = new FileData(file, ex.getMessage());
			return new GeneralDocument<>(id, data);
		}
	}

}
