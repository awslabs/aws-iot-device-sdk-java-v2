package software.amazon.awssdk.eventstreamrpc;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import software.amazon.awssdk.crt.eventstream.ServerConnectionContinuationHandler;

/**
 * This is really the entire service interface base class
 */
public interface OperationContinuationHandlerFactory {
    Function<OperationContinuationHandlerContext, ? extends ServerConnectionContinuationHandler> getOperationHandler(final String operationName);
    Collection<String> getAllOperations();
    boolean hasHandlerForOperation(String operation);

    //this may not be a good use of a default method impl as implementers can override it
    //also InvalidServiceConfigurationException is a needed exception to be thrown from IpcServer
    default void validateAllOperationsSet() {
        if (!getAllOperations().stream().allMatch(op -> hasHandlerForOperation(op))) {
            String unmappedOperations = getAllOperations().stream()
                    .filter(op -> !hasHandlerForOperation(op)).collect(Collectors.joining(","));
            throw new InvalidServiceConfigurationException(this.getClass().getName() +
                    " does not have all operations mapped! Unmapped operations: {" + unmappedOperations + "}");
        }
    }
}
