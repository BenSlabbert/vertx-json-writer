package org.example.dto;

import org.example.processor.annotation.JsonWriter;

@JsonWriter
public record Person(String name, int age, boolean bool, Job job) {}
