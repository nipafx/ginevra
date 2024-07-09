package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Outline;
import dev.nipafx.ginevra.render.Renderer;

public class Executor {

	private final Store store;
	private final Renderer renderer;
	private final FileSystem fileSystem;

	public Executor(Store store, Renderer renderer, FileSystem fileSystem) {
		this.store = store;
		this.renderer = renderer;
		this.fileSystem = fileSystem;
	}

	public void build(Outline outline) {
		if (!(outline instanceof NodeOutline nodeOutline))
			throw new IllegalArgumentException("Unexpected outline type: " + outline.getClass().getSimpleName());

		new Builder(nodeOutline, store, renderer, fileSystem).build();
	}

}
