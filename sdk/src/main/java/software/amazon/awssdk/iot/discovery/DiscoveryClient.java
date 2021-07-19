package software.amazon.awssdk.iot.discovery;

import com.google.gson.Gson;
import software.amazon.awssdk.crt.http.*;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.iot.discovery.model.DiscoverResponse;

import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DiscoveryClient implements AutoCloseable {
    public static final String TLS_EXT_ALPN = "x-amzn-http-ca";

    private static final String HTTP_HEADER_REQUEST_ID = "x-amzn-RequestId";
    private static final String HTTP_HEADER_ERROR_TYPE = "x-amzn-ErrorType";
    private static final String AWS_DOMAIN_DEFAULT = "amazonaws.com";
    private static final Map<String, String> AWS_DOMAIN_SUFFIX_MAP = new HashMap<>();
    static {
        AWS_DOMAIN_SUFFIX_MAP.put("cn-north-1", "amazonaws.com.cn");
        AWS_DOMAIN_SUFFIX_MAP.put("cn-northwest-1", "amazonaws.com.cn");
        AWS_DOMAIN_SUFFIX_MAP.put("us-isob-east-1", "sc2s.sgov.gov");
        AWS_DOMAIN_SUFFIX_MAP.put("us-iso-east-1", "c2s.ic.gov");
    }
    private static final Gson GSON = new Gson();

    private final HttpClientConnectionManager httpClientConnectionManager;

    public DiscoveryClient(final DiscoveryClientConfig config) {
        this.httpClientConnectionManager = HttpClientConnectionManager.create(
                new HttpClientConnectionManagerOptions()
                    .withClientBootstrap(config.getBootstrap())
                    .withProxyOptions(config.getProxyOptions())
                    .withSocketOptions(config.getSocketOptions())
                    .withMaxConnections(config.getMaxConnections())
                    .withTlsContext(config.getTlsContext())
                    .withUri(URI.create("https://" + getHostname(config)))
                    .withPort(TlsContextOptions.isAlpnSupported() ? 443 : 8443));
    }

    public CompletableFuture<DiscoverResponse> discover(final String thingName) {
        if(thingName == null) {
            throw new IllegalArgumentException("ThingName cannot be null!");
        }
        return CompletableFuture.supplyAsync(() -> {
            try(final HttpClientConnection connection = httpClientConnectionManager.acquireConnection().get()) {
                final String requestHttpPath =  "/greengrass/discover/thing/" + thingName;
                final HttpHeader[] headers = new HttpHeader[] {
                        new HttpHeader("host", httpClientConnectionManager.getUri().getHost())
                };
                final HttpRequest request = new HttpRequest("GET", requestHttpPath, headers, null);
                //we are storing everything until we get the entire response
                final CompletableFuture<Integer> responseComplete = new CompletableFuture<>();
                final StringBuilder jsonBodyResponseBuilder = new StringBuilder();
                final Map<String, String> responseInfo = new HashMap<>();
                try(final HttpStream stream = connection.makeRequest(request, new HttpStreamResponseHandler() {
                        @Override
                        public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType, HttpHeader[] httpHeaders) {
                            Arrays.stream(httpHeaders).forEach(header -> {
                                responseInfo.put(header.getName(), header.getValue());
                            });
                        }
                        @Override
                        public int onResponseBody(HttpStream stream, byte bodyBytes[]) {
                            jsonBodyResponseBuilder.append(new String(bodyBytes, StandardCharsets.UTF_8));
                            return bodyBytes.length;
                        }
                        @Override
                        public void onResponseComplete(HttpStream httpStream, int errorCode) {
                            responseComplete.complete(errorCode);
                        }})) {
                    stream.activate();
                    responseComplete.get();
                    if (stream.getResponseStatusCode() != 200) {
                        throw new RuntimeException(String.format("Error %s(%d); RequestId: %s",
                                HTTP_HEADER_ERROR_TYPE, stream.getResponseStatusCode(), HTTP_HEADER_REQUEST_ID));
                    }
                    final String responseString = jsonBodyResponseBuilder.toString();
                    return GSON.fromJson(new StringReader(responseString), DiscoverResponse.class);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            catch(InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private static String CN_ENDPOINT_TEMPLATE = "greengrass.ats.iot.%s.%s";
    private static String DEFAULT_ENDPOINT_TEMPLATE = "greengrass-ats.iot.%s.%s";

    private static String getHostname(final DiscoveryClientConfig config) {
        // Temporary fix for connection with china endpoint
        String endpointTemplate = DEFAULT_ENDPOINT_TEMPLATE;
        if (config.getRegion().equals("cn-north-1")) {
            endpointTemplate = CN_ENDPOINT_TEMPLATE;
        }

        return String.format(endpointTemplate,
                config.getRegion(), AWS_DOMAIN_SUFFIX_MAP.getOrDefault(config.getRegion(), AWS_DOMAIN_DEFAULT));
    }

    @Override
    public void close() {
        if(httpClientConnectionManager != null) {
            httpClientConnectionManager.close();
        }
    }
}
