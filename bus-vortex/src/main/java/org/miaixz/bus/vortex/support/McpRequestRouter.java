/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.support;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.annotation.NonNull;

/**
 * An implementation of the Router interface for the MCP protocol, responsible for route selection and load balancing.
 * <p>
 * This class implements the routing logic for the MCP (Message Communication Protocol). It supports various load
 * balancing strategies, including round-robin, random, and weighted, and provides a health check mechanism for service
 * instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class McpRequestRouter implements Router {

    /**
     * Predefined {@code ExchangeStrategies} instance for WebClient configuration.
     * <p>
     * This instance is cached upon class loading to improve performance and is configured with a maximum memory size to
     * prevent `OutOfMemoryError` from large requests. The memory limit is set to 128MB to handle most typical request
     * sizes while protecting the system from memory exhaustion attacks.
     */
    private static final ExchangeStrategies CACHED_EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128))).build();

    /**
     * A thread-safe cache for WebClient instances, keyed by base URL.
     * <p>
     * This map ensures that a WebClient instance is reused for a given base URL, optimizing resource usage and
     * performance. Each WebClient is configured with the same ExchangeStrategies to maintain consistent behavior across
     * all HTTP requests. The ConcurrentHashMap implementation provides thread-safe operations without explicit
     * synchronization.
     */
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    /**
     * A thread-safe cache for service instances, keyed by service name.
     * <p>
     * This map stores lists of service instances for each service name, allowing quick lookup of available instances
     * during routing. The CopyOnWriteArrayList ensures thread-safe iteration and modification operations, which is
     * important for service discovery scenarios where instances may be dynamically added or removed.
     */
    private final Map<String, List<ServiceInstance>> serviceCache = new ConcurrentHashMap<>();

    /**
     * Load balancing counters for the round-robin strategy, keyed by service name.
     * <p>
     * This map maintains atomic counters for each service name to implement the round-robin load balancing algorithm.
     * The AtomicInteger ensures thread-safe increment operations, preventing race conditions when multiple requests are
     * being routed simultaneously. Each service name has its own counter to maintain independent round-robin sequences.
     */
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * The default load balancing strategy.
     * <p>
     * This constant defines the fallback strategy when no specific strategy is configured. Round-robin is chosen as the
     * default because it provides a simple, fair distribution of requests across available instances and is easy to
     * implement and understand.
     */
    private static final EnumValue.Balance DEFAULT_STRATEGY = EnumValue.Balance.ROUND_ROBIN;

    /**
     * The default weight for service instances.
     * <p>
     * This constant defines the default weight assigned to service instances when no explicit weight is specified. A
     * weight of 1 means each instance has equal priority in weighted load balancing algorithms. This value is used
     * during service instance initialization and as a fallback when invalid weight values are provided.
     */
    private static final int DEFAULT_WEIGHT = 1;

    /**
     * Handles client requests by routing them to a target service and returning the response.
     * <p>
     * This is the core routing method for the MCP protocol. It selects an appropriate service instance based on the
     * configured load balancing strategy, then builds and sends the request. The process includes:
     * <ol>
     * <li>Validating input parameters</li>
     * <li>Retrieving the list of service instances</li>
     * <li>Performing health checks</li>
     * <li>Applying the load balancing strategy to select an instance</li>
     * <li>Building and sending the request</li>
     * <li>Processing the response</li>
     * </ol>
     *
     * @param request The client's {@code ServerRequest} object.
     * @param context The request context, containing parameters and configuration.
     * @param assets  The configuration assets for the target service.
     * @return A {@code Mono<ServerResponse>} representing the asynchronous response from the target service.
     * @throws IllegalArgumentException if the service name or service instances are invalid.
     */
    @NonNull
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        // Get request method and path for logging
        String method = request.methodName();
        String path = request.path();

        // 1. Get service name
        String serviceName = assets.getName();
        if (!StringKit.hasText(serviceName)) {
            Logger.info(
                    "==>       MCP: [N/A] [{}] [{}] [MCP_SERVICE_ERROR] - Service name cannot be empty",
                    method,
                    path);
            return Mono.error(new IllegalArgumentException("Service name cannot be empty"));
        }
        Logger.info("==>       MCP: [N/A] [{}] [{}] [MCP_SERVICE_NAME] - Service name: {}", method, path, serviceName);

        // 2. Get service instance list
        List<ServiceInstance> instances = serviceCache.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            Logger.info(
                    "==>       MCP: [N/A] [{}] [{}] [MCP_INSTANCES_ERROR] - No available instances for service: {}",
                    method,
                    path,
                    serviceName);
            return Mono.error(new IllegalArgumentException("No available instances for service: " + serviceName));
        }
        Logger.info(
                "==>       MCP: [N/A] [{}] [{}] [MCP_INSTANCES_FOUND] - Found {} instances for service: {}",
                method,
                path,
                instances.size(),
                serviceName);

        // 3. Perform health check
        instances = instances.stream().filter(ServiceInstance::isHealthy).collect(Collectors.toList());
        Logger.info(
                "==>       MCP: [N/A] [{}] [{}] [MCP_HEALTH_CHECK] - {} healthy instances out of {}",
                method,
                path,
                instances.size(),
                serviceCache.get(serviceName).size());

        if (instances.isEmpty()) {
            Logger.info(
                    "==>       MCP: [N/A] [{}] [{}] [MCP_HEALTH_ERROR] - All instances are unhealthy for service: {}",
                    method,
                    path,
                    serviceName);
            return Mono.error(new IllegalStateException("All instances are unhealthy for service: " + serviceName));
        }

        // 4. Get load balancing strategy
        EnumValue.Balance balance = parseLoadBalanceStrategy(null);
        Logger.info(
                "==>       MCP: [N/A] [{}] [{}] [MCP_BALANCE_STRATEGY] - Using load balancing strategy: {}",
                method,
                path,
                balance);

        // 5. Apply load balancing strategy to select an instance
        ServiceInstance selectedInstance = selectInstance(instances, balance, serviceName);
        Logger.info(
                "==>       MCP: [N/A] [{}] [{}] [MCP_INSTANCE_SELECTED] - Selected instance: {}:{}{}",
                method,
                path,
                selectedInstance.getHost(),
                selectedInstance.getPort(),
                selectedInstance.getPath() != null ? selectedInstance.getPath() : "");

        // 6. Update statistics
        selectedInstance.incrementRequestCount();
        Logger.info(
                "==>       MCP: [N/A] [{}] [{}] [MCP_INSTANCE_STATS] - Instance request count: {}",
                method,
                path,
                selectedInstance.getRequestCount());

        // 7. Build and send the MCP request
        return buildAndSendMcpRequest(request, context, assets, selectedInstance, method, path);
    }

    /**
     * Builds and sends an MCP protocol request to the selected service instance.
     * <p>
     * This method constructs the MCP request, including the target URL, headers, and body, and sends it to the target
     * service.
     *
     * @param request  The original client {@code ServerRequest}.
     * @param context  The request context.
     * @param assets   The configuration assets.
     * @param instance The selected service instance.
     * @param method   The HTTP method for logging.
     * @param path     The request path for logging.
     * @return A {@code Mono<ResponseEntity<DataBuffer>>} with the response from the target service.
     */
    private Mono<ServerResponse> buildAndSendMcpRequest(
            ServerRequest request,
            Context context,
            Assets assets,
            ServiceInstance instance,
            String method,
            String path) {
        // 1. Build the base URL for the MCP service
        String baseUrl = buildMcpBaseUrl(instance);
        Logger.info("==>       MCP: [N/A] [{}] [{}] [MCP_BASEURL] - Base URL: {}", method, path, baseUrl);

        // 2. Get or create the WebClient for MCP
        WebClient webClient = clients.computeIfAbsent(
                baseUrl,
                client -> WebClient.builder().exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build());
        Logger.info("==>       MCP: [N/A] [{}] [{}] [MCP_CLIENT] - WebClient created/retrieved", method, path);

        // 3. Build the target URI
        String targetUri = buildMcpTargetUri(assets, context);
        Logger.info("==>       MCP: [N/A] [{}] [{}] [MCP_TARGET_URI] - Target URI: {}", method, path, targetUri);

        // 4. Configure the MCP request
        WebClient.RequestBodySpec bodySpec = webClient.method(context.getHttpMethod()).uri(targetUri);
        Logger.info(
                "==>       MCP: [N/A] [{}] [{}] [MCP_METHOD] - HTTP method: {}",
                method,
                path,
                context.getHttpMethod());

        // 5. Configure MCP protocol-specific headers
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
            // Add MCP-specific headers
            headers.add("X-MCP-Protocol", "1.0");
            headers.add("X-MCP-Request-ID", context.getX_request_id());
            headers.add("X-MCP-Instance-ID", instance.getInstanceId());
        });
        Logger.info("==>       MCP: [N/A] [{}] [{}] [MCP_HEADERS] - MCP protocol headers configured", method, path);

        // 6. Handle the MCP request body
        Map<String, String> params = context.getRequestMap();
        if (!params.isEmpty()) {
            // MCP protocol uses JSON to transmit parameters
            bodySpec.contentType(MediaType.APPLICATION_JSON).bodyValue(params);
            Logger.info(
                    "==>       MCP: [N/A] [{}] [{}] [MCP_BODY] - Request body configured with {} parameters",
                    method,
                    path,
                    params.size());
        } else {
            Logger.info("==>       MCP: [N/A] [{}] [{}] [MCP_BODY] - No request parameters", method, path);
        }

        // 7. Send the MCP request
        Logger.info(
                "==>       MCP: [N/A] [{}] [{}] [MCP_SEND] - Sending request with timeout: {}ms",
                method,
                path,
                assets.getTimeout());
        return bodySpec.httpRequest(clientHttpRequest -> {
            HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
            reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
        }).retrieve().toEntity(DataBuffer.class).flatMap(this::processResponse);
    }

    /**
     * Builds the base URL for an MCP service.
     * <p>
     * This method constructs the base URL from the host, port, and path of a service instance. The MCP protocol uses a
     * specific URL format, including a protocol prefix.
     *
     * @param instance The service instance.
     * @return The constructed MCP base URL string.
     */
    private String buildMcpBaseUrl(ServiceInstance instance) {
        StringBuilder baseUrlBuilder = new StringBuilder("mcp://").append(instance.getHost());
        if (instance.getPort() > 0) {
            baseUrlBuilder.append(Symbol.COLON).append(instance.getPort());
        }
        if (instance.getPath() != null && !instance.getPath().isEmpty()) {
            if (!instance.getPath().startsWith(Symbol.SLASH)) {
                baseUrlBuilder.append(Symbol.SLASH);
            }
            baseUrlBuilder.append(instance.getPath());
        }
        return baseUrlBuilder.toString();
    }

    /**
     * Builds the target URI for an MCP request.
     * <p>
     * This method constructs the target URI based on the URL from the configuration assets and parameters from the
     * request context.
     *
     * @param assets  The configuration assets.
     * @param context The request context.
     * @return The constructed MCP target URI string.
     */
    private String buildMcpTargetUri(Assets assets, Context context) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(assets.getUrl());

        // Add MCP-specific parameters
        builder.queryParam("protocol", "mcp");
        builder.queryParam("version", "1.0");
        builder.queryParam("requestId", context.getX_request_id());

        // Add request parameters
        Map<String, String> params = context.getRequestMap();
        if (!params.isEmpty()) {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
            params.forEach(multiValueMap::add);
            builder.queryParams(multiValueMap);
        }

        return builder.build().toUriString();
    }

    /**
     * Processes the response data.
     * <p>
     * This method converts the {@code ResponseEntity} from the target service into a {@code ServerResponse}. It copies
     * the headers but removes the CONTENT_LENGTH header to avoid conflicts.
     *
     * @param responseEntity The response entity containing headers and body.
     * @return A {@code Mono<ServerResponse>} for the client.
     */
    private Mono<ServerResponse> processResponse(ResponseEntity<DataBuffer> responseEntity) {
        return ServerResponse.ok().headers(headers -> {
            headers.addAll(responseEntity.getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(
                responseEntity.getBody() == null ? BodyInserters.empty()
                        : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody())));
    }

    /**
     * Parses the load balancing strategy from a configuration string.
     * <p>
     * If the configuration is invalid or not provided, the default strategy is used.
     *
     * @param loadBalanceConfig The load balancing configuration string.
     * @return The parsed {@code LoadBalanceStrategy} enum.
     */
    private EnumValue.Balance parseLoadBalanceStrategy(String loadBalanceConfig) {
        if (!StringKit.hasText(loadBalanceConfig)) {
            return DEFAULT_STRATEGY;
        }
        try {
            return EnumValue.Balance.valueOf(loadBalanceConfig.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_STRATEGY;
        }
    }

    /**
     * Selects a service instance based on the load balancing strategy.
     *
     * @param instances   The list of available service instances.
     * @param balance     The load balancing strategy.
     * @param serviceName The name of the service.
     * @return The selected service instance.
     */
    private ServiceInstance selectInstance(
            List<ServiceInstance> instances,
            EnumValue.Balance balance,
            String serviceName) {
        switch (balance) {
            case ROUND_ROBIN:
                return roundRobinSelect(instances, serviceName);

            case RANDOM:
                return randomSelect(instances);

            case WEIGHT:
                return weightSelect(instances);

            default:
                return roundRobinSelect(instances, serviceName);
        }
    }

    /**
     * Selects a service instance using the round-robin strategy.
     *
     * @param instances   The list of available service instances.
     * @param serviceName The name of the service.
     * @return The selected service instance.
     */
    private ServiceInstance roundRobinSelect(List<ServiceInstance> instances, String serviceName) {
        AtomicInteger counter = counters.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
        int index = Math.abs(counter.getAndIncrement()) % instances.size();
        return instances.get(index);
    }

    /**
     * Selects a service instance using the random strategy.
     *
     * @param instances The list of available service instances.
     * @return The selected service instance.
     */
    private ServiceInstance randomSelect(List<ServiceInstance> instances) {
        int index = (int) (Math.random() * instances.size());
        return instances.get(index);
    }

    /**
     * Selects a service instance using the weighted strategy.
     *
     * @param instances The list of available service instances.
     * @return The selected service instance.
     */
    private ServiceInstance weightSelect(List<ServiceInstance> instances) {
        int totalWeight = instances.stream().mapToInt(ServiceInstance::getWeight).sum();
        if (totalWeight <= 0) {
            return roundRobinSelect(instances, instances.get(0).getServiceName());
        }

        int randomWeight = (int) (Math.random() * totalWeight);
        int currentWeight = 0;
        for (ServiceInstance instance : instances) {
            currentWeight += instance.getWeight();
            if (randomWeight <= currentWeight) {
                return instance;
            }
        }
        // Fallback to the first instance (should theoretically not happen)
        return instances.get(0);
    }

    /**
     * Adds a service instance to the cache.
     *
     * @param serviceName The name of the service.
     * @param instance    The service instance.
     */
    public void addServiceInstance(String serviceName, ServiceInstance instance) {
        Objects.requireNonNull(serviceName, "Service name cannot be null");
        Objects.requireNonNull(instance, "Service instance cannot be null");

        serviceCache.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>()).add(instance);
        instance.setHealthy(true);
        instance.setLastHealthCheckTime(System.currentTimeMillis());
    }

    /**
     * Removes a service instance from the cache.
     *
     * @param serviceName The name of the service.
     * @param instanceId  The ID of the instance to remove.
     */
    public void removeServiceInstance(String serviceName, String instanceId) {
        Objects.requireNonNull(serviceName, "Service name cannot be null");
        Objects.requireNonNull(instanceId, "Instance ID cannot be null");

        List<ServiceInstance> instances = serviceCache.get(serviceName);
        if (instances != null) {
            instances.removeIf(instance -> instanceId.equals(instance.getInstanceId()));
        }
    }

    /**
     * Updates the health status of a service instance.
     *
     * @param serviceName The name of the service.
     * @param instanceId  The ID of the instance.
     * @param healthy     The new health status.
     */
    public void updateInstanceHealth(String serviceName, String instanceId, boolean healthy) {
        Objects.requireNonNull(serviceName, "Service name cannot be null");
        Objects.requireNonNull(instanceId, "Instance ID cannot be null");

        List<ServiceInstance> instances = serviceCache.get(serviceName);
        if (instances != null) {
            instances.stream().filter(instance -> instanceId.equals(instance.getInstanceId())).forEach(instance -> {
                instance.setHealthy(healthy);
                instance.setLastHealthCheckTime(System.currentTimeMillis());
            });
        }
    }

    /**
     * Inner class representing an MCP service instance.
     * <p>
     * This class encapsulates all information about a single service instance in the MCP system, including its network
     * location, health status, load balancing weight, and request statistics. It provides methods to manage the
     * instance's lifecycle and track its performance metrics.
     * </p>
     */
    public static class ServiceInstance {

        /**
         * The unique identifier for this service instance.
         * <p>
         * This ID is used to distinguish between multiple instances of the same service and is typically generated by
         * the service discovery mechanism or assigned during instance registration. It must be unique within the scope
         * of a single service name.
         */
        private final String instanceId;

        /**
         * The name of the service this instance belongs to.
         * <p>
         * This field identifies the logical service that this instance is a part of. Multiple instances with the same
         * service name but different instance IDs represent different physical endpoints for the same logical service.
         */
        private final String serviceName;

        /**
         * The host address where this service instance is running.
         * <p>
         * This can be either an IP address or a hostname that can be resolved to an IP address. It is used to construct
         * the URL for sending requests to this instance.
         */
        private final String host;

        /**
         * The port number where this service instance is listening for requests.
         * <p>
         * This port number, combined with the host address, forms the complete network endpoint for the service
         * instance. It must be a valid port number (1-65535) and should be the actual port where the service is
         * listening.
         */
        private final int port;

        /**
         * The path prefix for this service instance.
         * <p>
         * This optional field specifies a path prefix that should be prepended to all request URLs when routing to this
         * instance. It allows multiple services to be hosted on the same host and port but with different path
         * prefixes. May be null or empty if no path prefix is needed.
         */
        private final String path;

        /**
         * The weight of this instance for weighted load balancing.
         * <p>
         * This value determines the relative proportion of requests that should be routed to this instance compared to
         * other instances of the same service. Higher values indicate a higher proportion of requests. The actual
         * weight used is either this value or DEFAULT_WEIGHT, whichever is greater.
         */
        private final int weight;

        /**
         * The current health status of this service instance.
         * <p>
         * This boolean flag indicates whether the instance is currently healthy and able to handle requests. Unhealthy
         * instances are excluded from the load balancing pool until their health status is restored. The volatile
         * keyword ensures that changes to this field are immediately visible to all threads.
         */
        private volatile boolean healthy;

        /**
         * The timestamp of the last health check performed on this instance.
         * <p>
         * This field stores the system time (in milliseconds) when the last health check was performed. It is used to
         * determine when the next health check should be scheduled and to detect instances that haven't been checked
         * recently. The volatile keyword ensures that changes to this field are immediately visible to all threads.
         */
        private volatile long lastHealthCheckTime;

        /**
         * The total number of requests that have been routed to this instance.
         * <p>
         * This counter tracks the cumulative number of requests that have been sent to this instance since it was
         * registered. It is used for monitoring and analytics purposes to understand the load distribution across
         * instances. The AtomicInteger ensures thread-safe increment operations.
         */
        private final AtomicInteger requestCount = new AtomicInteger(0);

        /**
         * Additional metadata associated with this service instance.
         * <p>
         * This map stores arbitrary key-value pairs of metadata that may be useful for routing decisions or monitoring.
         * Common uses include storing version information, deployment environment, region/zone information, or custom
         * routing tags. The ConcurrentHashMap ensures thread-safe access to the metadata.
         */
        private final Map<String, String> metadata = new ConcurrentHashMap<>();

        /**
         * Constructor.
         *
         * @param instanceId  The instance ID.
         * @param serviceName The service name.
         * @param host        The host address.
         * @param port        The port.
         * @param path        The service path.
         * @param weight      The weight for load balancing.
         */
        public ServiceInstance(String instanceId, String serviceName, String host, int port, String path, int weight) {
            this.instanceId = instanceId;
            this.serviceName = serviceName;
            this.host = host;
            this.port = port;
            this.path = path;
            this.weight = weight > 0 ? weight : DEFAULT_WEIGHT;
            this.healthy = true;
            this.lastHealthCheckTime = System.currentTimeMillis();
        }

        /**
         * Gets the instance ID.
         *
         * @return The instance ID.
         */
        public String getInstanceId() {
            return instanceId;
        }

        /**
         * Gets the service name.
         *
         * @return The service name.
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * Gets the host address.
         *
         * @return The host address.
         */
        public String getHost() {
            return host;
        }

        /**
         * Gets the port.
         *
         * @return The port.
         */
        public int getPort() {
            return port;
        }

        /**
         * Gets the service path.
         *
         * @return The service path.
         */
        public String getPath() {
            return path;
        }

        /**
         * Gets the weight.
         *
         * @return The weight.
         */
        public int getWeight() {
            return weight;
        }

        /**
         * Checks if the instance is healthy.
         *
         * @return The health status.
         */
        public boolean isHealthy() {
            return healthy;
        }

        /**
         * Sets the health status.
         *
         * @param healthy The new health status.
         */
        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        /**
         * Gets the timestamp of the last health check.
         *
         * @return The last health check timestamp.
         */
        public long getLastHealthCheckTime() {
            return lastHealthCheckTime;
        }

        /**
         * Sets the timestamp of the last health check.
         *
         * @param lastHealthCheckTime The last health check timestamp.
         */
        public void setLastHealthCheckTime(long lastHealthCheckTime) {
            this.lastHealthCheckTime = lastHealthCheckTime;
        }

        /**
         * Gets the request count.
         *
         * @return The request count.
         */
        public int getRequestCount() {
            return requestCount.get();
        }

        /**
         * Increments the request count.
         */
        public void incrementRequestCount() {
            requestCount.incrementAndGet();
        }

        /**
         * Gets the metadata map.
         *
         * @return The metadata map.
         */
        public Map<String, String> getMetadata() {
            return metadata;
        }

        /**
         * Adds metadata.
         *
         * @param key   The key.
         * @param value The value.
         */
        public void addMetadata(String key, String value) {
            metadata.put(key, value);
        }

        /**
         * Gets a metadata value.
         *
         * @param key The key.
         * @return The value.
         */
        public String getMetadata(String key) {
            return metadata.get(key);
        }
    }

}
