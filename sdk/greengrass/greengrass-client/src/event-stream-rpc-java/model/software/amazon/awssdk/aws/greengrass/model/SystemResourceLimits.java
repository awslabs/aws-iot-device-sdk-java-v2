package software.amazon.awssdk.aws.greengrass.model;

import com.google.gson.annotations.Expose;
import java.lang.Double;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import java.util.Optional;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public class SystemResourceLimits implements EventStreamJsonMessage {
  public static final String APPLICATION_MODEL_TYPE = "aws.greengrass#SystemResourceLimits";

  public static final SystemResourceLimits VOID;

  static {
    VOID = new SystemResourceLimits() {
      @Override
      public boolean isVoid() {
        return true;
      }
    };
  }

  @Expose(
      serialize = true,
      deserialize = true
  )
  private Optional<Long> memory;

  @Expose(
      serialize = true,
      deserialize = true
  )
  private Optional<Double> cpus;

  public SystemResourceLimits() {
    this.memory = Optional.empty();
    this.cpus = Optional.empty();
  }

  public Long getMemory() {
    if (memory.isPresent()) {
      return memory.get();
    }
    return null;
  }

  public void setMemory(final Long memory) {
    this.memory = Optional.ofNullable(memory);
  }

  public Double getCpus() {
    if (cpus.isPresent()) {
      return cpus.get();
    }
    return null;
  }

  public void setCpus(final Double cpus) {
    this.cpus = Optional.ofNullable(cpus);
  }

  @Override
  public String getApplicationModelType() {
    return APPLICATION_MODEL_TYPE;
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    if (!(rhs instanceof SystemResourceLimits)) return false;
    if (this == rhs) return true;
    final SystemResourceLimits other = (SystemResourceLimits)rhs;
    boolean isEquals = true;
    isEquals = isEquals && this.memory.equals(other.memory);
    isEquals = isEquals && this.cpus.equals(other.cpus);
    return isEquals;
  }

  @Override
  public int hashCode() {
    return Objects.hash(memory, cpus);
  }
}
