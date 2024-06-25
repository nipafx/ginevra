package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document.Data;

import java.nio.file.Path;

public record HtmlDocumentData(Path slug, Element html) implements Data { }
