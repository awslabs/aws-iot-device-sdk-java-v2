package software.amazon.awssdk.aws.greengrass.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public class ResumeComponentResponse implements EventStreamJsonMessage {
  public static final String APPLICATION_MODEL_TYPE = "aws.greengrass#ResumeComponentResponse";

  public static final ResumeComponentResponse VOID;

  static {
    VOID = new ResumeComponentResponse() {
      @Override
      public boolean isVoid() {
        return true;
      }
    };
  }

  public ResumeComponentResponse() {
  }

  @Override
  public String getApplicationModelType() {
    return APPLICATION_MODEL_TYPE;
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    if (!(rhs instanceof ResumeComponentResponse)) return false;
    if (this == rhs) return true;
    final ResumeComponentResponse other = (ResumeComponentResponse)rhs;
    boolean isEquals = true;
    return isEquals;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }
}
