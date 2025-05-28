/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

class GenerationException extends RuntimeException {

  GenerationException(Throwable cause) {
    super(cause);
  }

  GenerationException(String msg) {
    super(msg);
  }
}
