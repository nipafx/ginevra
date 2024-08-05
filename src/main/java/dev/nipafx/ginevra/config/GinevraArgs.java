package dev.nipafx.ginevra.config;

import dev.nipafx.ginevra.execution.SitePaths;

import java.nio.file.Path;
import java.util.Optional;

public interface GinevraArgs {

	sealed interface ActionArgs { }

	record BuildArgs(Optional<Path> siteFolder, Optional<Path> resourcesFolder, Optional<Path> cssFolder) implements ActionArgs {

		public SitePaths createPaths() {
			return new SitePaths(
					siteFolder.orElse(Path.of("site")),
					resourcesFolder.orElse(Path.of("resources")),
					cssFolder.orElse(Path.of("style"))
			);
		}

	}

	record DevelopArgs(Path sources, Optional<Integer> port, Optional<Boolean> pureTemplates) implements ActionArgs {

		public int portOrDefault() {
			return port.orElse(8000);
		}

		public boolean pureTemplatesOrDefault() {
			return pureTemplates.orElse(false);
		}

	}

}
