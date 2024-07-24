package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.util.FileSystemUtils;
import dev.nipafx.ginevra.util.FileWatchEvent;
import dev.nipafx.ginevra.util.InMemoryCompiler;
import dev.nipafx.ginevra.util.InMemoryCompiler.Compilation;
import dev.nipafx.ginevra.util.InMemoryCompiler.FailedCompilation;
import dev.nipafx.ginevra.util.InMemoryCompiler.SuccessfulCompilation;
import dev.nipafx.ginevra.util.MultiplexingQueue;

import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

class LiveCodeUpdater {

	private final Path sourceRoot;
	private final String configClassName;
	private final MultiplexingQueue<FileWatchEvent> fileEvents;
	private final List<Consumer<Class<? extends SiteConfiguration>>> recompileListeners;

	LiveCodeUpdater(Path sourceRoot, String configurationClassName) {
		this.sourceRoot = sourceRoot;
		this.configClassName = configurationClassName;
		this.fileEvents = new MultiplexingQueue<>(this::processFileWatchEvent, "live-code-updater");
		this.recompileListeners = new CopyOnWriteArrayList<>();
	}

	void observe(Consumer<Class<? extends SiteConfiguration>> listener) {
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
			compileAndLoadConfigType()
					.ifPresent(configType -> recompileListeners.forEach(listener -> listener.accept(configType)));
	}

	Optional<Class<? extends SiteConfiguration>> compileAndLoadConfigType() {
		return switch (recompileSources()) {
			case FailedCompilation failed -> {
				reportFailedCompilation(failed);
				yield Optional.empty();
			}
			case SuccessfulCompilation successful -> loadConfigurationClass(successful);
		};
	}

	private Compilation recompileSources() {
		var compiler = new InMemoryCompiler(sourceRoot);
		return compiler.compileSources();
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

	private Optional<Class<? extends SiteConfiguration>> loadConfigurationClass(SuccessfulCompilation compilation) {
		System.out.printf("SUCCESSFUL compilation of sources in %s%n", sourceRoot);
		var classLoader = new ByteArrayClassLoader(getClass().getClassLoader(), compilation.classes());
		StoreUtils.SITE_CLASS_LOADER.set(Optional.of(classLoader));
		try {
			var configType = classLoader.loadClass(configClassName);
			if (SiteConfiguration.class.isAssignableFrom(configType)) {
				@SuppressWarnings("unchecked")
				var typedConfigClass = (Class<? extends SiteConfiguration>) configType;
				return Optional.of(typedConfigClass);
			} else {
				// TODO: log message
				return Optional.empty();
			}
		} catch (ReflectiveOperationException ex) {
			// TODO: handle error
			ex.printStackTrace();
			return Optional.empty();
		}
	}

	private static class ByteArrayClassLoader extends ClassLoader {

		private final Map<String, byte[]> byteCode;
		private final ConcurrentMap<String, Class<?>> classes;

		private ByteArrayClassLoader(ClassLoader parent, Map<String, byte[]> byteCode) {
			super("site", parent);
			this.byteCode = Map.copyOf(byteCode);
			this.classes = new ConcurrentHashMap<>();
		}

		@Override
		public Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
			if (byteCode.containsKey(className))
				return classes.computeIfAbsent(
						className,
						name -> defineClass(name, byteCode.get(name), 0, byteCode.get(name).length));
			return super.loadClass(className, resolve);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			if (byteCode.containsKey(name))
				return defineClass(name, byteCode.get(name), 0, byteCode.get(name).length);
			throw new ClassNotFoundException(name);
		}

	}

}
