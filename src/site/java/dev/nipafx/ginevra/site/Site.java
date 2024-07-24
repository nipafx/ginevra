package dev.nipafx.ginevra.site;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.config.GinevraArgs.BuildArgs;
import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.site.documents.LandingPageText;
import dev.nipafx.ginevra.site.documents.SiteDoc;
import dev.nipafx.ginevra.site.templates.LandingPage;

import java.nio.file.Path;
import java.util.Optional;

public class Site implements SiteConfiguration {

	private static final Path LANDING_FOLDER = Path.of("src/site/resources/landing");
	private static final Path STATIC_FOLDER = Path.of("src/site/resources/static");

	private static final Path SITE_FOLDER = Path.of("target/site");

	public static void main(String[] args) {
		Ginevra.build(Site.class, args);
	}

	@Override
	public BuildArgs updateBuildArguments(BuildArgs arguments) {
		return new BuildArgs(
				arguments.siteFolder().or(() -> Optional.of(SITE_FOLDER)),
				arguments.resourcesFolder(),
				arguments.cssFolder());
	}

	@Override
	public Outline createOutline(Outliner outliner) {
		outliner
				.source(new SiteDoc("Ginevra"))
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

		return outliner.build();
	}

}
