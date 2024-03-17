package dev.nipafx.site.nipafx_dev;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;
import dev.nipafx.ginevra.execution.Paths;
import dev.nipafx.ginevra.outline.FileData;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Outliner.StepKey;
import dev.nipafx.site.nipafx_dev.data.ArticleData;
import dev.nipafx.site.nipafx_dev.data.SiteData;
import dev.nipafx.site.nipafx_dev.templates.ArticlePage;
import dev.nipafx.site.nipafx_dev.templates.LandingPage;

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

		StepKey<SiteData> siteData = outliner.source(SiteData.create());
		outliner.store(siteData);

		StepKey<FileData> content = outliner.sourceFileSystem("articles", CONTENT_FOLDER.resolve("articles"));
		StepKey<ArticleData.Markdown> markdown = outliner.transformMarkdown(content, ArticleData.Markdown.class);
		StepKey<ArticleData.Parsed> parsed = outliner.merge(markdown, siteData, (doc, siteD) -> List.of(new GeneralDocument<>(
						doc.id().transform("parsed"),
						ArticleData.Parsed.from(doc.data(), siteD.data().defaultInlineCodeLanguage()))));
		outliner.store(parsed, "articles");

		outliner.generate(new ArticlePage());
		outliner.generate(new LandingPage());

		outliner.build().run();
	}

}
