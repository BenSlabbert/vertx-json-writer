/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxjsonwriter.processor;

public class GenerationException extends RuntimeException {

  public GenerationException(Throwable cause) {
    super(cause);
  }

  public GenerationException(String msg) {
    super(msg);
  }
}
