package dev.nipafx.ginevra.html;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Gatherer;

/**
 * A helper for the common logic of mapping between text and single children
 * and merging text children.
 */
record TextChildren(String text, List<? extends Element> children) {

	TextChildren {
		if (text != null && !children.isEmpty())
			throw new IllegalArgumentException("Specify either a text or children, but not both");
		children = children.stream()
				.gather(joinConsecutiveTexts())
				.toList();
		if (children.size() == 1 && children.getFirst() instanceof Text(var childText)) {
			children = List.of();
			text = childText;
		}
	}

	private static Gatherer<Element, ?, Element> joinConsecutiveTexts() {
		return Gatherer.of(
				() -> new AtomicReference<>(""),
				(state, element, downstream) -> {
					var textState = state.get();
					if (element instanceof Text(var text)) {
						state.set(textState + text);
						return true;
					} else {
						if (!textState.isBlank()) {
							downstream.push(new Text(textState));
							state.set("");
						}
						return downstream.push(element);
					}
				},
				(_, _) -> { throw new IllegalStateException(); },
				(state, downstream) -> {
					var text = state.get();
					if (!text.isBlank())
						downstream.push(new Text(text));
				}
		);
	}


}
