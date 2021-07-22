/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package greengrass;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.mqtt.*;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;
import software.amazon.awssdk.iot.discovery.DiscoveryClient;
import software.amazon.awssdk.iot.discovery.DiscoveryClientConfig;
import software.amazon.awssdk.iot.discovery.model.ConnectivityInfo;
import software.amazon.awssdk.iot.discovery.model.DiscoverResponse;
import software.amazon.awssdk.iot.discovery.model.GGCore;
import software.amazon.awssdk.iot.discovery.model.GGGroup;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static software.amazon.awssdk.iot.discovery.DiscoveryClient.TLS_EXT_ALPN;

public class BasicDiscovery {
    static String thingName;
    static String rootCaPath;
    static String certPath;
    static String keyPath;
    static String region = "us-east-1";
    static String topic = "test/topic";
    static String mode = "both";
    static boolean showHelp = false;

    static String proxyHost;
    static int proxyPort;

    static void printUsage() {
        System.out.println(
                "Usage:\n"+
                "  --help        This message\n" +
                "  --thing_name  Thing name to use\n" +
                "  -r|--region   AWS IoT service region\n" +
                "  -a|--rootca   Path to the root certificate\n" +
                "  -c|--cert     Path to the IoT thing certificate\n" +
                "  -k|--key      Path to the IoT thing private key\n" +
                "  -t|--topic    Topic to subscribe/publish to (optional)\n" +
                "  -m|--mode  Message to publish (optional)\n" +
                "  --proxyhost   Websocket proxy host to use\n" +
                "  --proxyport   Websocket proxy port to use\n");
    }

    static void parseCommandLine(String[] args) {
        for (int idx = 0; idx < args.length; ++idx) {
            switch (args[idx]) {
                case "--help":
                    showHelp = true;
                    break;
                case "--thingName":
                    if (idx + 1 < args.length) {
                        thingName = args[++idx];
                    }
                    break;
                case "-r":
                case "--region":
                    if (idx + 1 < args.length) {
                        region = args[++idx];
                    }
                    break;
                case "-a":
                case "--rootca":
                    if (idx + 1 < args.length) {
                        rootCaPath = args[++idx];
                        final File rootCaFile = new File(rootCaPath);
                        if (!rootCaFile.isFile()) {
                            throw new RuntimeException("Cannot load root CA from path: " + rootCaFile.getAbsolutePath());
                        }
                        rootCaPath = rootCaFile.getAbsolutePath();
                    }
                    break;
                case "-c":
                case "--cert":
                    if (idx + 1 < args.length) {
                        certPath = args[++idx];
                        final File certFile = new File(certPath);
                        if (!certFile.isFile()) {
                            throw new RuntimeException("Cannot load certificate from path: " + certFile.getAbsolutePath());
                        }
                        certPath = certFile.getAbsolutePath();
                    }
                    break;
                case "-k":
                case "--key":
                    if (idx + 1 < args.length) {
                        keyPath = args[++idx];
                        final File keyFile = new File(keyPath);
                        if (!keyFile.isFile()) {
                            throw new RuntimeException("Cannot load private key from path: " + keyFile.getAbsolutePath());
                        }
                        keyPath = keyFile.getAbsolutePath();
                    }
                    break;
                case "-t":
                case "--topic":
                    if (idx + 1 < args.length) {
                        topic = args[++idx];
                    }
                    break;
                case "-m":
                case "--mode":
                    if (idx + 1 < args.length) {
                        mode = args[++idx];
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
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }

    public static void main(String[] args) {
        Log.initLoggingFromSystemProperties();

        parseCommandLine(args);
        if (showHelp || thingName == null ||
            certPath == null || keyPath == null) {
            printUsage();
            return;
        }

        try(final EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                final HostResolver resolver = new HostResolver(eventLoopGroup);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                final TlsContextOptions tlsCtxOptions = TlsContextOptions.createWithMtlsFromPath(certPath, keyPath)) {
            if(TlsContextOptions.isAlpnSupported()) {
                tlsCtxOptions.withAlpnList(TLS_EXT_ALPN);
            }
            if(rootCaPath != null) {
                tlsCtxOptions.overrideDefaultTrustStoreFromPath(null, rootCaPath);
            }
            HttpProxyOptions proxyOptions = null;
            if (proxyHost != null && proxyPort > 0) {
                proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(proxyHost);
                proxyOptions.setPort(proxyPort);
            }

            try(final DiscoveryClientConfig discoveryClientConfig =
                        new DiscoveryClientConfig(clientBootstrap, tlsCtxOptions,
                        new SocketOptions(), region, 1, proxyOptions);
                final DiscoveryClient discoveryClient = new DiscoveryClient(discoveryClientConfig);
                final MqttClientConnection connection = getClientFromDiscovery(discoveryClient, clientBootstrap)) {

                if ("subscribe".equals(mode) || "both".equals(mode)) {
                    final CompletableFuture<Integer> subFuture = connection.subscribe(topic, QualityOfService.AT_MOST_ONCE, message -> {
                        System.out.println(String.format("Message received on topic %s: %s",
                                message.getTopic(), new String(message.getPayload(), StandardCharsets.UTF_8)));
                    });

                    subFuture.get();
                }

                final Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = null;
                    if ("publish".equals(mode) || "both".equals(mode)) {
                        System.out.println("Enter the message you want to publish to topic " + topic + " and press Enter. " +
                                "Type 'exit' or 'quit' to exit this program: ");
                        input = scanner.nextLine();
                    }

                    if ("exit".equals(input) || "quit".equals(input)) {
                        System.out.println("Terminating...");
                        break;
                    }

                    if ("publish".equals(mode) || "both".equals(mode)) {
                        final CompletableFuture<Integer> publishResult = connection.publish(new MqttMessage(topic,
                                input.getBytes(StandardCharsets.UTF_8), QualityOfService.AT_MOST_ONCE, false));
                        Integer result = publishResult.get();
                    }
                }
            }
        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            System.out.println("Exception thrown: " + ex.toString());
            ex.printStackTrace();
        }
        CrtResource.waitForNoResources();
        System.out.println("Complete!");
    }

    private static MqttClientConnection getClientFromDiscovery(final DiscoveryClient discoveryClient,
                                                               final ClientBootstrap bootstrap) throws ExecutionException, InterruptedException {
        final CompletableFuture<DiscoverResponse> futureResponse = discoveryClient.discover(thingName);
        final DiscoverResponse response = futureResponse.get();
        if(response.getGGGroups() != null) {
            final Optional<GGGroup> groupOpt = response.getGGGroups().stream().findFirst();
            if(groupOpt.isPresent()) {
                final GGGroup group = groupOpt.get();
                final GGCore core = group.getCores().stream().findFirst().get();

                for (ConnectivityInfo connInfo : core.getConnectivity()) {
                    final String dnsOrIp = connInfo.getHostAddress();
                    final Integer port = connInfo.getPortNumber();

                    System.out.println(String.format("Connecting to group ID %s, with thing arn %s, using endpoint %s:%d",
                            group.getGGGroupId(), core.getThingArn(), dnsOrIp, port));

                    final AwsIotMqttConnectionBuilder connectionBuilder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certPath, keyPath)
                            .withClientId(thingName)
                            .withPort(port.shortValue())
                            .withEndpoint(dnsOrIp)
                            .withBootstrap(bootstrap)
                            .withConnectionEventCallbacks(new MqttClientConnectionEvents() {
                                @Override
                                public void onConnectionInterrupted(int errorCode) {
                                    System.out.println("Connection interrupted: " + errorCode);
                                }

                                @Override
                                public void onConnectionResumed(boolean sessionPresent) {
                                    System.out.println("Connection resumed!");
                                }
                            });
                    if (group.getCAs() != null) {
                        connectionBuilder.withCertificateAuthority(group.getCAs().get(0));
                    }

                    try (MqttClientConnection connection = connectionBuilder.build()) {
                        if (connection.connect().get()) {
                            System.out.println("Session resumed");
                        } else {
                            System.out.println("Started a clean session");
                        }

                        /* This lets the connection escape the try block without getting cleaned up */
                        connection.addRef();

                        return connection;
                    } catch (Exception e) {
                        System.out.println(String.format("Connection failed with exception %s", e.toString()));
                    }
                }

                throw new RuntimeException("ThingName " + thingName + " could not connect to the green grass core using any of the endpoint connectivity options");
            }
        }
        throw new RuntimeException("ThingName " + thingName + " does not have a Greengrass group/core configuration");
    }
}
