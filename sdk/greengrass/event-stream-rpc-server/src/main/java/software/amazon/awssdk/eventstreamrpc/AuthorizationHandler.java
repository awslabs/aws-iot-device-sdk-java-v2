package software.amazon.awssdk.eventstreamrpc;

import java.util.function.Function;

/**
 * Handler receives the input data of the connection message and produces an authorization result
 * which is a decision on accept or rejecting the connection
 *
 * -The apply function must return an Authorization object with a non-null AuthenticationData object
 * returned. It's great idea for implementations to log appropriate input
 *
 */
public interface AuthorizationHandler extends Function<AuthenticationData, Authorization> { }
