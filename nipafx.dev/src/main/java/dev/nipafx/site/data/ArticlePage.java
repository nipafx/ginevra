package dev.nipafx.site.data;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

public record ArticlePage(String title, String slug, List<Element> contentParsedAsMarkdown) implements Data { }
