package dev.nipafx.ginevra.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.nipafx.args.Args;
import dev.nipafx.args.ArgsParseException;
import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.config.GinevraArgs.BuildArgs;
import dev.nipafx.ginevra.config.GinevraArgs.DevelopArgs;
import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.execution.LiveCodeUpdate.Changes;
import dev.nipafx.ginevra.parse.JsonParser;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.YamlParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import dev.nipafx.ginevra.parse.jackson.JacksonJsonParserFactory;
import dev.nipafx.ginevra.parse.jackson.JacksonYamlParserFactory;
import dev.nipafx.ginevra.render.Renderer;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.parser.Parser;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

		try {
			siteBuilder.build(outline);
		} catch (InterruptedException ex) {
			// do nothing, execution will stop soon after this anyway
			ex.printStackTrace();
		}
	}

	// develop

	public static void developSite(Class<? extends SiteConfiguration> configType, DevelopArgs developArgs, String[] args) {
		var pureTemplates = developArgs.pureTemplatesOrDefault();
		var codeUpdater = new LiveCodeUpdater(developArgs.sources(), configType.getName());
		var code = codeUpdater
				.compileAndUpdateCode()
				.orElseThrow(() -> new IllegalArgumentException("The site sources must compile when launching Ginevra"));
		var configuration = createConfiguration(code.configType(), args);
		var outline = createOutline(configuration);

		var store = new LiveStore();
		var renderer = new Renderer(store, Path.of("resources"), Path.of("style"));
		var server = new LiveServer();
		var siteBuilder = new LiveSiteBuilder(store, renderer, server, developArgs.pureTemplatesOrDefault());

		siteBuilder.build(outline, developArgs.portOrDefault());
		codeUpdater.observe(codeUpdate -> rebuild(siteBuilder, codeUpdate.configType(), codeUpdate.changes(), args));

		waitForever();
	}

	private static void rebuild(
			LiveSiteBuilder siteBuilder, Class<? extends SiteConfiguration> configType, Changes changes, String[] args) {
		var configuration = createConfiguration(configType, args);
		var outline = createOutline(configuration);
		siteBuilder.rebuild(outline, changes);
	}

	// misc
	private static <TYPE> TYPE createConfiguration(Class<TYPE> type, String[] args) {
		@SuppressWarnings("unchecked")
		var constructors = (Constructor<TYPE>[]) type.getConstructors();
		var siteConfiguration = Stream
				.of(constructors)
				.filter(constructor -> 0 < constructor.getParameterCount() && constructor.getParameterCount() <= 3)
				.filter(constructor -> Stream
						.of(constructor.getParameterTypes())
						.allMatch(Class::isRecord))
				.findFirst()
				.map(constructor -> {
					try {
						var argsWithoutAction = Arrays.copyOfRange(args, 1, args.length);
						var paramTypes = constructor.getParameterTypes();
						var argsRecords = switch (constructor.getParameterCount()) {
							case 1 -> new Object[] { Args.parseLeniently(argsWithoutAction, paramTypes[0]) };
							case 2 -> {
								var parsed = Args.parseLeniently(argsWithoutAction, paramTypes[0], paramTypes[1]);
								yield new Object[]{ parsed.first(), parsed.second() };
							}
							case 3 -> {
								var parsed = Args.parseLeniently(argsWithoutAction, paramTypes[0], paramTypes[1], paramTypes[2]);
								yield new Object[]{ parsed.first(), parsed.second(), parsed.third() };
							}
							default -> throw new IllegalStateException("Unexpected number of constructor arguments");
						};
						return constructor.newInstance(argsRecords);
					} catch (ArgsParseException ex) {
						// TODO: handle error
						throw new IllegalArgumentException("The site configuration arguments contained errors", ex);
					} catch (ReflectiveOperationException ex) {
						throw new IllegalArgumentException("The site configuration could not be instantiated", ex);
					}
				});
		if (siteConfiguration.isPresent())
			return siteConfiguration.get();


		try {
			return type
					.getConstructor(String[].class)
					.newInstance((Object) args);
		} catch (NoSuchMethodException ex) {
			/* do nothing */
		} catch (ReflectiveOperationException ex) {
			// TODO: handle error
			throw new IllegalArgumentException("The site configuration could not be instantiated", ex);
		}

		try {
			return type
					.getConstructor()
					.newInstance();
		} catch (NoSuchMethodException ex) {
			/* do nothing */
		} catch (ReflectiveOperationException ex) {
			// TODO: handle error
			throw new IllegalArgumentException("The site configuration could not be instantiated", ex);
		}

		var message = """
				The site configuration type needs to have a public constructor with the following parameters: \
				either one to three record types, or a `String[]`, or no parameters. \
				`%s` has neither of those.""".formatted(type.getName());
		throw new IllegalArgumentException(message);
	}

	private static NodeOutline createOutline(SiteConfiguration configuration) {
		var outline = configuration.createOutline(
				new NodeOutliner(locateMarkdownParser(), locateJsonParser(), locateYamlParser()));
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
		return Optional.of(CommonmarkMarkdownParser.create());
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

	private static Optional<JsonParser> locateJsonParser() {
		if (!isJacksonJsonPresent())
			return Optional.empty();
		return Optional.of(JacksonJsonParser.create());
	}

	private static boolean isJacksonJsonPresent() {
		try {
			Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	private static Optional<YamlParser> locateYamlParser() {
		if (!isJacksonYamlPresent())
			return Optional.empty();
		return Optional.of(JacksonYamlParser.create());
	}

	private static boolean isJacksonYamlPresent() {
		try {
			Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLMapper");
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	private static void waitForever() {
		try {
			Thread.sleep(Duration.ofDays(365));
		} catch (InterruptedException ex) {
			// let the thread die when it is interrupted
		}
	}

	// INNER CLASSES
	// (to allow the absence of optional dependencies, the classes "touching" them must not be loaded before
	//  their presence is confirmed; hence the indirection with these otherwise pointless inner classes)

	private static class CommonmarkMarkdownParser {

		private static MarkdownParser create() {
			var commonmarkParser = Parser
					.builder()
					.extensions(List.of(YamlFrontMatterExtension.create()))
					.build();
			return new CommonmarkParser(commonmarkParser);
		}

	}

	private static class JacksonJsonParser {

		private static JsonParser create() {
			var mapper = new ObjectMapper()
					.registerModule(new Jdk8Module())
					.registerModule(new JavaTimeModule());
			return JacksonJsonParserFactory.forJson(mapper);
		}

	}

	private static class JacksonYamlParser {

		private static YamlParser create() {
			var yamlMapper = new YAMLMapper();
			yamlMapper
					.registerModule(new Jdk8Module())
					.registerModule(new JavaTimeModule());
			return JacksonYamlParserFactory.forYaml(yamlMapper);
		}

	}

}
