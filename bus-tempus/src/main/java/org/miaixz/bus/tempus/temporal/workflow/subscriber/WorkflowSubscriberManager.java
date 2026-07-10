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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.Subscriber;
import org.miaixz.bus.tempus.temporal.worker.WorkflowTransport;
import org.miaixz.bus.tempus.temporal.worker.WorkflowTransportState;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowBindingOptions;
import org.miaixz.bus.tempus.temporal.workflow.WorkflowOptionsFactory;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.WorkerPlugin;
import io.temporal.worker.tuning.PollerBehaviorAutoscaling;

/**
 * Temporal worker subscriber manager.
 * <p>
 * This manager creates worker runtime resources, registers workflows and activities, runs health checks, rebuilds
 * disconnected runtimes, and releases resources during shutdown.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WorkflowSubscriberManager implements Subscriber, AutoCloseable {

    /**
     * Temporal options factory.
     */
    private final WorkflowOptionsFactory factory;

    /**
     * Temporal workflow transport.
     */
    private final WorkflowTransport transport;

    /**
     * Worker binding configuration.
     */
    private final WorkflowSubscriberBinding binding;

    /**
     * Unified workflow binding options.
     */
    private final WorkflowBindingOptions bindingOptions;

    /**
     * Shutdown idempotency marker.
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * Startup idempotency marker.
     */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * Reconnect scheduling idempotency marker.
     */
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);

    /**
     * Current worker runtime.
     */
    private volatile WorkflowSubscriberRuntime runtime = new WorkflowSubscriberRuntime();

    /**
     * Health check and reconnect scheduler.
     */
    private volatile ScheduledExecutorService scheduler;

    /**
     * Creates a Temporal worker subscriber manager.
     *
     * @param binding        worker subscriber binding
     * @param transport      workflow transport
     * @param factory        Temporal options factory
     * @param bindingOptions unified workflow binding options
     */
    public WorkflowSubscriberManager(WorkflowSubscriberBinding binding, WorkflowTransport transport,
            WorkflowOptionsFactory factory, WorkflowBindingOptions bindingOptions) {
        Assert.notNull(factory, "factory must not be null");
        this.binding = binding;
        this.transport = transport;
        this.factory = factory;
        this.bindingOptions = completeOptions(bindingOptions, binding);
    }

    /**
     * Starts the Temporal worker.
     */
    @Override
    public synchronized void start() {
        if (!binding.isEnabled()) {
            runtime.markStopped();
            Logger.info(
                    false,
                    "Tempus",
                    "Temporal worker startup skipped: enabled=false, taskQueue={}",
                    binding.getTaskQueue());
            return;
        }
        if (!started.compareAndSet(false, true)) {
            Logger.info(
                    false,
                    "Tempus",
                    "Temporal worker startup skipped: alreadyStarted=true, taskQueue={}, state={}",
                    binding.getTaskQueue(),
                    runtime.getState());
            return;
        }
        Assert.notNull(binding.getEndpoint(), "temporal.endpoint must not be null");
        Assert.notNull(binding.getTaskQueue(), "temporal.task.queue must not be null");

        ensureScheduler();
        startRuntime("startup");
        scheduleHealthCheck();
    }

    /**
     * Checks whether the worker is running.
     *
     * @return {@code true} when the worker is running
     */
    public boolean isRunning() {
        return runtime.isRunning();
    }

    /**
     * Closes this subscriber manager.
     */
    @Override
    public void close() {
        shutdown();
    }

    /**
     * Stops the scheduler and releases worker runtime resources.
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
                runtime.getState());
        ScheduledExecutorService currentScheduler = scheduler;
        if (currentScheduler != null) {
            currentScheduler.shutdownNow();
            scheduler = null;
        }
        closeRuntime(runtime, true);
        Logger.info(
                false,
                "Tempus",
                "Temporal worker shutdown completed: taskQueue={}, state={}",
                binding.getTaskQueue(),
                runtime.getState());
    }

    /**
     * Starts a new worker runtime.
     *
     * @param reason startup reason
     */
    private synchronized void startRuntime(String reason) {
        if (shutdown.get()) {
            return;
        }
        WorkflowSubscriberRuntime previous = runtime;
        WorkflowSubscriberRuntime next = new WorkflowSubscriberRuntime();
        next.setReconnectAttempts(previous.getReconnectAttempts());
        next.markStarting();
        runtime = next;
        try {
            Logger.info(
                    true,
                    "Tempus",
                    "Temporal worker startup started: endpoint={}, namespace={}, taskQueue={}, reason={}, maxConcurrent={}",
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getTaskQueue(),
                    reason,
                    binding.getMaxConcurrent());
            Object transportHandle = transport.create(binding);
            next.setTransportHandle(transportHandle);
            WorkflowClient workflowClient = transport.client(transportHandle, binding);
            next.setWorkflowClient(workflowClient);
            WorkerFactoryOptions factoryOptions = factory.createWorkerFactoryOptions(bindingOptions);
            WorkerFactory workerFactory = createWorkerFactory(workflowClient, factoryOptions);
            next.setWorkerFactory(workerFactory);
            WorkerOptions workerOptions = factory.createWorkerOptions(bindingOptions, binding.getMaxConcurrent());
            Worker worker = createWorker(workerFactory, binding.getTaskQueue(), workerOptions);
            next.setWorker(worker);
            binding.registerWorkflowsAndActivities(worker);
            startWorkerFactory(workerFactory);
            next.markRunning();
            reconnectScheduled.set(false);
            Logger.info(
                    false,
                    "Tempus",
                    "Temporal worker startup completed: endpoint={}, namespace={}, taskQueue={}, maxConcurrent={}, workflowPollers={}, activityPollers={}, taskQueueActivitiesPerSecond={}, state={}",
                    binding.getEndpoint(),
                    binding.getNamespace(),
                    binding.getTaskQueue(),
                    binding.getMaxConcurrent(),
                    bindingOptions.resolveMaxWorkflowTaskPollers(),
                    bindingOptions.resolveMaxActivityTaskPollers(),
                    bindingOptions.resolveMaxTaskQueueActivitiesPerSecond(),
                    next.getState());
        } catch (Exception e) {
            next.markFailure(e);
            closeRuntime(next, false);
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Temporal worker startup failed: endpoint={}, taskQueue={}, state={}, nextRetrySeconds={}, exception={}",
                    binding.getEndpoint(),
                    binding.getTaskQueue(),
                    next.getState(),
                    reconnectBackoffSeconds(next),
                    e.getClass().getSimpleName());
            scheduleReconnect("startup_failed");
        }
    }

    /**
     * Schedules periodic health checks.
     */
    private void scheduleHealthCheck() {
        ScheduledExecutorService currentScheduler = ensureScheduler();
        long intervalSeconds = bindingOptions.resolveWorkerHealthIntervalSeconds();
        currentScheduler
                .scheduleWithFixedDelay(this::safeHealthCheck, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Runs a safe health check.
     */
    private void safeHealthCheck() {
        try {
            healthCheck();
        } catch (RuntimeException e) {
            runtime.markFailure(e);
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Temporal worker health check failed: taskQueue={}, exception={}",
                    binding.getTaskQueue(),
                    e.getClass().getSimpleName());
            scheduleReconnect("health_check_exception");
        }
    }

    /**
     * Runs a worker health check.
     */
    void healthCheck() {
        if (shutdown.get() || !binding.isEnabled()) {
            return;
        }
        WorkflowSubscriberRuntime currentRuntime = runtime;
        if (!currentRuntime.isRunning()) {
            scheduleReconnect("runtime_not_running");
            return;
        }
        WorkerFactory workerFactory = currentRuntime.getWorkerFactory();
        if (workerFactory == null || workerFactory.isShutdown() || workerFactory.isTerminated()) {
            rebuildRuntime("worker_factory_closed");
            return;
        }
        String transportState = transport.state(currentRuntime.getTransportHandle());
        Logger.debug(
                false,
                "Tempus",
                "Temporal worker health transport state: taskQueue={}, transportState={}",
                binding.getTaskQueue(),
                transportState);
        if (isShutdownTransportState(transportState)) {
            rebuildRuntime("transport_shutdown");
            return;
        }
        if (isTransientFailureTransportState(transportState)) {
            currentRuntime.markFailure(null);
            if (currentRuntime.getConsecutiveFailures() >= bindingOptions.resolveWorkerHealthFailureThreshold()) {
                rebuildRuntime("health_threshold_reached");
            }
            return;
        }
        if (!transport
                .health(currentRuntime.getTransportHandle(), bindingOptions.resolveWorkerHealthProbeTimeoutSeconds())) {
            currentRuntime.markFailure(null);
            if (currentRuntime.getConsecutiveFailures() >= bindingOptions.resolveWorkerHealthFailureThreshold()) {
                rebuildRuntime("health_threshold_reached");
            }
            return;
        }
        currentRuntime.markRunning();
        Logger.debug(false, "Tempus", "Temporal worker health check completed: taskQueue={}", binding.getTaskQueue());
    }

    /**
     * Rebuilds the worker runtime.
     *
     * @param reason rebuild reason
     */
    private synchronized void rebuildRuntime(String reason) {
        if (shutdown.get()) {
            return;
        }
        WorkflowSubscriberRuntime currentRuntime = runtime;
        long backoffSeconds = reconnectBackoffSeconds(currentRuntime);
        Logger.warn(
                true,
                "Tempus",
                "Temporal worker rebuild started: reason={}, state={}, consecutiveFailures={}, attempt={}, backoffSeconds={}",
                reason,
                currentRuntime.getState(),
                currentRuntime.getConsecutiveFailures(),
                currentRuntime.getReconnectAttempts() + 1,
                backoffSeconds);
        closeRuntime(currentRuntime, false);
        currentRuntime.markReconnectScheduled();
        try {
            TimeUnit.SECONDS.sleep(backoffSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Temporal worker rebuild interrupted: taskQueue={}",
                    binding.getTaskQueue());
            return;
        }
        long startedAt = System.currentTimeMillis();
        startRuntime(reason);
        if (runtime.isRunning()) {
            Logger.info(
                    false,
                    "Tempus",
                    "Temporal worker rebuild completed: attempt={}, taskQueue={}, elapsedMillis={}",
                    runtime.getReconnectAttempts(),
                    binding.getTaskQueue(),
                    System.currentTimeMillis() - startedAt);
        }
    }

    /**
     * Schedules reconnect.
     *
     * @param reason scheduling reason
     */
    private void scheduleReconnect(String reason) {
        if (shutdown.get() || !bindingOptions.isWorkerReconnectEnabled()) {
            Logger.warn(
                    false,
                    "Tempus",
                    "Temporal worker reconnect skipped: enabled={}, shutdown={}, reason={}, taskQueue={}",
                    bindingOptions.isWorkerReconnectEnabled(),
                    shutdown.get(),
                    reason,
                    binding.getTaskQueue());
            return;
        }
        if (!reconnectScheduled.compareAndSet(false, true)) {
            return;
        }
        WorkflowSubscriberRuntime currentRuntime = runtime;
        long backoffSeconds = reconnectBackoffSeconds(currentRuntime);
        currentRuntime.markReconnectScheduled();
        Logger.warn(
                false,
                "Tempus",
                "Temporal worker reconnect scheduled: reason={}, taskQueue={}, attempt={}, backoffSeconds={}",
                reason,
                binding.getTaskQueue(),
                currentRuntime.getReconnectAttempts(),
                backoffSeconds);
        ensureScheduler().schedule(() -> {
            reconnectScheduled.set(false);
            startRuntime(reason);
        }, backoffSeconds, TimeUnit.SECONDS);
    }

    /**
     * Creates a Temporal worker factory for the workflow client.
     *
     * @param workflowClient       workflow client
     * @param workerFactoryOptions worker factory options
     * @return Temporal worker factory
     */
    WorkerFactory createWorkerFactory(WorkflowClient workflowClient, WorkerFactoryOptions workerFactoryOptions) {
        return WorkerFactory.newInstance(workflowClient, workerFactoryOptions);
    }

    /**
     * Creates a Temporal worker for the configured task queue.
     *
     * @param workerFactory Temporal worker factory
     * @param taskQueue     task queue
     * @param workerOptions worker options
     * @return Temporal worker
     */
    Worker createWorker(WorkerFactory workerFactory, String taskQueue, WorkerOptions workerOptions) {
        synchronized (workerFactory) {
            try {
                Map<String, Worker> workers = (Map<String, Worker>) field(workerFactory, "workers");
                Worker existingWorker = workers.get(taskQueue);
                if (existingWorker != null) {
                    return existingWorker;
                }
                WorkflowClient workflowClient = (WorkflowClient) field(workerFactory, "workflowClient");
                List<WorkerPlugin> plugins = (List<WorkerPlugin>) field(workerFactory, "plugins");
                WorkerOptions.Builder optionsBuilder = workerOptions == null ? WorkerOptions.newBuilder()
                        : WorkerOptions.newBuilder(workerOptions);
                for (WorkerPlugin plugin : plugins) {
                    plugin.configureWorker(taskQueue, optionsBuilder);
                }
                WorkerOptions validatedOptions = optionsBuilder.validateAndBuildWithDefaults();
                int workflowPollers = Math.max(1, validatedOptions.getMaxConcurrentWorkflowTaskPollers());
                int initialWorkflowPollers = Math.min(5, workflowPollers);
                validatedOptions = WorkerOptions.newBuilder(validatedOptions)
                        .setWorkflowTaskPollersBehavior(
                                new PollerBehaviorAutoscaling(1, workflowPollers, initialWorkflowPollers))
                        .validateAndBuildWithDefaults();
                Constructor<Worker> constructor = Worker.class.getDeclaredConstructor(
                        WorkflowClient.class,
                        String.class,
                        WorkerFactoryOptions.class,
                        WorkerOptions.class,
                        Class.forName("com.uber.m3.tally.Scope"),
                        Class.forName("io.temporal.internal.worker.WorkflowRunLockManager"),
                        Class.forName("io.temporal.internal.worker.WorkflowExecutorCache"),
                        boolean.class,
                        Class.forName("io.temporal.internal.sync.WorkflowThreadExecutor"),
                        List.class,
                        List.class,
                        Class.forName("io.temporal.internal.worker.NamespaceCapabilities"));
                constructor.setAccessible(true);
                Worker worker = constructor.newInstance(
                        workflowClient,
                        taskQueue,
                        field(workerFactory, "factoryOptions"),
                        validatedOptions,
                        field(workerFactory, "metricsScope"),
                        field(workerFactory, "runLocks"),
                        field(workerFactory, "cache"),
                        false,
                        field(workerFactory, "workflowThreadExecutor"),
                        workflowClient.getOptions().getContextPropagators(),
                        plugins,
                        field(workerFactory, "namespaceCapabilities"));
                workers.put(taskQueue, worker);
                for (WorkerPlugin plugin : plugins) {
                    plugin.initializeWorker(taskQueue, worker);
                }
                Logger.info(
                        false,
                        "Tempus",
                        "Temporal Jackson3 worker created with sticky task queue disabled and async workflow poller enabled: taskQueue={}",
                        taskQueue);
                return worker;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create Temporal Jackson3 worker", e);
            }
        }
    }

    /**
     * Reads a Temporal SDK field needed by the Jackson3 worker creation path.
     */
    private Object field(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    /**
     * Starts the Temporal worker factory.
     *
     * @param workerFactory Temporal worker factory
     */
    void startWorkerFactory(WorkerFactory workerFactory) {
        workerFactory.start();
    }

    /**
     * Closes the specified runtime.
     *
     * @param targetRuntime target runtime
     * @param finalStop     whether this is the final stop
     */
    private void closeRuntime(WorkflowSubscriberRuntime targetRuntime, boolean finalStop) {
        if (targetRuntime == null) {
            return;
        }
        if (finalStop || targetRuntime.getState() != EnumValue.Lifecycle.ERROR) {
            targetRuntime.markStopping();
        }
        WorkerFactory workerFactory = targetRuntime.getWorkerFactory();
        if (workerFactory != null) {
            try {
                Logger.debug(
                        true,
                        "Tempus",
                        "Temporal worker factory shutdown started: taskQueue={}",
                        binding.getTaskQueue());
                workerFactory.shutdown();
                workerFactory.awaitTermination(bindingOptions.resolveShutdownAwaitSeconds(), TimeUnit.SECONDS);
                Logger.debug(
                        false,
                        "Tempus",
                        "Temporal worker factory shutdown completed: taskQueue={}",
                        binding.getTaskQueue());
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
                targetRuntime.setWorkerFactory(null);
                targetRuntime.setWorker(null);
            }
        }
        Object transportHandle = targetRuntime.getTransportHandle();
        if (transportHandle != null) {
            try {
                transport.shutdown(transportHandle);
            } catch (RuntimeException e) {
                Logger.warn(
                        false,
                        "Tempus",
                        e,
                        "Temporal worker transport shutdown failed: taskQueue={}, exception={}",
                        binding.getTaskQueue(),
                        e.getClass().getSimpleName());
            } finally {
                targetRuntime.setTransportHandle(null);
                targetRuntime.setWorkflowClient(null);
            }
        }
        if (finalStop) {
            targetRuntime.markStopped();
        }
    }

    /**
     * Calculates reconnect backoff in seconds.
     *
     * @param targetRuntime target runtime
     * @return backoff in seconds
     */
    private long reconnectBackoffSeconds(WorkflowSubscriberRuntime targetRuntime) {
        int attempt = targetRuntime == null ? 1 : Math.max(1, targetRuntime.getReconnectAttempts() + 1);
        long initial = bindingOptions.resolveWorkerReconnectInitialBackoffSeconds();
        long max = bindingOptions.resolveWorkerReconnectMaxBackoffSeconds();
        long multiplier = 1L << Math.min(attempt - 1, 30);
        return Math.min(initial * multiplier, max);
    }

    /**
     * Completes workflow binding options with subscriber binding defaults.
     *
     * @param source  source workflow binding options
     * @param binding workflow subscriber binding
     * @return completed workflow binding options
     */
    private static WorkflowBindingOptions completeOptions(
            WorkflowBindingOptions source,
            WorkflowSubscriberBinding binding) {
        WorkflowBindingOptions target = source == null ? WorkflowBindingOptions.defaults() : source;
        if (binding != null && !StringKit.hasText(target.getTaskQueue())) {
            target.setTaskQueue(binding.getTaskQueue());
        }
        return target;
    }

    /**
     * Checks whether the transport state means shutdown.
     *
     * @param transportState transport state name
     * @return {@code true} when the transport state means shutdown
     */
    private static boolean isShutdownTransportState(String transportState) {
        return WorkflowTransportState.SHUTDOWN.matches(transportState);
    }

    /**
     * Checks whether the transport state means transient failure.
     *
     * @param transportState transport state name
     * @return {@code true} when the transport state means transient failure
     */
    private static boolean isTransientFailureTransportState(String transportState) {
        return WorkflowTransportState.TRANSIENT_FAILURE.matches(transportState);
    }

    /**
     * Returns the health check and reconnect scheduler, creating it when needed.
     *
     * @return health check and reconnect scheduler
     */
    private synchronized ScheduledExecutorService ensureScheduler() {
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "bus-tempus-worker-" + binding.getTaskQueue());
                thread.setDaemon(true);
                return thread;
            });
        }
        return scheduler;
    }

    /**
     * Returns the current runtime.
     *
     * @return current runtime
     */
    WorkflowSubscriberRuntime runtime() {
        return runtime;
    }

}
