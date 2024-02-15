package dev.nipafx.site.nipafx_dev.data;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document.Data;

import java.util.List;

public record ArticlePage(String title, String slug, String description, List<String> tags, List<Element> contentParsedAsMarkdown) implements Data { }
