package dev.nipafx.ginevra;

import dev.nipafx.args.Args;
import dev.nipafx.args.ArgsParseException;
import dev.nipafx.ginevra.execution.FullOutliner;
import dev.nipafx.ginevra.execution.Paths;
import dev.nipafx.ginevra.execution.Store;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import dev.nipafx.ginevra.render.Renderer;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.parser.Parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static java.util.function.UnaryOperator.identity;

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

	public static Ginevra initialize(String[] args) {
		return initialize(args, identity());
	}

	public static Ginevra initialize(String[] args, UnaryOperator<Configuration> updateConfig) {
		var store = new Store();
		var paths = parseConfiguration(args, updateConfig).createPaths();
		var markdownParser = locateMarkdownParser();
		var renderer = new Renderer(store, paths.resourcesFolder(), paths.cssFolder());
		return new Ginevra(store, markdownParser, renderer, paths);
	}

	private static Configuration parseConfiguration(String[] args, UnaryOperator<Configuration> updateConfig) {
		try {
			return updateConfig.apply(Args.parse(args, Configuration.class));
		} catch (ArgsParseException ex) {
			throw new IllegalArgumentException(ex);
		}
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

	public record Configuration(Optional<Path> siteFolder, Optional<Path> resourcesFolder, Optional<Path> cssFolder) {

		public Paths createPaths() {
			return new Paths(
					siteFolder.orElse(Path.of("site")),
					resourcesFolder.orElse(Path.of("resources")),
					cssFolder.orElse(Path.of("style"))
			);
		}

	}

}
