package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.Src;

import java.nio.file.Path;

public final class Resources {

	private Resources() {
		// private constructor to prevent instantiation
	}

	public static Src include(String name) {
		return new ReSrc(name);
	}

	public static Src includeIfLocal(String path) {
		var isLocal = Path.of(path).getFileName().toString().equals(path);
		return isLocal
				? new ReSrc(path)
				: Src.direct(path);
	}

}
