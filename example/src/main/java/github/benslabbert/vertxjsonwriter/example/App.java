/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.example;

import github.benslabbert.vertxjsonwriter.example.dto.Job;
import github.benslabbert.vertxjsonwriter.example.dto.JobJson;
import github.benslabbert.vertxjsonwriter.example.dto.Person;
import github.benslabbert.vertxjsonwriter.example.dto.PersonJson;
import io.vertx.core.json.JsonObject;

public class App {

  public static void main(String[] args) {
    Job job = Job.builder().name("Software Engineer").build();
    JsonObject json = JobJson.toJson(job);
    System.err.println(json.encodePrettily());

    Job fromJson = JobJson.fromJson(json);
    System.err.printf("equals ? %b%n", fromJson.equals(job));

    Person person = Person.builder().name("Alice").age(30).bool(true).job(job).build();
    JsonObject personJson = PersonJson.toJson(person);
    Person personFromJson = PersonJson.fromJson(personJson);
    System.err.printf("equals ? %b%n", personFromJson.equals(person));
  }
}
