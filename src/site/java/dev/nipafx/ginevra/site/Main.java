package dev.nipafx.ginevra.site;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;
import dev.nipafx.ginevra.site.data.LandingPageText;
import dev.nipafx.ginevra.site.data.SiteData;
import dev.nipafx.ginevra.site.templates.LandingPage;

import java.nio.file.Path;
import java.util.Optional;

public class Main {

	private static final Path STATIC_FOLDER = Path.of(Main.class.getClassLoader().getResource("static").getPath());
	private static final Path LANDING_FOLDER = Path.of(Main.class.getClassLoader().getResource("landing").getPath());

	private static final Path SITE_FOLDER = Path.of("target/site");

	public static void main(String[] args) {
		var ginevra = Ginevra.initialize(args, cfg -> new Configuration(
				cfg.siteFolder().or(() -> Optional.of(SITE_FOLDER)),
				cfg.resourcesFolder(),
				cfg.cssFolder()));
		var outliner = ginevra.newOutliner();

		outliner
				.source(new SiteData("Ginevra"))
				.store();
		outliner
				.sourceBinaryFiles("static", STATIC_FOLDER)
				.storeResource();

		outliner
				.sourceTextFiles("landing", LANDING_FOLDER)
				.transformMarkdown(LandingPageText.Markdown.class)
				.transform("parsed", LandingPageText.Parsed::parse)
				.store("landingPageTexts");

		outliner.generate(new LandingPage());
		outliner.generateStaticResources(Path.of(""), "favicon.ico");

		outliner.build().run();
	}

}
