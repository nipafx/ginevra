package dev.nipafx.ginevra;

import dev.nipafx.ginevra.execution.FullOutliner;
import dev.nipafx.ginevra.execution.MapStore;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import org.commonmark.parser.Parser;

import java.util.Optional;

public class Ginevra {

	private final Store store;
	private final Optional<MarkdownParser> markdownParser;

	private Ginevra(Store store, Optional<MarkdownParser> markdownParser) {
		this.store = store;
		this.markdownParser = markdownParser;
	}

	public static Ginevra initialize(Configuration config) {
		var store = new MapStore();
		var markdownParser = new CommonmarkParser(Parser.builder().build());
		return new Ginevra(store, Optional.of(markdownParser));
	}

	public Outliner newOutliner() {
		return new FullOutliner(store, markdownParser);
	}

	public record Configuration(String[] args) { }

}
