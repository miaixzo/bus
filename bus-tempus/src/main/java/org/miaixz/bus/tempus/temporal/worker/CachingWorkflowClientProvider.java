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
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.logger.Logger;

import io.temporal.client.WorkflowClient;

/**
 * Caches Temporal workflow clients and service stub handles by endpoint.
 * <p>
 * This implementation reuses service stub handles and {@link WorkflowClient} instances for repeated publications
 * targeting the same Temporal endpoint.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * Cache of Temporal workflow clients keyed by endpoint.
     */
    private final Map<String, WorkflowClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Creates a caching workflow client provider.
     *
     * @param stubsProvider the service stubs provider
     */
    public CachingWorkflowClientProvider(WorkflowServiceStubsProvider stubsProvider) {
        this.stubsProvider = stubsProvider;
    }

    /**
     * Returns a cached workflow client for the specified endpoint, creating one if necessary.
     *
     * @param endpoint the Temporal server endpoint
     * @return the workflow client
     * @throws IllegalArgumentException if {@code endpoint} is {@code null}
     */
    @Override
    public WorkflowClient createWorkflowClient(String endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("temporal.endpoint must not be null");
        }
        return clientCache.computeIfAbsent(endpoint, this::createAndCacheClient);
    }

    /**
     * Creates and caches a workflow client for the specified endpoint.
     *
     * @param endpoint the Temporal server endpoint
     * @return the workflow client
     */
    private WorkflowClient createAndCacheClient(String endpoint) {
        Object serviceStubs = serviceStubsCache.computeIfAbsent(endpoint, this::createServiceStubs);
        Logger.info("[{}] Creating workflow client for endpoint: {}", getClass().getSimpleName(), endpoint);
        try {
            return stubsProvider.createWorkflowClient(serviceStubs);
        } catch (Exception e) {
            Logger.error(
                    "[{}] Failed to create workflow client for endpoint: {}, error: {}",
                    getClass().getSimpleName(),
                    endpoint,
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Creates a service stub handle for the specified endpoint.
     *
     * @param endpoint the Temporal server endpoint
     * @return the created service stub handle
     */
    private Object createServiceStubs(String endpoint) {
        Logger.info("[{}] Creating workflow service stubs for endpoint: {}", getClass().getSimpleName(), endpoint);
        try {
            return stubsProvider.createServiceStubs(endpoint);
        } catch (Exception e) {
            Logger.error(
                    "[{}] Failed to create workflow service stubs for endpoint: {}, error: {}",
                    getClass().getSimpleName(),
                    endpoint,
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Closes all cached service stub handles and clears the internal caches maintained by this provider.
     */
    @Override
    public void close() {
        for (Map.Entry<String, Object> entry : serviceStubsCache.entrySet()) {
            String endpoint = entry.getKey();
            Object serviceStubs = entry.getValue();
            try {
                stubsProvider.shutdownServiceStubs(serviceStubs);
                Logger.debug(
                        "[{}] Closed workflow service stubs for endpoint: {}",
                        getClass().getSimpleName(),
                        endpoint);
            } catch (Exception e) {
                Logger.warn(
                        "[{}] Failed to close workflow service stubs for endpoint: {}, error: {}",
                        getClass().getSimpleName(),
                        endpoint,
                        e.getMessage(),
                        e);
            }
        }
        serviceStubsCache.clear();
        clientCache.clear();
    }

}
