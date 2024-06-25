package dev.nipafx.ginevra.render;

import java.util.Set;

public record HtmlWithResources(String html, Set<ResourceFile> referencedResources) { }
