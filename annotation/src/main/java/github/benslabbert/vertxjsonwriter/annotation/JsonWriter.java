/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JsonWriter {

  // todo: make this a package level annotation which can apply
  //  to all classes in the package
}
