/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package rawpubsub;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.*;
import software.amazon.awssdk.iot.iotjobs.model.RejectedError;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class RawPubSub {
    static String clientId = "test-" + UUID.randomUUID().toString();
    static String rootCaPath;
    static String certPath;
    static String keyPath;
    static String endpoint;
    static String topic = "test/topic";
    static String message = "Hello World!";
    static int    messagesToPublish = 10;
    static boolean showHelp = false;

    static String userName;
    static String password;
    static String protocolName = "mqtt";
    static List<String> authParams;

    static void printUsage() {
        System.out.println(
                "Usage:\n"+
                "  --help        This message\n"+
                "  --clientId    Client ID to use when connecting (optional)\n"+
                "  -e|--endpoint AWS IoT service endpoint hostname\n"+
                "  -r|--rootca   Path to the root certificate\n"+
                "  -c|--cert     Path to the IoT thing certificate\n"+
                "  -k|--key      Path to the IoT thing private key\n"+
                "  -t|--topic    Topic to subscribe/publish to (optional)\n"+
                "  -m|--message  Message to publish (optional)\n"+
                "  -n|--count    Number of messages to publish (optional)"+
                "  -u|--username Username to use as part of the connection/authentication process\n"+
                "  --password    Password to use as part of the connection/authentication process\n"+
                "  --protocol    (optional) Communication protocol to use; defaults to mqtt\n"+
                "  --auth_params (optional) Comma delimited list of auth parameters. For websockets these will be set as headers.  For raw mqtt these will be appended to user_name.\n"
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
                case "-u":
                case "--username":
                    if (idx + 1 < args.length) {
                        userName = args[++idx];
                    }
                    break;
                case "--password":
                    if (idx + 1 < args.length) {
                        password = args[++idx];
                    }
                    break;
                case "--protocol":
                    if (idx + 1 < args.length) {
                        protocolName = args[++idx];
                    }
                    break;
                case "--auth_params":
                    if (idx + 1 < args.length) {
                        authParams = Arrays.asList(args[++idx].split("\\s*,\\s*"));
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

        if (authParams != null && authParams.size() > 0) {
            if (userName.length() > 0) {
                StringBuilder usernameBuilder = new StringBuilder();

                usernameBuilder.append(userName);
                usernameBuilder.append("?");
                for (int i = 0; i < authParams.size(); ++i) {
                    usernameBuilder.append(authParams.get(i));
                    if (i + 1 < authParams.size()) {
                        usernameBuilder.append("&");
                    }
                }

                userName = usernameBuilder.toString();
            }
        }

        try(EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            HostResolver resolver = new HostResolver(eventLoopGroup);
            ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup, resolver);
            TlsContextOptions tlsContextOptions = TlsContextOptions.createWithMtlsFromPath(certPath, keyPath)) {
            tlsContextOptions.overrideDefaultTrustStoreFromPath(null, rootCaPath);

            int port = 8883;
            if (TlsContextOptions.isAlpnSupported()) {
                port = 443;
                tlsContextOptions.withAlpnList(protocolName);
            }

            try(TlsContext tlsContext = new TlsContext(tlsContextOptions);
                MqttClient client = new MqttClient(clientBootstrap, tlsContext);
                MqttConnectionConfig config = new MqttConnectionConfig()) {

                config.setMqttClient(client);
                config.setClientId(clientId);
                config.setConnectionCallbacks(callbacks);
                config.setCleanSession(true);
                config.setEndpoint(endpoint);
                config.setPort(port);

                if (userName != null && userName.length() > 0) {
                    config.setLogin(userName, password);
                }

                try (MqttClientConnection connection = new MqttClientConnection(config)) {

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
            }
        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            System.out.println("Exception encountered: " + ex.toString());
        }

        CrtResource.waitForNoResources();
        System.out.println("Complete!");
    }
}
