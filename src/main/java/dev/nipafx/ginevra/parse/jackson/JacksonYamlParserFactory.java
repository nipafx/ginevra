package dev.nipafx.ginevra.parse.jackson;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/*
 * To allow the absence of optional dependencies, the classes "touching" them must not be loaded before
 * their presence is confirmed; hence the indirection with these otherwise pointless factories.
 */
public class JacksonYamlParserFactory {

	public static JacksonParser forYaml(YAMLMapper mapper) {
		return new JacksonParser("Jackson Dataformat YAML", mapper);
	}

}
