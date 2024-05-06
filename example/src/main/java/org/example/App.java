package org.example;

import io.vertx.core.json.JsonObject;
import org.example.dto.Job;
import org.example.dto.Person;

public class App {

  public static void main(String[] args) {
    Job job = Job.builder().name("Software Engineer").build();
    JsonObject json = job.toJson();
    System.err.println(json.encodePrettily());

    Job fromJson = Job.fromJson(json);
    System.err.printf("equals ? %b%n", fromJson.equals(job));

    Person person = Person.builder().name("Alice").age(30).bool(true).job(job).build();
    JsonObject personJson = person.toJson();
    Person personFromJson = Person.fromJson(personJson);
    System.err.printf("equals ? %b%n", personFromJson.equals(person));
  }
}
