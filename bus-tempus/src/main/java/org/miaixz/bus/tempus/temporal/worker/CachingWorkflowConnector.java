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
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.StringJoiner;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Binding;

import io.temporal.client.WorkflowClient;

/**
 * Caches Temporal workflow clients and transport handles by endpoint.
 * <p>
 * This implementation reuses transport handles and {@link WorkflowClient} instances for repeated publications targeting
 * the same Temporal endpoint.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CachingWorkflowConnector implements WorkflowConnector, AutoCloseable {

    /**
     * Transport used to create handles for endpoints that are not yet cached.
     */
    private final WorkflowTransport transport;

    /**
     * Cache of Temporal transport handles keyed by endpoint.
     */
    private final Map<String, Object> transportCache = new ConcurrentHashMap<>();

    /**
     * Cache of Temporal workflow clients keyed by endpoint + namespace + identity.
     */
    private final Map<String, WorkflowClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Detached transport handles retained for deferred cleanup after invalidation.
     */
    private final Queue<Object> retiredTransports = new ConcurrentLinkedQueue<>();

    /**
     * Creates a caching workflow connector.
     *
     * @param transport the workflow transport
     */
    public CachingWorkflowConnector(WorkflowTransport transport) {
        this.transport = transport;
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
    public WorkflowClient client(Binding binding) {
        Assert.notNull(binding, "binding must not be null");
        Assert.notNull(binding.getEndpoint(), "temporal.endpoint must not be null");

        String cacheKey = toClientCacheKey(binding.getEndpoint(), binding.getNamespace(), binding.getIdentity());
        boolean cacheHit = clientCache.containsKey(cacheKey);
        Logger.debug(
                true,
                "Tempus",
                "Workflow client cache lookup started: endpoint={}, namespace={}, identity={}, cacheHit={}",
                binding.getEndpoint(),
                binding.getNamespace(),
                binding.getIdentity(),
                cacheHit);
        WorkflowClient client = clientCache.computeIfAbsent(cacheKey, key -> createAndCacheClient(binding));
        Logger.debug(
                false,
                "Tempus",
                "Workflow client cache lookup completed: endpoint={}, namespace={}, identity={}, cacheHit={}, clientCacheSize={}",
                binding.getEndpoint(),
                binding.getNamespace(),
                binding.getIdentity(),
                cacheHit,
                clientCache.size());
        return client;
    }

    /**
     * Creates and caches a workflow client for the specified binding.
     *
     * @param binding temporal configuration
     * @return the workflow client
     */
    private WorkflowClient createAndCacheClient(Binding binding) {
        String endpoint = binding.getEndpoint();
        Object transportHandle = transportCache.computeIfAbsent(endpoint, key -> createTransportHandle(binding));
        Logger.info(
                true,
                "Tempus",
                "Workflow client creation started: endpoint={}, namespace={}, identity={}",
                endpoint,
                binding.getNamespace(),
                binding.getIdentity());

        try {
            WorkflowClient client = transport.client(transportHandle, binding);
            Logger.info(
                    false,
                    "Tempus",
                    "Workflow client creation completed: endpoint={}, namespace={}, identity={}, clientType={}",
                    endpoint,
                    binding.getNamespace(),
                    binding.getIdentity(),
                    client.getClass().getName());
            return client;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Workflow client creation failed: endpoint={}, namespace={}, identity={}, exception={}",
                    endpoint,
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Creates a transport handle for the specified binding.
     *
     * @param binding temporal configuration
     * @return the created transport handle
     */
    private Object createTransportHandle(Binding binding) {
        String endpoint = binding.getEndpoint();
        Logger.info(
                true,
                "Tempus",
                "Workflow transport handle creation started: endpoint={}, namespace={}, identity={}",
                endpoint,
                binding.getNamespace(),
                binding.getIdentity());
        try {
            Object transportHandle = transport.create(binding);
            Logger.info(
                    false,
                    "Tempus",
                    "Workflow transport handle creation completed: endpoint={}, namespace={}, identity={}, handleType={}",
                    endpoint,
                    binding.getNamespace(),
                    binding.getIdentity(),
                    transportHandle == null ? null : transportHandle.getClass().getName());
            return transportHandle;
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Workflow transport handle creation failed: endpoint={}, namespace={}, identity={}, exception={}",
                    endpoint,
                    binding.getNamespace(),
                    binding.getIdentity(),
                    e.getClass().getSimpleName());
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
        String ns = namespace == null ? Normal.EMPTY : namespace;
        String id = identity == null ? Normal.EMPTY : identity;
        return StringJoiner.of(Symbol.OR).append(endpoint).append(ns).append(id).toString();
    }

    /**
     * Invalidates all cached clients and transport handles for the specified endpoint.
     * <p>
     * Called on transient connection errors to force re-creation of stale connections on the next publish attempt.
     *
     * @param endpoint the Temporal server endpoint to invalidate
     */
    @Override
    public void invalidate(String endpoint) {
        if (endpoint == null) {
            Logger.debug(false, "Tempus", "Workflow client cache invalidation skipped: endpointPresent=false");
            return;
        }
        int beforeClientCount = clientCache.size();
        int beforeTransportCount = transportCache.size();
        clientCache.keySet().removeIf(key -> key.startsWith(endpoint + Symbol.OR));
        Object detached = transportCache.remove(endpoint);
        if (detached != null) {
            retiredTransports.offer(detached);
        }
        Logger.info(
                false,
                "Tempus",
                "Workflow client cache invalidated: endpoint={}, beforeClientCount={}, afterClientCount={}, beforeTransportCount={}, afterTransportCount={}, retiredTransportCount={}",
                endpoint,
                beforeClientCount,
                clientCache.size(),
                beforeTransportCount,
                transportCache.size(),
                retiredTransports.size());
    }

    /**
     * Closes all cached transport handles and clears the internal caches maintained by this connector.
     */
    @Override
    public void close() {
        Logger.info(
                true,
                "Tempus",
                "Workflow client connector close started: transportCount={}, clientCount={}, retiredTransportCount={}",
                transportCache.size(),
                clientCache.size(),
                retiredTransports.size());
        for (Map.Entry<String, Object> entry : transportCache.entrySet()) {
            closeTransportHandle(entry.getKey(), entry.getValue());
        }
        while (!retiredTransports.isEmpty()) {
            closeTransportHandle("retired", retiredTransports.poll());
        }
        transportCache.clear();
        clientCache.clear();
        retiredTransports.clear();
        Logger.info(false, "Tempus", "Workflow client connector close completed");
    }

    /**
     * Closes a single cached transport handle and logs failures without interrupting connector-wide cleanup.
     *
     * @param endpoint        the endpoint label associated with the transport handle
     * @param transportHandle the transport handle
     */
    private void closeTransportHandle(String endpoint, Object transportHandle) {
        if (transportHandle == null) {
            return;
        }
        try {
            Logger.debug(
                    true,
                    "Tempus",
                    "Workflow transport handle close started: endpoint={}, handleType={}",
                    endpoint,
                    transportHandle.getClass().getName());
            transport.shutdown(transportHandle);
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow transport handle close completed: endpoint={}, handleType={}",
                    endpoint,
                    transportHandle.getClass().getName());
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Workflow transport handle close failed: endpoint={}, handleType={}, exception={}",
                    endpoint,
                    transportHandle.getClass().getName(),
                    e.getClass().getSimpleName());
        }
    }

}
