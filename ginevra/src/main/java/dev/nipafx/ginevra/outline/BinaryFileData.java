package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.FileData;

import java.nio.file.Path;

public record BinaryFileData(Path file, byte[] content) implements FileData { }
