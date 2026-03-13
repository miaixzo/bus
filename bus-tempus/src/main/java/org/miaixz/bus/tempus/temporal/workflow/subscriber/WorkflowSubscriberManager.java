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
package org.miaixz.bus.tempus.temporal.workflow.subscriber;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Subscriber;
import org.miaixz.bus.tempus.temporal.worker.WorkflowServiceStubsProvider;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;

/**
 * Manages the subscriber lifecycle of a Temporal worker.
 * <p>
 * This class validates worker configuration, creates the required Temporal infrastructure objects, registers workflows
 * and activities, and shuts down worker resources when they are no longer needed.
 * <p>
 * This class implements {@link AutoCloseable} to support try-with-resources for proper resource cleanup.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WorkflowSubscriberManager implements Subscriber {

    /**
     * Factory used to create worker options.
     */
    private final WorkflowSubscriberOptionsFactory factory;

    /**
     * Provider used to create Temporal service stub handles.
     */
    private final WorkflowServiceStubsProvider provider;

    /**
     * Worker binding containing endpoint, queue, and registration settings.
     */
    private final WorkflowSubscriberBinding binding;

    /**
     * Ensures shutdown is idempotent.
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

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
     * @param binding  the worker subscriber binding
     * @param provider the service stubs provider
     * @param factory  the worker options factory
     */
    public WorkflowSubscriberManager(WorkflowSubscriberBinding binding, WorkflowServiceStubsProvider provider,
            WorkflowSubscriberOptionsFactory factory) {
        this.binding = binding;
        this.provider = provider;
        this.factory = factory;
    }

    /**
     * Backward-compatible constructor using the default {@link WorkflowSubscriberOptionsFactory}.
     *
     * @param binding       the worker subscriber binding
     * @param stubsProvider the service stubs provider
     */
    public WorkflowSubscriberManager(WorkflowSubscriberBinding binding, WorkflowServiceStubsProvider stubsProvider) {
        this(binding, stubsProvider, new WorkflowSubscriberOptionsFactory());
    }

    /**
     * Starts the Temporal worker if it is enabled.
     *
     * @throws IllegalArgumentException if required worker properties are missing
     */
    @Override
    public void start() {
        if (!binding.isEnabled()) {
            Logger.info("[{}] Worker is disabled, skipping initialization", getClass().getSimpleName());
            return;
        }

        if (binding.getEndpoint() == null) {
            throw new IllegalArgumentException("temporal.endpoint must not be null");
        }
        if (binding.getTaskQueue() == null) {
            throw new IllegalArgumentException("temporal.task.queue must not be null");
        }

        try {
            Logger.info(
                    "[{}] Initializing worker, endpoint: {}, queue: {}, maxConcurrent: {}",
                    getClass().getSimpleName(),
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    binding.getMaxConcurrent());

            serviceStubs = provider.createServiceStubs(binding);
            Logger.debug(
                    "[{}] Created service stubs for endpoint: {}",
                    getClass().getSimpleName(),
                    binding.getEndpoint());

            WorkflowClient client = provider.createWorkflowClient(serviceStubs, binding);
            Logger.debug("[{}] Created workflow client", getClass().getSimpleName());

            workerFactory = WorkerFactory.newInstance(client);
            Logger.debug("[{}] Created worker factory", getClass().getSimpleName());

            WorkerOptions workerOptions = factory.createWorkerOptions(binding.getMaxConcurrent());
            Worker worker = workerFactory.newWorker(binding.getTaskQueue(), workerOptions);
            Logger.debug("[{}] Created worker for queue: {}", getClass().getSimpleName(), binding.getTaskQueue());

            binding.registerWorkflowsAndActivities(worker);
            Logger.debug("[{}] Registered workflows and activities", getClass().getSimpleName());

            workerFactory.start();

            Logger.info(
                    "[{}] Worker started successfully, listening on queue: {}",
                    getClass().getSimpleName(),
                    binding.getTaskQueue());

        } catch (Exception e) {
            Logger.error(
                    "[{}] Failed to start worker, endpoint: {}, queue: {}, error: {}",
                    getClass().getSimpleName(),
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    /**
     * Shuts down the worker factory and service stubs created by this manager.
     */
    @Override
    public void shutdown() {
        if (!shutdown.compareAndSet(false, true)) {
            return;
        }

        try {
            if (workerFactory != null) {
                Logger.info("[{}] Shutting down worker...", getClass().getSimpleName());
                workerFactory.shutdown();
                workerFactory.awaitTermination(10, TimeUnit.SECONDS);
                Logger.info("[{}] Worker shutdown completed", getClass().getSimpleName());
            }
        } catch (Exception e) {
            Logger.warn("[{}] Worker shutdown encountered an error: {}", getClass().getSimpleName(), e.getMessage(), e);
        } finally {
            workerFactory = null;
            if (serviceStubs != null) {
                try {
                    provider.shutdownServiceStubs(serviceStubs);
                } finally {
                    serviceStubs = null;
                }
            }
        }
    }

}
