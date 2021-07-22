package software.amazon.awssdk.aws.greengrass;

import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import software.amazon.awssdk.aws.greengrass.model.BinaryMessage;
import software.amazon.awssdk.aws.greengrass.model.ComponentDetails;
import software.amazon.awssdk.aws.greengrass.model.ComponentNotFoundError;
import software.amazon.awssdk.aws.greengrass.model.ComponentUpdatePolicyEvents;
import software.amazon.awssdk.aws.greengrass.model.ConfigurationUpdateEvent;
import software.amazon.awssdk.aws.greengrass.model.ConfigurationUpdateEvents;
import software.amazon.awssdk.aws.greengrass.model.ConfigurationValidityReport;
import software.amazon.awssdk.aws.greengrass.model.ConfigurationValidityStatus;
import software.amazon.awssdk.aws.greengrass.model.ConflictError;
import software.amazon.awssdk.aws.greengrass.model.CreateDebugPasswordRequest;
import software.amazon.awssdk.aws.greengrass.model.CreateDebugPasswordResponse;
import software.amazon.awssdk.aws.greengrass.model.CreateLocalDeploymentRequest;
import software.amazon.awssdk.aws.greengrass.model.CreateLocalDeploymentResponse;
import software.amazon.awssdk.aws.greengrass.model.DeferComponentUpdateRequest;
import software.amazon.awssdk.aws.greengrass.model.DeferComponentUpdateResponse;
import software.amazon.awssdk.aws.greengrass.model.DeleteThingShadowRequest;
import software.amazon.awssdk.aws.greengrass.model.DeleteThingShadowResponse;
import software.amazon.awssdk.aws.greengrass.model.DeploymentStatus;
import software.amazon.awssdk.aws.greengrass.model.FailedUpdateConditionCheckError;
import software.amazon.awssdk.aws.greengrass.model.GetComponentDetailsRequest;
import software.amazon.awssdk.aws.greengrass.model.GetComponentDetailsResponse;
import software.amazon.awssdk.aws.greengrass.model.GetConfigurationRequest;
import software.amazon.awssdk.aws.greengrass.model.GetConfigurationResponse;
import software.amazon.awssdk.aws.greengrass.model.GetLocalDeploymentStatusRequest;
import software.amazon.awssdk.aws.greengrass.model.GetLocalDeploymentStatusResponse;
import software.amazon.awssdk.aws.greengrass.model.GetSecretValueRequest;
import software.amazon.awssdk.aws.greengrass.model.GetSecretValueResponse;
import software.amazon.awssdk.aws.greengrass.model.GetThingShadowRequest;
import software.amazon.awssdk.aws.greengrass.model.GetThingShadowResponse;
import software.amazon.awssdk.aws.greengrass.model.InvalidArgumentsError;
import software.amazon.awssdk.aws.greengrass.model.InvalidArtifactsDirectoryPathError;
import software.amazon.awssdk.aws.greengrass.model.InvalidRecipeDirectoryPathError;
import software.amazon.awssdk.aws.greengrass.model.InvalidTokenError;
import software.amazon.awssdk.aws.greengrass.model.IoTCoreMessage;
import software.amazon.awssdk.aws.greengrass.model.JsonMessage;
import software.amazon.awssdk.aws.greengrass.model.LifecycleState;
import software.amazon.awssdk.aws.greengrass.model.ListComponentsRequest;
import software.amazon.awssdk.aws.greengrass.model.ListComponentsResponse;
import software.amazon.awssdk.aws.greengrass.model.ListLocalDeploymentsRequest;
import software.amazon.awssdk.aws.greengrass.model.ListLocalDeploymentsResponse;
import software.amazon.awssdk.aws.greengrass.model.ListNamedShadowsForThingRequest;
import software.amazon.awssdk.aws.greengrass.model.ListNamedShadowsForThingResponse;
import software.amazon.awssdk.aws.greengrass.model.LocalDeployment;
import software.amazon.awssdk.aws.greengrass.model.MQTTMessage;
import software.amazon.awssdk.aws.greengrass.model.PauseComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.PauseComponentResponse;
import software.amazon.awssdk.aws.greengrass.model.PostComponentUpdateEvent;
import software.amazon.awssdk.aws.greengrass.model.PreComponentUpdateEvent;
import software.amazon.awssdk.aws.greengrass.model.PublishMessage;
import software.amazon.awssdk.aws.greengrass.model.PublishToIoTCoreRequest;
import software.amazon.awssdk.aws.greengrass.model.PublishToIoTCoreResponse;
import software.amazon.awssdk.aws.greengrass.model.PublishToTopicRequest;
import software.amazon.awssdk.aws.greengrass.model.PublishToTopicResponse;
import software.amazon.awssdk.aws.greengrass.model.QOS;
import software.amazon.awssdk.aws.greengrass.model.ReportedLifecycleState;
import software.amazon.awssdk.aws.greengrass.model.RequestStatus;
import software.amazon.awssdk.aws.greengrass.model.ResourceNotFoundError;
import software.amazon.awssdk.aws.greengrass.model.RestartComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.RestartComponentResponse;
import software.amazon.awssdk.aws.greengrass.model.ResumeComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.ResumeComponentResponse;
import software.amazon.awssdk.aws.greengrass.model.RunWithInfo;
import software.amazon.awssdk.aws.greengrass.model.SecretValue;
import software.amazon.awssdk.aws.greengrass.model.SendConfigurationValidityReportRequest;
import software.amazon.awssdk.aws.greengrass.model.SendConfigurationValidityReportResponse;
import software.amazon.awssdk.aws.greengrass.model.ServiceError;
import software.amazon.awssdk.aws.greengrass.model.StopComponentRequest;
import software.amazon.awssdk.aws.greengrass.model.StopComponentResponse;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToComponentUpdatesRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToComponentUpdatesResponse;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToConfigurationUpdateRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToConfigurationUpdateResponse;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToIoTCoreRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToIoTCoreResponse;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToTopicRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToTopicResponse;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToValidateConfigurationUpdatesRequest;
import software.amazon.awssdk.aws.greengrass.model.SubscribeToValidateConfigurationUpdatesResponse;
import software.amazon.awssdk.aws.greengrass.model.SubscriptionResponseMessage;
import software.amazon.awssdk.aws.greengrass.model.SystemResourceLimits;
import software.amazon.awssdk.aws.greengrass.model.UnauthorizedError;
import software.amazon.awssdk.aws.greengrass.model.UpdateConfigurationRequest;
import software.amazon.awssdk.aws.greengrass.model.UpdateConfigurationResponse;
import software.amazon.awssdk.aws.greengrass.model.UpdateStateRequest;
import software.amazon.awssdk.aws.greengrass.model.UpdateStateResponse;
import software.amazon.awssdk.aws.greengrass.model.UpdateThingShadowRequest;
import software.amazon.awssdk.aws.greengrass.model.UpdateThingShadowResponse;
import software.amazon.awssdk.aws.greengrass.model.ValidateAuthorizationTokenRequest;
import software.amazon.awssdk.aws.greengrass.model.ValidateAuthorizationTokenResponse;
import software.amazon.awssdk.aws.greengrass.model.ValidateConfigurationUpdateEvent;
import software.amazon.awssdk.aws.greengrass.model.ValidateConfigurationUpdateEvents;
import software.amazon.awssdk.eventstreamrpc.EventStreamRPCServiceModel;
import software.amazon.awssdk.eventstreamrpc.OperationModelContext;
import software.amazon.awssdk.eventstreamrpc.model.EventStreamJsonMessage;

public class GreengrassCoreIPCServiceModel extends EventStreamRPCServiceModel {
  private static final GreengrassCoreIPCServiceModel INSTANCE = new GreengrassCoreIPCServiceModel();

  public static final String SERVICE_NAMESPACE = "aws.greengrass";

  public static final String SERVICE_NAME = SERVICE_NAMESPACE + "#" + "GreengrassCoreIPC";

  private static final Set<String> SERVICE_OPERATION_SET = new HashSet<String>();

  private static final Map<String, OperationModelContext> SERVICE_OPERATION_MODEL_MAP = new HashMap<String, OperationModelContext>();

  private static final Map<String, Class<? extends EventStreamJsonMessage>> SERVICE_OBJECT_MODEL_MAP = new HashMap<String, Class<? extends EventStreamJsonMessage>>();

  public static final String SUBSCRIBE_TO_IOT_CORE = SERVICE_NAMESPACE + "#" + "SubscribeToIoTCore";

  private static final SubscribeToIoTCoreOperationContext _SUBSCRIBE_TO_IOT_CORE_OPERATION_CONTEXT = new SubscribeToIoTCoreOperationContext();

  public static final String RESUME_COMPONENT = SERVICE_NAMESPACE + "#" + "ResumeComponent";

  private static final ResumeComponentOperationContext _RESUME_COMPONENT_OPERATION_CONTEXT = new ResumeComponentOperationContext();

  public static final String PUBLISH_TO_IOT_CORE = SERVICE_NAMESPACE + "#" + "PublishToIoTCore";

  private static final PublishToIoTCoreOperationContext _PUBLISH_TO_IOT_CORE_OPERATION_CONTEXT = new PublishToIoTCoreOperationContext();

  public static final String SUBSCRIBE_TO_CONFIGURATION_UPDATE = SERVICE_NAMESPACE + "#" + "SubscribeToConfigurationUpdate";

  private static final SubscribeToConfigurationUpdateOperationContext _SUBSCRIBE_TO_CONFIGURATION_UPDATE_OPERATION_CONTEXT = new SubscribeToConfigurationUpdateOperationContext();

  public static final String DELETE_THING_SHADOW = SERVICE_NAMESPACE + "#" + "DeleteThingShadow";

  private static final DeleteThingShadowOperationContext _DELETE_THING_SHADOW_OPERATION_CONTEXT = new DeleteThingShadowOperationContext();

  public static final String DEFER_COMPONENT_UPDATE = SERVICE_NAMESPACE + "#" + "DeferComponentUpdate";

  private static final DeferComponentUpdateOperationContext _DEFER_COMPONENT_UPDATE_OPERATION_CONTEXT = new DeferComponentUpdateOperationContext();

  public static final String SUBSCRIBE_TO_VALIDATE_CONFIGURATION_UPDATES = SERVICE_NAMESPACE + "#" + "SubscribeToValidateConfigurationUpdates";

  private static final SubscribeToValidateConfigurationUpdatesOperationContext _SUBSCRIBE_TO_VALIDATE_CONFIGURATION_UPDATES_OPERATION_CONTEXT = new SubscribeToValidateConfigurationUpdatesOperationContext();

  public static final String GET_CONFIGURATION = SERVICE_NAMESPACE + "#" + "GetConfiguration";

  private static final GetConfigurationOperationContext _GET_CONFIGURATION_OPERATION_CONTEXT = new GetConfigurationOperationContext();

  public static final String SUBSCRIBE_TO_TOPIC = SERVICE_NAMESPACE + "#" + "SubscribeToTopic";

  private static final SubscribeToTopicOperationContext _SUBSCRIBE_TO_TOPIC_OPERATION_CONTEXT = new SubscribeToTopicOperationContext();

  public static final String GET_COMPONENT_DETAILS = SERVICE_NAMESPACE + "#" + "GetComponentDetails";

  private static final GetComponentDetailsOperationContext _GET_COMPONENT_DETAILS_OPERATION_CONTEXT = new GetComponentDetailsOperationContext();

  public static final String PUBLISH_TO_TOPIC = SERVICE_NAMESPACE + "#" + "PublishToTopic";

  private static final PublishToTopicOperationContext _PUBLISH_TO_TOPIC_OPERATION_CONTEXT = new PublishToTopicOperationContext();

  public static final String LIST_COMPONENTS = SERVICE_NAMESPACE + "#" + "ListComponents";

  private static final ListComponentsOperationContext _LIST_COMPONENTS_OPERATION_CONTEXT = new ListComponentsOperationContext();

  public static final String CREATE_DEBUG_PASSWORD = SERVICE_NAMESPACE + "#" + "CreateDebugPassword";

  private static final CreateDebugPasswordOperationContext _CREATE_DEBUG_PASSWORD_OPERATION_CONTEXT = new CreateDebugPasswordOperationContext();

  public static final String GET_THING_SHADOW = SERVICE_NAMESPACE + "#" + "GetThingShadow";

  private static final GetThingShadowOperationContext _GET_THING_SHADOW_OPERATION_CONTEXT = new GetThingShadowOperationContext();

  public static final String SEND_CONFIGURATION_VALIDITY_REPORT = SERVICE_NAMESPACE + "#" + "SendConfigurationValidityReport";

  private static final SendConfigurationValidityReportOperationContext _SEND_CONFIGURATION_VALIDITY_REPORT_OPERATION_CONTEXT = new SendConfigurationValidityReportOperationContext();

  public static final String UPDATE_THING_SHADOW = SERVICE_NAMESPACE + "#" + "UpdateThingShadow";

  private static final UpdateThingShadowOperationContext _UPDATE_THING_SHADOW_OPERATION_CONTEXT = new UpdateThingShadowOperationContext();

  public static final String UPDATE_CONFIGURATION = SERVICE_NAMESPACE + "#" + "UpdateConfiguration";

  private static final UpdateConfigurationOperationContext _UPDATE_CONFIGURATION_OPERATION_CONTEXT = new UpdateConfigurationOperationContext();

  public static final String VALIDATE_AUTHORIZATION_TOKEN = SERVICE_NAMESPACE + "#" + "ValidateAuthorizationToken";

  private static final ValidateAuthorizationTokenOperationContext _VALIDATE_AUTHORIZATION_TOKEN_OPERATION_CONTEXT = new ValidateAuthorizationTokenOperationContext();

  public static final String RESTART_COMPONENT = SERVICE_NAMESPACE + "#" + "RestartComponent";

  private static final RestartComponentOperationContext _RESTART_COMPONENT_OPERATION_CONTEXT = new RestartComponentOperationContext();

  public static final String GET_LOCAL_DEPLOYMENT_STATUS = SERVICE_NAMESPACE + "#" + "GetLocalDeploymentStatus";

  private static final GetLocalDeploymentStatusOperationContext _GET_LOCAL_DEPLOYMENT_STATUS_OPERATION_CONTEXT = new GetLocalDeploymentStatusOperationContext();

  public static final String GET_SECRET_VALUE = SERVICE_NAMESPACE + "#" + "GetSecretValue";

  private static final GetSecretValueOperationContext _GET_SECRET_VALUE_OPERATION_CONTEXT = new GetSecretValueOperationContext();

  public static final String UPDATE_STATE = SERVICE_NAMESPACE + "#" + "UpdateState";

  private static final UpdateStateOperationContext _UPDATE_STATE_OPERATION_CONTEXT = new UpdateStateOperationContext();

  public static final String LIST_NAMED_SHADOWS_FOR_THING = SERVICE_NAMESPACE + "#" + "ListNamedShadowsForThing";

  private static final ListNamedShadowsForThingOperationContext _LIST_NAMED_SHADOWS_FOR_THING_OPERATION_CONTEXT = new ListNamedShadowsForThingOperationContext();

  public static final String SUBSCRIBE_TO_COMPONENT_UPDATES = SERVICE_NAMESPACE + "#" + "SubscribeToComponentUpdates";

  private static final SubscribeToComponentUpdatesOperationContext _SUBSCRIBE_TO_COMPONENT_UPDATES_OPERATION_CONTEXT = new SubscribeToComponentUpdatesOperationContext();

  public static final String LIST_LOCAL_DEPLOYMENTS = SERVICE_NAMESPACE + "#" + "ListLocalDeployments";

  private static final ListLocalDeploymentsOperationContext _LIST_LOCAL_DEPLOYMENTS_OPERATION_CONTEXT = new ListLocalDeploymentsOperationContext();

  public static final String STOP_COMPONENT = SERVICE_NAMESPACE + "#" + "StopComponent";

  private static final StopComponentOperationContext _STOP_COMPONENT_OPERATION_CONTEXT = new StopComponentOperationContext();

  public static final String PAUSE_COMPONENT = SERVICE_NAMESPACE + "#" + "PauseComponent";

  private static final PauseComponentOperationContext _PAUSE_COMPONENT_OPERATION_CONTEXT = new PauseComponentOperationContext();

  public static final String CREATE_LOCAL_DEPLOYMENT = SERVICE_NAMESPACE + "#" + "CreateLocalDeployment";

  private static final CreateLocalDeploymentOperationContext _CREATE_LOCAL_DEPLOYMENT_OPERATION_CONTEXT = new CreateLocalDeploymentOperationContext();

  static {
    SERVICE_OPERATION_MODEL_MAP.put(SUBSCRIBE_TO_IOT_CORE, _SUBSCRIBE_TO_IOT_CORE_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(SUBSCRIBE_TO_IOT_CORE);
    SERVICE_OPERATION_MODEL_MAP.put(RESUME_COMPONENT, _RESUME_COMPONENT_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(RESUME_COMPONENT);
    SERVICE_OPERATION_MODEL_MAP.put(PUBLISH_TO_IOT_CORE, _PUBLISH_TO_IOT_CORE_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(PUBLISH_TO_IOT_CORE);
    SERVICE_OPERATION_MODEL_MAP.put(SUBSCRIBE_TO_CONFIGURATION_UPDATE, _SUBSCRIBE_TO_CONFIGURATION_UPDATE_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(SUBSCRIBE_TO_CONFIGURATION_UPDATE);
    SERVICE_OPERATION_MODEL_MAP.put(DELETE_THING_SHADOW, _DELETE_THING_SHADOW_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(DELETE_THING_SHADOW);
    SERVICE_OPERATION_MODEL_MAP.put(DEFER_COMPONENT_UPDATE, _DEFER_COMPONENT_UPDATE_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(DEFER_COMPONENT_UPDATE);
    SERVICE_OPERATION_MODEL_MAP.put(SUBSCRIBE_TO_VALIDATE_CONFIGURATION_UPDATES, _SUBSCRIBE_TO_VALIDATE_CONFIGURATION_UPDATES_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(SUBSCRIBE_TO_VALIDATE_CONFIGURATION_UPDATES);
    SERVICE_OPERATION_MODEL_MAP.put(GET_CONFIGURATION, _GET_CONFIGURATION_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(GET_CONFIGURATION);
    SERVICE_OPERATION_MODEL_MAP.put(SUBSCRIBE_TO_TOPIC, _SUBSCRIBE_TO_TOPIC_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(SUBSCRIBE_TO_TOPIC);
    SERVICE_OPERATION_MODEL_MAP.put(GET_COMPONENT_DETAILS, _GET_COMPONENT_DETAILS_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(GET_COMPONENT_DETAILS);
    SERVICE_OPERATION_MODEL_MAP.put(PUBLISH_TO_TOPIC, _PUBLISH_TO_TOPIC_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(PUBLISH_TO_TOPIC);
    SERVICE_OPERATION_MODEL_MAP.put(LIST_COMPONENTS, _LIST_COMPONENTS_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(LIST_COMPONENTS);
    SERVICE_OPERATION_MODEL_MAP.put(CREATE_DEBUG_PASSWORD, _CREATE_DEBUG_PASSWORD_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(CREATE_DEBUG_PASSWORD);
    SERVICE_OPERATION_MODEL_MAP.put(GET_THING_SHADOW, _GET_THING_SHADOW_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(GET_THING_SHADOW);
    SERVICE_OPERATION_MODEL_MAP.put(SEND_CONFIGURATION_VALIDITY_REPORT, _SEND_CONFIGURATION_VALIDITY_REPORT_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(SEND_CONFIGURATION_VALIDITY_REPORT);
    SERVICE_OPERATION_MODEL_MAP.put(UPDATE_THING_SHADOW, _UPDATE_THING_SHADOW_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(UPDATE_THING_SHADOW);
    SERVICE_OPERATION_MODEL_MAP.put(UPDATE_CONFIGURATION, _UPDATE_CONFIGURATION_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(UPDATE_CONFIGURATION);
    SERVICE_OPERATION_MODEL_MAP.put(VALIDATE_AUTHORIZATION_TOKEN, _VALIDATE_AUTHORIZATION_TOKEN_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(VALIDATE_AUTHORIZATION_TOKEN);
    SERVICE_OPERATION_MODEL_MAP.put(RESTART_COMPONENT, _RESTART_COMPONENT_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(RESTART_COMPONENT);
    SERVICE_OPERATION_MODEL_MAP.put(GET_LOCAL_DEPLOYMENT_STATUS, _GET_LOCAL_DEPLOYMENT_STATUS_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(GET_LOCAL_DEPLOYMENT_STATUS);
    SERVICE_OPERATION_MODEL_MAP.put(GET_SECRET_VALUE, _GET_SECRET_VALUE_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(GET_SECRET_VALUE);
    SERVICE_OPERATION_MODEL_MAP.put(UPDATE_STATE, _UPDATE_STATE_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(UPDATE_STATE);
    SERVICE_OPERATION_MODEL_MAP.put(LIST_NAMED_SHADOWS_FOR_THING, _LIST_NAMED_SHADOWS_FOR_THING_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(LIST_NAMED_SHADOWS_FOR_THING);
    SERVICE_OPERATION_MODEL_MAP.put(SUBSCRIBE_TO_COMPONENT_UPDATES, _SUBSCRIBE_TO_COMPONENT_UPDATES_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(SUBSCRIBE_TO_COMPONENT_UPDATES);
    SERVICE_OPERATION_MODEL_MAP.put(LIST_LOCAL_DEPLOYMENTS, _LIST_LOCAL_DEPLOYMENTS_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(LIST_LOCAL_DEPLOYMENTS);
    SERVICE_OPERATION_MODEL_MAP.put(STOP_COMPONENT, _STOP_COMPONENT_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(STOP_COMPONENT);
    SERVICE_OPERATION_MODEL_MAP.put(PAUSE_COMPONENT, _PAUSE_COMPONENT_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(PAUSE_COMPONENT);
    SERVICE_OPERATION_MODEL_MAP.put(CREATE_LOCAL_DEPLOYMENT, _CREATE_LOCAL_DEPLOYMENT_OPERATION_CONTEXT);
    SERVICE_OPERATION_SET.add(CREATE_LOCAL_DEPLOYMENT);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToIoTCoreRequest.APPLICATION_MODEL_TYPE, SubscribeToIoTCoreRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToIoTCoreResponse.APPLICATION_MODEL_TYPE, SubscribeToIoTCoreResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(ServiceError.APPLICATION_MODEL_TYPE, ServiceError.class);
    SERVICE_OBJECT_MODEL_MAP.put(UnauthorizedError.APPLICATION_MODEL_TYPE, UnauthorizedError.class);
    SERVICE_OBJECT_MODEL_MAP.put(ResumeComponentRequest.APPLICATION_MODEL_TYPE, ResumeComponentRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(ResumeComponentResponse.APPLICATION_MODEL_TYPE, ResumeComponentResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(ResourceNotFoundError.APPLICATION_MODEL_TYPE, ResourceNotFoundError.class);
    SERVICE_OBJECT_MODEL_MAP.put(PublishToIoTCoreRequest.APPLICATION_MODEL_TYPE, PublishToIoTCoreRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(PublishToIoTCoreResponse.APPLICATION_MODEL_TYPE, PublishToIoTCoreResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToConfigurationUpdateRequest.APPLICATION_MODEL_TYPE, SubscribeToConfigurationUpdateRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToConfigurationUpdateResponse.APPLICATION_MODEL_TYPE, SubscribeToConfigurationUpdateResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(DeleteThingShadowRequest.APPLICATION_MODEL_TYPE, DeleteThingShadowRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(DeleteThingShadowResponse.APPLICATION_MODEL_TYPE, DeleteThingShadowResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(InvalidArgumentsError.APPLICATION_MODEL_TYPE, InvalidArgumentsError.class);
    SERVICE_OBJECT_MODEL_MAP.put(DeferComponentUpdateRequest.APPLICATION_MODEL_TYPE, DeferComponentUpdateRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(DeferComponentUpdateResponse.APPLICATION_MODEL_TYPE, DeferComponentUpdateResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToValidateConfigurationUpdatesRequest.APPLICATION_MODEL_TYPE, SubscribeToValidateConfigurationUpdatesRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToValidateConfigurationUpdatesResponse.APPLICATION_MODEL_TYPE, SubscribeToValidateConfigurationUpdatesResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetConfigurationRequest.APPLICATION_MODEL_TYPE, GetConfigurationRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetConfigurationResponse.APPLICATION_MODEL_TYPE, GetConfigurationResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToTopicRequest.APPLICATION_MODEL_TYPE, SubscribeToTopicRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToTopicResponse.APPLICATION_MODEL_TYPE, SubscribeToTopicResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetComponentDetailsRequest.APPLICATION_MODEL_TYPE, GetComponentDetailsRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetComponentDetailsResponse.APPLICATION_MODEL_TYPE, GetComponentDetailsResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(PublishToTopicRequest.APPLICATION_MODEL_TYPE, PublishToTopicRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(PublishToTopicResponse.APPLICATION_MODEL_TYPE, PublishToTopicResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(ListComponentsRequest.APPLICATION_MODEL_TYPE, ListComponentsRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(ListComponentsResponse.APPLICATION_MODEL_TYPE, ListComponentsResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(CreateDebugPasswordRequest.APPLICATION_MODEL_TYPE, CreateDebugPasswordRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(CreateDebugPasswordResponse.APPLICATION_MODEL_TYPE, CreateDebugPasswordResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetThingShadowRequest.APPLICATION_MODEL_TYPE, GetThingShadowRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetThingShadowResponse.APPLICATION_MODEL_TYPE, GetThingShadowResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(SendConfigurationValidityReportRequest.APPLICATION_MODEL_TYPE, SendConfigurationValidityReportRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(SendConfigurationValidityReportResponse.APPLICATION_MODEL_TYPE, SendConfigurationValidityReportResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(UpdateThingShadowRequest.APPLICATION_MODEL_TYPE, UpdateThingShadowRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(UpdateThingShadowResponse.APPLICATION_MODEL_TYPE, UpdateThingShadowResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(ConflictError.APPLICATION_MODEL_TYPE, ConflictError.class);
    SERVICE_OBJECT_MODEL_MAP.put(UpdateConfigurationRequest.APPLICATION_MODEL_TYPE, UpdateConfigurationRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(UpdateConfigurationResponse.APPLICATION_MODEL_TYPE, UpdateConfigurationResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(FailedUpdateConditionCheckError.APPLICATION_MODEL_TYPE, FailedUpdateConditionCheckError.class);
    SERVICE_OBJECT_MODEL_MAP.put(ValidateAuthorizationTokenRequest.APPLICATION_MODEL_TYPE, ValidateAuthorizationTokenRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(ValidateAuthorizationTokenResponse.APPLICATION_MODEL_TYPE, ValidateAuthorizationTokenResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(InvalidTokenError.APPLICATION_MODEL_TYPE, InvalidTokenError.class);
    SERVICE_OBJECT_MODEL_MAP.put(RestartComponentRequest.APPLICATION_MODEL_TYPE, RestartComponentRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(RestartComponentResponse.APPLICATION_MODEL_TYPE, RestartComponentResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(ComponentNotFoundError.APPLICATION_MODEL_TYPE, ComponentNotFoundError.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetLocalDeploymentStatusRequest.APPLICATION_MODEL_TYPE, GetLocalDeploymentStatusRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetLocalDeploymentStatusResponse.APPLICATION_MODEL_TYPE, GetLocalDeploymentStatusResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetSecretValueRequest.APPLICATION_MODEL_TYPE, GetSecretValueRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(GetSecretValueResponse.APPLICATION_MODEL_TYPE, GetSecretValueResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(UpdateStateRequest.APPLICATION_MODEL_TYPE, UpdateStateRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(UpdateStateResponse.APPLICATION_MODEL_TYPE, UpdateStateResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(ListNamedShadowsForThingRequest.APPLICATION_MODEL_TYPE, ListNamedShadowsForThingRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(ListNamedShadowsForThingResponse.APPLICATION_MODEL_TYPE, ListNamedShadowsForThingResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToComponentUpdatesRequest.APPLICATION_MODEL_TYPE, SubscribeToComponentUpdatesRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscribeToComponentUpdatesResponse.APPLICATION_MODEL_TYPE, SubscribeToComponentUpdatesResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(ListLocalDeploymentsRequest.APPLICATION_MODEL_TYPE, ListLocalDeploymentsRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(ListLocalDeploymentsResponse.APPLICATION_MODEL_TYPE, ListLocalDeploymentsResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(StopComponentRequest.APPLICATION_MODEL_TYPE, StopComponentRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(StopComponentResponse.APPLICATION_MODEL_TYPE, StopComponentResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(PauseComponentRequest.APPLICATION_MODEL_TYPE, PauseComponentRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(PauseComponentResponse.APPLICATION_MODEL_TYPE, PauseComponentResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(CreateLocalDeploymentRequest.APPLICATION_MODEL_TYPE, CreateLocalDeploymentRequest.class);
    SERVICE_OBJECT_MODEL_MAP.put(CreateLocalDeploymentResponse.APPLICATION_MODEL_TYPE, CreateLocalDeploymentResponse.class);
    SERVICE_OBJECT_MODEL_MAP.put(InvalidRecipeDirectoryPathError.APPLICATION_MODEL_TYPE, InvalidRecipeDirectoryPathError.class);
    SERVICE_OBJECT_MODEL_MAP.put(InvalidArtifactsDirectoryPathError.APPLICATION_MODEL_TYPE, InvalidArtifactsDirectoryPathError.class);
    SERVICE_OBJECT_MODEL_MAP.put(QOS.APPLICATION_MODEL_TYPE, QOS.class);
    SERVICE_OBJECT_MODEL_MAP.put(IoTCoreMessage.APPLICATION_MODEL_TYPE, IoTCoreMessage.class);
    SERVICE_OBJECT_MODEL_MAP.put(ConfigurationUpdateEvents.APPLICATION_MODEL_TYPE, ConfigurationUpdateEvents.class);
    SERVICE_OBJECT_MODEL_MAP.put(ValidateConfigurationUpdateEvents.APPLICATION_MODEL_TYPE, ValidateConfigurationUpdateEvents.class);
    SERVICE_OBJECT_MODEL_MAP.put(SubscriptionResponseMessage.APPLICATION_MODEL_TYPE, SubscriptionResponseMessage.class);
    SERVICE_OBJECT_MODEL_MAP.put(ComponentDetails.APPLICATION_MODEL_TYPE, ComponentDetails.class);
    SERVICE_OBJECT_MODEL_MAP.put(PublishMessage.APPLICATION_MODEL_TYPE, PublishMessage.class);
    SERVICE_OBJECT_MODEL_MAP.put(ConfigurationValidityReport.APPLICATION_MODEL_TYPE, ConfigurationValidityReport.class);
    SERVICE_OBJECT_MODEL_MAP.put(RequestStatus.APPLICATION_MODEL_TYPE, RequestStatus.class);
    SERVICE_OBJECT_MODEL_MAP.put(LocalDeployment.APPLICATION_MODEL_TYPE, LocalDeployment.class);
    SERVICE_OBJECT_MODEL_MAP.put(SecretValue.APPLICATION_MODEL_TYPE, SecretValue.class);
    SERVICE_OBJECT_MODEL_MAP.put(ReportedLifecycleState.APPLICATION_MODEL_TYPE, ReportedLifecycleState.class);
    SERVICE_OBJECT_MODEL_MAP.put(ComponentUpdatePolicyEvents.APPLICATION_MODEL_TYPE, ComponentUpdatePolicyEvents.class);
    SERVICE_OBJECT_MODEL_MAP.put(MQTTMessage.APPLICATION_MODEL_TYPE, MQTTMessage.class);
    SERVICE_OBJECT_MODEL_MAP.put(ConfigurationUpdateEvent.APPLICATION_MODEL_TYPE, ConfigurationUpdateEvent.class);
    SERVICE_OBJECT_MODEL_MAP.put(ValidateConfigurationUpdateEvent.APPLICATION_MODEL_TYPE, ValidateConfigurationUpdateEvent.class);
    SERVICE_OBJECT_MODEL_MAP.put(JsonMessage.APPLICATION_MODEL_TYPE, JsonMessage.class);
    SERVICE_OBJECT_MODEL_MAP.put(BinaryMessage.APPLICATION_MODEL_TYPE, BinaryMessage.class);
    SERVICE_OBJECT_MODEL_MAP.put(LifecycleState.APPLICATION_MODEL_TYPE, LifecycleState.class);
    SERVICE_OBJECT_MODEL_MAP.put(ConfigurationValidityStatus.APPLICATION_MODEL_TYPE, ConfigurationValidityStatus.class);
    SERVICE_OBJECT_MODEL_MAP.put(DeploymentStatus.APPLICATION_MODEL_TYPE, DeploymentStatus.class);
    SERVICE_OBJECT_MODEL_MAP.put(PreComponentUpdateEvent.APPLICATION_MODEL_TYPE, PreComponentUpdateEvent.class);
    SERVICE_OBJECT_MODEL_MAP.put(PostComponentUpdateEvent.APPLICATION_MODEL_TYPE, PostComponentUpdateEvent.class);
    SERVICE_OBJECT_MODEL_MAP.put(RunWithInfo.APPLICATION_MODEL_TYPE, RunWithInfo.class);
    SERVICE_OBJECT_MODEL_MAP.put(SystemResourceLimits.APPLICATION_MODEL_TYPE, SystemResourceLimits.class);
  }

  private GreengrassCoreIPCServiceModel() {
  }

  public static GreengrassCoreIPCServiceModel getInstance() {
    return INSTANCE;
  }

  @Override
  public String getServiceName() {
    return "aws.greengrass#GreengrassCoreIPC";
  }

  public static SubscribeToIoTCoreOperationContext getSubscribeToIoTCoreModelContext() {
    return _SUBSCRIBE_TO_IOT_CORE_OPERATION_CONTEXT;
  }

  public static ResumeComponentOperationContext getResumeComponentModelContext() {
    return _RESUME_COMPONENT_OPERATION_CONTEXT;
  }

  public static PublishToIoTCoreOperationContext getPublishToIoTCoreModelContext() {
    return _PUBLISH_TO_IOT_CORE_OPERATION_CONTEXT;
  }

  public static SubscribeToConfigurationUpdateOperationContext getSubscribeToConfigurationUpdateModelContext(
      ) {
    return _SUBSCRIBE_TO_CONFIGURATION_UPDATE_OPERATION_CONTEXT;
  }

  public static DeleteThingShadowOperationContext getDeleteThingShadowModelContext() {
    return _DELETE_THING_SHADOW_OPERATION_CONTEXT;
  }

  public static DeferComponentUpdateOperationContext getDeferComponentUpdateModelContext() {
    return _DEFER_COMPONENT_UPDATE_OPERATION_CONTEXT;
  }

  public static SubscribeToValidateConfigurationUpdatesOperationContext getSubscribeToValidateConfigurationUpdatesModelContext(
      ) {
    return _SUBSCRIBE_TO_VALIDATE_CONFIGURATION_UPDATES_OPERATION_CONTEXT;
  }

  public static GetConfigurationOperationContext getGetConfigurationModelContext() {
    return _GET_CONFIGURATION_OPERATION_CONTEXT;
  }

  public static SubscribeToTopicOperationContext getSubscribeToTopicModelContext() {
    return _SUBSCRIBE_TO_TOPIC_OPERATION_CONTEXT;
  }

  public static GetComponentDetailsOperationContext getGetComponentDetailsModelContext() {
    return _GET_COMPONENT_DETAILS_OPERATION_CONTEXT;
  }

  public static PublishToTopicOperationContext getPublishToTopicModelContext() {
    return _PUBLISH_TO_TOPIC_OPERATION_CONTEXT;
  }

  public static ListComponentsOperationContext getListComponentsModelContext() {
    return _LIST_COMPONENTS_OPERATION_CONTEXT;
  }

  public static CreateDebugPasswordOperationContext getCreateDebugPasswordModelContext() {
    return _CREATE_DEBUG_PASSWORD_OPERATION_CONTEXT;
  }

  public static GetThingShadowOperationContext getGetThingShadowModelContext() {
    return _GET_THING_SHADOW_OPERATION_CONTEXT;
  }

  public static SendConfigurationValidityReportOperationContext getSendConfigurationValidityReportModelContext(
      ) {
    return _SEND_CONFIGURATION_VALIDITY_REPORT_OPERATION_CONTEXT;
  }

  public static UpdateThingShadowOperationContext getUpdateThingShadowModelContext() {
    return _UPDATE_THING_SHADOW_OPERATION_CONTEXT;
  }

  public static UpdateConfigurationOperationContext getUpdateConfigurationModelContext() {
    return _UPDATE_CONFIGURATION_OPERATION_CONTEXT;
  }

  public static ValidateAuthorizationTokenOperationContext getValidateAuthorizationTokenModelContext(
      ) {
    return _VALIDATE_AUTHORIZATION_TOKEN_OPERATION_CONTEXT;
  }

  public static RestartComponentOperationContext getRestartComponentModelContext() {
    return _RESTART_COMPONENT_OPERATION_CONTEXT;
  }

  public static GetLocalDeploymentStatusOperationContext getGetLocalDeploymentStatusModelContext() {
    return _GET_LOCAL_DEPLOYMENT_STATUS_OPERATION_CONTEXT;
  }

  public static GetSecretValueOperationContext getGetSecretValueModelContext() {
    return _GET_SECRET_VALUE_OPERATION_CONTEXT;
  }

  public static UpdateStateOperationContext getUpdateStateModelContext() {
    return _UPDATE_STATE_OPERATION_CONTEXT;
  }

  public static ListNamedShadowsForThingOperationContext getListNamedShadowsForThingModelContext() {
    return _LIST_NAMED_SHADOWS_FOR_THING_OPERATION_CONTEXT;
  }

  public static SubscribeToComponentUpdatesOperationContext getSubscribeToComponentUpdatesModelContext(
      ) {
    return _SUBSCRIBE_TO_COMPONENT_UPDATES_OPERATION_CONTEXT;
  }

  public static ListLocalDeploymentsOperationContext getListLocalDeploymentsModelContext() {
    return _LIST_LOCAL_DEPLOYMENTS_OPERATION_CONTEXT;
  }

  public static StopComponentOperationContext getStopComponentModelContext() {
    return _STOP_COMPONENT_OPERATION_CONTEXT;
  }

  public static PauseComponentOperationContext getPauseComponentModelContext() {
    return _PAUSE_COMPONENT_OPERATION_CONTEXT;
  }

  public static CreateLocalDeploymentOperationContext getCreateLocalDeploymentModelContext() {
    return _CREATE_LOCAL_DEPLOYMENT_OPERATION_CONTEXT;
  }

  @Override
  public final Collection<String> getAllOperations() {
    // Return a defensive copy so caller cannot change internal structure of service model
    return new HashSet<String>(SERVICE_OPERATION_SET);
  }

  @Override
  protected Optional<Class<? extends EventStreamJsonMessage>> getServiceClassType(
      String applicationModelType) {
    if (SERVICE_OBJECT_MODEL_MAP.containsKey(applicationModelType)) {
      return Optional.of(SERVICE_OBJECT_MODEL_MAP.get(applicationModelType));
    }
    return Optional.empty();
  }

  @Override
  public OperationModelContext getOperationModelContext(String operationName) {
    return SERVICE_OPERATION_MODEL_MAP.get(operationName);
  }
}
