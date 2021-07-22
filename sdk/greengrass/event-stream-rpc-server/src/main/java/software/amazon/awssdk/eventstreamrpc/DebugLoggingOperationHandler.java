package software.amazon.awssdk.eventstreamrpc;

import com.google.gson.Gson;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Useful to set as a handler for an operation with no implementation yet.
 */
public class DebugLoggingOperationHandler extends OperationContinuationHandler
        <EventStreamJsonMessage, EventStreamJsonMessage, EventStreamJsonMessage, EventStreamJsonMessage> {
    private static final Logger LOGGER = Logger.getLogger(DebugLoggingOperationHandler.class.getName());
    private final OperationModelContext operationModelContext;

    public DebugLoggingOperationHandler(final OperationModelContext modelContext, final OperationContinuationHandlerContext context) {
        super(context);
        this.operationModelContext = modelContext;
    }

    @Override
    public OperationModelContext<EventStreamJsonMessage, EventStreamJsonMessage, EventStreamJsonMessage, EventStreamJsonMessage> getOperationModelContext() {
        return operationModelContext;
    }

    /**
     * Called when the underlying continuation is closed. Gives operations a chance to cleanup whatever
     * resources may be on the other end of an open stream. Also invoked when an underlying ServerConnection
     * is closed associated with the stream/continuation
     */
    @Override
    protected void onStreamClosed() {
        LOGGER.info(String.format("%s operation onStreamClosed()",
                operationModelContext.getOperationName()));
    }

    @Override
    public EventStreamJsonMessage handleRequest(EventStreamJsonMessage request) {
        LOGGER.info(String.format("%s operation handleRequest() ::  %s", operationModelContext.getOperationName(),
                operationModelContext.getServiceModel().toJson(request)));
        return new EventStreamJsonMessage() {
            @Override
            public byte[] toPayload(Gson gson) {
                return "{}".getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getApplicationModelType() {
                return operationModelContext.getResponseApplicationModelType();
            }
        };
    }

    @Override
    public void handleStreamEvent(EventStreamJsonMessage streamRequestEvent) {
        LOGGER.info(String.format("%s operation handleStreamEvent() ::  %s", operationModelContext.getOperationName(),
                operationModelContext.getServiceModel().toJson(streamRequestEvent)));
    }
}
