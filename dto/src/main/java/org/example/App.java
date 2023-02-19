package org.example;

import org.example.dto.Job;
import org.example.dto.JobJsonWriter;
import org.example.dto.Person;
import org.example.dto.PersonJsonWriter;

public class App {

  public static void main(String[] args) {
    byte[] names = JobJsonWriter.toJson(new Job("job1"));
    System.err.println(new String(names));

    byte[] bytes = PersonJsonWriter.toJson(new Person("person", 1, new Job("job2")));
    System.err.println(new String(bytes));
  }
}
