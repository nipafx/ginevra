package dev.nipafx.site.nipafx_dev;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;
import dev.nipafx.ginevra.execution.Paths;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.Outliner.StepKey;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Store.DocCollection;
import dev.nipafx.site.nipafx_dev.data.ArticlePage;
import dev.nipafx.site.nipafx_dev.data.FullArticle;

import java.nio.file.Path;

public class Main {

	private static final Path CONTENT_FOLDER = Path.of(Main.class.getClassLoader().getResource("content").getPath());
	private static final Path SITE_FOLDER = Path.of("nipafx.dev/target/site");

	public static void main(String[] args) {
		var config = new Configuration(
				new Paths(SITE_FOLDER)
		).update(args);
		var ginevra = Ginevra.initialize(config);
		var outliner = ginevra.newOutliner();

		StepKey<FileData> content = outliner.sourceFileSystem("articles", CONTENT_FOLDER.resolve("articles"));
		StepKey<FullArticle> markdown = outliner.transformMarkdown(content, FullArticle.class);
		var articleCollection = new DocCollection("articles");
		outliner.store(markdown, articleCollection);
		outliner.generate(new Store.CollectionQuery<>(articleCollection, ArticlePage.class), new ArticleTemplate());

		outliner.build().run();
	}

}
