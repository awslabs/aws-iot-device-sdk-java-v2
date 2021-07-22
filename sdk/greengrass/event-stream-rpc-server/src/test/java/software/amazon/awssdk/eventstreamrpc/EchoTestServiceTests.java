package software.amazon.awssdk.eventstreamrpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awstest.*;
import software.amazon.awssdk.awstest.model.*;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.eventstreamrpc.echotest.EchoTestServiceRunner;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamOperationError;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

public class EchoTestServiceTests {
    static {
        Log.initLoggingToFile(Log.LogLevel.Trace, "crt-EchoTestService.log");
    }

    final BiConsumer<EchoTestRPC, MessageData> DO_ECHO_FN = (client, data) -> {
        final EchoMessageRequest request = new EchoMessageRequest();
        request.setMessage(data);
        final EchoMessageResponseHandler responseHandler = client.echoMessage(request, Optional.empty());
        EchoMessageResponse response = null;
        try {
            response = responseHandler.getResponse().get();
        } catch (InterruptedException | ExecutionException e) {
            Assertions.fail(e);
        }
        Assertions.assertEquals(request.getMessage(), response.getMessage(), "Data echoed back not equivalent!");
    };

    @Test
    public void testInvokeEchoMessage() throws Exception {
        final CompletableFuture<Void> clientErrorAfter = EchoTestServiceRunner.runLocalEchoTestServer((connection, client) -> {
            //note the successive calls are actually growing the same original message
            //rather than replacing any single field set. Instead of using lambdas, we could
            //use a parameterized test, but this has the benefit of proving successive calls work cleanly
            final MessageData data = new MessageData();
            data.setEnumMessage(FruitEnum.PINEAPPLE);
            DO_ECHO_FN.accept(client, data);

            data.setStringMessage("Hello EventStream RPC world");
            DO_ECHO_FN.accept(client, data);

            data.setBooleanMessage(true);
            DO_ECHO_FN.accept(client, data);

            data.setBlobMessage(new byte[] {23, 42, -120, -3, 53});
            DO_ECHO_FN.accept(client, data);

            data.setTimeMessage(Instant.now());
            DO_ECHO_FN.accept(client, data);
        });

        try {
            clientErrorAfter.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //eat this because it means there was no exception which is good
        } catch (ExecutionException e) {
            //throw this because it means the client did have a problem
            Assertions.fail(e.getCause());
        }
    }
    
    //@Test //this test takes too long to complete so turn it off by default
    public void testLongRunningServerOperations() throws Exception {
        EchoTestServiceRunner.runLocalEchoTestServerClientLoop((connection, client) -> {
            //note the successive calls are actually growing the same original message
            //rather than replacing any single field set. Instead of using lambdas, we could
            //use a parameterized test, but this has the benefit of proving successive calls work cleanly
            final MessageData data = new MessageData();
            data.setEnumMessage(FruitEnum.PINEAPPLE);
            DO_ECHO_FN.accept(client, data);

            try {
                Thread.sleep(50);  //sleep just so we're not completely pummeling the server?
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 1 << 17);
    }

    public void futureCausesOperationError(final CompletableFuture<?> future, Class<? extends EventStreamOperationError> clazz, String code) {
        try {
            future.get();
        } catch (ExecutionException e) {
            final Throwable t = e.getCause();
            if (t == null) {
                Assertions.fail("ExecutionException thrown has no CausedBy exception. Something else went wrong with future completion");
            } else if(!clazz.isInstance(t)) {
                Assertions.fail("ExecutionException thrown has unexpected caused type", t);
            } else {
                final EventStreamOperationError error = (EventStreamOperationError)t;
                Assertions.assertEquals(code, error.getErrorCode(), "Non-matching error code returned");
            }
        } catch (InterruptedException e) {
            Assertions.fail(e.getCause());
        }
    }

    @Test
    public void testInvokeErrorOperation() throws Exception {
        final CompletableFuture<Void> clientErrorAfter = EchoTestServiceRunner.runLocalEchoTestServer((connection, client) -> {
            final CauseServiceErrorResponseHandler responseHandler = client.causeServiceError(new CauseServiceErrorRequest(), Optional.empty());
            futureCausesOperationError(responseHandler.getResponse(), ServiceError.class, "ServiceError");

            //after an error, perform another operation on the same connection that should still be open for business
            final MessageData data = new MessageData();
            data.setStringMessage("Post error string message");
            DO_ECHO_FN.accept(client, data);
        });
        try {
            clientErrorAfter.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //eat this because it means there was no exception which is good
        } catch (ExecutionException e) {
            //throw this because it means the client did have a problem
            Assertions.fail(e.getCause());
        }
    }

    @Test
    public void testInvokeEchoStreamMessages() throws Exception {
        final CompletableFuture<Void> clientErrorAfter = EchoTestServiceRunner.runLocalEchoTestServer((connection, client) -> {
            final EchoStreamingRequest req = EchoStreamingRequest.VOID;

            final List<EchoStreamingMessage> messagesToSend = new ArrayList<>();

            final EchoStreamingMessage msg1 = new EchoStreamingMessage();
            final MessageData data1 = new MessageData();
            data1.setStringMessage("fooStreamingMessage");
            msg1.setStreamMessage(data1);
            messagesToSend.add(msg1);

            final EchoStreamingMessage msg2 = new EchoStreamingMessage();
            final MessageData data2 = new MessageData();
            data2.setEnumMessage(FruitEnum.ORANGE);
            msg2.setStreamMessage(data2);
            messagesToSend.add(msg2);

            final EchoStreamingMessage msg3 = new EchoStreamingMessage();
            final MessageData data3 = new MessageData();
            data3.setTimeMessage(Instant.now());
            msg3.setStreamMessage(data3);
            messagesToSend.add(msg3);

            final EchoStreamingMessage msg4 = new EchoStreamingMessage();
            final MessageData data4 = new MessageData();
            final List<String> listOfStrings = new ArrayList<>(3);
            listOfStrings.add("item1");
            listOfStrings.add("item2");
            listOfStrings.add("item3");
            data4.setStringListMessage(listOfStrings);
            msg4.setStreamMessage(data4);
            messagesToSend.add(msg4);

            final EchoStreamingMessage msg5 = new EchoStreamingMessage();
            final Pair kvPair = new Pair();
            kvPair.setKey("keyTest");
            kvPair.setValue("testValue");
            msg5.setKeyValuePair(kvPair);

            final CompletableFuture<Void> finishedStreamingEvents = new CompletableFuture<>();
            final CompletableFuture<Void> streamClosedFuture = new CompletableFuture<>();
            final Iterator<EchoStreamingMessage> sentIterator = messagesToSend.iterator();
            final int numEventsVerified[] = new int[] { 0 };
            final EchoStreamMessagesResponseHandler responseHandler = client.echoStreamMessages(req, Optional.of(new StreamResponseHandler<EchoStreamingMessage>() {
                @Override
                public void onStreamEvent(EchoStreamingMessage streamEvent) {
                    if (sentIterator.hasNext()) {
                        final EchoStreamingMessage expectedMsg = sentIterator.next();
                        if (!expectedMsg.equals(streamEvent)) {
                            finishedStreamingEvents.completeExceptionally(new RuntimeException("Steam message echo'd is not the same as sent!"));
                        } else {
                            numEventsVerified[0]++;
                            if (numEventsVerified[0] == messagesToSend.size()) {
                                finishedStreamingEvents.complete(null);
                            }
                        }
                    }
                    else {
                        finishedStreamingEvents.completeExceptionally(new RuntimeException("Service returned an extra unexpected message back over stream: " +
                                EchoTestRPCServiceModel.getInstance().toJsonString(streamEvent)));
                    }
                }

                @Override
                public boolean onStreamError(Throwable error) {
                    finishedStreamingEvents.completeExceptionally(
                            new RuntimeException("Service threw an error while waiting for stream events!", error));
                    streamClosedFuture.completeExceptionally(new RuntimeException("Service threw an error while waiting for stream events!", error));
                    return true;
                }

                @Override
                public void onStreamClosed() {
                    streamClosedFuture.complete(null);
                }
            }));
            messagesToSend.stream().forEachOrdered(event -> {
                responseHandler.sendStreamEvent(event); //no need to slow down?
            });

            try {
                finishedStreamingEvents.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                Assertions.fail(e);
            }

            //after a streaming operation, perform another operation on the same connection
            final MessageData data = new MessageData();
            data.setEnumMessage(FruitEnum.PINEAPPLE);
            DO_ECHO_FN.accept(client, data);

            //now command the stream to close
            final EchoStreamingMessage closeMsg = new EchoStreamingMessage();
            final MessageData dataClose = new MessageData();
            dataClose.setStringMessage("close");    //implementation of the close operation in test-codegen-model
            closeMsg.setStreamMessage(dataClose);
            responseHandler.sendStreamEvent(closeMsg);
            try {
                streamClosedFuture.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                Assertions.fail(e);
            }
        });
        try {
            clientErrorAfter.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //eat this because it means there was no exception which is good
        } catch (ExecutionException e) {
            //throw this because it means the client did have a problem
            Assertions.fail(e.getCause());
        }
    }

    @Test
    public void testInvokeEchoStreamError() throws Exception {
        final CompletableFuture<Void> clientErrorAfter = EchoTestServiceRunner.runLocalEchoTestServer((connection, client) -> {

            final CompletableFuture<Throwable> exceptionReceivedFuture = new CompletableFuture<>();
            final CauseStreamServiceToErrorResponseHandler streamErrorResponseHandler = client.causeStreamServiceToError(EchoStreamingRequest.VOID, Optional.of(new StreamResponseHandler<EchoStreamingMessage>() {
                @Override
                public void onStreamEvent(EchoStreamingMessage streamEvent) {
                    exceptionReceivedFuture.completeExceptionally(new RuntimeException("Stream event received when expecting error!"));
               }

                @Override
                public boolean onStreamError(Throwable error) {
                    //this is normal, but we are looking for a specific one
                    exceptionReceivedFuture.complete(error);
                    return true;
                }

                @Override
                public void onStreamClosed() {
                    if (!exceptionReceivedFuture.isDone()) {
                        exceptionReceivedFuture.completeExceptionally(new RuntimeException("Stream closed before exception thrown!"));
                    }
                }
            }));

            try {
                final EchoStreamingMessage msg = new EchoStreamingMessage();
                final MessageData data = new MessageData();
                data.setStringMessage("basicStringMessage");
                msg.setStreamMessage(data);
                streamErrorResponseHandler.sendStreamEvent(msg);   //sends message, exception should be is the response
                final Throwable t = exceptionReceivedFuture.get();
                Assertions.assertTrue(t instanceof ServiceError);
                final ServiceError error = (ServiceError)t;
                Assertions.assertEquals("ServiceError", error.getErrorCode());
            } catch (InterruptedException e) {
                Assertions.fail(e);
            } catch (ExecutionException e) {
                Assertions.fail(e);
            }

            //after a streaming response error, perform another operation on the same
            //connection that should still be open for business
            final MessageData data = new MessageData();
            data.setStringMessage("Post stream error string message");
            DO_ECHO_FN.accept(client, data);
        });
        try {
            clientErrorAfter.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //eat this because it means there was no exception which is good
        } catch (ExecutionException e) {
            //throw this because it means the client did have a problem
            Assertions.fail(e.getCause());
        }
    }
}
