package dev.nipafx.site.nipafx_dev.data;

import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.outline.Document.Data;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class ArticleData {

	public record Markdown(String title, String slug, String description, String date, String tags, Path file, String content, List<Element> contentParsedAsMarkdown) implements Data { }

	public record Parsed(String title, String slug, String description, LocalDate date, List<String> tags, Path file, String content, List<Element> contentParsedAsMarkdown) implements Data {

		public static Parsed from(Markdown md) {
			var tags = List.of(md
					.tags()
					.substring(1, md.tags().length() - 1)
					.split(", "));
			return new Parsed(md.title(), md.slug(), md.description(), LocalDate.parse(md.date), tags, md.file(), md.content(), md.contentParsedAsMarkdown());
		}

	}

	public record Page(String title, String slug, String description, LocalDate date, List<String> tags, List<Element> contentParsedAsMarkdown) implements Data { }

}
