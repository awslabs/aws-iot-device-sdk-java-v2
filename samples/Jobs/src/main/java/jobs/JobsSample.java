/* Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package jobs;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttClient;
import software.amazon.awssdk.crt.mqtt.MqttConnection;
import software.amazon.awssdk.crt.mqtt.MqttConnectionEvents;
import software.amazon.awssdk.iot.iotjobs.IotJobsClient;
import software.amazon.awssdk.iot.iotjobs.model.DescribeJobExecutionRequest;
import software.amazon.awssdk.iot.iotjobs.model.DescribeJobExecutionResponse;
import software.amazon.awssdk.iot.iotjobs.model.DescribeJobExecutionSubscriptionRequest;
import software.amazon.awssdk.iot.iotjobs.model.GetPendingJobExecutionsRequest;
import software.amazon.awssdk.iot.iotjobs.model.GetPendingJobExecutionsResponse;
import software.amazon.awssdk.iot.iotjobs.model.GetPendingJobExecutionsSubscriptionRequest;
import software.amazon.awssdk.iot.iotjobs.model.JobExecutionSummary;
import software.amazon.awssdk.iot.iotjobs.model.JobStatus;
import software.amazon.awssdk.iot.iotjobs.model.RejectedError;
import software.amazon.awssdk.iot.iotjobs.model.StartNextJobExecutionResponse;
import software.amazon.awssdk.iot.iotjobs.model.StartNextPendingJobExecutionRequest;
import software.amazon.awssdk.iot.iotjobs.model.StartNextPendingJobExecutionSubscriptionRequest;
import software.amazon.awssdk.iot.iotjobs.model.UpdateJobExecutionRequest;
import software.amazon.awssdk.iot.iotjobs.model.UpdateJobExecutionSubscriptionRequest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JobsSample {
    static String clientId = "samples-client-id";
    static String thingName;
    static String rootCaPath;
    static String certPath;
    static String keyPath;
    static String endpoint;
    static boolean showHelp = false;
    static int port = 8883;

    static CompletableFuture<Void> gotResponse;
    static List<String> availableJobs = new LinkedList<>();
    static String currentJobId;
    static long currentExecutionNumber = 0;
    static int currentVersionNumber = 0;

    static void printUsage() {
        System.out.println(
                "Usage:\n"+
                "  --help        This message\n"+
                "  --thingName   The name of the IoT thing\n"+
                "  --clientId    Client ID to use when connecting (optional)\n"+
                "  -e|--endpoint AWS IoT service endpoint hostname\n"+
                "  -p|--port     Port to connect to on the endpoint\n"+
                "  -r|--rootca   Path to the root certificate\n"+
                "  -c|--cert     Path to the IoT thing certificate\n"+
                "  -k|--key      Path to the IoT thing public key"
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
                case "--thingName":
                    if (idx + 1 < args.length) {
                        thingName = args[++idx];
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
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }

    static void onRejectedError(RejectedError error) {
        System.out.println("Request rejected: " + error.code.toString() + ": " + error.message);
        System.exit(1);
    }

    static void onGetPendingJobExecutionsAccepted(GetPendingJobExecutionsResponse response) {
        System.out.println("Pending Jobs: " + (response.queuedJobs.size() + response.inProgressJobs.size() == 0 ? "none" : ""));
        for (JobExecutionSummary job : response.inProgressJobs) {
            availableJobs.add(job.jobId);
            System.out.println("  In Progress: " + job.jobId + " @  " + job.lastUpdatedAt.toString());
        }
        for (JobExecutionSummary job : response.queuedJobs) {
            availableJobs.add(job.jobId);
            System.out.println("  " + job.jobId + " @ " + job.lastUpdatedAt.toString());
        }
        gotResponse.complete(null);
    }

    static void onDescribeJobExecutionAccepted(DescribeJobExecutionResponse response) {
        System.out.println("Describe Job: " + response.execution.jobId + " version: " + response.execution.versionNumber);
        if (response.execution.jobDocument != null) {
            response.execution.jobDocument.forEach((key, value) -> {
                System.out.println("  " + key + ": " + value);
            });
        }
        gotResponse.complete(null);
    }

    static void onStartNextPendingJobExecutionAccepted(StartNextJobExecutionResponse response) {
        System.out.println("Start Job: " + response.execution.jobId);
        currentJobId = response.execution.jobId;
        currentExecutionNumber = response.execution.executionNumber;
        currentVersionNumber = response.execution.versionNumber;
        gotResponse.complete(null);
    }

    public static void main(String[] args) {
        parseCommandLine(args);
        if (showHelp || thingName == null || endpoint == null || rootCaPath == null || certPath == null || keyPath == null) {
            printUsage();
            return;
        }

        try {
            EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup);
            TlsContextOptions tlsContextOptions = TlsContextOptions.createWithMTLS(certPath, keyPath);
            tlsContextOptions.overrideDefaultTrustStore(null, rootCaPath);
            TlsContext tlsContext = new TlsContext(tlsContextOptions);
            MqttClient client = new MqttClient(clientBootstrap, tlsContext);

            MqttConnection connection = new MqttConnection(client, new MqttConnectionEvents() {
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
            });
            IotJobsClient jobs = new IotJobsClient(connection);

            CompletableFuture<Boolean> connected = connection.connect(
                    clientId,
                    endpoint, port,
                    null, tlsContext, true, 0)
                    .exceptionally((ex) -> {
                        System.out.println("Exception occurred during connect: " + ex.toString());
                        return null;
                    });
            boolean sessionPresent = connected.get();
            System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");

            {
                GetPendingJobExecutionsSubscriptionRequest subscriptionRequest = new GetPendingJobExecutionsSubscriptionRequest();
                subscriptionRequest.thingName = "crt-test";
                CompletableFuture<Integer> subscribed = jobs.SubscribeToGetPendingJobExecutionsAccepted(
                        subscriptionRequest, JobsSample::onGetPendingJobExecutionsAccepted)
                        .exceptionally((ex) -> {
                            System.out.println("Failed to subscribe to GetPendingJobExecutions: " + ex.toString());
                            return null;
                        });
                subscribed.get();
                System.out.println("Subscribed to GetPendingJobExecutionsAccepted");

                gotResponse = new CompletableFuture<>();

                subscribed = jobs.SubscribeToGetPendingJobExecutionsRejected(subscriptionRequest, JobsSample::onRejectedError);
                subscribed.get();
                System.out.println("Subscribed to GetPendingJobExecutionsRejected");

                GetPendingJobExecutionsRequest publishRequest = new GetPendingJobExecutionsRequest();
                publishRequest.thingName = thingName;
                CompletableFuture<Integer> published = jobs.PublishGetPendingJobExecutions(publishRequest)
                        .exceptionally((ex) -> {
                            System.out.println("Exception occurred during publish: " + ex.toString());
                            gotResponse.complete(null);
                            return null;
                        });
                published.get();
                gotResponse.get();
            }

            if (availableJobs.isEmpty()) {
                System.out.println("No jobs queued, no further work to do");
            }

            gotResponse.get();

            for (String jobId : availableJobs) {
                gotResponse = new CompletableFuture<>();
                DescribeJobExecutionSubscriptionRequest subscriptionRequest = new DescribeJobExecutionSubscriptionRequest();
                subscriptionRequest.thingName = thingName;
                subscriptionRequest.jobId = jobId;
                jobs.SubscribeToDescribeJobExecutionAccepted(subscriptionRequest, JobsSample::onDescribeJobExecutionAccepted);
                jobs.SubscribeToDescribeJobExecutionRejected(subscriptionRequest, JobsSample::onRejectedError);

                DescribeJobExecutionRequest publishRequest = new DescribeJobExecutionRequest();
                publishRequest.thingName = thingName;
                publishRequest.jobId = jobId;
                publishRequest.includeJobDocument = true;
                publishRequest.executionNumber = 1L;
                jobs.PublishDescribeJobExecution(publishRequest);
                gotResponse.get();
            }

            for (int jobIdx = 0; jobIdx < availableJobs.size(); ++jobIdx) {
                {
                    gotResponse = new CompletableFuture<>();

                    // Start the next pending job
                    StartNextPendingJobExecutionSubscriptionRequest subscriptionRequest = new StartNextPendingJobExecutionSubscriptionRequest();
                    subscriptionRequest.thingName = thingName;

                    jobs.SubscribeToStartNextPendingJobExecutionAccepted(subscriptionRequest, JobsSample::onStartNextPendingJobExecutionAccepted);
                    jobs.SubscribeToStartNextPendingJobExecutionRejected(subscriptionRequest, JobsSample::onRejectedError);

                    StartNextPendingJobExecutionRequest publishRequest = new StartNextPendingJobExecutionRequest();
                    publishRequest.thingName = thingName;
                    publishRequest.stepTimeoutInMinutes = 15L;
                    jobs.PublishStartNextPendingJobExecution(publishRequest);

                    gotResponse.get();
                }

                {
                    // Update the service to let it know we're executing
                    gotResponse = new CompletableFuture<>();

                    UpdateJobExecutionSubscriptionRequest subscriptionRequest = new UpdateJobExecutionSubscriptionRequest();
                    subscriptionRequest.thingName = thingName;
                    subscriptionRequest.jobId = currentJobId;
                    jobs.SubscribeToUpdateJobExecutionAccepted(subscriptionRequest, (response) -> {
                        System.out.println("Marked job " + currentJobId + " IN_PROGRESS");
                        gotResponse.complete(null);
                    });
                    jobs.SubscribeToUpdateJobExecutionRejected(subscriptionRequest, JobsSample::onRejectedError);

                    UpdateJobExecutionRequest publishRequest = new UpdateJobExecutionRequest();
                    publishRequest.thingName = thingName;
                    publishRequest.jobId = currentJobId;
                    publishRequest.executionNumber = currentExecutionNumber;
                    publishRequest.status = JobStatus.IN_PROGRESS;
                    publishRequest.expectedVersion = currentVersionNumber++;
                    jobs.PublishUpdateJobExecution(publishRequest);

                    gotResponse.get();
                }

                // Fake doing something
                Thread.sleep(1000);

                {
                    // Update the service to let it know we're done
                    gotResponse = new CompletableFuture<>();

                    UpdateJobExecutionSubscriptionRequest subscriptionRequest = new UpdateJobExecutionSubscriptionRequest();
                    subscriptionRequest.thingName = thingName;
                    subscriptionRequest.jobId = currentJobId;
                    jobs.SubscribeToUpdateJobExecutionAccepted(subscriptionRequest, (response) -> {
                        System.out.println("Marked job " + currentJobId + " SUCCEEDED");
                        gotResponse.complete(null);
                    });
                    jobs.SubscribeToUpdateJobExecutionRejected(subscriptionRequest, JobsSample::onRejectedError);

                    UpdateJobExecutionRequest publishRequest = new UpdateJobExecutionRequest();
                    publishRequest.thingName = thingName;
                    publishRequest.jobId = currentJobId;
                    publishRequest.executionNumber = currentExecutionNumber;
                    publishRequest.status = JobStatus.SUCCEEDED;
                    publishRequest.expectedVersion = currentVersionNumber++;
                    jobs.PublishUpdateJobExecution(publishRequest);

                    gotResponse.get();
                }
            }

            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get();
        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            System.out.println("Exception encountered: " + ex.toString());
        }

        System.out.println("Complete!");
    }
}
