package dev.nipafx.ginevra.parse.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonYamlParserTests {

	private final JacksonParser parser;

	JacksonYamlParserTests() {
		YAMLMapper mapper = new YAMLMapper();
		mapper.registerModule(new Jdk8Module());
		this.parser = JacksonParser.forYaml(mapper);
	}

	@Test
	void parseValue() {
		var author = parser.parseValue("""
						name: Jane Doe
						website: jane.doe
						""",
				Author.class);

		assertThat(author).isEqualTo(new Author("Jane Doe", "jane.doe"));
	}

	@Test
	void parseList() {
		var authors = parser.parseList("""
						- name: Jane Doe
						  website: jane.doe
						- name: John Doe
						  website: john.doe
						""",
				Author.class);

		assertThat(authors).containsExactly(
				new Author("Jane Doe", "jane.doe"),
				new Author("John Doe", "john.doe")
		);
	}

	@Test
	void parseMap() {
		var authors = parser.parseMap("""
						jane:
						  name: Jane Doe
						  website: jane.doe
						john:
						  name: John Doe
						  website: john.doe
						""",
				Author.class,
				(id, author) -> new Author(author.name(), author.website(), id));

		assertThat(authors).containsExactly(
				new Author("Jane Doe", "jane.doe", "jane"),
				new Author("John Doe", "john.doe", "john")
		);
	}

	private record Author(String name, String website, Optional<String> id) {

		public Author(String name, String website) {
			this(name, website, Optional.empty());
		}

		public Author(String name, String website, String id) {
			this(name, website, Optional.of(id));
		}

	}

}
