package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.HtmlDocument;

import java.util.Set;

record HtmlDocumentWithResources(HtmlDocument document, Set<ResourceFile> referencedResources) { }
