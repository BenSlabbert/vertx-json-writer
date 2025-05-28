/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

final class Util {

  private Util() {}

  static String getGenericType(String className) {
    return className.substring(className.indexOf('<') + 1, className.indexOf('>'));
  }
}
