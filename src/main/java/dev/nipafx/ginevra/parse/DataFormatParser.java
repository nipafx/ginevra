package dev.nipafx.ginevra.parse;

import java.util.List;
import java.util.function.BiFunction;

public sealed interface DataFormatParser permits YamlParser {

	String name();

	<DOCUMENT> DOCUMENT parseValue(String data, Class<DOCUMENT> type);

	<DOCUMENT> List<DOCUMENT> parseList(String data, Class<DOCUMENT> type);

	<VALUE, DOCUMENT> List<DOCUMENT> parseMap(String data, Class<VALUE> type, BiFunction<String, VALUE, DOCUMENT> mapper);

}
