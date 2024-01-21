package dev.nipafx.site;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.Outliner.StepKey;
import dev.nipafx.ginevra.outline.Store.DocCollection;

import java.nio.file.Path;

public class Main {

	public static final Path CONTENT = Path.of(Main.class.getClassLoader().getResource("content").getPath());

	public static void main(String[] args) {
		var ginevra = Ginevra.initialize(new Configuration(args));
		var outliner = ginevra.newOutliner();

		StepKey<FileData> content = outliner.sourceFileSystem("articles", CONTENT.resolve("articles"));
		StepKey<FullArticle> markdown = outliner.transformMarkdown(content, FullArticle.class);
		outliner.store(markdown, new DocCollection("articles"));

		outliner.build().run();
	}

}
