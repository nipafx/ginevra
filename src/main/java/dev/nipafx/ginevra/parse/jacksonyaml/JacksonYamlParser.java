package dev.nipafx.ginevra.parse.jacksonyaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.nipafx.ginevra.parse.YamlParser;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class JacksonYamlParser implements YamlParser {

	private final YAMLMapper yamlMapper;

	public JacksonYamlParser(YAMLMapper yamlMapper) {
		this.yamlMapper = yamlMapper;
	}

	@Override
	public String name() {
		return "Jackson Dataformat YAML";
	}

	@Override
	public <DOCUMENT> DOCUMENT parseValue(String data, Class<DOCUMENT> type) {
		try {
			return yamlMapper.readValue(data, type);
		} catch (JsonProcessingException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public <DOCUMENT> List<DOCUMENT> parseList(String data, Class<DOCUMENT> type) {
		try {
			var listType = new TypeReference<List<JsonNode>>() { };
			return yamlMapper
					.readValue(data, listType).stream()
					.map(element -> yamlMapper.convertValue(element, type))
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
			return yamlMapper
					.readValue(data, listType)
					.entrySet().stream()
					.map(entry -> {
						var value = yamlMapper.convertValue(entry.getValue(), type);
						return entryMapper.apply(entry.getKey(), value);
					})
					.toList();
		} catch (JsonProcessingException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

}
