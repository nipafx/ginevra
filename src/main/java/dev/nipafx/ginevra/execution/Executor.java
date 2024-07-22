package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.render.Renderer;

public class Executor {

	private final SiteBuilder siteBuilder;

	public Executor(Paths paths) {
		var store = new OneTimeStore();
		var renderer = new Renderer(store, paths.resourcesFolder(), paths.cssFolder());
		var fileSystem = FileSystem.create(paths);

		this.siteBuilder = new OneTimeSiteBuilder(store, renderer, fileSystem);
//		this.siteBuilder = new LiveSiteBuilder(new LiveStore(), renderer, new LiveServer());
	}

	public void build(Outline outline) {
		if (!(outline instanceof NodeOutline nodeOutline))
			throw new IllegalArgumentException("Unexpected outline type: " + outline.getClass().getSimpleName());

		siteBuilder.build(nodeOutline);
	}

}
