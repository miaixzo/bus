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
package org.miaixz.bus.cron.temporal.worker;

import java.util.concurrent.TimeUnit;

import org.miaixz.bus.logger.Logger;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;

/**
 * Manages the subscriber lifecycle of a Temporal worker.
 * <p>
 * This class validates worker configuration, creates the required Temporal infrastructure objects, registers workflows
 * and activities, and shuts down worker resources when they are no longer needed.
 */
public class WorkerSubscriberManager implements WorkerSubscriber {

    /**
     * Worker definition containing endpoint, queue, and registration settings.
     */
    private final WorkerSubscriberDefinition config;

    /**
     * Provider used to create Temporal service stub handles.
     */
    private final WorkflowServiceStubsProvider stubsProvider;

    /**
     * Temporal worker factory created during startup.
     */
    private WorkerFactory workerFactory;

    /**
     * Opaque Temporal service stub handle created during startup.
     */
    private Object serviceStubs;

    /**
     * Creates a Temporal worker subscriber manager.
     *
     * @param config        the worker subscriber definition
     * @param stubsProvider the service stubs provider
     */
    public WorkerSubscriberManager(WorkerSubscriberDefinition config, WorkflowServiceStubsProvider stubsProvider) {
        this.config = config;
        this.stubsProvider = stubsProvider;
    }

    /**
     * Starts the Temporal worker if it is enabled.
     *
     * @throws IllegalArgumentException if required worker properties are missing
     */
    @Override
    public void start() {
        if (!config.isEnabled()) {
            Logger.info("[TemporalWorker] Worker is disabled, skipping initialization");
            return;
        }

        if (config.getEndpoint() == null) {
            throw new IllegalArgumentException("temporal.endpoint must not be null");
        }
        if (config.getTaskQueue() == null) {
            throw new IllegalArgumentException("temporal.task.queue must not be null");
        }

        try {
            Logger.info(
                    "[TemporalWorker] Initializing worker, endpoint: {}, queue: {}",
                    config.getEndpoint(),
                    config.getTaskQueue());

            serviceStubs = stubsProvider.createServiceStubs(config.getEndpoint());
            WorkflowClient client = stubsProvider.createWorkflowClient(serviceStubs);
            workerFactory = WorkerFactory.newInstance(client);

            WorkerOptions workerOptions = WorkerOptions.newBuilder()
                    .setMaxConcurrentActivityExecutionSize(config.getMaxConcurrent())
                    .setMaxConcurrentWorkflowTaskExecutionSize(config.getMaxConcurrent()).build();

            Worker worker = workerFactory.newWorker(config.getTaskQueue(), workerOptions);
            config.registerWorkflowsAndActivities(worker);
            workerFactory.start();

            Logger.info("[TemporalWorker] Worker started successfully, listening on queue: {}", config.getTaskQueue());

        } catch (Exception e) {
            Logger.error("[TemporalWorker] Failed to start worker: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Shuts down the worker factory and service stubs created by this manager.
     */
    @Override
    public void shutdown() {
        if (workerFactory != null) {
            Logger.info("[TemporalWorker] Shutting down worker...");
            workerFactory.shutdown();
            workerFactory.awaitTermination(10, TimeUnit.SECONDS);
            Logger.info("[TemporalWorker] Worker shutdown completed");
        }

        if (serviceStubs != null) {
            stubsProvider.shutdownServiceStubs(serviceStubs);
        }
    }

}
