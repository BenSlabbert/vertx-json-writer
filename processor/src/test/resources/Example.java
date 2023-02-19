package my.test;

import org.example.processor.annotation.JsonWriter;
import java.util.List;

@JsonWriter
public record Example (String name) {
}
