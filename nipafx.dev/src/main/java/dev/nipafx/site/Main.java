package dev.nipafx.site;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.Outliner.StepKey;

import java.nio.file.Path;

public class Main {

	public static final Path CONTENT = Path.of(Main.class.getClassLoader().getResource("content").getPath());

	public static void main(String[] args) {
		var ginevra = Ginevra.initialize(new Configuration(args));
		var outliner = ginevra.newOutliner();

		StepKey<FileData> content = outliner.sourceFileSystem("articles", CONTENT.resolve("articles"));
		StepKey<ArticleFrontMatter> markdown = outliner.transformMarkdown(content, ArticleFrontMatter.class);
		outliner.store(markdown);

		outliner.build().run();
	}

}
