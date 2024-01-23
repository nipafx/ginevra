package dev.nipafx.ginevra;

import dev.nipafx.ginevra.execution.FullOutliner;
import dev.nipafx.ginevra.execution.MapStore;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import dev.nipafx.ginevra.render.HtmlRenderer;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.parser.Parser;

import java.util.List;
import java.util.Optional;

public class Ginevra {

	private final Store store;
	private final Optional<MarkdownParser> markdownParser;
	private final HtmlRenderer renderer;

	private Ginevra(Store store, HtmlRenderer renderer, Optional<MarkdownParser> markdownParser) {
		this.store = store;
		this.renderer = renderer;
		this.markdownParser = markdownParser;
	}

	public static Ginevra initialize(Configuration config) {
		var store = new MapStore();
		var commonmarkParser = Parser
				.builder()
				.extensions(List.of(YamlFrontMatterExtension.create()))
				.build();
		var markdownParser = new CommonmarkParser(commonmarkParser);
		return new Ginevra(store, new HtmlRenderer(), Optional.of(markdownParser));
	}

	public Outliner newOutliner() {
		return new FullOutliner(store, renderer, markdownParser);
	}

	public record Configuration(String[] args) { }

}
