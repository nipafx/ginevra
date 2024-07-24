package dev.nipafx.ginevra.config;

import dev.nipafx.ginevra.config.GinevraArgs.BuildArgs;
import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.outline.Outliner;

public interface SiteConfiguration {

	default BuildArgs updateBuildArguments(BuildArgs arguments) {
		return arguments;
	}

	Outline createOutline(Outliner outliner);

}
