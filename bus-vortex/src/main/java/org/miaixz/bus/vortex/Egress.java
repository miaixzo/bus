/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import org.miaixz.bus.core.cache.provider.LRUCache;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.errors.*;
import org.miaixz.bus.errors.recovery.RecoveryCoordinator;
import org.miaixz.bus.errors.recovery.RecoveryPlan;
import org.miaixz.bus.extra.mq.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.magic.Performance;

import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.PrematureCloseException;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
import reactor.util.retry.Retry;

/**
 * Provides outbound HTTP, gRPC gateway and message broker resources for Vortex routes.
 *
 * @author Kimi Liu
 */
public class Egress {

    /**
     * Keeps outbound resource access on the protocol-specific components.
     */
    public Egress() {
        throw new UnsupportedOperationException("Egress class cannot be instantiated");
    }

    /**
     * Returns the deepest cause in one exception chain.
     *
     * @param error source error
     * @return deepest cause, or the source error when no nested cause exists
     */
    public static Throwable rootCause(Throwable error) {
        if (error == null) {
            return null;
        }
        Throwable current = error;
        int depth = 0;
        while (current.getCause() != null && current.getCause() != current && depth++ < 64) {
            current = current.getCause();
        }
        return current;
    }

    /**
     * Resolves the effective timeout from one route asset.
     *
     * @param assets route asset
     * @return timeout in seconds
     */
    public static long timeoutSeconds(Assets assets) {
        if (assets == null || assets.getTimeout() == null || assets.getTimeout() <= 0) {
            return Normal._60;
        }
        return assets.getTimeout();
    }

    /**
     * Creates outbound requests using the shared HTTP client and connection pool from {@link Holder}.
     */
    public static final class Http {

        /**
         * Utility entry point for the shared HTTP client.
         */
        private Http() {
        }

        /**
         * Releases initialized HTTP resources in the order coordinated by the gateway lifecycle.
         *
         * @param lifecycle error reporting context for gateway shutdown
         */
        static void close(ErrorContext lifecycle) {
            try {
                ConnectionProvider provider = Holder.getConnectionProviderIfPresent();
                if (provider != null) {
                    provider.dispose();
                    Logger.info(false, "Vortex", "Runtime HTTP connection pool closed");
                }
            } catch (Exception error) {
                ErrorBoundary.diagnose(lifecycle, "connection-pool-close", error, null);
            }

            try {
                LoopResources loops = Holder.getLoopResourcesIfPresent();
                if (loops != null) {
                    loops.disposeLater().block(Duration.ofSeconds(5));
                    Logger.info(false, "Vortex", "Runtime HTTP event loops closed");
                }
            } catch (Exception error) {
                ErrorBoundary.diagnose(lifecycle, "event-loops-close", error, null);
            }
        }

        /**
         * Creates a downstream request spec from the shared outbound {@link WebClient}.
         * <p>
         * The returned spec is still configured by the caller for headers, body insertion and response handling. This
         * keeps protocol-specific proxy behavior in the executor while centralizing the underlying HTTP client
         * resources here.
         *
         * @param method The downstream HTTP method.
         * @param uri    The downstream URI.
         * @return The request body spec for the outbound HTTP exchange.
         */
        public static WebClient.RequestBodySpec request(HttpMethod method, URI uri) {
            return client().method(method).uri(uri);
        }

        /**
         * Returns the shared outbound {@link WebClient}.
         * <p>
         * This method is exposed for framework-level integration points that need direct WebClient access. Changes
         * applied to the shared client construction, such as global filters or codecs, apply to all consumers of this
         * facade.
         *
         * @return The shared WebClient.
         */
        public static WebClient webClient() {
            return client();
        }

        /**
         * Checks whether outbound HTTP resources have already been initialized.
         * <p>
         * This is primarily useful for diagnostics and lifecycle checks because resource creation is lazy and
         * request-driven.
         *
         * @return {@code true} if resources are initialized.
         */
        public static boolean initialized() {
            return ClientHolder.initialized();
        }

        /**
         * Checks whether one outbound failure is safe to retry.
         *
         * @param error source error
         * @return {@code true} when the cause chain represents a transient outbound failure
         */
        public static boolean isRetryable(Throwable error) {
            Throwable current = error;
            int depth = 0;
            while (current != null && depth++ < 64) {
                if (current instanceof TimeoutException || current instanceof SocketTimeoutException
                        || current instanceof ConnectException || current instanceof PrematureCloseException
                        || current instanceof IOException) {
                    return true;
                }
                current = current.getCause();
            }
            return false;
        }

        /**
         * Builds a shared retry policy for outbound requests.
         *
         * @param retries max retry attempts; values below or equal to zero disable retry
         * @param scene   logical outbound scene
         * @param method  outbound HTTP method
         * @param uri     outbound URI retained in the public retry API
         * @return retry specification
         */
        public static Retry retrySpec(int retries, String scene, String method, URI uri) {
            boolean replayable = RecoveryCoordinator
                    .canReplay(isRetrySafe(method), true, RecoveryPlan.Delivery.REPLAY_SAFE);
            int maxRetries = replayable ? Math.min(Math.max(retries, 0), Holder.get().getMaxRetries()) : 0;
            if (maxRetries <= 0) {
                return Retry.max(0).filter(error -> false);
            }
            return Retry.from(signals -> {
                var plan = new RecoveryPlan("vortex-egress", maxRetries, Duration.ofNanos(Long.MAX_VALUE));
                var coordinator = new RecoveryCoordinator(plan);
                var operation = ErrorBoundary.standard().open();
                var context = new ErrorContext("vortex-egress", scene == null ? "retry" : scene,
                        ErrorContext.Phase.LOCAL);
                var facts = new FailureFacts(FailureFacts.Source.LOCAL,
                        ErrorEngine.descriptor(ErrorType.DEPENDENCY_UNAVAILABLE), 0);
                return Retry.backoff(maxRetries, Duration.ofMillis(Holder.outboundRetryBackoffMillis()))
                        .maxBackoff(Duration.ofMillis(Holder.outboundRetryMaxBackoffMillis()))
                        .filter(
                                error -> isRetryable(error) && coordinator
                                        .tryRetry(plan.owner(), true, true, RecoveryPlan.Delivery.REPLAY_SAFE, false))
                        .doBeforeRetry(retrySignal -> {
                            Throwable failure = retrySignal.failure();
                            var decision = operation.decide(context, facts, failure);
                            operation.observe(
                                    context,
                                    decision,
                                    false,
                                    failure,
                                    null,
                                    "retry-" + (retrySignal.totalRetries() + 1) + "-of-" + maxRetries);
                        }).onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                        .generateCompanion(signals);
            });
        }

        /**
         * Resolves the effective retry attempts from one route asset.
         *
         * @param assets route asset
         * @return retry attempts
         */
        public static int retryAttempts(Assets assets) {
            if (assets == null || assets.getRetries() == null || assets.getRetries() <= 0) {
                return 0;
            }
            return assets.getRetries();
        }

        /**
         * Resolves retries for methods that can be repeated without changing downstream state.
         *
         * @param assets route asset
         * @param method outbound method
         * @return retries in range {@code 0..2}
         */
        public static int retryAttempts(Assets assets, HttpMethod method) {
            if (method == null || !isRetrySafe(method.name())) {
                return 0;
            }
            return Math.min(retryAttempts(assets), Holder.get().getMaxRetries());
        }

        /**
         * Reports whether an outbound method is eligible for replay before response headers arrive.
         *
         * @param method HTTP method name
         * @return whether an attempt can safely be replayed before response headers arrive
         */
        public static boolean isRetrySafe(String method) {
            return HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method)
                    || HttpMethod.OPTIONS.matches(method);
        }

        /**
         * Applies timeout and retry protection to one outbound request.
         *
         * @param mono           outbound publisher
         * @param timeoutSeconds timeout in seconds; values below or equal to zero disable timeout
         * @param retries        max retry attempts; values below or equal to zero disable retry
         * @param scene          logical outbound scene
         * @param method         outbound HTTP method
         * @param uri            outbound URI
         * @param <T>            response type
         * @return guarded publisher
         */
        public static <T> Mono<T> guard(
                Mono<T> mono,
                long timeoutSeconds,
                int retries,
                String scene,
                String method,
                URI uri) {
            int effectiveRetries = Math.max(retries, 0);
            Mono<T> guarded = timeoutSeconds > 0 ? mono.timeout(Duration.ofSeconds(timeoutSeconds)) : mono;
            if (effectiveRetries <= 0) {
                return guarded;
            }
            return guarded.retryWhen(retrySpec(effectiveRetries, scene, method, uri));
        }

        /**
         * Returns the lazily initialized outbound HTTP client facade.
         *
         * @return The shared outbound WebClient.
         */
        private static WebClient client() {
            return ClientHolder.get();
        }

        /**
         * Creates the shared WebClient facade backed by Reactor Netty.
         * <p>
         * The {@link HttpClient} is created from the existing Vortex connection provider so current connection pool
         * performance settings continue to apply. The {@link ExchangeStrategies} instance keeps the previous codec
         * memory limit while moving the configuration out of individual executors.
         *
         * @return The newly created outbound WebClient.
         */
        private static WebClient create() {
            HttpClient httpClient = HttpClient.create(Holder.connectionProvider()).runOn(Holder.loopResources(), false)
                    .option(ChannelOption.ALLOCATOR, Holder.allocator());
            ExchangeStrategies strategies = ExchangeStrategies.builder()
                    .codecs(
                            configurer -> configurer.defaultCodecs()
                                    .maxInMemorySize(Math.toIntExact(Holder.get().getMaxBufferedResponseSize())))
                    .build();
            WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                    .exchangeStrategies(strategies).build();

            Logger.info(
                    true,
                    "Vortex",
                    "Outbound HTTP client initialized: implementation={}, transport={}",
                    "reactor-netty",
                    "nio");
            return webClient;
        }

        /**
         * Lazy holder for the outbound HTTP client.
         * <p>
         * The holder uses double-checked locking so the connection provider is not touched during class loading. This
         * avoids creating HTTP resources before {@link Holder#of(Performance)} has applied the runtime performance
         * configuration.
         *
         * @author Kimi Liu
         */
        private static final class ClientHolder {

            /**
             * Shared outbound WebClient, initialized on the first outbound proxy request.
             */
            private static volatile WebClient client;

            /**
             * Keeps the shared outbound HTTP client inside its holder.
             */
            private ClientHolder() {
                // No initialization required.
            }

            /**
             * Returns the shared client, creating it once when first requested.
             *
             * @return The shared outbound WebClient.
             */
            private static WebClient get() {
                WebClient current = client;
                if (current == null) {
                    synchronized (ClientHolder.class) {
                        current = client;
                        if (current == null) {
                            current = create();
                            client = current;
                        }
                    }
                }
                return current;
            }

            /**
             * Checks whether the lazy holder has already created the shared resources.
             *
             * @return {@code true} if resources are already initialized.
             */
            private static boolean initialized() {
                return client != null;
            }

        }

    }

    /**
     * Builds JSON requests to a gRPC gateway using the shared outbound HTTP client.
     */
    public static final class Grpc {

        /**
         * Utility entry point for gRPC gateway requests.
         */
        private Grpc() {
        }

        /**
         * Creates the HTTP POST used to invoke a gRPC gateway method.
         *
         * @param uri     gateway method URI
         * @param payload serialized JSON request
         * @return outbound request with its JSON body
         */
        public static WebClient.RequestHeadersSpec<?> request(URI uri, String payload) {
            return Http.request(HttpMethod.POST, uri).contentType(MediaType.APPLICATION_JSON).bodyValue(payload);
        }

    }

    /**
     * Owns message broker producers and the worker pool used for blocking sends.
     */
    public static final class Mq implements AutoCloseable {

        /**
         * Reports producer cleanup failures without interrupting release of other producers.
         */
        private static final ComponentFailure FAILURES = new ComponentFailure("vortex", "mq-cleanup");

        /**
         * Broker resources cached by address, also used as the lock protecting their active sends.
         */
        private final LRUCache<String, CachedProducer> producerCache;

        /**
         * Worker pool that drains accepted sends before closing the producer cache.
         */
        private final ThreadPoolExecutor workers;

        /**
         * Creates the bounded producer cache and the dedicated sending pool.
         */
        public Mq() {
            this.producerCache = new LRUCache<>(Holder.getMaxProducerCacheSize(), 0);
            this.producerCache.setListener((key, entry) -> {
                entry.evicted = true;
                if (entry.activeSends == 0) {
                    closeEntry(entry);
                }
            });

            int processors = Runtime.getRuntime().availableProcessors();
            this.workers = new ThreadPoolExecutor(processors * 2, processors * 4, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(1000), task -> {
                        Thread thread = new Thread(task, "vortex-mq-producer-pool");
                        thread.setDaemon(true);
                        thread.setPriority(Thread.NORM_PRIORITY);
                        return thread;
                    }, new ThreadPoolExecutor.AbortPolicy()) {

                /**
                 * Closes the cache after all admitted sends have finished.
                 */
                @Override
                protected void terminated() {
                    synchronized (producerCache) {
                        producerCache.clear();
                    }
                    Logger.info(false, "Vortex", "MQ resources stopped: protocol=mq");
                }
            };
        }

        /**
         * Sends a message using a cached producer on the MQ worker pool.
         *
         * @param brokerUrl broker address and port
         * @param message   message to send
         * @return completion after the producer accepts the message
         */
        public Mono<Void> send(String brokerUrl, Message message) {
            return Mono.create(sink -> {
                try {
                    workers.execute(() -> {
                        try {
                            sendMessage(brokerUrl, message);
                            sink.success();
                        } catch (Throwable error) {
                            sink.error(error);
                        }
                    });
                } catch (RejectedExecutionException error) {
                    sink.error(error);
                }
            });
        }

        /**
         * Pins the producer until the blocking send finishes so cache eviction can defer closing it.
         *
         * @param brokerUrl broker address and port
         * @param message   message to send
         */
        private void sendMessage(String brokerUrl, Message message) {
            CachedProducer entry;
            synchronized (producerCache) {
                entry = producerCache.get(brokerUrl, true, 0, () -> {
                    Logger.info(true, "Vortex", "Creating MQ producer: brokerUrlChars={}", brokerUrl.length());
                    MQProvider provider = MQFactory.createEngine(MQConfig.of(brokerUrl));
                    try {
                        return new CachedProducer(provider, Objects.requireNonNull(provider.getProducer()));
                    } catch (RuntimeException | Error error) {
                        if (provider instanceof AutoCloseable resource) {
                            closeResource(resource);
                        }
                        throw error;
                    }
                });
                entry.activeSends++;
            }
            try {
                synchronized (entry) {
                    entry.producer.send(message);
                }
            } finally {
                boolean close;
                synchronized (producerCache) {
                    close = --entry.activeSends == 0 && entry.evicted;
                }
                if (close) {
                    closeEntry(entry);
                }
            }
        }

        /**
         * Closes the producer and any connections owned by its provider.
         *
         * @param entry retired broker resources
         */
        private void closeEntry(CachedProducer entry) {
            closeResource(entry.producer);
            if (entry.provider instanceof AutoCloseable resource && resource != entry.producer) {
                closeResource(resource);
            }
        }

        /**
         * Closes one MQ resource and reports cleanup failures.
         *
         * @param resource producer or provider
         */
        private void closeResource(AutoCloseable resource) {
            try {
                resource.close();
            } catch (Exception error) {
                FAILURES.failed(ErrorType.IO_FAILURE, error);
            }
        }

        /**
         * Stops accepting sends and waits for the pool to release its producers.
         */
        @Override
        public void close() {
            workers.shutdown();
            try {
                if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                    Logger.warn("MQ workers are still draining accepted sends");
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Holds a producer until all sends using it have finished, including sends pending during eviction.
         */
        private static final class CachedProducer {

            /**
             * Provider that owns the connection used by the producer.
             */
            private final MQProvider provider;

            /**
             * Producer associated with one broker.
             */
            private final Producer producer;

            /**
             * Number of active sends using the producer.
             */
            private int activeSends;

            /**
             * Whether the producer has been removed from the cache.
             */
            private boolean evicted;

            /**
             * Wraps a producer for use in the cache.
             *
             * @param provider initialized broker provider
             * @param producer broker producer
             */
            private CachedProducer(MQProvider provider, Producer producer) {
                this.provider = provider;
                this.producer = producer;
            }
        }
    }

}
