package dev.nipafx.ginevra.util;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class InMemoryCompiler {

	private final Path sourceRoot;
	private final JavaCompiler compiler;

	public InMemoryCompiler(Path sourceRoot) {
		this.sourceRoot = sourceRoot;
		this.compiler = ToolProvider.getSystemJavaCompiler();
	}

	public Compilation compileSources() {
		var additionalOutput = new StringWriter();
		var diagnostics = new DiagnosticCollector<>();
		var fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));
		var options = List.of("--enable-preview", "--release", Runtime.version().feature() + "");
		var classesForAnnotationProcessing = List.<String> of();
		var sourceFiles = filesToCompile(sourceRoot);
		var compilationTask = compiler
				.getTask(additionalOutput, fileManager, diagnostics, options, classesForAnnotationProcessing, sourceFiles);

		var success = compilationTask.call();
		var classes = fileManager
				.classFiles
				.entrySet().stream()
				.collect(toUnmodifiableMap(Entry::getKey, entry -> entry.getValue().toByteArray()));
		return success
				? new SuccessfulCompilation(diagnostics.getDiagnostics(), additionalOutput.toString(), classes)
				: new FailedCompilation(diagnostics.getDiagnostics(), additionalOutput.toString());
	}

	private static List<? extends JavaFileObject> filesToCompile(Path sourceRoot) {
		try (var files = Files
				.find(sourceRoot, Integer.MAX_VALUE, (path, _) -> path.getFileName().toString().endsWith(Kind.SOURCE.extension))) {
			return files
					.map(JavaSourceFile::new)
					.toList();
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
			return List.of();
		}
	}

	private static class JavaSourceFile extends SimpleJavaFileObject {

		private final Path file;

		JavaSourceFile(Path file) {
			super(file.toUri(), Kind.SOURCE);
			this.file = file;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return Files.readString(file);
		}

	}

	private static class JavaClassFile extends SimpleJavaFileObject {

		private final ByteArrayOutputStream outputStream;

		JavaClassFile(String className, Kind kind) {
			if (kind != Kind.CLASS)
				throw new IllegalArgumentException("This type is intended for class files only");
			var objectUri = "class:///%s%s".formatted(className.replace('.', '/'), kind.extension);
			super(URI.create(objectUri), kind);

			this.outputStream = new ByteArrayOutputStream();
		}

		@Override
		public OutputStream openOutputStream() {
			return outputStream;
		}

		public byte[] toByteArray() {
			return outputStream.toByteArray();
		}

	}

	private static class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

		private final ConcurrentMap<String, JavaClassFile> classFiles = new ConcurrentHashMap<>();

		InMemoryJavaFileManager(JavaFileManager fileManager) {
			super(fileManager);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
			var classFile = new JavaClassFile(className, kind);
			var previous = classFiles.put(className, classFile);
			if (previous != null)
				throw new IllegalStateException("Two class files of the same name were created");
			return classFile;
		}

	}

	public sealed interface Compilation { }
	public record SuccessfulCompilation(List<Diagnostic<?>> diagnostics, String additionalOutput, Map<String, byte[]> classes) implements Compilation { }
	public record FailedCompilation(List<Diagnostic<?>> diagnostics, String additionalOutput) implements Compilation { }

}
