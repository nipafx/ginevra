package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.BinaryFileData;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Document.SourceId;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.TextFileData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;

class FileSource<DATA extends Record & FileData> implements Source<DATA> {

	private final String name;
	private final Path path;
	private final FileLoader<DATA> loader;
	private final List<Consumer<Document<DATA>>> consumers;

	private FileSource(String name, Path path, FileLoader<DATA> loader) {
		this.name = name;
		this.path = path;
		this.loader = loader;
		this.consumers = new ArrayList<>();
	}

	public static FileSource<TextFileData> forTextFiles(String name, Path path) {
		return new FileSource<>(name, path, file -> new TextFileData(file, Files.readString(file)));
	}

	public static FileSource<BinaryFileData> forBinaryFiles(String name, Path path) {
		return new FileSource<>(name, path, file -> new BinaryFileData(file, Files.readAllBytes(file)));
	}

	@Override
	public void register(Consumer<Document<DATA>> consumer) {
		consumers.add(consumer);
	}

	@Override
	public void loadAll() {
		var files = Files.isDirectory(path)
				? loadAllFromDirectory(path)
				: loadFile(path).stream().toList();
		files.forEach(file -> consumers.forEach(consumer -> consumer.accept(file)));
	}

	private List<Document<DATA>> loadAllFromDirectory(Path directory) {
		try (var files = Files.walk(directory, 1)) {
			return files
					.filter(not(Files::isDirectory))
					.filter(file -> !file.getFileName().toString().startsWith("."))
					.map(this::loadFile)
					.flatMap(Optional::stream)
					.toList();
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
			return List.of();
		}
	}

	private Optional<Document<DATA>> loadFile(Path file) {
		var id = new SourceId(STR."FileSystem: '\{name}'", file.toUri());
		try {
			var data = loader.load(file);
			return Optional.of(new GeneralDocument<>(id, data));
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
			return Optional.empty();
		}
	}

	interface FileLoader<DATA extends Record & FileData> {

		DATA load(Path file) throws IOException;

	}

}
