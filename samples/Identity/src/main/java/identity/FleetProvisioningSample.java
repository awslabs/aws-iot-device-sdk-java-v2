/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package identity;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;
import software.amazon.awssdk.iot.iotidentity.IotIdentityClient;
import software.amazon.awssdk.iot.iotidentity.model.CreateCertificateFromCsrRequest;
import software.amazon.awssdk.iot.iotidentity.model.CreateCertificateFromCsrResponse;
import software.amazon.awssdk.iot.iotidentity.model.CreateCertificateFromCsrSubscriptionRequest;
import software.amazon.awssdk.iot.iotidentity.model.CreateKeysAndCertificateRequest;
import software.amazon.awssdk.iot.iotidentity.model.CreateKeysAndCertificateResponse;
import software.amazon.awssdk.iot.iotidentity.model.CreateKeysAndCertificateSubscriptionRequest;
import software.amazon.awssdk.iot.iotidentity.model.ErrorResponse;
import software.amazon.awssdk.iot.iotidentity.model.RegisterThingRequest;
import software.amazon.awssdk.iot.iotidentity.model.RegisterThingResponse;
import software.amazon.awssdk.iot.iotidentity.model.RegisterThingSubscriptionRequest;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import com.google.gson.Gson;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FleetProvisioningSample {
    static String clientId = "test-" + UUID.randomUUID().toString();
    static String rootCaPath;
    static String certPath;
    static String keyPath;
    static String endpoint;
    static String templateName;
    static String templateParameters;
    static String csrPath;
    static boolean showHelp = false;
    static int port = 8883;

    static CompletableFuture<Void> gotResponse;
    static IotIdentityClient iotIdentityClient;

    static CreateKeysAndCertificateResponse createKeysAndCertificateResponse;
    static CreateCertificateFromCsrResponse createCertificateFromCsrResponse;
    static RegisterThingResponse registerThingResponse;

    static void printUsage() {
        System.out.println(
                "Usage:\n"+
                "  --help        This message\n"+
                "  --clientId    Client ID to use when connecting (optional)\n"+
                "  -e|--endpoint AWS IoT service endpoint hostname\n"+
                "  -p|--port     Port to connect to on the endpoint\n"+
                "  -r|--rootca   Path to the root certificate\n"+
                "  -c|--cert     Path to the IoT thing certificate\n"+
                "  -k|--key      Path to the IoT thing private key\n"+
                "  -t|--templateName      Provisioning template name\n"+
                "  -tp|--templateParameters     Provisioning template parameters\n"+
                "  -cr|--csr      Path to the CSR"
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
                case "--templateName":
                    if (idx + 1 < args.length) {
                        templateName = args[++idx];
                    }
                    break;
                case "-tp":
                case "--templateParameters":
                    if (idx + 1 < args.length) {
                        templateParameters = args[++idx];
                    }
                    break;
                case "-cr":
                case "--csr":
                    if (idx + 1 < args.length) {
                        csrPath = args[++idx];
                    }
                    break;
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }

    static void onRejectedKeys(ErrorResponse response) {
        System.out.println("CreateKeysAndCertificate Request rejected, errorCode: " + response.errorCode +
                ", errorMessage: " + response.errorMessage +
                ", statusCode: " + response.statusCode);

        gotResponse.complete(null);
        System.exit(1);
    }

    static void onRejectedCsr(ErrorResponse response) {
        System.out.println("CreateCertificateFromCsr Request rejected, errorCode: " + response.errorCode +
                ", errorMessage: " + response.errorMessage +
                ", statusCode: " + response.statusCode);

        gotResponse.complete(null);
        System.exit(1);
    }

    static void onRejectedRegister(ErrorResponse response) {

        System.out.println("RegisterThing Request rejected, errorCode: " + response.errorCode +
                ", errorMessage: " + response.errorMessage +
                ", statusCode: " + response.statusCode);

        gotResponse.complete(null);
        System.exit(1);
    }

    static void onCreateKeysAndCertificateAccepted(CreateKeysAndCertificateResponse response) {
        System.out.println("CreateKeysAndCertificate response certificateId: " + response.certificateId);
        if (response != null) {
            createKeysAndCertificateResponse = response;
        } else {
            System.out.println("CreateKeysAndCertificate response is null");
        }
        gotResponse.complete(null);
    }

    static void onCreateCertificateFromCsrResponseAccepted(CreateCertificateFromCsrResponse response) {
        System.out.println("CreateCertificateFromCsr response certificateId: " + response.certificateId);
        if (response != null) {
            createCertificateFromCsrResponse = response;
        } else {
            System.out.println("CreateCertificateFromCsr response is null");
        }
        gotResponse.complete(null);
    }

    static void onRegisterThingAccepted(RegisterThingResponse response) {
        System.out.println("RegisterThing response thingName: " + response.thingName);
        if (response != null) {
            gotResponse.complete(null);
            registerThingResponse = response;
        } else {
            System.out.println("RegisterThing response is null");
        }
    }

    static void onException(Exception e) {
        e.printStackTrace();
        System.out.println("Exception occurred " + e);
    }

    public static void main(String[] args) {
        parseCommandLine(args);
        if (showHelp || endpoint == null || rootCaPath == null || certPath == null || keyPath == null || templateName == null || templateParameters == null) {
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

        try(EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            HostResolver resolver = new HostResolver(eventLoopGroup);
            ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup, resolver);
            AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certPath, keyPath)) {

            builder.withCertificateAuthorityFromPath(null, rootCaPath)
                    .withEndpoint(endpoint)
                    .withClientId(clientId)
                    .withCleanSession(true)
                    .withBootstrap(clientBootstrap)
                    .withConnectionEventCallbacks(callbacks);

            try(MqttClientConnection connection = builder.build()) {
                iotIdentityClient = new IotIdentityClient(connection);

                CompletableFuture<Boolean> connected = connection.connect();
                try {
                    boolean sessionPresent = connected.get();
                    System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
                } catch (Exception ex) {
                    throw new RuntimeException("Exception occurred during connect", ex);
                }

                try {
                    if (csrPath == null) {
                        createKeysAndCertificateWorkflow();
                    } else {
                        createCertificateFromCsrWorkflow();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Exception occurred during connect", e);
                }

                CompletableFuture<Void> disconnected = connection.disconnect();
                disconnected.get();
            }
        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            System.out.println("Exception encountered: " + ex.toString());
        }

        CrtResource.waitForNoResources();
        System.out.println("Complete!");
    }

    private static void createKeysAndCertificateWorkflow() throws Exception {
        CreateKeysAndCertificateSubscriptionRequest createKeysAndCertificateSubscriptionRequest = new CreateKeysAndCertificateSubscriptionRequest();
        CompletableFuture<Integer> keysSubscribedAccepted = iotIdentityClient.SubscribeToCreateKeysAndCertificateAccepted(
                createKeysAndCertificateSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onCreateKeysAndCertificateAccepted);

        keysSubscribedAccepted.get();
        System.out.println("Subscribed to CreateKeysAndCertificateAccepted");

        CompletableFuture<Integer> keysSubscribedRejected = iotIdentityClient.SubscribeToCreateKeysAndCertificateRejected(
                createKeysAndCertificateSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onRejectedKeys);

        keysSubscribedRejected.get();
        System.out.println("Subscribed to CreateKeysAndCertificateRejected");


        RegisterThingSubscriptionRequest registerThingSubscriptionRequest = new RegisterThingSubscriptionRequest();
        registerThingSubscriptionRequest.templateName = templateName;

        CompletableFuture<Integer> subscribedRegisterAccepted = iotIdentityClient.SubscribeToRegisterThingAccepted(
                registerThingSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onRegisterThingAccepted,
                FleetProvisioningSample::onException);

        subscribedRegisterAccepted.get();
        System.out.println("Subscribed to SubscribeToRegisterThingAccepted");

        CompletableFuture<Integer> subscribedRegisterRejected = iotIdentityClient.SubscribeToRegisterThingRejected(
                registerThingSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onRejectedRegister,
                FleetProvisioningSample::onException);

        subscribedRegisterRejected.get();
        System.out.println("Subscribed to SubscribeToRegisterThingRejected");

        CompletableFuture<Integer> publishKeys = iotIdentityClient.PublishCreateKeysAndCertificate(
                new CreateKeysAndCertificateRequest(),
                QualityOfService.AT_LEAST_ONCE);

        publishKeys.get();
        System.out.println("Published to CreateKeysAndCertificate");

        waitForKeysRequest();

        gotResponse = new CompletableFuture<>();

        System.out.println("RegisterThing now....");
        RegisterThingRequest registerThingRequest = new RegisterThingRequest();
        registerThingRequest.certificateOwnershipToken = createKeysAndCertificateResponse.certificateOwnershipToken;
        registerThingRequest.templateName = templateName;

        if (templateParameters != null && templateParameters != "") {
            registerThingRequest.parameters = new Gson().fromJson(templateParameters, HashMap.class);
        }

        CompletableFuture<Integer> publishRegister = iotIdentityClient.PublishRegisterThing(
                registerThingRequest,
                QualityOfService.AT_LEAST_ONCE);

        System.out.println("####### I am here");
        try {
            publishRegister.get();
            System.out.println("Published to RegisterThing");
        } catch(Exception ex) {
            throw new RuntimeException("Exception occurred during publish", ex);
        }
        gotResponse.get();
        waitForRegisterRequest();
    }

    private static void createCertificateFromCsrWorkflow() throws Exception {
        CreateCertificateFromCsrSubscriptionRequest createCertificateFromCsrSubscriptionRequest = new CreateCertificateFromCsrSubscriptionRequest();
        CompletableFuture<Integer> csrSubscribedAccepted = iotIdentityClient.SubscribeToCreateCertificateFromCsrAccepted(
                createCertificateFromCsrSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onCreateCertificateFromCsrResponseAccepted);

        csrSubscribedAccepted.get();
        System.out.println("Subscribed to CreateCertificateFromCsrAccepted");

        CompletableFuture<Integer> csrSubscribedRejected = iotIdentityClient.SubscribeToCreateCertificateFromCsrRejected(
                createCertificateFromCsrSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onRejectedCsr);

        csrSubscribedRejected.get();
        System.out.println("Subscribed to CreateCertificateFromCsrRejected");

        RegisterThingSubscriptionRequest registerThingSubscriptionRequest = new RegisterThingSubscriptionRequest();
        registerThingSubscriptionRequest.templateName = templateName;

        CompletableFuture<Integer> subscribedRegisterAccepted = iotIdentityClient.SubscribeToRegisterThingAccepted(
                registerThingSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onRegisterThingAccepted,
                FleetProvisioningSample::onException);

        subscribedRegisterAccepted.get();
        System.out.println("Subscribed to SubscribeToRegisterThingAccepted");

        CompletableFuture<Integer> subscribedRegisterRejected = iotIdentityClient.SubscribeToRegisterThingRejected(
                registerThingSubscriptionRequest,
                QualityOfService.AT_LEAST_ONCE,
                FleetProvisioningSample::onRejectedRegister,
                FleetProvisioningSample::onException);

        subscribedRegisterRejected.get();
        System.out.println("Subscribed to SubscribeToRegisterThingRejected");

        String csrContents = new String(Files.readAllBytes(Paths.get(csrPath)));

        CreateCertificateFromCsrRequest createCertificateFromCsrRequest = new CreateCertificateFromCsrRequest();
        createCertificateFromCsrRequest.certificateSigningRequest = csrContents;
        CompletableFuture<Integer> publishCsr = iotIdentityClient.PublishCreateCertificateFromCsr(
                createCertificateFromCsrRequest,
                QualityOfService.AT_LEAST_ONCE);

        publishCsr.get();
        System.out.println("Published to CreateCertificateFromCsr");

        waitForCsrRequest();

        gotResponse = new CompletableFuture<>();

        RegisterThingRequest registerThingRequest = new RegisterThingRequest();
        registerThingRequest.certificateOwnershipToken = createCertificateFromCsrResponse.certificateOwnershipToken;
        registerThingRequest.templateName = templateName;
        registerThingRequest.parameters = new Gson().fromJson(templateParameters, HashMap.class);
        CompletableFuture<Integer> publishRegister = iotIdentityClient.PublishRegisterThing(
                registerThingRequest,
                QualityOfService.AT_LEAST_ONCE);

        publishRegister.get();
        System.out.println("Published to RegisterThing");

        waitForRegisterRequest();
    }

    public static void waitForCsrRequest() {
        try {
            // Wait for the response.
            int loopCount = 0;
            while (loopCount < 30 && createCertificateFromCsrResponse == null) {
                if (createCertificateFromCsrResponse != null) {
                    break;
                }
                System.out.println("Waiting...for CreateCertificateFromCsrResponse");
                loopCount += 1;
                Thread.sleep(50L);
            }
        } catch (InterruptedException e) {
            System.out.println("Exception occured");
        }
    }

    public static void waitForKeysRequest() {
        try {
            // Wait for the response.
            int loopCount = 0;
            while (loopCount < 30 && createKeysAndCertificateResponse == null) {
                if (createKeysAndCertificateResponse != null) {
                    break;
                }
                System.out.println("Waiting...for CreateKeysAndCertificateResponse");
                loopCount += 1;
                Thread.sleep(50L);
            }
        } catch (InterruptedException e) {
            System.out.println("Exception occured");
        }
    }

    public static void waitForRegisterRequest() {
        try {
            // Wait for the response.
            int loopCount = 0;
            while (loopCount < 30 && registerThingResponse == null) {
                if (registerThingResponse != null) {
                    break;
                }
                System.out.println("Waiting...for registerThingResponse");
                loopCount += 1;
                Thread.sleep(50L);
            }
        } catch (InterruptedException e) {
            System.out.println("Exception occured");
        }
    }
}
