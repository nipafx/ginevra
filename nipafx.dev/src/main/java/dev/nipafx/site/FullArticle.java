package dev.nipafx.site;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document.Data;

import java.nio.file.Path;
import java.util.List;

public record FullArticle(String title, String slug, Path file, String content, List<Element> contentParsedAsMarkdown) implements Data { }
