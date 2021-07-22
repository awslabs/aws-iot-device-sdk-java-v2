package software.amazon.awssdk.eventstreamrpc;

import java.util.Collection;

public abstract class EventStreamRPCServiceHandler implements OperationContinuationHandlerFactory {
    private AuthenticationHandler authenticationHandler;
    private AuthorizationHandler authorizationHandler;

    public EventStreamRPCServiceHandler() {
        authorizationHandler = null;
    }

    protected abstract EventStreamRPCServiceModel getServiceModel();

    /**
     * Probably only useful for logging
     * @return Returns the service name for the set of RPC operations
     */
    public String getServiceName() {
        return getServiceModel().getServiceName();
    }

    /**
     * TODO: How may we want to protect this from being re-assigned after service creation?
     * @param handler
     */
    public void setAuthorizationHandler(final AuthorizationHandler handler) {
        this.authorizationHandler = handler;
    }

    /**
     * Use this to determine if the connection should be accepted or rejected for this service
     *
     * @return
     */
    public AuthorizationHandler getAuthorizationHandler() {
        return authorizationHandler;
    }

    @Override
    public Collection<String> getAllOperations() {
        return getServiceModel().getAllOperations();
    }

    /**
     * Pulls caller/client identity when server connection occurs
     * @return
     */
    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    /**
     * TODO: How may we want to protect this from being re-assigned after service creation?
     * @param authenticationHandler
     */
    public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }
}
