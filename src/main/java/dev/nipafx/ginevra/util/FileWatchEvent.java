package dev.nipafx.ginevra.util;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Optional;

import static dev.nipafx.ginevra.util.FileWatchEventKind.CREATED;
import static dev.nipafx.ginevra.util.FileWatchEventKind.DELETED;
import static dev.nipafx.ginevra.util.FileWatchEventKind.MODIFIED;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.Objects.requireNonNull;

public record FileWatchEvent(FileWatchEventKind kind, Path path) {

	public FileWatchEvent {
		requireNonNull(kind);
		requireNonNull(path);
	}

	static Optional<FileWatchEvent> fromWatchEvent(WatchEvent<?> event, Path parent) {
		if (event.kind() == OVERFLOW)
			return Optional.empty();

		// only OVERFLOW events are not WatchEvent<Path>
		var path = parent.resolve((Path) event.context());

		if (event.kind() == ENTRY_CREATE)
			return Optional.of(new FileWatchEvent(CREATED, path));
		if (event.kind() == ENTRY_MODIFY)
			return Optional.of(new FileWatchEvent(MODIFIED, path));
		if (event.kind() == ENTRY_DELETE)
			return Optional.of(new FileWatchEvent(DELETED, path));

		throw new IllegalStateException();
	}

}
