package dev.nipafx.ginevra.site.documents;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.HtmlContent;

import java.nio.file.Path;

public interface LandingPageText {

	record Markdown(Path file, HtmlContent contentParsedAsMarkdown) implements Document { }

	record Parsed(String id, HtmlContent text) implements Document {

		public static Parsed parse(Markdown md) {
			var fileName = md.file.getFileName().toString();
			return new Parsed(
					fileName.substring(0, fileName.length() - ".md".length()),
					md.contentParsedAsMarkdown);
		}

	}

}
