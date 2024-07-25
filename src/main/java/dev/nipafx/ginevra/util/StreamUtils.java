package dev.nipafx.ginevra.util;

import java.util.Collection;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class StreamUtils {

	@SafeVarargs
	public static <IN, OUT> Gatherer<IN, ?, OUT> only(Class<? extends OUT>... types) {
		return Gatherer.of((_, element, downstream) -> {
			for (var type : types)
				if (type.isInstance(element)) {
					downstream.push(type.cast(element));
					break;
				}
			return !downstream.isRejecting();
		});
	}

	public static <LEFT, RIGHT> Stream<Pair<LEFT, RIGHT>> crossProduct(Collection<LEFT> left, Collection<RIGHT> right) {
		return left.stream()
				.flatMap(l -> right.stream().map(r -> new Pair<>(l, r)));
	}

	public record Pair<LEFT, RIGHT>(LEFT left, RIGHT right) { }

}
