package dev.nipafx.ginevra.html;

public interface Src {

	static Src none() {
		return new Source(null);
	}

	static Src direct(String path) {
		return new Source(path);
	}

	String path();

}
