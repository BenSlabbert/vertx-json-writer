/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

sealed interface SchemaGenerator
    permits ArraySchemaGenerator,
        BooleanSchemaGenerator,
        DateSchemaGenerator,
        DateTimeSchemaGenerator,
        IntegerSchemaGenerator,
        NumberSchemaGenerator,
        ObjectSchemaGenerator,
        StringSchemaGenerator {

  String print();
}
