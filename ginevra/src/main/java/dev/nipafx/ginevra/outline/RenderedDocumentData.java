package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.HtmlDocument;
import dev.nipafx.ginevra.outline.Document.Data;

import java.nio.file.Path;

public record RenderedDocumentData(Path slug, HtmlDocument html) implements Data { }
