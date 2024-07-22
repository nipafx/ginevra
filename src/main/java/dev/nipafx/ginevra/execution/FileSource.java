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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.function.Predicate.not;

class FileSource<DOCUMENT extends Record & FileDocument> implements Source<DOCUMENT> {

	private final String name;
	private final Path path;
	private final FileLoader<DOCUMENT> loader;
	private final List<Consumer<SourceEvent>> listeners;

	private FileSource(String name, Path path, FileLoader<DOCUMENT> loader) {
		this.name = name;
		this.path = path;
		this.loader = loader;
		this.listeners = new ArrayList<>();
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
	public void onChange(Consumer<SourceEvent> listener) {
		listeners.add(listener);

		try {
			var watcher = FileSystems.getDefault().newWatchService();
			path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
			Thread
					.ofVirtual()
					.name("watcher: " + path)
					.start(() -> watchForChanges(watcher));
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
		}
	}

	private void watchForChanges(WatchService watcher) {
		try {
			var valid = true;
			while (valid) {
				var watchKey = watcher.take();
				// This sleep effectively groups events:
				// IDEs and text editors may write file content and meta information (like the edit date) separately,
				// which can lead to two separate events in quick succession. By sleeping for a short period,
				// these writes will appear as a single event with a WatchEvent.count() > 1.
				Thread.sleep(50);
				for (var fileEvent : watchKey.pollEvents())
					processFileEvent(fileEvent).ifPresent(this::raiseEvent);
				valid = watchKey.reset();
			}
		} catch (InterruptedException ex) {
			// if the thread is interrupted, exit the loop (and let the thread die)
		}
	}

	private Optional<SourceEvent> processFileEvent(WatchEvent<?> fileEvent) {
		if (fileEvent.kind() == OVERFLOW)
			return Optional.empty();

		@SuppressWarnings("unchecked")
		var file = path.resolve(((WatchEvent<Path>) fileEvent).context());
		if (isTemporaryChange(fileEvent, file))
			return Optional.empty();

		if (fileEvent.kind() == ENTRY_CREATE)
			return loadFile(file).map(Added::new);
		if (fileEvent.kind() == ENTRY_MODIFY)
			return loadFile(file).map(Changed::new);
		if (fileEvent.kind() == ENTRY_DELETE)
			return Optional.of(new Removed(createIdFor(file)));

		throw new IllegalStateException("This code should be unreachable");
	}

	private static boolean isTemporaryChange(WatchEvent<?> event, Path file) {
		if (file.getFileName().startsWith("."))
			return true;
		if (file.toString().endsWith("~"))
			return true;

		return false;
	}

	private void raiseEvent(SourceEvent event) {
		listeners.forEach(listener -> listener.accept(event));
	}

	interface FileLoader<DOCUMENT extends Record & FileDocument> {

		DOCUMENT load(Path file) throws IOException;

	}

}
