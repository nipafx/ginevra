package dev.nipafx.ginevra.parse.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.nipafx.ginevra.parse.JsonParser;
import dev.nipafx.ginevra.parse.YamlParser;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class JacksonParser implements JsonParser, YamlParser {

	private final ObjectMapper mapper;
	private final String name;

	private JacksonParser(String name, ObjectMapper mapper) {
		this.mapper = mapper;
		this.name = name;
	}

	public static JacksonParser forJson(ObjectMapper mapper) {
		return new JacksonParser("Jackson Dataformat JSON", mapper);
	}

	public static JacksonParser forYaml(YAMLMapper mapper) {
		return new JacksonParser("Jackson Dataformat YAML", mapper);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public <DOCUMENT> DOCUMENT parseValue(String data, Class<DOCUMENT> type) {
		try {
			return mapper.readValue(data, type);
		} catch (JsonProcessingException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public <DOCUMENT> List<DOCUMENT> parseList(String data, Class<DOCUMENT> type) {
		try {
			var listType = new TypeReference<List<JsonNode>>() { };
			return mapper
					.readValue(data, listType).stream()
					.map(element -> mapper.convertValue(element, type))
					.toList();
		} catch (JsonProcessingException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public <VALUE, DOCUMENT> List<DOCUMENT> parseMap(String data, Class<VALUE> type, BiFunction<String, VALUE, DOCUMENT> entryMapper) {
		try {
			var listType = new TypeReference<Map<String, JsonNode>>() { };
			return mapper
					.readValue(data, listType)
					.entrySet().stream()
					.map(entry -> {
						var value = mapper.convertValue(entry.getValue(), type);
						return entryMapper.apply(entry.getKey(), value);
					})
					.toList();
		} catch (JsonProcessingException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

}
