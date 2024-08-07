package dev.nipafx.ginevra.util;

import java.util.List;
import java.util.stream.Stream;

public class CollectionUtils {

	@SafeVarargs
	public static <ELEMENT> List<ELEMENT> add(List<? extends ELEMENT> list, ELEMENT... elements) {
		return Stream.concat(list.stream(), Stream.of(elements)).toList();
	}

	public static boolean allEmpty(List<?>... lists) {
		return Stream.of(lists).allMatch(List::isEmpty);
	}


}
