package my.test;

import org.example.processor.annotation.JsonWriter;
import java.util.List;

@JsonWriter(name = "example")
public record Example (String name) {
}
