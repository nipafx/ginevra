package dev.nipafx.ginevra.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class FileSystemUtils {

	public static byte[] readAllBytes(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static FileWatch watchFolder(Path root, Consumer<FileWatchEvent> eventHandler) throws IOException {
		var watcher = FileSystems.getDefault().newWatchService();
		var key = root.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
		var threadName = "file system watcher: '%s'".formatted(root);

		var folders = new ConcurrentHashMap<>(Map.of(key, root));
		startWatcherThread(threadName, watcher, folders, eventHandler);

		return key::cancel;
	}

	public static FileWatch watchFolderStructure(Path root, Consumer<FileWatchEvent> eventHandler) throws IOException {
		var watcher = FileSystems.getDefault().newWatchService();

		var folders = new ConcurrentHashMap<WatchKey, Path>();
		registerWatcherRecursively(root, watcher, folders);

		var threadName = "file system watcher: '%s' (recursive)".formatted(root);
		Consumer<FileWatchEvent> observeNewDirectoriesThenHandleEvent = event -> {
			try {
				if (Files.isDirectory(event.path()))
					registerWatcherRecursively(event.path(), watcher, folders);
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
			eventHandler.accept(event);
		};
		startWatcherThread(threadName, watcher, folders, observeNewDirectoriesThenHandleEvent);

		return () -> folders.keySet().forEach(WatchKey::cancel);
	}

	private static void registerWatcherRecursively(Path path, WatchService watcher, ConcurrentHashMap<WatchKey, Path> folders) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				var key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
				folders.put(key, dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void startWatcherThread(
			String threadName, WatchService watcher, ConcurrentMap<WatchKey, Path> folders, Consumer<FileWatchEvent> eventHandler) {
		Thread
				.ofVirtual()
				.name(threadName)
				.start(() -> {
					try {
						watchForChanges(watcher, folders, eventHandler);
					} catch (Exception ex) {
						//TODO: handle error
						ex.printStackTrace();
					}
				});
	}

	private static void watchForChanges(
			WatchService watcher, ConcurrentMap<WatchKey, Path> folders, Consumer<FileWatchEvent> eventHandler) {
		try {
			while (true) {
				var watchKey = watcher.take();
				var parent = folders.get(watchKey);
				// This sleep effectively groups events:
				// IDEs and text editors may write file content and meta information (like the edit date) separately,
				// which can lead to two separate events in quick succession. By sleeping for a short period,
				// these writes will appear as a single event with a WatchEvent.count() > 1.
				Thread.sleep(50);
				for (var fileEvent : watchKey.pollEvents())
					FileWatchEvent
							.fromWatchEvent(fileEvent, parent)
							.ifPresent(eventHandler);
				var validKey = watchKey.reset();
				if (!validKey)
					folders.remove(watchKey);
			}
		} catch (InterruptedException ex) {
			// if the thread is interrupted, exit the loop (and let the thread die)
		}
	}

	public static boolean isTemporaryChange(FileWatchEvent event) {
		if (Files.isDirectory(event.path()))
			return false;
		
		if (event.path().getFileName().startsWith("."))
			return true;
		if (event.path().toString().endsWith("~"))
			return true;

		return false;
	}

}
