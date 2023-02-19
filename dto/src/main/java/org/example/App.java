package org.example;

import java.util.Map;
import org.example.dto.Job;
import org.example.dto.JobJsonWriter;
import org.example.dto.Person;
import org.example.dto.PersonJsonWriter;
import org.example.dto.PrimitiveEntity;
import org.example.dto.PrimitiveEntityJsonWriter;

public class App {

  public static void main(String[] args) {
    byte[] names = JobJsonWriter.toJson(new Job("job1"));
    System.err.println(new String(names));

    byte[] bytes = PersonJsonWriter.toJson(new Person("person1", 1, true, new Job("job2")));
    System.err.println(new String(bytes));

    PrimitiveEntity c =
        new PrimitiveEntity("name", 1, true, 1.2F, 3.7, (short) 2, 'c', (byte) 0x11, 6L);

    Map<String, String> stringStringMap1 = PrimitiveEntityJsonWriter.toMap(c);
    System.err.println(stringStringMap1);
    System.err.println(new String(PrimitiveEntityJsonWriter.toJson(c)));
  }
}
