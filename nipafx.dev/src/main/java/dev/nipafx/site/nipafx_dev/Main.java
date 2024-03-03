package dev.nipafx.site.nipafx_dev;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;
import dev.nipafx.ginevra.execution.Paths;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Outliner.StepKey;
import dev.nipafx.ginevra.outline.Store;
import dev.nipafx.ginevra.outline.Store.DocCollection;
import dev.nipafx.site.nipafx_dev.data.ArticleData;
import dev.nipafx.site.nipafx_dev.templates.ArticlePage;

import java.nio.file.Path;
import java.util.List;

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
		StepKey<ArticleData.Markdown> markdown = outliner.transformMarkdown(content, ArticleData.Markdown.class);
		StepKey<ArticleData.Parsed> parsed = outliner.transform(markdown, doc -> List.of(new GeneralDocument<>(
				doc.id().transform("parsed"),
				ArticleData.Parsed.from(doc.data()))));
		var articleCollection = new DocCollection("articles");
		outliner.store(parsed, articleCollection);

		outliner.generate(new Store.CollectionQuery<>(articleCollection, ArticleData.Page.class), new ArticlePage());

		outliner.build().run();
	}

}
