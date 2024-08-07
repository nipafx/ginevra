package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.execution.LiveCodeUpdate.Changes;
import dev.nipafx.ginevra.util.FileSystemUtils;
import dev.nipafx.ginevra.util.FileWatchEvent;
import dev.nipafx.ginevra.util.InMemoryCompiler;
import dev.nipafx.ginevra.util.InMemoryCompiler.FailedCompilation;
import dev.nipafx.ginevra.util.InMemoryCompiler.SuccessfulCompilation;
import dev.nipafx.ginevra.util.MultiplexingQueue;

import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

class LiveCodeUpdater {

	private final Path sourceRoot;
	private final String configClassName;

	private final MultiplexingQueue<FileWatchEvent> fileEvents;
	private final List<Consumer<LiveCodeUpdate>> recompileListeners;

	LiveCodeUpdater(Path sourceRoot, String configurationClassName) {
		this.sourceRoot = sourceRoot;
		this.configClassName = configurationClassName;
		this.fileEvents = new MultiplexingQueue<>(this::processFileWatchEvent, "live-code-updater");
		this.recompileListeners = new CopyOnWriteArrayList<>();
	}

	void observe(Consumer<LiveCodeUpdate> listener) {
		try {
			recompileListeners.add(listener);
			FileSystemUtils.watchFolderStructure(sourceRoot, fileEvents::add);
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
		}
	}

	private void processFileWatchEvent(FileWatchEvent event) {
		var changedPath = event.path();
		// don't try to draw conclusions from the kind of file system entry the path is referencing
		// (e.g. a directory or a "regular" file) as in the case of a deletion, nothing meaningful
		// can be determined
		if (!FileSystemUtils.isTemporaryChange(event) && changedPath.getFileName().toString().endsWith(".java"))
			compileAndUpdateCode()
					.ifPresent(update -> recompileListeners.forEach(listener -> listener.accept(update)));
	}

	Optional<LiveCodeUpdate> compileAndUpdateCode() {
		var compiler = new InMemoryCompiler(sourceRoot);
		return switch (compiler.compileSources()) {
			case FailedCompilation failed -> {
				reportFailedCompilation(failed);
				yield Optional.empty();
			}
			case SuccessfulCompilation successful -> Optional.of(updateLiveCode(successful));
		};
	}

	private void reportFailedCompilation(FailedCompilation compilation) {
		System.out.printf("FAILED compilation of sources in %s:%n", sourceRoot);
		compilation
				.diagnostics().stream()
				.filter(diagnostic -> diagnostic.getKind() == Kind.ERROR)
				.forEach(diagnostic -> {
					System.out.println();
					diagnostic.toString().lines().forEach(line -> System.out.println("\t" + line));
				});
		if (!compilation.additionalOutput().isBlank()) {
			System.out.println();
			compilation
					.additionalOutput()
					.lines()
					.forEach(line -> System.out.println("\t" + line));
		}
		System.out.println();
	}

	private LiveCodeUpdate updateLiveCode(SuccessfulCompilation compilation) {
		System.out.printf("SUCCESSFUL compilation of sources in %s%n", sourceRoot);
		var classLoader = new ByteArrayClassLoader(getClass().getClassLoader(), compilation.classes());
		var changes = ByteArrayClassLoader
				.swap(classLoader)
				.map(previousLoader -> determineChanges(previousLoader, classLoader))
				.orElseGet(() -> Changes.forChangedTypes(List.of()));
		try {
			var configType = classLoader.loadClass(configClassName);
			if (SiteConfiguration.class.isAssignableFrom(configType)) {
				@SuppressWarnings("unchecked")
				var typedConfigClass = (Class<? extends SiteConfiguration>) configType;
				return new LiveCodeUpdate(typedConfigClass, changes);
			} else {
				// TODO: handle error
				throw new IllegalStateException();
			}
		} catch (ReflectiveOperationException ex) {
			// TODO: handle error (user might've renamed the class)
			throw new IllegalStateException(ex);
		}
	}

	private Changes determineChanges(ByteArrayClassLoader previousLoader, ByteArrayClassLoader nextLoader) {
		var changedTypes = nextLoader
				.byteCode()
				.entrySet().stream()
				.filter(type -> previousLoader.byteCode().containsKey(type.getKey()))
				.filter(type -> !Arrays.equals(type.getValue(), previousLoader.byteCode().get(type.getKey())))
				.<Class<?>> map(type -> {
					try {
						return nextLoader.loadClass(type.getKey());
					} catch (ClassNotFoundException ex) {
						// TODO: handle error
						throw new IllegalStateException(ex);
					}
				})
				.toList();
		return Changes.forChangedTypes(changedTypes);
	}

}
