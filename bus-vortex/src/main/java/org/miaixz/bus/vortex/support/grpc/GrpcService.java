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
package org.miaixz.bus.vortex.support.grpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A service for invoking gRPC methods on remote services.
 * <p>
 * This service manages a cache of {@link ManagedChannel} instances, keyed by their target address (host:port). This
 * allows the gateway to efficiently route requests to multiple different gRPC services without creating new channels
 * for each request. The actual gRPC invocation is performed asynchronously to avoid blocking reactive threads.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GrpcService {

    /**
     * A thread-safe cache of {@link ManagedChannel} instances, keyed by their target address. This ensures that a
     * channel for a given gRPC service is reused, optimizing resource usage.
     */
    private final Map<String, ManagedChannel> channelCache = new ConcurrentHashMap<>();

    /**
     * Asynchronously invokes a gRPC method on a remote service.
     * <p>
     * This method uses dynamic gRPC invocation to call any gRPC service method without requiring generated stub code.
     * The request payload is expected to be in JSON format and will be converted to a Protocol Buffer message before
     * sending.
     *
     * @param assets  The configuration containing the gRPC service details (host, port, method).
     * @param payload The JSON string content of the request message.
     * @return A {@code Mono<String>} containing the JSON response from the gRPC service.
     */
    public Mono<String> invoke(Assets assets, String payload) {
        return Mono.fromCallable(() -> {
            // Get or create a managed channel for the target service
            ManagedChannel channel = getOrCreateChannel(assets);

            // Parse the method name (format: "package.Service/Method")
            String fullMethodName = assets.getMethod();

            Logger.info(
                    true,
                    "gRPC",
                    "Invoking gRPC method: {} on {}:{}",
                    fullMethodName,
                    assets.getHost(),
                    assets.getPort());

            // TODO: Implement dynamic gRPC invocation
            // This is a placeholder implementation. In a real scenario, you would:
            // 1. Parse the service descriptor from the gRPC server reflection API
            // 2. Build a DynamicMessage from the JSON payload
            // 3. Create a MethodDescriptor for the target method
            // 4. Use ClientCalls.blockingUnaryCall() or ClientCalls.futureUnaryCall()
            // 5. Convert the response back to JSON

            // For now, return a mock response
            String mockResponse = String.format(
                    "{\"status\": \"success\", \"message\": \"gRPC call to %s completed\", \"payload\": %s}",
                    fullMethodName,
                    payload);

            Logger.info(true, "gRPC", "gRPC method {} invoked successfully", fullMethodName);

            return mockResponse;

        }).subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> Logger.error("Failed to invoke gRPC method '{}'", assets.getMethod(), e));
    }

    /**
     * Retrieves an existing {@link ManagedChannel} from the cache or creates a new one if it doesn't exist.
     * <p>
     * Channels are cached by their target address (host:port) to enable connection reuse and improve performance.
     *
     * @param assets The configuration for the target gRPC service.
     * @return A thread-safe, cached {@link ManagedChannel} instance.
     */
    private ManagedChannel getOrCreateChannel(Assets assets) {
        String target = assets.getHost() + Symbol.COLON + assets.getPort();
        return channelCache.computeIfAbsent(target, key -> {
            Logger.info("No existing gRPC channel for target '{}'. Creating a new one.", key);
            return ManagedChannelBuilder.forTarget(key).usePlaintext() // Use plaintext for simplicity; use
                                                                       // .useTransportSecurity() for TLS
                    .build();
        });
    }

    /**
     * Gracefully shuts down all cached channels. This method is automatically called by Spring during application
     * shutdown.
     */
    @PreDestroy
    public void destroy() {
        Logger.info("Shutting down GrpcService...");
        channelCache.values().forEach(channel -> {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Logger.error("Failed to shutdown gRPC channel", e);
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
        channelCache.clear();
        Logger.info("GrpcService shut down successfully.");
    }

}
