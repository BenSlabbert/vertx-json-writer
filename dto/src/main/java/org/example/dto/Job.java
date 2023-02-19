package org.example.dto;

import org.example.processor.annotation.JsonWriter;

@JsonWriter
public record Job(String name) {}
