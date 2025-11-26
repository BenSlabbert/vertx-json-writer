/* Licensed under Apache-2.0 2025. */
package github.benslabbert.vertxjsonwriter.processor;

final class Util {

  private Util() {}

  static String getGenericType(String className) {
    return className.substring(className.indexOf('<') + 1, className.indexOf('>'));
  }

  /// my.test.Nested.Inner
  /// if this is an inner type, return Nested_Inner
  static String simpleName(String classname) {
    int firstClassIdx = 0;
    for (int i = 0; i < classname.length(); i++) {
      if (Character.isUpperCase(classname.charAt(i))) {
        firstClassIdx = i;
        break;
      }
    }
    return classname.substring(firstClassIdx).replaceAll("\\.", "_");
  }
}
