package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.render.ResourceFile;
import dev.nipafx.ginevra.render.ResourceFile.CopiedFile;
import dev.nipafx.ginevra.render.ResourceFile.CssFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

interface SiteFileSystem {

	static SiteFileSystem create(SitePaths sitePaths) {
		return new ActualFileSystem(sitePaths);
	}

	void initialize();

	void writeTemplatedFile(TemplatedFile file);

	void copyStaticFile(Path file, Path targetFolder);

	record TemplatedFile(Path slug, String content, Set<ResourceFile> referencedResources) { }

	class ActualFileSystem implements SiteFileSystem {

		private final SitePaths sitePaths;

		public ActualFileSystem(SitePaths sitePaths) {
			this.sitePaths = sitePaths;
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
		}

		private void copyFile(CopiedFile copiedFile) {
			var target = sitePaths.siteFolder().resolve(copiedFile.target());
			try {
				// copied files have a hashed name, so if a target file of that name already exists
				// it can be assumed to be up-to-date and nothing needs to be done
				if (!Files.exists(target))
					Files.copy(copiedFile.source(), target);
			} catch (IOException ex) {
				// TODO: handle error
				ex.printStackTrace();
			}
		}

		private void writeCssFile(CssFile cssFile) {
			var targetFile = sitePaths.siteFolder().resolve(cssFile.file()).toAbsolutePath();
			// CSS files have a hashed name, so if a target file of that name already exists
			// it can be assumed to be up-to-date and nothing needs to be done
			if (!Files.exists(targetFile))
				writeToFile(targetFile, cssFile.content());
		}

		private void writeToFile(Path filePath, String fileContent) {
			try {
				// some files can change without Ginevra noticing,
				// so they need to be deleted and recreated
				Files.createDirectories(filePath.getParent());
				Files.deleteIfExists(filePath);
				Files.writeString(filePath, fileContent);
			} catch (IOException ex) {
				// TODO: handle error
				ex.printStackTrace();
			}
		}

		@Override
		public void copyStaticFile(Path file, Path targetFolder) {
			try {
				var fullTargetFolder = sitePaths.siteFolder().resolve(targetFolder).toAbsolutePath();
				Files.createDirectories(fullTargetFolder);
				var targetFile = fullTargetFolder.resolve(file.getFileName());
				// these files can change without Ginevra noticing,
				// so they need to be deleted and recreated
				Files.deleteIfExists(targetFile);
				Files.copy(file, targetFile);
			} catch (IOException ex) {
				// TODO: handle error
				ex.printStackTrace();
			}
		}

	}

}
