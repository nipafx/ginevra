package dev.nipafx.site;

import dev.nipafx.ginevra.outline.Document.Data;

public record ArticleFrontMatter(String title, String slug) implements Data { }
