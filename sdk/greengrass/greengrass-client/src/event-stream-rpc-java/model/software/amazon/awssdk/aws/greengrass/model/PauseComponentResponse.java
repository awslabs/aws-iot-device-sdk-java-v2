package software.amazon.awssdk.aws.greengrass.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public class PauseComponentResponse implements EventStreamJsonMessage {
  public static final String APPLICATION_MODEL_TYPE = "aws.greengrass#PauseComponentResponse";

  public static final PauseComponentResponse VOID;

  static {
    VOID = new PauseComponentResponse() {
      @Override
      public boolean isVoid() {
        return true;
      }
    };
  }

  public PauseComponentResponse() {
  }

  @Override
  public String getApplicationModelType() {
    return APPLICATION_MODEL_TYPE;
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    if (!(rhs instanceof PauseComponentResponse)) return false;
    if (this == rhs) return true;
    final PauseComponentResponse other = (PauseComponentResponse)rhs;
    boolean isEquals = true;
    return isEquals;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }
}
