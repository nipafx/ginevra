package dev.nipafx.ginevra.site.documents;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document;

import java.nio.file.Path;
import java.util.List;

public interface LandingPageText {

	record Markdown(Path file, List<Element> contentParsedAsMarkdown) implements Document { }

	record Parsed(String id, List<Element> text) implements Document {

		public static Parsed parse(Markdown md) {
			var fileName = md.file.getFileName().toString();
			return new Parsed(
					fileName.substring(0, fileName.length() - ".md".length()),
					md.contentParsedAsMarkdown);
		}

	}

}
