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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.*;
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
 * MCP协议路由策略实现类，负责MCP协议的路由选择和负载均衡。
 * <p>
 * 该类实现了Strategy接口，专门处理MCP（Message Communication Protocol）协议的路由逻辑。 支持多种负载均衡策略，包括轮询、随机、权重等，并提供了服务实例的健康检查机制。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MCPStrategyRouter implements Strategy {

    /**
     * 预定义的ExchangeStrategies实例，用于WebClient配置。
     * <p>
     * 该实例在类加载时初始化并缓存，避免重复创建，提高性能。 配置了最大内存大小限制，防止大请求导致内存溢出。
     * </p>
     */
    private static final ExchangeStrategies CACHED_EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Config.MAX_INMEMORY_SIZE)).build();

    /**
     * 线程安全的WebClient缓存，按baseUrl存储已初始化的WebClient实例。
     * <p>
     * 用于优化与目标服务的通信，避免重复创建WebClient实例。 使用ConcurrentHashMap保证线程安全。
     * </p>
     */
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    /**
     * 服务实例缓存，存储所有可用的MCP服务实例。
     * <p>
     * 使用ConcurrentHashMap保证线程安全，键为服务名称，值为服务实例列表。
     * </p>
     */
    private final Map<String, List<ServiceInstance>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 负载均衡计数器，用于轮询策略。
     * <p>
     * 使用AtomicInteger保证原子性操作，确保在高并发环境下的正确性。
     * </p>
     */
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * 负载均衡策略枚举。
     */
    private enum LoadBalanceStrategy {
        ROUND_ROBIN, // 轮询
        RANDOM, // 随机
        WEIGHT // 权重
    }

    /**
     * 默认负载均衡策略。
     */
    private static final LoadBalanceStrategy DEFAULT_STRATEGY = LoadBalanceStrategy.ROUND_ROBIN;

    /**
     * 默认权重值。
     */
    private static final int DEFAULT_WEIGHT = 1;

    /**
     * 默认健康检查间隔（毫秒）。
     */
    private static final long DEFAULT_HEALTH_CHECK_INTERVAL = 30000;

    /**
     * 处理客户端请求，构建并转发到目标服务，返回响应。
     * <p>
     * 该方法是MCP协议路由的核心，负责根据配置的负载均衡策略选择合适的服务实例，然后构建和发送请求。 处理流程包括：
     * <ol>
     * <li>验证输入参数</li>
     * <li>获取服务实例列表</li>
     * <li>执行健康检查</li>
     * <li>应用负载均衡策略选择实例</li>
     * <li>构建和发送请求</li>
     * <li>处理响应</li>
     * </ol>
     * </p>
     *
     * @param request 客户端的ServerRequest对象，包含请求的所有信息
     * @param context 请求上下文，包含请求参数和配置信息
     * @param assets  配置资产，包含目标服务的配置信息
     * @return Mono<ServerResponse> 包含目标服务的响应，以响应式方式返回
     * @throws IllegalArgumentException 如果服务名称或服务实例无效
     */
    @NonNull
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        // 1. 获取服务名称
        String serviceName = assets.getName();
        if (!StringKit.hasText(serviceName)) {
            return Mono.error(new IllegalArgumentException("Service name cannot be empty"));
        }

        // 2. 获取服务实例列表
        List<ServiceInstance> instances = serviceCache.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            return Mono.error(new IllegalArgumentException("No available instances for service: " + serviceName));
        }

        // 3. 执行健康检查
        instances = instances.stream().filter(ServiceInstance::isHealthy).collect(Collectors.toList());

        if (instances.isEmpty()) {
            return Mono.error(new IllegalStateException("All instances are unhealthy for service: " + serviceName));
        }

        // 4. 获取负载均衡策略
        LoadBalanceStrategy strategy = parseLoadBalanceStrategy(null);

        // 5. 应用负载均衡策略选择实例
        ServiceInstance selectedInstance = selectInstance(instances, strategy, serviceName);

        // 6. 更新统计信息
        selectedInstance.incrementRequestCount();

        // 7. 构建和发送请求
        return buildAndSendMcpRequest(request, context, assets, selectedInstance);
    }

    /**
     * 构建和发送MCP协议请求到目标服务。
     * <p>
     * 该方法负责构建MCP协议请求，包括构建目标URL、配置请求头、处理请求体，并发送请求到目标服务。 支持MCP协议特有的消息格式和通信模式。
     * </p>
     *
     * @param request  客户端的ServerRequest对象，包含原始请求信息
     * @param context  请求上下文，包含请求参数和配置信息
     * @param assets   配置资产，包含目标服务的配置信息
     * @param instance 选中的服务实例
     * @return Mono<ResponseEntity<DataBuffer>> 目标服务的响应实体，包含响应头和响应体
     */
    private Mono<ServerResponse> buildAndSendMcpRequest(ServerRequest request, Context context, Assets assets,
            ServiceInstance instance) {
        // 1. 构建MCP服务的基础URL
        String baseUrl = buildMcpBaseUrl(instance);

        // 2. 获取或创建MCP客户端
        WebClient webClient = clients.computeIfAbsent(baseUrl,
                client -> WebClient.builder().exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build());

        // 3. 构建目标URI
        String targetUri = buildMcpTargetUri(assets, context);

        // 4. 配置MCP请求
        WebClient.RequestBodySpec bodySpec = webClient.method(assets.getHttpMethod()).uri(targetUri);

        // 5. 配置MCP协议特有的请求头
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
            // 添加MCP协议特有的头信息
            headers.add("X-MCP-Protocol", "1.0");
            headers.add("X-MCP-Request-ID", context.getX_request_id());
            headers.add("X-MCP-Instance-ID", instance.getInstanceId());
        });

        // 6. 处理MCP请求体
        Map<String, String> params = context.getRequestMap();
        if (!params.isEmpty()) {
            // MCP协议使用JSON格式传输参数
            bodySpec.contentType(MediaType.APPLICATION_JSON).bodyValue(params);
        }

        // 7. 发送MCP请求
        return bodySpec.httpRequest(clientHttpRequest -> {
            HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
            reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
        }).retrieve().toEntity(DataBuffer.class).flatMap(this::processResponse);
    }

    /**
     * 构建MCP服务的基础URL。
     * <p>
     * 该方法根据服务实例中的主机、端口和路径信息构建MCP服务的基础URL。 MCP协议使用特定的URL格式，包括协议前缀和服务路径。
     * </p>
     *
     * @param instance 服务实例，包含目标服务的主机、端口和路径信息
     * @return 构建的MCP基础URL字符串
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
     * 构建MCP协议的目标URI。
     * <p>
     * 该方法根据配置资产中的URL和请求上下文中的参数构建MCP协议的目标URI。 MCP协议使用特定的URI格式，包含资源路径和操作类型。
     * </p>
     *
     * @param assets  配置资产，包含目标服务的URL信息
     * @param context 请求上下文，包含请求参数
     * @return 构建的MCP目标URI字符串
     */
    private String buildMcpTargetUri(Assets assets, Context context) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(assets.getUrl());

        // 添加MCP协议特有的参数
        builder.queryParam("protocol", "mcp");
        builder.queryParam("version", "1.0");
        builder.queryParam("requestId", context.getX_request_id());

        // 添加请求参数
        Map<String, String> params = context.getRequestMap();
        if (!params.isEmpty()) {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
            params.forEach(multiValueMap::add);
            builder.queryParams(multiValueMap);
        }

        return builder.build().encode().toUriString();
    }

    /**
     * 处理响应数据。
     * <p>
     * 该方法将目标服务返回的响应实体转换为ServerResponse对象。 会复制响应头，但移除CONTENT_LENGTH头以避免冲突。 如果响应体为空，则返回空响应体。
     * </p>
     *
     * @param responseEntity 响应实体，包含响应头和响应体
     * @return Mono<ServerResponse> 处理后的响应
     */
    private Mono<ServerResponse> processResponse(ResponseEntity<DataBuffer> responseEntity) {
        return ServerResponse.ok().headers(headers -> {
            headers.addAll(responseEntity.getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(responseEntity.getBody() == null ? BodyInserters.empty()
                : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody())));
    }

    /**
     * 解析负载均衡策略配置。
     * <p>
     * 从配置中解析负载均衡策略，如果未配置或配置无效，则使用默认策略。
     * </p>
     *
     * @param loadBalanceConfig 负载均衡配置字符串
     * @return 解析后的负载均衡策略枚举
     */
    private LoadBalanceStrategy parseLoadBalanceStrategy(String loadBalanceConfig) {
        if (!StringKit.hasText(loadBalanceConfig)) {
            return DEFAULT_STRATEGY;
        }

        try {
            return LoadBalanceStrategy.valueOf(loadBalanceConfig.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_STRATEGY;
        }
    }

    /**
     * 根据负载均衡策略选择服务实例。
     * <p>
     * 支持轮询、随机和权重三种负载均衡策略。
     * </p>
     *
     * @param instances   可用服务实例列表
     * @param strategy    负载均衡策略
     * @param serviceName 服务名称
     * @return 选中的服务实例
     */
    private ServiceInstance selectInstance(List<ServiceInstance> instances, LoadBalanceStrategy strategy,
            String serviceName) {
        switch (strategy) {
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
     * 轮询策略选择服务实例。
     * <p>
     * 使用原子计数器确保轮询的公平性，避免在高并发环境下出现倾斜。
     * </p>
     *
     * @param instances   可用服务实例列表
     * @param serviceName 服务名称
     * @return 选中的服务实例
     */
    private ServiceInstance roundRobinSelect(List<ServiceInstance> instances, String serviceName) {
        AtomicInteger counter = counters.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
        int index = Math.abs(counter.getAndIncrement()) % instances.size();
        return instances.get(index);
    }

    /**
     * 随机策略选择服务实例。
     * <p>
     * 使用随机数生成器选择服务实例，适用于负载均衡要求不高的场景。
     * </p>
     *
     * @param instances 可用服务实例列表
     * @return 选中的服务实例
     */
    private ServiceInstance randomSelect(List<ServiceInstance> instances) {
        int index = (int) (Math.random() * instances.size());
        return instances.get(index);
    }

    /**
     * 权重策略选择服务实例。
     * <p>
     * 根据服务实例的权重进行选择，权重越高的实例被选中的概率越大。
     * </p>
     *
     * @param instances 可用服务实例列表
     * @return 选中的服务实例
     */
    private ServiceInstance weightSelect(List<ServiceInstance> instances) {
        // 计算总权重
        int totalWeight = instances.stream().mapToInt(ServiceInstance::getWeight).sum();

        if (totalWeight <= 0) {
            // 如果没有权重配置，则使用轮询策略
            return roundRobinSelect(instances, instances.get(0).getServiceName());
        }

        // 生成随机权重值
        int randomWeight = (int) (Math.random() * totalWeight);

        // 根据权重选择实例
        int currentWeight = 0;
        for (ServiceInstance instance : instances) {
            currentWeight += instance.getWeight();
            if (randomWeight <= currentWeight) {
                return instance;
            }
        }

        // 如果没有选中（理论上不会发生），返回第一个实例
        return instances.get(0);
    }

    /**
     * 添加服务实例到缓存。
     * <p>
     * 该方法用于动态添加服务实例，支持服务发现机制。
     * </p>
     *
     * @param serviceName 服务名称
     * @param instance    服务实例
     */
    public void addServiceInstance(String serviceName, ServiceInstance instance) {
        Objects.requireNonNull(serviceName, "Service name cannot be null");
        Objects.requireNonNull(instance, "Service instance cannot be null");

        serviceCache.computeIfAbsent(serviceName, k -> new CopyOnWriteArrayList<>()).add(instance);

        // 初始化健康检查
        instance.setHealthy(true);
        instance.setLastHealthCheckTime(System.currentTimeMillis());
    }

    /**
     * 移除服务实例。
     * <p>
     * 该方法用于动态移除服务实例，支持服务下线机制。
     * </p>
     *
     * @param serviceName 服务名称
     * @param instanceId  实例ID
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
     * 更新服务实例的健康状态。
     * <p>
     * 该方法用于健康检查机制，更新服务实例的健康状态。
     * </p>
     *
     * @param serviceName 服务名称
     * @param instanceId  实例ID
     * @param healthy     健康状态
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
     * 服务实例内部类，表示一个MCP服务实例。
     * <p>
     * 包含服务实例的基本信息、健康状态和统计信息。
     * </p>
     */
    public static class ServiceInstance {
        private final String instanceId;
        private final String serviceName;
        private final String host;
        private final int port;
        private final String path;
        private final int weight;
        private volatile boolean healthy;
        private volatile long lastHealthCheckTime;
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final Map<String, String> metadata = new ConcurrentHashMap<>();

        /**
         * 构造函数。
         *
         * @param instanceId  实例ID
         * @param serviceName 服务名称
         * @param host        主机地址
         * @param port        端口
         * @param path        服务路径
         * @param weight      权重
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
         * 获取实例ID。
         *
         * @return 实例ID
         */
        public String getInstanceId() {
            return instanceId;
        }

        /**
         * 获取服务名称。
         *
         * @return 服务名称
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * 获取主机地址。
         *
         * @return 主机地址
         */
        public String getHost() {
            return host;
        }

        /**
         * 获取端口。
         *
         * @return 端口
         */
        public int getPort() {
            return port;
        }

        /**
         * 获取服务路径。
         *
         * @return 服务路径
         */
        public String getPath() {
            return path;
        }

        /**
         * 获取权重。
         *
         * @return 权重
         */
        public int getWeight() {
            return weight;
        }

        /**
         * 检查实例是否健康。
         *
         * @return 健康状态
         */
        public boolean isHealthy() {
            return healthy;
        }

        /**
         * 设置健康状态。
         *
         * @param healthy 健康状态
         */
        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        /**
         * 获取上次健康检查时间。
         *
         * @return 上次健康检查时间戳
         */
        public long getLastHealthCheckTime() {
            return lastHealthCheckTime;
        }

        /**
         * 设置上次健康检查时间。
         *
         * @param lastHealthCheckTime 上次健康检查时间戳
         */
        public void setLastHealthCheckTime(long lastHealthCheckTime) {
            this.lastHealthCheckTime = lastHealthCheckTime;
        }

        /**
         * 获取请求计数。
         *
         * @return 请求计数
         */
        public int getRequestCount() {
            return requestCount.get();
        }

        /**
         * 增加请求计数。
         */
        public void incrementRequestCount() {
            requestCount.incrementAndGet();
        }

        /**
         * 获取元数据。
         *
         * @return 元数据映射
         */
        public Map<String, String> getMetadata() {
            return metadata;
        }

        /**
         * 添加元数据。
         *
         * @param key   键
         * @param value 值
         */
        public void addMetadata(String key, String value) {
            metadata.put(key, value);
        }

        /**
         * 获取元数据值。
         *
         * @param key 键
         * @return 值
         */
        public String getMetadata(String key) {
            return metadata.get(key);
        }
    }

}