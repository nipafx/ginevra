package dev.nipafx.ginevra.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Paths(Path siteFolder, Path cssFolder) {

	public Paths(Path siteFolder) {
		this(siteFolder, Path.of("styles"));
	}

	public void createFolders() throws IOException {
		Files.createDirectories(siteFolder.toAbsolutePath());
		Files.createDirectories(cssFolder.toAbsolutePath());
	}

}
