package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.execution.LiveCodeUpdate.Rebuild;
import dev.nipafx.ginevra.execution.LiveCodeUpdate.Rebuild.Components;
import dev.nipafx.ginevra.execution.LiveCodeUpdate.Rebuild.Full;
import dev.nipafx.ginevra.execution.LiveCodeUpdate.Rebuild.None;
import dev.nipafx.ginevra.execution.LiveCodeUpdate.Rebuild.Templates;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.outline.Template;
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
	private final boolean pureTemplates;

	private final MultiplexingQueue<FileWatchEvent> fileEvents;
	private final List<Consumer<LiveCodeUpdate>> recompileListeners;

	LiveCodeUpdater(Path sourceRoot, String configurationClassName, boolean pureTemplates) {
		this.sourceRoot = sourceRoot;
		this.configClassName = configurationClassName;
		this.pureTemplates = pureTemplates;
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
		var rebuild = ByteArrayClassLoader
				.swap(classLoader)
				.map(previousLoader -> determineRebuild(previousLoader, classLoader))
				.orElse(new Rebuild.Full());
		try {
			var configType = classLoader.loadClass(configClassName);
			if (SiteConfiguration.class.isAssignableFrom(configType)) {
				@SuppressWarnings("unchecked")
				var typedConfigClass = (Class<? extends SiteConfiguration>) configType;
				return new LiveCodeUpdate(typedConfigClass, rebuild);
			} else {
				// TODO: handle error
				throw new IllegalStateException();
			}
		} catch (ReflectiveOperationException ex) {
			// TODO: handle error (user might've renamed the class)
			throw new IllegalStateException(ex);
		}
	}

	private Rebuild determineRebuild(ByteArrayClassLoader previousLoader, ByteArrayClassLoader nextLoader) {
		// A full rebuild is unnecessary when only HTML templating changes but it's difficult to precisely
		// determine whether that is the case. In order to err on the side of too many rebuilds, one is triggered
		// unless very narrow requirements are met:
		//
		//  (a) the user guarantees that implementations of `Template` (and other such types) are "pure"
		//      (in the sense that they're not involved in anything but templating) and
		//  (b) only classes that implement those interfaces were modified
		//
		// Added and removed classes can be ignored. If no other class was modified, their addition/removal
		// can only impact the build when the rest of the code uses advanced dynamic features (like a class path
		// scan or service loader interaction) that is arguably out of scope of a static site build.

		if (!pureTemplates)
			return new Rebuild.Full();

		var changedTypes = nextLoader
				.byteCode()
				.entrySet().stream()
				.filter(type -> previousLoader.byteCode().containsKey(type.getKey()))
				.filter(type -> !Arrays.equals(type.getValue(), previousLoader.byteCode().get(type.getKey())))
				.map(type -> {
					try {
						return nextLoader.loadClass(type.getKey());
					} catch (ClassNotFoundException ex) {
						// TODO: handle error
						throw new IllegalStateException(ex);
					}
				})
				.toList();

		Rebuild rebuild = new None();
		for (var type : changedTypes) {
			rebuild = switch (type) {
				case Class<?> _
						when CustomElement.class.isAssignableFrom(type) || CssStyle.class.isAssignableFrom(type) ->
						rebuild instanceof None ? new Components() : rebuild;
				case Class<?> _ when Template.class.isAssignableFrom(type) -> switch (rebuild) {
					case None _, Components _ -> new Templates(List.of(type));
					case Templates templates -> templates.addTemplate(type);
					case Full full -> full;
				};
				default -> new Rebuild.Full();
			};
		}

		return rebuild;
	}

}
