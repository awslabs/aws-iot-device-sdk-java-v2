package software.amazon.awssdk.eventstreamrpc;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import software.amazon.awssdk.crt.eventstream.Header;
import software.amazon.awssdk.crt.eventstream.MessageFlags;
import software.amazon.awssdk.crt.eventstream.MessageType;
import software.amazon.awssdk.crt.eventstream.ServerConnectionContinuationHandler;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamOperationError;

public abstract class OperationContinuationHandler
            <RequestType extends EventStreamJsonMessage, ResponseType extends EventStreamJsonMessage,
            StreamingRequestType extends EventStreamJsonMessage, StreamingResponseType extends EventStreamJsonMessage>
        extends ServerConnectionContinuationHandler
        implements StreamEventPublisher<StreamingResponseType> {
    private static final Logger LOGGER = Logger.getLogger(OperationContinuationHandler.class.getName());

    private OperationContinuationHandlerContext context;
    private List<Header> initialRequestHeaders;
    private RequestType initialRequest;

    abstract public OperationModelContext<RequestType, ResponseType, StreamingRequestType, StreamingResponseType>
        getOperationModelContext();

    public OperationContinuationHandler(final OperationContinuationHandlerContext context) {
        super(context.getContinuation());
        this.context = context;
    }

    @Override
    final protected void onContinuationClosed() {
        LOGGER.finer(String.format("%s stream continuation closed.", getOperationName()));
        try {
            onStreamClosed();
        }
        catch(Exception e) {
            LOGGER.severe(String.format("%s threw %s: %s", getOperationName(), e.getClass().getCanonicalName(), e.getMessage()));
        }
    }


    final protected Class<RequestType> getRequestClass() {
        return getOperationModelContext().getRequestTypeClass();
    }

    final protected Class<ResponseType> getResponseClass() {
        return getOperationModelContext().getResponseTypeClass();
    }

    final protected Class<StreamingRequestType> getStreamingRequestClass() {
        return getOperationModelContext().getStreamingRequestTypeClass().get();
    }

    final protected Class<StreamingResponseType> getStreamingResponseClass() {
        return getOperationModelContext().getStreamingResponseTypeClass().get();
    }

    /**
     * Returns the operation name implemented by the handler. Generated code should populate this
     * @return
     */
    private String getOperationName() {
        return getOperationModelContext().getOperationName();
    }

    /**
     * Called when the underlying continuation is closed. Gives operations a chance to cleanup whatever
     * resources may be on the other end of an open stream. Also invoked when an underlying ServerConnection
     * is closed associated with the stream/continuation
     */
    protected abstract void onStreamClosed();

    /**
     * Should return  true iff operation has either streaming input or output. If neither, return false and only allows
     * an initial-request -> initial->response before closing the continuation.
     *
     * @return
     */
    final protected boolean isStreamingOperation() {
        return getOperationModelContext().isStreamingOperation();
    }

    /**
     * Main request handler for any operation to do work on an initial request. Streaming response operations
     * still must send an initial-response which is empty.
     *
     * Implementers should not call sendStreamEvent() during handleRequest() to send a streaming response after
     * an initial-response. This would violate the sequence of messages expected to occur for the specific
     * operation. Override "afterHandleRequest()" as a way of being informed of the quickest possible time
     * to sent a stream response after handleRequest returns.
     *
     * @param request
     * @return
     */
    public abstract ResponseType handleRequest(final RequestType request);

    /**
     * Override to appropriately enforce stream responses are sent after the initial response.
     * This only gets called if handleRequest returns normally and starts to send a response.
     */
    public void afterHandleRequest() { }

    /**
     * Handle an incoming stream event from the connected client on the operation.
     *
     * If the implementation throws an exception, the framework will respond with the modeled
     * exception to the client, if it is modeled. If it is not modeled, it will respond with
     * an internal error and log appropriately. Either case, throwing an exception will result
     * in closing the stream. To keep the stream open, do not throw
     *
     * @param streamRequestEvent
     */
    public abstract void handleStreamEvent(final StreamingRequestType streamRequestEvent);

    /**
     * Retrieves the underlying EventStream request headers for inspection. Pulling these headers
     * out shouldn't be necessary as it means operations are aware of the underlying protocol. Any
     * headers needed to be pulled are candidates for what should be in the service model directly
     * @return
     */
    final protected List<Header> getInitialRequestHeaders() {
        return initialRequestHeaders;   //not a defensive copy
    }

    /**
     * Retrieves the initial request object that initiated the stream
     *
     * For use in handler implementations if initial request is wanted to handle further in-out events
     * May be unecessary memory, but also initial request may be used by framework to log errors with
     * 'request-id' like semantics
     *
     * @return
     */
    final protected RequestType getInitialRequest() {
        return initialRequest;
    }

    /**
     * Retrieves the operation handler context. Use for inspecting state outside of the
     * limited scope of this operation handler.
     *
     * @return
     */
    final protected OperationContinuationHandlerContext getContext () {
        return context;
    }

    /**
     * TODO: close stream should be sent with the final message, or separately? Either should be fine
     * @return
     */
    @Override
    final public CompletableFuture<Void> closeStream() {
        LOGGER.fine(String.format("[%s] closing stream", getOperationName()));
        return continuation.sendMessage(null, null,
                MessageType.ApplicationMessage, MessageFlags.TerminateStream.getByteValue())
            .whenComplete((res, ex) -> {
                if (ex != null) {
                    LOGGER.fine(String.format("[%s] closed stream", getOperationName()));
                } else {
                    LOGGER.fine(String.format("[%s] %s closing stream: ", getOperationName(),
                            ex.getClass().getName(), ex.getMessage()));
                }
                continuation.close();
            });
    }

    /**
     * Used so other processes/events going on in the server can push events back into this
     * operation's opened continuation
     *
     * @param streamingResponse
     */
    final public CompletableFuture<Void> sendStreamEvent(final StreamingResponseType streamingResponse) {
        return sendMessage(streamingResponse, false);
    }

    final protected CompletableFuture<Void> sendMessage(final EventStreamJsonMessage message, final boolean close) {
        if (continuation.isClosed()) { //is this check necessary?
            return CompletableFuture.supplyAsync(() -> { throw new EventStreamClosedException(continuation.getNativeHandle()); });
        }
        final List<Header> responseHeaders = new ArrayList<>();
        byte[] outputPayload = getOperationModelContext().getServiceModel().toJson(message);
        responseHeaders.add(Header.createHeader(EventStreamRPCServiceModel.CONTENT_TYPE_HEADER,
                EventStreamRPCServiceModel.CONTENT_TYPE_APPLICATION_JSON));
        responseHeaders.add(Header.createHeader(EventStreamRPCServiceModel.SERVICE_MODEL_TYPE_HEADER, message.getApplicationModelType()));

        return continuation.sendMessage(responseHeaders, outputPayload, MessageType.ApplicationMessage,
                close ? MessageFlags.TerminateStream.getByteValue() : 0)
                .whenComplete((res, ex) -> {
                    if (close) {
                        continuation.close();
                    }
                });
    }

    /**
     * Sends an error over the stream. Same method is used for errors from the initial response or any errors
     * that occur while the stream is open. It will always close the stream/continuation on the same message
     * using the terminate flag on the same message
     * @param message
     * @return
     */
    final protected CompletableFuture<Void> sendModeledError(final EventStreamJsonMessage message) {
        if (continuation.isClosed()) {  //is this check necessary?
            return CompletableFuture.supplyAsync(() -> { throw new EventStreamClosedException(continuation.getNativeHandle()); });
        }
        final List<Header> responseHeaders = new ArrayList<>();
        byte[] outputPayload = getOperationModelContext().getServiceModel().toJson(message);
        responseHeaders.add(Header.createHeader(EventStreamRPCServiceModel.CONTENT_TYPE_HEADER,
                EventStreamRPCServiceModel.CONTENT_TYPE_APPLICATION_JSON));
        responseHeaders.add(Header.createHeader(EventStreamRPCServiceModel.SERVICE_MODEL_TYPE_HEADER, message.getApplicationModelType()));

        return continuation.sendMessage(responseHeaders, outputPayload,
                MessageType.ApplicationError, MessageFlags.TerminateStream.getByteValue())
                .whenComplete((res, ex) -> {
                    //complete silence on any error closing here
                    continuation.close();
                });
    }

    private void invokeAfterHandleRequest() {
        try {
            afterHandleRequest();
        } catch (Exception e) {
            LOGGER.warning(String.format("%s.%s afterHandleRequest() threw %s: %s",
                    getOperationModelContext().getServiceModel().getServiceName(),
                    getOperationName(), e.getClass().getCanonicalName(),
                    e.getMessage()));
        }
    }

    @Override
    final protected void onContinuationMessage(List<Header> list, byte[] bytes, MessageType messageType, int i) {
        LOGGER.fine("Continuation native id: " + continuation.getNativeHandle());
        final EventStreamRPCServiceModel serviceModel = getOperationModelContext().getServiceModel();

        try {
            if (initialRequest != null) {
                //TODO: FIX empty close messages arrive here and throw exception
                final StreamingRequestType streamEvent = serviceModel.fromJson(getStreamingRequestClass(), bytes);
                //exceptions occurring during this processing will result in closure of stream
                handleStreamEvent(streamEvent);
            } else { //this is the initial request
                initialRequestHeaders = new ArrayList<>(list);
                initialRequest = serviceModel.fromJson(getRequestClass(), bytes);
                //call into business logic
                
                final ResponseType result = handleRequest(initialRequest);
                if (result != null) {
                    if (!getResponseClass().isInstance(result)) {
                        throw new RuntimeException("Handler for operation [" + getOperationName()
                                + "] did not return expected type. Found: " + result.getClass().getName());
                    }
                    sendMessage(result, !isStreamingOperation()).whenComplete((res, ex) -> {
                        if (ex != null) {
                            LOGGER.severe(ex.getClass().getName() + " sending response message: " + ex.getMessage());
                        } else {
                            LOGGER.finer("Response successfully sent");
                        }
                    });
                    invokeAfterHandleRequest();
                } else {
                    //not streaming, but null response? we have a problem
                    throw new RuntimeException("Operation handler returned null response!");
                }
            }
        } catch (EventStreamOperationError e) {
            //We do not check if the specific exception thrown is a part of the core service?
            sendModeledError(e);
            invokeAfterHandleRequest();
        } catch (Exception e) {
            final List<Header> responseHeaders = new ArrayList<>(1);
            byte[] outputPayload = "InternalServerError".getBytes(StandardCharsets.UTF_8);
            responseHeaders.add(Header.createHeader(EventStreamRPCServiceModel.CONTENT_TYPE_HEADER,
                    EventStreamRPCServiceModel.CONTENT_TYPE_APPLICATION_TEXT));
            // TODO: are there any exceptions we wouldn't want to return a generic server fault?
            // TODO: this is the kind of exception that should be logged with a request ID especially in a server-client context
            LOGGER.severe(String.format("[%s] operation threw unexpected %s: %s", getOperationName(),
                    e.getClass().getCanonicalName(), e.getMessage()));

            continuation.sendMessage(responseHeaders, outputPayload, MessageType.ApplicationError, MessageFlags.TerminateStream.getByteValue())
                    .whenComplete((res, ex) -> {
                        if (ex != null) {
                            LOGGER.severe(ex.getClass().getName() + " sending error response message: " + ex.getMessage());
                        }
                        else {
                            LOGGER.finer("Error response successfully sent");
                        }
                        continuation.close();
                    });
        }
    }
}
