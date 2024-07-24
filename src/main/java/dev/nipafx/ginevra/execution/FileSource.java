package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.BinaryFileData;
import dev.nipafx.ginevra.outline.Envelope;
import dev.nipafx.ginevra.outline.FileDocument;
import dev.nipafx.ginevra.outline.SenderId;
import dev.nipafx.ginevra.outline.SimpleEnvelope;
import dev.nipafx.ginevra.outline.Source;
import dev.nipafx.ginevra.outline.SourceEvent;
import dev.nipafx.ginevra.outline.SourceEvent.Added;
import dev.nipafx.ginevra.outline.SourceEvent.Changed;
import dev.nipafx.ginevra.outline.SourceEvent.Removed;
import dev.nipafx.ginevra.outline.TextFileData;
import dev.nipafx.ginevra.util.FileSystemUtils;
import dev.nipafx.ginevra.util.FileWatchEvent;
import dev.nipafx.ginevra.util.FileWatch;

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
	private final List<Consumer<SourceEvent>> listeners;

	private Optional<FileWatch> currentWatch;

	private FileSource(String name, Path path, FileLoader<DOCUMENT> loader) {
		this.name = name;
		this.path = path;
		this.loader = loader;
		this.listeners = new ArrayList<>();
		this.currentWatch = Optional.empty();
	}

	static FileSource<TextFileData> forTextFiles(String name, Path path) {
		return new FileSource<>(name, path, file -> new TextFileData(file, Files.readString(file)));
	}

	static FileSource<BinaryFileData> forBinaryFiles(String name, Path path) {
		return new FileSource<>(name, path, file -> new BinaryFileData(file, Files.readAllBytes(file)));
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
		var id = createIdFor(file);
		try {
			var data = loader.load(file);
			return Optional.of(new SimpleEnvelope<>(id, List.of(data)));
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
			return Optional.empty();
		}
	}

	private SenderId createIdFor(Path file) {
		return SenderId.source("FileSystem: '%s'".formatted(name), file.toUri());
	}

	@Override
	public void observeChanges(Consumer<SourceEvent> listener) {
		listeners.add(listener);
		try {
			if (currentWatch.isPresent())
				throw new IllegalStateException("Changes are already being observed");
			var watch = FileSystemUtils.watchFolder(path, this::processFileEvent);
			currentWatch = Optional.of(watch);
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
		}
	}

	private void processFileEvent(FileWatchEvent event) {
		createSourceEvent(event).ifPresent(this::raiseEvent);
	}

	private Optional<SourceEvent> createSourceEvent(FileWatchEvent event) {
		// don't try to draw conclusions from the kind of file system entry the path is referencing
		// (e.g. a directory or a "regular" file) as in the case of a deletion, nothing meaningful
		// can be determined
		if (FileSystemUtils.isTemporaryChange(event))
			return Optional.empty();

		var file = event.path();
		return switch (event.kind()) {
			case CREATED -> loadFile(file).map(Added::new);
			case MODIFIED -> loadFile(file).map(Changed::new);
			case DELETED -> Optional.of(new Removed(createIdFor(file)));
		};
	}

	private void raiseEvent(SourceEvent event) {
		listeners.forEach(listener -> listener.accept(event));
	}

	@Override
	public void stopObservation() {
		currentWatch
				.orElseThrow(() -> new IllegalStateException("No ongoing observation"))
				.stopObservation();
		currentWatch = Optional.empty();
	}

	interface FileLoader<DOCUMENT extends Record & FileDocument> {

		DOCUMENT load(Path file) throws IOException;

	}

}
