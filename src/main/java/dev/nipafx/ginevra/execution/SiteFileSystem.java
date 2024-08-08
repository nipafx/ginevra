package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.render.ResourceFile;
import dev.nipafx.ginevra.render.ResourceFile.CopiedFile;
import dev.nipafx.ginevra.render.ResourceFile.CssFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

interface SiteFileSystem {

	static SiteFileSystem create(SitePaths sitePaths) {
		return new ActualFileSystem(sitePaths);
	}

	void initialize();

	void writeTemplatedFile(TemplatedFile file);

	void copyStaticFile(Path file, Path targetFolder);

	void awaitCompletion() throws InterruptedException;

	record TemplatedFile(Path slug, String content, Set<ResourceFile> referencedResources) { }

	class ActualFileSystem implements SiteFileSystem {

		private final SitePaths sitePaths;
		private final Set<Path> writtenFiles;
		private final ExecutorService executor;

		public ActualFileSystem(SitePaths sitePaths) {
			this.sitePaths = sitePaths;
			this.writtenFiles = Collections.newSetFromMap(new ConcurrentHashMap<>());
			this.executor = Executors.newVirtualThreadPerTaskExecutor();
		}

		@Override
		public void initialize() {
			try {
				sitePaths.createFolders();
			} catch (IOException ex) {
				// TODO: handle error
				throw new UncheckedIOException(ex);
			}
		}

		@Override
		public void writeTemplatedFile(TemplatedFile file) {
			executor.submit(() -> {
				var filePath = sitePaths.siteFolder().resolve(file.slug()).resolve("index.html").toAbsolutePath();

				writeToFile(filePath, file.content());
				file
						.referencedResources()
						.forEach(res -> {
							switch (res) {
								case CopiedFile copiedFile -> copyFile(copiedFile);
								case CssFile cssFile -> writeCssFile(cssFile);
							}
						});
			});
		}

		private void copyFile(CopiedFile copiedFile) {
			var targetFile = sitePaths.siteFolder().resolve(copiedFile.target());
			var fileNotWrittenBefore = writtenFiles.add(targetFile);
			// copied files have a hashed name, so if a target file of that name already exists
			// it can be assumed to be up-to-date and nothing needs to be done
			if (fileNotWrittenBefore && !Files.exists(targetFile))
				try {
					Files.copy(copiedFile.source(), targetFile);
				} catch (IOException ex) {
					writtenFiles.remove(targetFile);
					// TODO: handle error
					ex.printStackTrace();
				}
		}

		private void writeCssFile(CssFile cssFile) {
			var targetFile = sitePaths.siteFolder().resolve(cssFile.file()).toAbsolutePath();
			var fileNotWrittenBefore = writtenFiles.add(targetFile);
			// CSS files have a hashed name, so if a target file of that name already exists
			// it can be assumed to be up-to-date and nothing needs to be done
			if (fileNotWrittenBefore && !Files.exists(targetFile))
				writeToFile(targetFile, cssFile.content());
		}

		private void writeToFile(Path filePath, String fileContent) {
			var fileWrittenBefore = !writtenFiles.add(filePath);
			if (fileWrittenBefore)
				return;

			try {
				// some files can change without Ginevra noticing,
				// so they need to be deleted and recreated
				Files.createDirectories(filePath.getParent());
				Files.deleteIfExists(filePath);
				Files.writeString(filePath, fileContent);
			} catch (IOException ex) {
				writtenFiles.remove(filePath);
				// TODO: handle error
				ex.printStackTrace();
			}
		}

		@Override
		public void copyStaticFile(Path file, Path targetFolder) {
			executor.submit(() -> {
				var fullTargetFolder = sitePaths.siteFolder().resolve(targetFolder).toAbsolutePath();
				var targetFile = fullTargetFolder.resolve(file.getFileName());

				var fileWrittenBefore = !writtenFiles.add(targetFile);
				if (fileWrittenBefore)
					return;

				try {
					Files.createDirectories(fullTargetFolder);
					// these files can change without Ginevra noticing,
					// so they need to be deleted and recreated
					Files.deleteIfExists(targetFile);
					Files.copy(file, targetFile);
				} catch (IOException ex) {
					writtenFiles.remove(targetFile);
					// TODO: handle error
					ex.printStackTrace();
				}
			});
		}

		@Override
		public void awaitCompletion() throws InterruptedException {
			executor.shutdown();
			var terminated = executor.awaitTermination(1, TimeUnit.DAYS);
			if (!terminated)
				throw new IllegalStateException("Huh?");
		}

	}

}
