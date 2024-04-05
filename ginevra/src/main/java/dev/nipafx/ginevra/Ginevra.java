package dev.nipafx.ginevra;

import dev.nipafx.ginevra.execution.FullOutliner;
import dev.nipafx.ginevra.execution.Store;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import dev.nipafx.ginevra.render.Renderer;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Ginevra {

	private final Store store;
	private final Optional<MarkdownParser> markdownParser;
	private final Renderer renderer;
	private final Paths paths;

	private Ginevra(Store store, Optional<MarkdownParser> markdownParser, Renderer renderer, Paths paths) {
		this.store = store;
		this.renderer = renderer;
		this.markdownParser = markdownParser;
		this.paths = paths;
	}

	public static Ginevra initialize(Configuration config) {
		var store = new Store();
		return new Ginevra(
				store,
				locateMarkdownParser(),
				new Renderer(store, config.paths().resourcesFolder, config.paths().cssFolder),
				config.paths());
	}

	private static Optional<MarkdownParser> locateMarkdownParser() {
		if (!isCommonMarkPresent())
			return Optional.empty();

		var commonmarkParser = Parser
				.builder()
				.extensions(List.of(YamlFrontMatterExtension.create()))
				.build();
		var parser = new CommonmarkParser(commonmarkParser);
		return Optional.of(parser);
	}

	private static boolean isCommonMarkPresent() {
		var moduleLayer = Ginevra.class.getModule().getLayer();
		if (moduleLayer != null)
			return moduleLayer.findModule("org.commonmark").isPresent()
					&& moduleLayer.findModule("org.commonmark.ext.front.matter").isPresent();
		else {
			try {
				Class.forName("org.commonmark.parser.Parser");
				return true;
			} catch (ClassNotFoundException ex) {
				return false;
			}
		}
	}

	public Outliner newOutliner() {
		return new FullOutliner(store, markdownParser, renderer, paths);
	}

	public record Configuration(Paths paths) {

		public Configuration update(String[] args) {
			return this;
		}

	}

	public record Paths(Path siteFolder, Path resourcesFolder, Path cssFolder) {

		public Paths(Path siteFolder) {
			this(siteFolder, Path.of("resources"), Path.of("styles"));
		}

		public void createFolders() throws IOException {
			Files.createDirectories(siteFolder.toAbsolutePath());
			Files.createDirectories(siteFolder.resolve(resourcesFolder).toAbsolutePath());
			Files.createDirectories(siteFolder.resolve(cssFolder).toAbsolutePath());
		}

	}

}
