# Json-Writer

This project is a simple code generator that will generate some handly toJson and fromJson methods that will convert a java record into a Json object with no runtime reflection

For example usage see [test](./dto/src/main/java/org/example/App.java)

A record like:

```java
package org.example.dto;

import org.example.processor.annotation.JsonWriter;

@JsonWriter
public record Job(String name) {}
```

Will have a class like this generated:

```java
package org.example.dto;

import org.example.dto.Job;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class JobJsonWriter {

	public static final String NAME = "name";

	public static Map<String, String> toMap(Job in) {
		return Map.of(
			NAME, in.name()
		);
	}

	public static Job fromMap(Map<String, String> in) {
		String name = in.get(NAME);
		return new Job(name);
	}

	public static byte[] toJsonBytes(Job in) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			JsonNode node = toJsonNode(objectMapper.createObjectNode(),in);
			return objectMapper.writeValueAsBytes(node);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toJsonString(Job in) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			JsonNode node = toJsonNode(objectMapper.createObjectNode(),in);
			return objectMapper.writeValueAsString(node);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	static JsonNode toJsonNode(ObjectNode root, Job in) {
		root.put(NAME, in.name());
		return root;
	}
}
```
