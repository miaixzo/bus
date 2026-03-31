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
package org.miaixz.bus.tempus.temporal.worker;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.text.StringJoiner;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Binding;

import io.temporal.client.WorkflowClient;

/**
 * Caches Temporal workflow clients and service stub handles by endpoint.
 * <p>
 * This implementation reuses service stub handles and {@link WorkflowClient} instances for repeated publications
 * targeting the same Temporal endpoint.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CachingWorkflowClientProvider implements WorkflowClientProvider, AutoCloseable {

    /**
     * Provider used to create service stub handles for endpoints that are not yet cached.
     */
    private final WorkflowServiceStubsProvider stubsProvider;

    /**
     * Cache of Temporal service stub handles keyed by endpoint.
     */
    private final Map<String, Object> serviceStubsCache = new ConcurrentHashMap<>();

    /**
     * Cache of Temporal workflow clients keyed by endpoint + namespace + identity.
     */
    private final Map<String, WorkflowClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Detached service stubs retained for deferred cleanup after invalidation.
     */
    private final Queue<Object> retiredServiceStubs = new ConcurrentLinkedQueue<>();

    /**
     * Creates a caching workflow client provider.
     *
     * @param stubsProvider the service stubs provider
     */
    public CachingWorkflowClientProvider(WorkflowServiceStubsProvider stubsProvider) {
        this.stubsProvider = stubsProvider;
    }

    /**
     * Returns a cached workflow client for the specified binding, creating one if necessary.
     * <p>
     * Client cache is keyed by endpoint + namespace + identity to ensure different clients do not accidentally share
     * the same underlying configuration.
     *
     * @param binding temporal configuration
     * @return the workflow client
     */
    @Override
    public WorkflowClient createWorkflowClient(Binding binding) {
        Assert.notNull(binding, "binding must not be null");
        Assert.notNull(binding.getEndpoint(), "temporal.endpoint must not be null");

        String cacheKey = toClientCacheKey(binding.getEndpoint(), binding.getNamespace(), binding.getIdentity());
        return clientCache.computeIfAbsent(cacheKey, key -> createAndCacheClient(binding));
    }

    /**
     * Creates and caches a workflow client for the specified binding.
     *
     * @param binding temporal configuration
     * @return the workflow client
     */
    private WorkflowClient createAndCacheClient(Binding binding) {
        String endpoint = binding.getEndpoint();
        Object serviceStubs = serviceStubsCache.computeIfAbsent(endpoint, key -> createServiceStubs(binding));
        Logger.info(
                "Creating workflow client for endpoint: {}, namespace: {}, identity: {}",
                endpoint,
                binding.getNamespace(),
                binding.getIdentity());

        try {
            return stubsProvider.createWorkflowClient(serviceStubs, binding);
        } catch (Exception e) {
            Logger.error(
                    "Failed to create workflow client for endpoint: {}, namespace: {}, identity: {}, error: {}",
                    endpoint,
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Creates a service stub handle for the specified binding.
     *
     * @param binding temporal configuration
     * @return the created service stub handle
     */
    private Object createServiceStubs(Binding binding) {
        String endpoint = binding.getEndpoint();
        Logger.info(
                "Creating workflow service stubs for endpoint: {}, namespace: {}, identity: {}",
                endpoint,
                binding.getNamespace(),
                binding.getIdentity());
        try {
            return stubsProvider.createServiceStubs(binding);
        } catch (Exception e) {
            Logger.error(
                    "Failed to create workflow service stubs for endpoint: {}, namespace: {}, identity: {}, error: {}",
                    endpoint,
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Builds the cache key used to isolate workflow clients by endpoint, namespace, and client identity.
     *
     * @param endpoint  the Temporal endpoint
     * @param namespace the Temporal namespace
     * @param identity  the Temporal client identity
     * @return the composite cache key
     */
    private static String toClientCacheKey(String endpoint, String namespace, String identity) {
        String ns = namespace == null ? "" : namespace;
        String id = identity == null ? "" : identity;
        return StringJoiner.of("|").append(endpoint).append(ns).append(id).toString();
    }

    /**
     * Invalidates all cached clients and service stubs for the specified endpoint.
     * <p>
     * Called on transient connection errors to force re-creation of stale connections on the next publish attempt.
     *
     * @param endpoint the Temporal server endpoint to invalidate
     */
    public void invalidate(String endpoint) {
        if (endpoint == null) {
            return;
        }
        clientCache.keySet().removeIf(key -> key.startsWith(endpoint + "|"));
        Object detached = serviceStubsCache.remove(endpoint);
        if (detached != null) {
            retiredServiceStubs.offer(detached);
        }
        Logger.info("Invalidated cached clients and detached service stubs for endpoint: {}", endpoint);
    }

    /**
     * Closes all cached service stub handles and clears the internal caches maintained by this provider.
     */
    @Override
    public void close() {
        for (Map.Entry<String, Object> entry : serviceStubsCache.entrySet()) {
            closeServiceStubs(entry.getKey(), entry.getValue());
        }
        while (!retiredServiceStubs.isEmpty()) {
            closeServiceStubs("retired", retiredServiceStubs.poll());
        }
        serviceStubsCache.clear();
        clientCache.clear();
        retiredServiceStubs.clear();
    }

    /**
     * Closes a single cached service stub and logs failures without interrupting provider-wide cleanup.
     *
     * @param endpoint     the endpoint label associated with the stub
     * @param serviceStubs the service stub instance
     */
    private void closeServiceStubs(String endpoint, Object serviceStubs) {
        if (serviceStubs == null) {
            return;
        }
        try {
            stubsProvider.shutdownServiceStubs(serviceStubs);
            Logger.debug("Closed workflow service stubs for endpoint: {}", endpoint);
        } catch (Exception e) {
            Logger.warn(
                    "Failed to close workflow service stubs for endpoint: {}, error: {}",
                    endpoint,
                    e.getMessage(),
                    e);
        }
    }

}
