package dev.nipafx.ginevra.outline;

import java.nio.file.Path;

public record BinaryFileDocument(Path file, byte[] content) implements FileDocument { }
