package dev.nipafx.ginevra.util;

import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils {

	public static <IN, OUT> Function<IN, Stream<OUT>> keepOnly(Class<OUT> type) {
		return in -> type.isInstance(in) ? Stream.of(type.cast(in)) : Stream.empty();
	}

}
