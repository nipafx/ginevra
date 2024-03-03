package dev.nipafx.ginevra.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StreamUtils {

	public static <IN, OUT> BiConsumer<IN, Consumer<OUT>> keepOnly(Class<OUT> type) {
		return (in, consumer) -> {
			if (type.isInstance(in))
				consumer.accept(type.cast(in));
		};
	}

}
