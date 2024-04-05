package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A document can represent any coherent group of information, e.g.:
 * <ul>
 *     <li>the lines of a file</li>
 *     <li>that same file content parsed by a Markdown parser</li>
 *     <li>objects from a JSON file</li>
 *     <li>lines from a CSV file</li>
 * </ul>
 */
public interface Document<DATA extends Record & Data> {

	// --- ID ---

	Id id();

	interface Id {

		Optional<Id> parent();

		default Id transform(String name) {
			return new TransformerId(this, name);
		}

		/**
		 * @return the full chain of parent IDs
		 */
		@Override
		String toString();

	}

	record SourceId(String name, URI location) implements Id {

		@Override
		public Optional<Id> parent() {
			return Optional.empty();
		}

		@Override
		public String toString() {
			return STR."Source[\{name}; \{location}]";
		}

	}

	record StoreId(String storeName) implements Id {

		@Override
		public Optional<Id> parent() {
			return Optional.empty();
		}

		@Override
		public String toString() {
			return STR."Store[\{storeName}]";
		}

	}

	record TransformerId(Id theParent, String name) implements Id {

		@Override
		public Optional<Id> parent() {
			return Optional.of(theParent);
		}

		@Override
		public String toString() {
			return STR."\{theParent} >>> Transformer[\{name}]";
		}

	}

	// --- DATA ---

	DATA data();

	interface Data {

	}

	interface FileData extends Data {

		Path file();

	}

	interface StringData extends Data {

		String dataAsString();

	}

}
