package org.example.dto;

import org.example.processor.annotation.JsonWriter;

@JsonWriter
public record PrimitiveEntity(
    String name,
    int number,
    boolean bool,
    float fl,
    double dub,
    short sh,
    char ch,
    byte b,
    long l) {}
