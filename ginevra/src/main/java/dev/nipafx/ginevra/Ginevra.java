package dev.nipafx.ginevra;

import dev.nipafx.ginevra.execution.FullOutliner;
import dev.nipafx.ginevra.execution.MapStore;
import dev.nipafx.ginevra.execution.Paths;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import dev.nipafx.ginevra.render.Renderer;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.parser.Parser;

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
		var store = new MapStore();
		var commonmarkParser = Parser
				.builder()
				.extensions(List.of(YamlFrontMatterExtension.create()))
				.build();
		var markdownParser = new CommonmarkParser(commonmarkParser);
		return new Ginevra(
				store,
				Optional.of(markdownParser),
				new Renderer(config.paths.cssFolder()),
				config.paths());
	}

	public Outliner newOutliner() {
		return new FullOutliner(store, markdownParser, renderer, paths);
	}

	public record Configuration(Paths paths) {

		public Configuration update(String[] args) {
			return this;
		}

	}

}
