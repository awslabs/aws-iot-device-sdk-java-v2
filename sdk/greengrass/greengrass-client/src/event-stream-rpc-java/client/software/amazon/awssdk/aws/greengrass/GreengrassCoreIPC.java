package software.amazon.awssdk.aws.greengrass;

import java.util.Optional;
import software.amazon.awssdk.aws.greengrass.model.ComponentUpdatePolicyEvents;
import software.amazon.awssdk.aws.greengrass.model.ConfigurationUpdateEvents;
import software.amazon.awssdk.aws.greengrass.model.CreateDebugPasswordRequest;
import software.amazon.awssdk.aws.greengrass.model.CreateLocalDeploymentRequest;
import software.amazon.awssdk.aws.greengrass.model.DeferComponentUpdateRequest;
import software.amazon.awssdk.aws.greengrass.model.DeleteThingShadowRequest;
import software.amazon.awssdk.aws.greengrass.model.GetComponentDetailsRequest;
import software.amazon.awssdk.aws.greengrass.model.GetConfigurationRequest;
import software.amazon.awssdk.aws.greengrass.model.GetLocalDeploymentStatusRequest;
import software.amazon.awssdk.aws.greengrass.model.GetSecretValueRequest;
import software.amazon.awssdk.aws.greengrass.model.GetThingShadowRequest;
import software.amazon.awssdk.aws.greengrass.model.IoTCoreMessage;
import software.amazon.awssdk.aws.greengrass.model.ListComponentsRequest;
import software.amazon.awssdk.aws.greengrass.model.ListLocalDeploymentsRequest;
import software.amazon.awssdk.aws.greengrass.model.ListNamedShadowsForThingRequest;
import software.amazon.awssdk.aws.greengrass.model.PauseComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.PublishToIoTCoreRequest;
import software.amazon.awssdk.aws.greengrass.model.PublishToTopicRequest;
import software.amazon.awssdk.aws.greengrass.model.RestartComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.ResumeComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.SendConfigurationValidityReportRequest;
import software.amazon.awssdk.aws.greengrass.model.StopComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToComponentUpdatesRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToConfigurationUpdateRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToIoTCoreRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToTopicRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToValidateConfigurationUpdatesRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscriptionResponseMessage;
import software.amazon.awssdk.aws.greengrass.model.UpdateConfigurationRequest;
import software.amazon.awssdk.aws.greengrass.model.UpdateStateRequest;
import software.amazon.awssdk.aws.greengrass.model.UpdateThingShadowRequest;
import software.amazon.awssdk.aws.greengrass.model.ValidateAuthorizationTokenRequest;
import software.amazon.awssdk.aws.greengrass.model.ValidateConfigurationUpdateEvents;
import software.amazon.awssdk.eventstreamrpc.StreamResponseHandler;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public interface GreengrassCoreIPC {
  SubscribeToIoTCoreResponseHandler subscribeToIoTCore(final SubscribeToIoTCoreRequest request,
      final Optional<StreamResponseHandler<IoTCoreMessage>> streamResponseHandler);

  ResumeComponentResponseHandler resumeComponent(final ResumeComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  PublishToIoTCoreResponseHandler publishToIoTCore(final PublishToIoTCoreRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  SubscribeToConfigurationUpdateResponseHandler subscribeToConfigurationUpdate(
      final SubscribeToConfigurationUpdateRequest request,
      final Optional<StreamResponseHandler<ConfigurationUpdateEvents>> streamResponseHandler);

  DeleteThingShadowResponseHandler deleteThingShadow(final DeleteThingShadowRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  DeferComponentUpdateResponseHandler deferComponentUpdate(
      final DeferComponentUpdateRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  SubscribeToValidateConfigurationUpdatesResponseHandler subscribeToValidateConfigurationUpdates(
      final SubscribeToValidateConfigurationUpdatesRequest request,
      final Optional<StreamResponseHandler<ValidateConfigurationUpdateEvents>> streamResponseHandler);

  GetConfigurationResponseHandler getConfiguration(final GetConfigurationRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  SubscribeToTopicResponseHandler subscribeToTopic(final SubscribeToTopicRequest request,
      final Optional<StreamResponseHandler<SubscriptionResponseMessage>> streamResponseHandler);

  GetComponentDetailsResponseHandler getComponentDetails(final GetComponentDetailsRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  PublishToTopicResponseHandler publishToTopic(final PublishToTopicRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  ListComponentsResponseHandler listComponents(final ListComponentsRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  CreateDebugPasswordResponseHandler createDebugPassword(final CreateDebugPasswordRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  GetThingShadowResponseHandler getThingShadow(final GetThingShadowRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  SendConfigurationValidityReportResponseHandler sendConfigurationValidityReport(
      final SendConfigurationValidityReportRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  UpdateThingShadowResponseHandler updateThingShadow(final UpdateThingShadowRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  UpdateConfigurationResponseHandler updateConfiguration(final UpdateConfigurationRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  ValidateAuthorizationTokenResponseHandler validateAuthorizationToken(
      final ValidateAuthorizationTokenRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  RestartComponentResponseHandler restartComponent(final RestartComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  GetLocalDeploymentStatusResponseHandler getLocalDeploymentStatus(
      final GetLocalDeploymentStatusRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  GetSecretValueResponseHandler getSecretValue(final GetSecretValueRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  UpdateStateResponseHandler updateState(final UpdateStateRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  ListNamedShadowsForThingResponseHandler listNamedShadowsForThing(
      final ListNamedShadowsForThingRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  SubscribeToComponentUpdatesResponseHandler subscribeToComponentUpdates(
      final SubscribeToComponentUpdatesRequest request,
      final Optional<StreamResponseHandler<ComponentUpdatePolicyEvents>> streamResponseHandler);

  ListLocalDeploymentsResponseHandler listLocalDeployments(
      final ListLocalDeploymentsRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  StopComponentResponseHandler stopComponent(final StopComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  PauseComponentResponseHandler pauseComponent(final PauseComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);

  CreateLocalDeploymentResponseHandler createLocalDeployment(
      final CreateLocalDeploymentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler);
}
