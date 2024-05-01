package dev.nipafx.site.nipafx_dev;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;
import dev.nipafx.ginevra.outline.BinaryFileData;
import dev.nipafx.ginevra.outline.GeneralDocument;
import dev.nipafx.ginevra.outline.Outliner.StepKey;
import dev.nipafx.ginevra.outline.TextFileData;
import dev.nipafx.site.nipafx_dev.data.ArticleData;
import dev.nipafx.site.nipafx_dev.data.SiteData;
import dev.nipafx.site.nipafx_dev.templates.ArticlePage;
import dev.nipafx.site.nipafx_dev.templates.LandingPage;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Main {

	private static final Path STATIC_FOLDER = Path.of(Main.class.getClassLoader().getResource("static").getPath());
	private static final Path CONTENT_FOLDER = Path.of(Main.class.getClassLoader().getResource("content").getPath());

	private static final Path SITE_FOLDER = Path.of("nipafx.dev/target/site");

	public static void main(String[] args) {
		var ginevraWithConfig = Ginevra.initialize(
				args,
				Config.class,
				cfg -> new Configuration(
						cfg.siteFolder().or(() -> Optional.of(SITE_FOLDER)),
						cfg.resourcesFolder(),
						cfg.cssFolder())
		);
		var config = ginevraWithConfig.config();
		var outliner = ginevraWithConfig.ginevra().newOutliner();

		StepKey<SiteData> siteData = outliner.source(SiteData.create());
		outliner.store(siteData);

		StepKey<BinaryFileData> images = outliner.sourceBinaryFiles("images", STATIC_FOLDER);
		outliner.storeResource(images);

		StepKey<TextFileData> content = outliner.sourceTextFiles("articles", CONTENT_FOLDER.resolve("articles"));
		StepKey<ArticleData.Markdown> markdown = outliner.transformMarkdown(content, ArticleData.Markdown.class);
		StepKey<ArticleData.Parsed> parsed = outliner.merge(markdown, siteData, (doc, siteD) -> List.of(new GeneralDocument<>(
						doc.id().transform("parsed"),
						ArticleData.Parsed.from(doc.data(), siteD.data().defaultInlineCodeLanguage()))));
		StepKey<ArticleData.Parsed> parsedNewArticles = outliner.filter(parsed, article -> article.date().getYear() >= 2020);
		outliner.store(parsedNewArticles, "articles");

		outliner.generate(new ArticlePage());
		outliner.generate(new LandingPage());

		outliner.generateStaticResources(Path.of(""), "icon.png");

		outliner.build().run();
	}

	public record Config(Optional<Boolean> haveFun) { }

}
