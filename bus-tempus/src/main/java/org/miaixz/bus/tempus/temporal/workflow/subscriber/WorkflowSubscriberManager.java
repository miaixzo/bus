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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.xyz.ExceptionKit;
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
 * @since Java 21+
 */
public class WorkflowSubscriberManager implements Subscriber, AutoCloseable {

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
     * Lifecycle state for start/shutdown coordination.
     */
    private volatile EnumValue.Lifecycle state = EnumValue.Lifecycle.UNKNOWN;

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
    public synchronized void start() {
        if (!binding.isEnabled()) {
            Logger.info(
                    false,
                    "Tempus",
                    "Temporal worker startup skipped: enabled=false, taskQueue={}",
                    binding.getTaskQueue());
            return;
        }
        if (state == EnumValue.Lifecycle.RUNNING) {
            Logger.info(
                    false,
                    "Tempus",
                    "Temporal worker startup skipped: state=RUNNING, taskQueue={}",
                    binding.getTaskQueue());
            return;
        }
        if (state == EnumValue.Lifecycle.STARTING) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Temporal worker startup rejected: state=STARTING, taskQueue={}",
                    binding.getTaskQueue());
            throw new IllegalStateException("worker is starting");
        }
        if (state == EnumValue.Lifecycle.STOPPED || shutdown.get()) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Temporal worker startup rejected: state={}, shutdown={}, taskQueue={}",
                    state,
                    shutdown.get(),
                    binding.getTaskQueue());
            throw new IllegalStateException("worker has been stopped and cannot be restarted");
        }

        Assert.notNull(binding.getEndpoint(), "temporal.endpoint must not be null");
        Assert.notNull(binding.getTaskQueue(), "temporal.task.queue must not be null");

        state = EnumValue.Lifecycle.STARTING;
        try {
            Logger.info(
                    true,
                    "Tempus",
                    "Temporal worker startup started: endpoint={}, taskQueue={}, maxConcurrent={}",
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    binding.getMaxConcurrent());

            serviceStubs = provider.createServiceStubs(binding);
            Logger.debug(false, "Tempus", "Temporal worker service stubs created: endpoint={}", binding.getEndpoint());

            WorkflowClient client = provider.createWorkflowClient(serviceStubs, binding);
            Logger.debug(
                    false,
                    "Tempus",
                    "Temporal worker workflow client created: endpoint={}, taskQueue={}",
                    binding.getEndpoint(),
                    binding.getTaskQueue());

            workerFactory = WorkerFactory.newInstance(client);
            Logger.debug(false, "Tempus", "Temporal worker factory created: taskQueue={}", binding.getTaskQueue());

            WorkerOptions workerOptions = factory.createWorkerOptions(binding.getMaxConcurrent());
            Worker worker = workerFactory.newWorker(binding.getTaskQueue(), workerOptions);
            Logger.debug(
                    false,
                    "Tempus",
                    "Temporal worker created: taskQueue={}, maxConcurrent={}",
                    binding.getTaskQueue(),
                    binding.getMaxConcurrent());

            Logger.debug(true, "Tempus", "Temporal worker registration started: taskQueue={}", binding.getTaskQueue());
            binding.registerWorkflowsAndActivities(worker);
            Logger.debug(
                    false,
                    "Tempus",
                    "Temporal worker registration completed: taskQueue={}",
                    binding.getTaskQueue());

            workerFactory.start();
            state = EnumValue.Lifecycle.RUNNING;

            Logger.info(
                    false,
                    "Tempus",
                    "Temporal worker startup completed: taskQueue={}, state={}",
                    binding.getTaskQueue(),
                    state);

        } catch (Exception e) {
            state = EnumValue.Lifecycle.UNKNOWN;
            cleanupResources();
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Temporal worker startup failed: endpoint={}, taskQueue={}, exception={}",
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Returns {@code true} if the worker is currently running.
     *
     * @return {@code true} if running
     */
    public boolean isRunning() {
        return state == EnumValue.Lifecycle.RUNNING;
    }

    /**
     * Closes this manager by delegating to {@link #shutdown()}.
     */
    @Override
    public void close() {
        shutdown();
    }

    /**
     * Shuts down the worker factory and service stubs created by this manager.
     */
    @Override
    public synchronized void shutdown() {
        if (!shutdown.compareAndSet(false, true)) {
            Logger.debug(
                    false,
                    "Tempus",
                    "Temporal worker shutdown skipped: alreadyShutdown=true, taskQueue={}",
                    binding.getTaskQueue());
            return;
        }
        Logger.info(
                true,
                "Tempus",
                "Temporal worker shutdown started: taskQueue={}, state={}",
                binding.getTaskQueue(),
                state);
        state = EnumValue.Lifecycle.STOPPING;

        cleanupResources();
        Logger.info(
                false,
                "Tempus",
                "Temporal worker shutdown completed: taskQueue={}, state={}",
                binding.getTaskQueue(),
                state);
    }

    /**
     * Releases worker-side resources after startup failure or final shutdown.
     */
    private void cleanupResources() {
        try {
            if (workerFactory != null) {
                Logger.info(
                        true,
                        "Tempus",
                        "Temporal worker factory shutdown started: taskQueue={}",
                        binding.getTaskQueue());
                workerFactory.shutdown();
                workerFactory.awaitTermination(10, TimeUnit.SECONDS);
                Logger.info(
                        false,
                        "Tempus",
                        "Temporal worker factory shutdown completed: taskQueue={}",
                        binding.getTaskQueue());
            }
        } catch (Exception e) {
            if (ExceptionKit.isCausedBy(e, InterruptedException.class)) {
                Thread.currentThread().interrupt();
            }
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Temporal worker factory shutdown failed: taskQueue={}, exception={}",
                    binding.getTaskQueue(),
                    e.getClass().getSimpleName());
        } finally {
            workerFactory = null;
            if (serviceStubs != null) {
                try {
                    Logger.info(
                            true,
                            "Tempus",
                            "Temporal worker service stubs shutdown started: endpoint={}",
                            binding.getEndpoint());
                    provider.shutdownServiceStubs(serviceStubs);
                    Logger.info(
                            false,
                            "Tempus",
                            "Temporal worker service stubs shutdown completed: endpoint={}",
                            binding.getEndpoint());
                } finally {
                    serviceStubs = null;
                }
            }
            state = EnumValue.Lifecycle.STOPPED;
        }
    }

}
