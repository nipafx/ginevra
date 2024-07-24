package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.config.GinevraArgs.BuildArgs;
import dev.nipafx.ginevra.config.GinevraArgs.DevelopArgs;
import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import dev.nipafx.ginevra.render.Renderer;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.parser.Parser;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class Executor {

	private Executor() {
		// private constructor to prevent initialization
	}

	// build

	public static void buildSite(Class<? extends SiteConfiguration> configType, BuildArgs buildArgs, String[] args) {
		var configuration = createConfiguration(configType, args);
		var updatedArgs = configuration.updateBuildArguments(buildArgs);
		var sitePaths = updatedArgs.createPaths();
		var outline = createOutline(configuration);

		var store = new OneTimeStore();
		var renderer = new Renderer(store, sitePaths.resourcesFolder(), sitePaths.cssFolder());
		var fileSystem = SiteFileSystem.create(sitePaths);
		var siteBuilder = new OneTimeSiteBuilder(store, renderer, fileSystem);

		siteBuilder.build(outline);
	}

	// develop

	public static void developSite(Class<? extends SiteConfiguration> configType, DevelopArgs developArgs, String[] args) {
		var codeUpdater = new LiveCodeUpdater(developArgs.sources(), configType.getName());
		var compiledConfigType = codeUpdater
				.compileAndLoadConfigType()
				.orElseThrow(() -> new IllegalArgumentException("The site sources must compile when launching Ginevra"));
		var configuration = createConfiguration(compiledConfigType, args);
		var outline = createOutline(configuration);

		var store = new LiveStore();
		var renderer = new Renderer(store, Path.of("resources"), Path.of("style"));
		var server = new LiveServer();
		var siteBuilder = new LiveSiteBuilder(store, renderer, server);

		siteBuilder.build(outline, developArgs.portOrDefault());
		codeUpdater.observe(recompiledConfigType -> rebuild(siteBuilder, recompiledConfigType, args));

		waitForever();
	}

	private static void rebuild(LiveSiteBuilder siteBuilder, Class<? extends SiteConfiguration> configType, String[] args) {
		var configuration = createConfiguration(configType, args);
		var outline = createOutline(configuration);
		siteBuilder.rebuild(outline);
	}

	// misc
	private static <TYPE> TYPE createConfiguration(Class<TYPE> type, String[] args) {
		try {
			return type
					.getConstructor(String[].class)
					.newInstance((Object) args);
		} catch (NoSuchMethodException ex) {
			/* do nothing */
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException("The site configuration could not be instantiated", ex);
		}

		try {
			return type
					.getConstructor()
					.newInstance();
		} catch (NoSuchMethodException ex) {
			/* do nothing */
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException("The site configuration could not be instantiated", ex);
		}

		var message = """
				The site configuration type needs to have parameterless constructor \
				or a constructor that accepts a `String[]` \
				but `%s` has neither""".formatted(type.getName());
		throw new IllegalArgumentException(message);
	}

	private static NodeOutline createOutline(SiteConfiguration configuration) {
		var outline = configuration.createOutline(new NodeOutliner(locateMarkdownParser()));
		if (!(outline instanceof NodeOutline nodeOutline))
			throw new UnsupportedOperationException("""
					Can't build from unexpected outline type '%s'. \
					Use the provided outliner to create the outline. \
					""".formatted(outline.getClass().getName()));
		return nodeOutline;
	}

	private static Optional<MarkdownParser> locateMarkdownParser() {
		if (!isCommonMarkPresent())
			return Optional.empty();

		var commonmarkParser = Parser
				.builder()
				.extensions(List.of(YamlFrontMatterExtension.create()))
				.build();
		var parser = new CommonmarkParser(commonmarkParser);
		return Optional.of(parser);
	}

	private static boolean isCommonMarkPresent() {
		var moduleLayer = Ginevra.class.getModule().getLayer();
		if (moduleLayer != null)
			return moduleLayer.findModule("org.commonmark").isPresent()
				   && moduleLayer.findModule("org.commonmark.ext.front.matter").isPresent();
		else {
			try {
				Class.forName("org.commonmark.parser.Parser");
				return true;
			} catch (ClassNotFoundException ex) {
				return false;
			}
		}
	}

	private static void waitForever() {
		try {
			Thread.sleep(Duration.ofDays(365));
		} catch (InterruptedException ex) {
			// let the thread die when it is interrupted
		}
	}

}
