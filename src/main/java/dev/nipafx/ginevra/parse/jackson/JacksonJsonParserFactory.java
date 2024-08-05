package dev.nipafx.ginevra.parse.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * To allow the absence of optional dependencies, the classes "touching" them must not be loaded before
 * their presence is confirmed; hence the indirection with these otherwise pointless factories.
 */
public class JacksonJsonParserFactory {

	public static JacksonParser forJson(ObjectMapper mapper) {
		return new JacksonParser("Jackson Dataformat JSON", mapper);
	}

}
