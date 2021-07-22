/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package pubsub;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.X509CredentialsProvider;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.ClientTlsContext;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;
import software.amazon.awssdk.iot.iotjobs.model.RejectedError;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class PubSub {
    static String clientId = "test-" + UUID.randomUUID().toString();
    static String rootCaPath;
    static String certPath;
    static String keyPath;
    static String endpoint;
    static String topic = "test/topic";
    static String message = "Hello World!";
    static int    messagesToPublish = 10;
    static boolean showHelp = false;
    static int port = 8883;

    static String proxyHost;
    static int proxyPort;
    static String region = "us-east-1";
    static boolean useWebsockets = false;
    static boolean useX509Credentials = false;
    static String x509RoleAlias;
    static String x509Endpoint;
    static String x509Thing;
    static String x509CertPath;
    static String x509KeyPath;
    static String x509RootCaPath;

    static void printUsage() {
        System.out.println(
                "Usage:\n"+
                "  --help            This message\n"+
                "  --clientId        Client ID to use when connecting (optional)\n"+
                "  -e|--endpoint     AWS IoT service endpoint hostname\n"+
                "  -p|--port         Port to connect to on the endpoint\n"+
                "  -r|--rootca       Path to the root certificate\n"+
                "  -c|--cert         Path to the IoT thing certificate\n"+
                "  -k|--key          Path to the IoT thing private key\n"+
                "  -t|--topic        Topic to subscribe/publish to (optional)\n"+
                "  -m|--message      Message to publish (optional)\n"+
                "  -n|--count        Number of messages to publish (optional)\n" +
                "  -w|--websockets   Use websockets\n" +
                "  --proxyhost       Websocket proxy host to use\n" +
                "  --proxyport       Websocket proxy port to use\n" +
                "  --region          Websocket signing region to use\n" +
                "  --x509            Use the x509 credentials provider while using websockets\n" +
                "  --x509rolealias   Role alias to use with the x509 credentials provider\n" +
                "  --x509endpoint    Endpoint to fetch x509 credentials from\n" +
                "  --x509thing       Thing name to fetch x509 credentials on behalf of\n" +
                "  --x509cert        Path to the IoT thing certificate used in fetching x509 credentials\n" +
                "  --x509key         Path to the IoT thing private key used in fetching x509 credentials\n" +
                "  --x509rootca      Path to the root certificate used in fetching x509 credentials\n"
        );
    }

    static void parseCommandLine(String[] args) {
        for (int idx = 0; idx < args.length; ++idx) {
            switch (args[idx]) {
                case "--help":
                    showHelp = true;
                    break;
                case "--clientId":
                    if (idx + 1 < args.length) {
                        clientId = args[++idx];
                    }
                    break;
                case "-e":
                case "--endpoint":
                    if (idx + 1 < args.length) {
                        endpoint = args[++idx];
                    }
                    break;
                case "-p":
                case "--port":
                    if (idx + 1 < args.length) {
                        port = Integer.parseInt(args[++idx]);
                    }
                    break;
                case "-r":
                case "--rootca":
                    if (idx + 1 < args.length) {
                        rootCaPath = args[++idx];
                    }
                    break;
                case "-c":
                case "--cert":
                    if (idx + 1 < args.length) {
                        certPath = args[++idx];
                    }
                    break;
                case "-k":
                case "--key":
                    if (idx + 1 < args.length) {
                        keyPath = args[++idx];
                    }
                    break;
                case "-t":
                case "--topic":
                    if (idx + 1 < args.length) {
                        topic = args[++idx];
                    }
                    break;
                case "-m":
                case "--message":
                    if (idx + 1 < args.length) {
                        message = args[++idx];
                    }
                    break;
                case "-n":
                case "--count":
                    if (idx + 1 < args.length) {
                        messagesToPublish = Integer.parseInt(args[++idx]);
                    }
                    break;
                case "-w":
                    useWebsockets = true;
                    break;
                case "--x509":
                    useX509Credentials = true;
                    useWebsockets = true;
                    break;
                case "--x509rolealias":
                    if (idx + 1 < args.length) {
                        x509RoleAlias = args[++idx];
                    }
                    break;
                case "--x509endpoint":
                    if (idx + 1 < args.length) {
                        x509Endpoint = args[++idx];
                    }
                    break;
                case "--x509thing":
                    if (idx + 1 < args.length) {
                        x509Thing = args[++idx];
                    }
                    break;
                case "--x509cert":
                    if (idx + 1 < args.length) {
                        x509CertPath = args[++idx];
                    }
                    break;
                case "--x509key":
                    if (idx + 1 < args.length) {
                        x509KeyPath = args[++idx];
                    }
                    break;
                case "--x509rootca":
                    if (idx + 1 < args.length) {
                        x509RootCaPath = args[++idx];
                    }
                    break;
                case "--proxyhost":
                    if (idx + 1 < args.length) {
                        proxyHost = args[++idx];
                    }
                    break;
                case "--proxyport":
                    if (idx + 1 < args.length) {
                        proxyPort = Integer.parseInt(args[++idx]);
                    }
                    break;
                case "--region":
                    if (idx + 1 < args.length) {
                        region = args[++idx];
                    }
                    break;
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }

    static void onRejectedError(RejectedError error) {
        System.out.println("Request rejected: " + error.code.toString() + ": " + error.message);
    }

    public static void main(String[] args) {
        parseCommandLine(args);
        if (showHelp || endpoint == null) {
            printUsage();
            return;
        }

        if (!useWebsockets) {
            if (certPath == null || keyPath == null) {
                printUsage();
                return;
            }
        } else if (useX509Credentials) {
            if (x509RoleAlias == null || x509Endpoint == null || x509Thing == null || x509CertPath == null || x509KeyPath == null) {
                printUsage();
                return;
            }
        }

        MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (errorCode != 0) {
                    System.out.println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
                }
            }

            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
            }
        };

        try(EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            HostResolver resolver = new HostResolver(eventLoopGroup);
            ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup, resolver);
            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certPath, keyPath)) {

            if (rootCaPath != null) {
                builder.withCertificateAuthorityFromPath(null, rootCaPath);
            }

            builder.withBootstrap(clientBootstrap)
                .withConnectionEventCallbacks(callbacks)
                .withClientId(clientId)
                .withEndpoint(endpoint)
                .withPort((short)port)
                .withCleanSession(true)
                .withProtocolOperationTimeoutMs(60000);

            HttpProxyOptions proxyOptions = null;
            if (proxyHost != null && proxyPort > 0) {
                proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(proxyHost);
                proxyOptions.setPort(proxyPort);

                builder.withHttpProxyOptions(proxyOptions);
            }

            if (useWebsockets) {
                builder.withWebsockets(true);
                builder.withWebsocketSigningRegion(region);

                if (useX509Credentials) {
                    try (TlsContextOptions x509TlsOptions = TlsContextOptions.createWithMtlsFromPath(x509CertPath, x509KeyPath)) {
                        if (x509RootCaPath != null) {
                            x509TlsOptions.withCertificateAuthorityFromPath(null, x509RootCaPath);
                        }

                        try (ClientTlsContext x509TlsContext = new ClientTlsContext(x509TlsOptions)) {
                            X509CredentialsProvider.X509CredentialsProviderBuilder x509builder = new X509CredentialsProvider.X509CredentialsProviderBuilder()
                                    .withClientBootstrap(clientBootstrap)
                                    .withTlsContext(x509TlsContext)
                                    .withEndpoint(x509Endpoint)
                                    .withRoleAlias(x509RoleAlias)
                                    .withThingName(x509Thing)
                                    .withProxyOptions(proxyOptions);
                            try (X509CredentialsProvider provider = x509builder.build()) {
                                builder.withWebsocketCredentialsProvider(provider);
                            }
                        }
                    }
                }
            }

            try(MqttClientConnection connection = builder.build()) {

                CompletableFuture<Boolean> connected = connection.connect();
                try {
                    boolean sessionPresent = connected.get();
                    System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
                } catch (Exception ex) {
                    throw new RuntimeException("Exception occurred during connect", ex);
                }

                CountDownLatch countDownLatch = new CountDownLatch(messagesToPublish);

                CompletableFuture<Integer> subscribed = connection.subscribe(topic, QualityOfService.AT_LEAST_ONCE, (message) -> {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("MESSAGE: " + payload);
                    countDownLatch.countDown();
                });

                subscribed.get();

                int count = 0;
                while (count++ < messagesToPublish) {
                    CompletableFuture<Integer> published = connection.publish(new MqttMessage(topic, message.getBytes(), QualityOfService.AT_LEAST_ONCE, false));
                    published.get();
                    Thread.sleep(1000);
                }
                
                countDownLatch.await();

                CompletableFuture<Void> disconnected = connection.disconnect();
                disconnected.get();
            }
        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            System.out.println("Exception encountered: " + ex.toString());
        }

        CrtResource.waitForNoResources();

        System.out.println("Complete!");
    }
}
