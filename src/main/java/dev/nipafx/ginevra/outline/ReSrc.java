package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.html.Src;

public class ReSrc implements Src {

	private final String resourceName;

	ReSrc(String resourceName) {
		this.resourceName = resourceName;
	}

	public String resourceName() {
		return resourceName;
	}

	@Override
	public String path() {
		return resourceName;
	}

}
