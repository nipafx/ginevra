package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.Element;

import java.nio.file.Path;

public record HtmlPage(Path slug, Element html) implements Document { }
