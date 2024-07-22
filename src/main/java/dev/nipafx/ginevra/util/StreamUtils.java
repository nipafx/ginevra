package dev.nipafx.ginevra.util;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StreamUtils {

	@SafeVarargs
	public static <IN, OUT> BiConsumer<IN, Consumer<OUT>> keepOnly(Class<? extends OUT>... types) {
		return (in, consumer) -> {
			for (var type : types)
				if (type.isInstance(in))
					consumer.accept(type.cast(in));
		};
	}

	public static <LEFT, RIGHT> Stream<Pair<LEFT, RIGHT>> crossProduct(Collection<LEFT> left, Collection<RIGHT> right) {
		return left.stream()
				.flatMap(l -> right.stream().map(r -> new Pair<>(l, r)));
	}

	public record Pair<LEFT, RIGHT>(LEFT left, RIGHT right) { }

}
