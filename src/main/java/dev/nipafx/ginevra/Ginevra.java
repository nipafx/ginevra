package dev.nipafx.ginevra;

import dev.nipafx.args.Args;
import dev.nipafx.args.ArgsParseException;
import dev.nipafx.args.Parsed2;
import dev.nipafx.args.Parsed3;
import dev.nipafx.ginevra.execution.Executor;
import dev.nipafx.ginevra.execution.NodeOutliner;
import dev.nipafx.ginevra.execution.Paths;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;
import dev.nipafx.ginevra.parse.MarkdownParser;
import dev.nipafx.ginevra.parse.commonmark.CommonmarkParser;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.parser.Parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static java.util.function.UnaryOperator.identity;

public class Ginevra {

	private final Outliner outliner;
	private final Executor executor;

	private Ginevra(Outliner outliner, Executor executor) {
		this.outliner = outliner;
		this.executor = executor;
	}

	// initialization

	public static Ginevra initialize(String[] args) {
		return initialize(args, identity());
	}

	public static Ginevra initialize(String[] args, UnaryOperator<Configuration> updateConfig) {
		var paths = parseConfiguration(args, updateConfig).createPaths();
		return createGinevra(paths);
	}

	private static Configuration parseConfiguration(String[] args, UnaryOperator<Configuration> updateConfig) {
		try {
			return updateConfig.apply(Args.parse(args, Configuration.class));
		} catch (ArgsParseException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public static <CONFIG extends Record> GinevraWithArgs<CONFIG> initialize(String[] args, Class<CONFIG> configType) {
		return initialize(args, configType, identity());
	}

	public static <CONFIG extends Record> GinevraWithArgs<CONFIG> initialize(
			String[] args, Class<CONFIG> configType, UnaryOperator<Configuration> updateConfig) {
		var configuration = parseConfiguration(args, configType, updateConfig);
		var ginevra = createGinevra(configuration.first().createPaths());
		return new GinevraWithArgs<>(ginevra, configuration.second());
	}

	private static <CONFIG extends Record> Parsed2<Configuration, CONFIG> parseConfiguration(
			String[] args, Class<CONFIG> configType, UnaryOperator<Configuration> updateConfig) {
		try {
			var parsed = Args.parse(args, Configuration.class, configType);
			return new Parsed2<>(updateConfig.apply(parsed.first()), parsed.second());
		} catch (ArgsParseException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	public static <CONFIG1 extends Record, CONFIG2 extends Record> GinevraWith2Args<CONFIG1, CONFIG2> initialize(
			String[] args, Class<CONFIG1> configType1, Class<CONFIG2> configType2) {
		return initialize(args, configType1, configType2, identity());
	}

	public static <CONFIG1 extends Record, CONFIG2 extends Record> GinevraWith2Args<CONFIG1, CONFIG2> initialize(
			String[] args, Class<CONFIG1> configType1, Class<CONFIG2> configType2, UnaryOperator<Configuration> updateConfig) {
		var configuration = parseConfiguration(args, configType1, configType2, updateConfig);
		var ginevra = createGinevra(configuration.first().createPaths());
		return new GinevraWith2Args<>(ginevra, configuration.second(), configuration.third());
	}

	private static <CONFIG1 extends Record, CONFIG2 extends Record> Parsed3<Configuration, CONFIG1, CONFIG2> parseConfiguration(
			String[] args, Class<CONFIG1> configType1, Class<CONFIG2> configType2, UnaryOperator<Configuration> updateConfig) {
		try {
			var parsed = Args.parse(args, Configuration.class, configType1, configType2);
			return new Parsed3<>(updateConfig.apply(parsed.first()), parsed.second(), parsed.third());
		} catch (ArgsParseException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private static Ginevra createGinevra(Paths paths) {
		var outliner = new NodeOutliner(locateMarkdownParser());
		var executor = new Executor(paths);
		return new Ginevra(outliner, executor);
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

	public record Configuration(Optional<Path> siteFolder, Optional<Path> resourcesFolder, Optional<Path> cssFolder) {

		public Paths createPaths() {
			return new Paths(
					siteFolder.orElse(Path.of("site")),
					resourcesFolder.orElse(Path.of("resources")),
					cssFolder.orElse(Path.of("style"))
			);
		}

	}

	public record GinevraWithArgs<CONFIG extends Record>(Ginevra ginevra, CONFIG config) { }

	public record GinevraWith2Args<CONFIG1 extends Record, CONFIG2 extends Record>(Ginevra ginevra, CONFIG1 config1, CONFIG2 config2) { }

	// misc

	public Outliner outliner() {
		return outliner;
	}

	public void build(Outline outline) {
		executor.build(outline);
	}

}
