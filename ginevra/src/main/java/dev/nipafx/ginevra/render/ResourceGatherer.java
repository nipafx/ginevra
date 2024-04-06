package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.execution.StoreFront;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Link;
import dev.nipafx.ginevra.html.Src;
import dev.nipafx.ginevra.outline.ReSrc;
import dev.nipafx.ginevra.outline.Template;
import dev.nipafx.ginevra.render.ResourceFile.CopiedFile;
import dev.nipafx.ginevra.render.ResourceFile.CssFile;
import dev.nipafx.ginevra.util.SHA256;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.html.HtmlElement.link;

class ResourceGatherer {

	private final StoreFront store;
	private final Path resourceFolder;
	private final Path cssFolder;

	private final Map<String, CopiedFile> resources;
	private final Map<String, CssFile> cssFiles;

	public ResourceGatherer(StoreFront store, Path resourceFolder, Path cssFolder) {
		this.store = store;
		this.resourceFolder = resourceFolder;
		this.cssFolder = cssFolder;

		this.resources = new HashMap<>();
		this.cssFiles = new HashMap<>();
	}

	public void includeStyle(Template<?> template) {
		includeObjectStyle(template);
	}

	public void includeStyle(CustomElement customElement) {
		includeObjectStyle(customElement);
	}

	private void includeObjectStyle(Object maybeStyled) {
		if (!(maybeStyled instanceof CssStyled<?> styled))
			return;

		var style = styled.style();
		var css = style.css().replaceSources(this::includeResource).interpolateSources();
		var contentHash = SHA256.hash(css);
		cssFiles.computeIfAbsent(contentHash, _ -> {
			var file = Path.of(computeCssFileName(style, contentHash));
			return new CssFile(cssFolder.resolve(file), css);
		});
	}

	private static String computeCssFileName(Record style, String contentHash) {
		var baseName = style.getClass().getName().replaceAll("[.$]", "-");
		return STR."\{baseName}--\{contentHash}.css";
	}

	public Src includeResource(Src src) {
		if (!(src instanceof ReSrc reSrc))
			return src;

		return resources.computeIfAbsent(reSrc.resourceName(), resourceName -> {
			var sourceFile = store
					.getResource(resourceName)
					.orElseThrow(() -> new IllegalArgumentException(STR."No resource with name '\{resourceName}'."))
					.data()
					.file();
			var targetFile = computeResourceFileName(sourceFile, resourceName);
			var targetPath = resourceFolder.resolve(targetFile);
			return new CopiedFile(sourceFile, targetPath);
		});
	}

	private static String computeResourceFileName(Path sourceFile, String resourceName) {
		var contentHash = "";
		try {
			contentHash = "--" + SHA256.hash(Files.readAllBytes(sourceFile));
		} catch (IOException ex) {
			// TODO: handle error
			ex.printStackTrace();
		}
		var sourceFileName = sourceFile.getFileName().toString();
		var dotIndex = sourceFileName.lastIndexOf(".");
		final String targetName;
		final String fileEnding;
		if (dotIndex < 0) {
			targetName = resourceName;
			fileEnding = "";
		} else {
			targetName = sourceFileName.substring(0, dotIndex);
			fileEnding = sourceFileName.substring(dotIndex);
		}

		return targetName + contentHash + fileEnding;
	}

	public Stream<ResourceFile> resources() {
		return Stream.concat(resources.values().stream(), cssFiles.values().stream());
	}

	public Stream<Link> cssHeadLinks() {
		return cssFiles
				.values().stream()
				.map(css -> link
						.href(STR."/\{css.file()}")
						.rel("stylesheet"));
	}

}
