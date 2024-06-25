package dev.nipafx.ginevra.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Paths(Path siteFolder, Path resourcesFolder, Path cssFolder) {

	public Paths(Path siteFolder) {
		this(siteFolder, Path.of("resources"), Path.of("styles"));
	}

	public void createFolders() throws IOException {
		Files.createDirectories(siteFolder.toAbsolutePath());
		Files.createDirectories(siteFolder.resolve(resourcesFolder).toAbsolutePath());
		Files.createDirectories(siteFolder.resolve(cssFolder).toAbsolutePath());
	}

}
