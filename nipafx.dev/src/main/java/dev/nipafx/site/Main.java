package dev.nipafx.site;

import dev.nipafx.ginevra.Ginevra;
import dev.nipafx.ginevra.Ginevra.Configuration;

import java.nio.file.Path;

public class Main {

	public static final Path CONTENT = Path.of(Main.class.getClassLoader().getResource("content/articles").getPath());

	public static void main(String[] args) {
		var ginevra = Ginevra.initialize(new Configuration(args));
		var outliner = ginevra.newOutliner();

		var content = outliner.sourceFileSystem(CONTENT);
		var markdown = outliner.transformMarkdown(content);
		outliner.store(markdown);

		outliner.build().run();
	}

}
