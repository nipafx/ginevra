package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.BinaryFileData;
import dev.nipafx.ginevra.outline.DocumentId;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.SourceEvent;
import dev.nipafx.ginevra.outline.TextFileData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.function.Predicate.not;

class FileSource<DOCUMENT extends Record & FileDocument> implements Source<DOCUMENT> {

	private final String name;
	private final Path path;
	private final FileLoader<DOCUMENT> loader;
	private final List<Consumer<SourceEvent<DOCUMENT>>> listeners;

	private FileSource(String name, Path path, FileLoader<DOCUMENT> loader) {
		this.name = name;
		this.path = path;
		this.loader = loader;
		this.listeners = new ArrayList<>();
	}

	public static FileSource<TextFileData> forTextFiles(String name, Path path) {
		return new FileSource<>(name, path, file -> new TextFileData(file, Files.readString(file)));
	}

	public static FileSource<BinaryFileData> forBinaryFiles(String name, Path path) {
		return new FileSource<>(name, path, file -> new BinaryFileData(file, Files.readAllBytes(file)));
	}

	@Override
	public void onChange(Consumer<SourceEvent<DOCUMENT>> listener) {
		// TODO: start observation
		listeners.add(listener);
	}

	@Override
	public List<Envelope<DOCUMENT>> loadAll() {
		return Files.isDirectory(path)
				? loadAllFromDirectory(path)
				: loadFile(path).stream().toList();
	}

	private List<Envelope<DOCUMENT>> loadAllFromDirectory(Path directory) {
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

	private Optional<Envelope<DOCUMENT>> loadFile(Path file) {
		var id = DocumentId.sourcedFrom("FileSystem: '%s'".formatted(name), file.toUri());
		try {
			var data = loader.load(file);
			return Optional.of(new SimpleEnvelope<>(id, data));
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
			return Optional.empty();
		}
	}

	interface FileLoader<DOCUMENT extends Record & FileDocument> {

		DOCUMENT load(Path file) throws IOException;

	}

}
