package software.amazon.awssdk.eventstreamrpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.eventstream.ServerConnectionContinuationHandler;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;
import software.amazon.awssdk.eventstreamrpc.test.TestAuthNZHandlers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.function.Function;

/**
 * Note: use different ports for different tests
 */
public class IpcServerTests {
    private static final Random RANDOM = new Random(); //default instantiation uses time
    public static int randomPort() {
        return RANDOM.nextInt(65535-1024) + 1024;
    }
    static {
        Log.initLoggingToFile(Log.LogLevel.Trace, "crt-IpcServerTests.log");
    }

    @Test
    public void testStartStopIpcServer() {
        final int port = randomPort();
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        final EventStreamRPCServiceHandler handler = new EventStreamRPCServiceHandler() {
            @Override
            protected EventStreamRPCServiceModel getServiceModel() {
                return new EventStreamRPCServiceModel() {
                    @Override
                    public String getServiceName() { return "TestService"; }

                    /**
                     * Retreives all operations on the service
                     *
                     * @return
                     */
                    @Override
                    public Collection<String> getAllOperations() { return new HashSet<>(); }

                    @Override
                    protected Optional<Class<? extends EventStreamJsonMessage>> getServiceClassType(String applicationModelType) {
                        return Optional.empty();
                    }

                    @Override
                    public OperationModelContext getOperationModelContext(String operationName) {
                        return null;
                    }
                };
            }

            @Override
            public Function<OperationContinuationHandlerContext, ? extends ServerConnectionContinuationHandler> getOperationHandler(String operationName) {
                return null;
            }

            @Override
            public boolean hasHandlerForOperation(String operation) { return true; }
        };
        handler.setAuthenticationHandler(TestAuthNZHandlers.getAuthNHandler());
        handler.setAuthorizationHandler(TestAuthNZHandlers.getAuthZHandler());

        try(final EventLoopGroup elGroup = new EventLoopGroup(1)) {
            final IpcServer ipcServer = new IpcServer(elGroup, socketOptions, null, "127.0.0.1", port, handler);
            ipcServer.runServer();

            Socket clientSocket = new Socket();
            SocketAddress address = new InetSocketAddress("127.0.0.1", port);
            clientSocket.connect(address, 3000);
            //no real assertion to be made here as long as the above connection works...
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testIpcServerDoubleStartFailure() {
        final int port = randomPort();
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        final EventStreamRPCServiceHandler handler = new EventStreamRPCServiceHandler() {
            @Override
            protected EventStreamRPCServiceModel getServiceModel() {
                return new EventStreamRPCServiceModel() {
                    @Override
                    public String getServiceName() { return "TestService"; }

                    @Override
                    public Collection<String> getAllOperations() { return new HashSet<>(); }

                    @Override
                    protected Optional<Class<? extends EventStreamJsonMessage>> getServiceClassType(String applicationModelType) { return Optional.empty(); }

                    @Override
                    public OperationModelContext getOperationModelContext(String operationName) { return null; }
                };
            }

            @Override
            public Function<OperationContinuationHandlerContext, ? extends ServerConnectionContinuationHandler> getOperationHandler(String operationName) {
                return null;
            }

            @Override
            public boolean hasHandlerForOperation(String operation) { return true; }
        };
        handler.setAuthenticationHandler(TestAuthNZHandlers.getAuthNHandler());
        handler.setAuthorizationHandler(TestAuthNZHandlers.getAuthZHandler());

        try (final EventLoopGroup elGroup = new EventLoopGroup(1)) {
            final IpcServer ipcServer = new IpcServer(elGroup, socketOptions, null, "127.0.0.1", port, handler);
            ipcServer.runServer();
            Assertions.assertThrows(IllegalStateException.class, () -> {
                ipcServer.runServer();
            });
        }
    }

    @Test
    public void testIpcServerModelNotSet() {
        final int port = randomPort();
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        final EventStreamRPCServiceHandler handler = new EventStreamRPCServiceHandler() {
            @Override
            protected EventStreamRPCServiceModel getServiceModel() {
                return null;
            }

            @Override
            public Function<OperationContinuationHandlerContext, ? extends ServerConnectionContinuationHandler> getOperationHandler(String operationName) {
                return null;
            }

            @Override
            public boolean hasHandlerForOperation(String operation) { return true; }   //what'll trigger the validation failure
        };
        handler.setAuthenticationHandler(TestAuthNZHandlers.getAuthNHandler());
        handler.setAuthorizationHandler(TestAuthNZHandlers.getAuthZHandler());

        try (final EventLoopGroup elGroup = new EventLoopGroup(1)) {
            final IpcServer ipcServer = new IpcServer(elGroup, socketOptions, null, "127.0.0.1", port, handler);
            Assertions.assertThrows(InvalidServiceConfigurationException.class, () -> {
                ipcServer.runServer();
            });
        }
    }

    @Test
    public void testIpcServerOperationNotSet() {
        final int port = randomPort();
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;
        final Set<String> OPERATION_SET = new HashSet<>();
        OPERATION_SET.add("dummyOperationName");

        final EventStreamRPCServiceHandler handler = new EventStreamRPCServiceHandler() {
            @Override
            protected EventStreamRPCServiceModel getServiceModel() {
                return new EventStreamRPCServiceModel() {
                    @Override
                    public String getServiceName() { return "TestService"; }

                    @Override
                    public Collection<String> getAllOperations() { return OPERATION_SET; }

                    @Override
                    protected Optional<Class<? extends EventStreamJsonMessage>> getServiceClassType(String applicationModelType) { return Optional.empty(); }

                    @Override
                    public OperationModelContext getOperationModelContext(String operationName) { return null; }
                };
            }

            @Override
            public Function<OperationContinuationHandlerContext, ? extends ServerConnectionContinuationHandler> getOperationHandler(String operationName) {
                return null;
            }

            @Override
            public boolean hasHandlerForOperation(String operation) { return false; }   //what'll trigger the validation failure
        };
        handler.setAuthenticationHandler(TestAuthNZHandlers.getAuthNHandler());
        handler.setAuthorizationHandler(TestAuthNZHandlers.getAuthZHandler());

        try (final EventLoopGroup elGroup = new EventLoopGroup(1)) {
            final IpcServer ipcServer = new IpcServer(elGroup, socketOptions, null, "127.0.0.1", port, handler);
            Assertions.assertThrows(InvalidServiceConfigurationException.class, () -> {
                ipcServer.runServer();
            });
        }
    }

    @Test
    public void testIpcServerAuthNUnset() {
        final int port = randomPort();
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;
        final Set<String> OPERATION_SET = new HashSet<>();
        OPERATION_SET.add("dummyOperationName");

        final EventStreamRPCServiceHandler handler = new EventStreamRPCServiceHandler() {
            @Override
            protected EventStreamRPCServiceModel getServiceModel() {
                return new EventStreamRPCServiceModel() {
                    @Override
                    public String getServiceName() { return "TestService"; }

                    @Override
                    public Collection<String> getAllOperations() { return new HashSet<>(); }

                    @Override
                    protected Optional<Class<? extends EventStreamJsonMessage>> getServiceClassType(String applicationModelType) { return Optional.empty(); }

                    @Override
                    public OperationModelContext getOperationModelContext(String operationName) { return null; }
                };
            }

            @Override
            public Function<OperationContinuationHandlerContext, ? extends ServerConnectionContinuationHandler> getOperationHandler(String operationName) {
                return null;
            }

            @Override
            public boolean hasHandlerForOperation(String operation) { return true; }
        };
        //missing handler.setAuthenticationHandler(TestAuthNZHandlers.getAuthNHandler());
        handler.setAuthorizationHandler(TestAuthNZHandlers.getAuthZHandler());

        try (final EventLoopGroup elGroup = new EventLoopGroup(1)) {
            final IpcServer ipcServer = new IpcServer(elGroup, socketOptions, null, "127.0.0.1", port, handler);
            Assertions.assertThrows(InvalidServiceConfigurationException.class, () -> {
                ipcServer.runServer();
            });
        }
    }

    @Test
    public void testIpcServerAuthZUnset() {
        final int port = randomPort();
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;
        final Set<String> OPERATION_SET = new HashSet<>();
        OPERATION_SET.add("dummyOperationName");

        final EventStreamRPCServiceHandler handler = new EventStreamRPCServiceHandler() {
            @Override
            protected EventStreamRPCServiceModel getServiceModel() {
                return new EventStreamRPCServiceModel() {
                    @Override
                    public String getServiceName() { return "TestService"; }

                    @Override
                    public Collection<String> getAllOperations() { return new HashSet<>(); }

                    @Override
                    protected Optional<Class<? extends EventStreamJsonMessage>> getServiceClassType(String applicationModelType) { return Optional.empty(); }

                    @Override
                    public OperationModelContext getOperationModelContext(String operationName) { return null; }
                };
            }

            @Override
            public Function<OperationContinuationHandlerContext, ? extends ServerConnectionContinuationHandler> getOperationHandler(String operationName) {
                return null;
            }

            @Override
            public boolean hasHandlerForOperation(String operation) { return true; }
        };
        handler.setAuthenticationHandler(TestAuthNZHandlers.getAuthNHandler());
        //missing: handler.setAuthorizationHandler(TestAuthNZHandlers.getAuthZHandler());

        try (final EventLoopGroup elGroup = new EventLoopGroup(1)) {
            final IpcServer ipcServer = new IpcServer(elGroup, socketOptions, null, "127.0.0.1", port, handler);
            Assertions.assertThrows(InvalidServiceConfigurationException.class, () -> {
                ipcServer.runServer();
            });
        }
    }
}
