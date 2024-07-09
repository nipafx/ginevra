package dev.nipafx.ginevra.outline;

import java.nio.file.Path;

public record BinaryFileData(Path file, byte[] content) implements FileDocument { }
