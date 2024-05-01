package dev.nipafx.ginevra.html;

public interface Src {

	static Src none() {
		return new Direct(null);
	}

	static Src direct(String path) {
		return new Direct(path);
	}

	String path();

	record Direct(String path) implements Src { }

}
