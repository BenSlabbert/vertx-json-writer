package my.test;

import org.example.processor.annotation.JsonWriter;
import java.util.List;

@JsonWriter
public record Example(String name) {

  public static Builder builder() {
    return null;
  }

  public interface Builder {
    Builder name(String name);

    Example build();
  }
}
