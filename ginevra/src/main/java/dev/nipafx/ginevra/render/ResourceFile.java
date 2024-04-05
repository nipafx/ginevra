package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Src;

import java.nio.file.Path;
import java.util.Objects;

public sealed interface ResourceFile {

	record CopiedFile(Path source, Path target) implements ResourceFile, Src {

		@Override
		public String path() {
			return Path.of("/").resolve(target).toString();
		}

		@Override
		public boolean equals(Object o) {
			return this == o
					|| o instanceof CopiedFile copiedFile
					&& Objects.equals(target, copiedFile.target);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(target);
		}

	}

	record CssFile(Path file, String content) implements ResourceFile {

		@Override
		public boolean equals(Object o) {
			return this == o
				   || o instanceof CssFile cssFile
					  && Objects.equals(file, cssFile.file);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(file);
		}

	}

}
