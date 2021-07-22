package software.amazon.awssdk.aws.greengrass;

import java.lang.Override;
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
import software.amazon.awssdk.eventstreamrpc.EventStreamRPCClient;
import software.amazon.awssdk.eventstreamrpc.EventStreamRPCConnection;
import software.amazon.awssdk.eventstreamrpc.StreamResponseHandler;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public class GreengrassCoreIPCClient extends EventStreamRPCClient implements GreengrassCoreIPC {
  public GreengrassCoreIPCClient(final EventStreamRPCConnection connection) {
    super(connection);
  }

  @Override
  public SubscribeToIoTCoreResponseHandler subscribeToIoTCore(
      final SubscribeToIoTCoreRequest request,
      final Optional<StreamResponseHandler<IoTCoreMessage>> streamResponseHandler) {
    final SubscribeToIoTCoreOperationContext operationContext = GreengrassCoreIPCServiceModel.getSubscribeToIoTCoreModelContext();
    return new SubscribeToIoTCoreResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public ResumeComponentResponseHandler resumeComponent(final ResumeComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final ResumeComponentOperationContext operationContext = GreengrassCoreIPCServiceModel.getResumeComponentModelContext();
    return new ResumeComponentResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public PublishToIoTCoreResponseHandler publishToIoTCore(final PublishToIoTCoreRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final PublishToIoTCoreOperationContext operationContext = GreengrassCoreIPCServiceModel.getPublishToIoTCoreModelContext();
    return new PublishToIoTCoreResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public SubscribeToConfigurationUpdateResponseHandler subscribeToConfigurationUpdate(
      final SubscribeToConfigurationUpdateRequest request,
      final Optional<StreamResponseHandler<ConfigurationUpdateEvents>> streamResponseHandler) {
    final SubscribeToConfigurationUpdateOperationContext operationContext = GreengrassCoreIPCServiceModel.getSubscribeToConfigurationUpdateModelContext();
    return new SubscribeToConfigurationUpdateResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public DeleteThingShadowResponseHandler deleteThingShadow(final DeleteThingShadowRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final DeleteThingShadowOperationContext operationContext = GreengrassCoreIPCServiceModel.getDeleteThingShadowModelContext();
    return new DeleteThingShadowResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public DeferComponentUpdateResponseHandler deferComponentUpdate(
      final DeferComponentUpdateRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final DeferComponentUpdateOperationContext operationContext = GreengrassCoreIPCServiceModel.getDeferComponentUpdateModelContext();
    return new DeferComponentUpdateResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public SubscribeToValidateConfigurationUpdatesResponseHandler subscribeToValidateConfigurationUpdates(
      final SubscribeToValidateConfigurationUpdatesRequest request,
      final Optional<StreamResponseHandler<ValidateConfigurationUpdateEvents>> streamResponseHandler) {
    final SubscribeToValidateConfigurationUpdatesOperationContext operationContext = GreengrassCoreIPCServiceModel.getSubscribeToValidateConfigurationUpdatesModelContext();
    return new SubscribeToValidateConfigurationUpdatesResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public GetConfigurationResponseHandler getConfiguration(final GetConfigurationRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final GetConfigurationOperationContext operationContext = GreengrassCoreIPCServiceModel.getGetConfigurationModelContext();
    return new GetConfigurationResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public SubscribeToTopicResponseHandler subscribeToTopic(final SubscribeToTopicRequest request,
      final Optional<StreamResponseHandler<SubscriptionResponseMessage>> streamResponseHandler) {
    final SubscribeToTopicOperationContext operationContext = GreengrassCoreIPCServiceModel.getSubscribeToTopicModelContext();
    return new SubscribeToTopicResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public GetComponentDetailsResponseHandler getComponentDetails(
      final GetComponentDetailsRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final GetComponentDetailsOperationContext operationContext = GreengrassCoreIPCServiceModel.getGetComponentDetailsModelContext();
    return new GetComponentDetailsResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public PublishToTopicResponseHandler publishToTopic(final PublishToTopicRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final PublishToTopicOperationContext operationContext = GreengrassCoreIPCServiceModel.getPublishToTopicModelContext();
    return new PublishToTopicResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public ListComponentsResponseHandler listComponents(final ListComponentsRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final ListComponentsOperationContext operationContext = GreengrassCoreIPCServiceModel.getListComponentsModelContext();
    return new ListComponentsResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public CreateDebugPasswordResponseHandler createDebugPassword(
      final CreateDebugPasswordRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final CreateDebugPasswordOperationContext operationContext = GreengrassCoreIPCServiceModel.getCreateDebugPasswordModelContext();
    return new CreateDebugPasswordResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public GetThingShadowResponseHandler getThingShadow(final GetThingShadowRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final GetThingShadowOperationContext operationContext = GreengrassCoreIPCServiceModel.getGetThingShadowModelContext();
    return new GetThingShadowResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public SendConfigurationValidityReportResponseHandler sendConfigurationValidityReport(
      final SendConfigurationValidityReportRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final SendConfigurationValidityReportOperationContext operationContext = GreengrassCoreIPCServiceModel.getSendConfigurationValidityReportModelContext();
    return new SendConfigurationValidityReportResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public UpdateThingShadowResponseHandler updateThingShadow(final UpdateThingShadowRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final UpdateThingShadowOperationContext operationContext = GreengrassCoreIPCServiceModel.getUpdateThingShadowModelContext();
    return new UpdateThingShadowResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public UpdateConfigurationResponseHandler updateConfiguration(
      final UpdateConfigurationRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final UpdateConfigurationOperationContext operationContext = GreengrassCoreIPCServiceModel.getUpdateConfigurationModelContext();
    return new UpdateConfigurationResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public ValidateAuthorizationTokenResponseHandler validateAuthorizationToken(
      final ValidateAuthorizationTokenRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final ValidateAuthorizationTokenOperationContext operationContext = GreengrassCoreIPCServiceModel.getValidateAuthorizationTokenModelContext();
    return new ValidateAuthorizationTokenResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public RestartComponentResponseHandler restartComponent(final RestartComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final RestartComponentOperationContext operationContext = GreengrassCoreIPCServiceModel.getRestartComponentModelContext();
    return new RestartComponentResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public GetLocalDeploymentStatusResponseHandler getLocalDeploymentStatus(
      final GetLocalDeploymentStatusRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final GetLocalDeploymentStatusOperationContext operationContext = GreengrassCoreIPCServiceModel.getGetLocalDeploymentStatusModelContext();
    return new GetLocalDeploymentStatusResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public GetSecretValueResponseHandler getSecretValue(final GetSecretValueRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final GetSecretValueOperationContext operationContext = GreengrassCoreIPCServiceModel.getGetSecretValueModelContext();
    return new GetSecretValueResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public UpdateStateResponseHandler updateState(final UpdateStateRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final UpdateStateOperationContext operationContext = GreengrassCoreIPCServiceModel.getUpdateStateModelContext();
    return new UpdateStateResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public ListNamedShadowsForThingResponseHandler listNamedShadowsForThing(
      final ListNamedShadowsForThingRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final ListNamedShadowsForThingOperationContext operationContext = GreengrassCoreIPCServiceModel.getListNamedShadowsForThingModelContext();
    return new ListNamedShadowsForThingResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public SubscribeToComponentUpdatesResponseHandler subscribeToComponentUpdates(
      final SubscribeToComponentUpdatesRequest request,
      final Optional<StreamResponseHandler<ComponentUpdatePolicyEvents>> streamResponseHandler) {
    final SubscribeToComponentUpdatesOperationContext operationContext = GreengrassCoreIPCServiceModel.getSubscribeToComponentUpdatesModelContext();
    return new SubscribeToComponentUpdatesResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public ListLocalDeploymentsResponseHandler listLocalDeployments(
      final ListLocalDeploymentsRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final ListLocalDeploymentsOperationContext operationContext = GreengrassCoreIPCServiceModel.getListLocalDeploymentsModelContext();
    return new ListLocalDeploymentsResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public StopComponentResponseHandler stopComponent(final StopComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final StopComponentOperationContext operationContext = GreengrassCoreIPCServiceModel.getStopComponentModelContext();
    return new StopComponentResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public PauseComponentResponseHandler pauseComponent(final PauseComponentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final PauseComponentOperationContext operationContext = GreengrassCoreIPCServiceModel.getPauseComponentModelContext();
    return new PauseComponentResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }

  @Override
  public CreateLocalDeploymentResponseHandler createLocalDeployment(
      final CreateLocalDeploymentRequest request,
      final Optional<StreamResponseHandler<EventStreamJsonMessage>> streamResponseHandler) {
    final CreateLocalDeploymentOperationContext operationContext = GreengrassCoreIPCServiceModel.getCreateLocalDeploymentModelContext();
    return new CreateLocalDeploymentResponseHandler(doOperationInvoke(operationContext, request, streamResponseHandler));
  }
}
