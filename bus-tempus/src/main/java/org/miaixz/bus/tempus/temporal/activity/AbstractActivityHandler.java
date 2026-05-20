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
package org.miaixz.bus.tempus.temporal.activity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.temporal.notifier.CallbackNotifier;
import org.miaixz.bus.tempus.temporal.notifier.NotificationMode;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ManualActivityCompletionClient;
import io.temporal.client.ActivityCompletionException;
import io.temporal.failure.CanceledFailure;

/**
 * Provides a reusable execution template for Temporal activities.
 * <p>
 * Implementations are responsible for creating an execution context, resolving a matching executor, and optionally
 * providing callback notifications. This base class coordinates heartbeat reporting, success response generation, and
 * failure handling.
 *
 * @param <R> the activity input type
 * @param <C> the activity context type
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractActivityHandler<R, C>
        implements ActivityContextFactory<R, C>, ActivityExecutorResolver<R, C>, ActivityResultSerializer {

    /**
     * Constructs a new AbstractActivityHandler instance.
     */
    public AbstractActivityHandler() {
        // No initialization required.
    }

    /**
     * Shared scheduler for activity heartbeats to avoid creating one thread per invocation.
     */
    private static final ScheduledExecutorService HEARTBEAT_SCHEDULER = createHeartbeatScheduler();

    /**
     * Executes the activity for the specified request.
     *
     * @param request the activity input
     * @return the serialized success response
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws RuntimeException         if activity execution fails
     */
    public String execute(R request) {
        Assert.notNull(request, "request must not be null");
        String taskId = getTaskId(request);
        CallbackNotifier<R> notifier = getCallbackNotifier();
        NotificationMode notificationMode = notifier == null ? NotificationMode.DISABLED : getNotificationMode();
        Logger.info(
                true,
                "Tempus",
                "Activity execution started: taskId={}, requestType={}, notificationMode={}, notifierPresent={}",
                taskId,
                request.getClass().getName(),
                notificationMode,
                notifier != null);
        try {
            heartbeat("processing", taskId);

            C context = create(request);
            ActivityExecutor<R, C> executor = resolve(request);
            Assert.state(executor != null, "No executor found for request: %s", request);
            Logger.debug(
                    false,
                    "Tempus",
                    "Activity executor resolved: taskId={}, executor={}, contextType={}",
                    taskId,
                    executor.getClass().getName(),
                    context == null ? null : context.getClass().getName());

            Object result = heartbeatDuring(() -> executor.execute(request, context), "processing", taskId);
            heartbeat("completed", taskId);
            Logger.info(
                    false,
                    "Tempus",
                    "Activity execution phase completed: taskId={}, resultType={}",
                    taskId,
                    result == null ? null : result.getClass().getName());

            if (notifier != null && notificationMode == NotificationMode.INLINE_BEFORE_COMPLETE) {
                notifySuccessBeforeComplete(notifier, request, result, taskId);
            } else if (notifier != null && notificationMode == NotificationMode.AFTER_COMPLETE) {
                return completeThenNotifySuccess(request, result, notifier, taskId);
            }
            return buildSuccessResponse(taskId, result);
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Activity execution failed: taskId={}, exception={}",
                    taskId,
                    e.getClass().getSimpleName());
            if (notifier != null && notificationMode == NotificationMode.INLINE_BEFORE_COMPLETE) {
                try {
                    Logger.debug(
                            true,
                            "Tempus",
                            "Inline failure notifier started: taskId={}, requestType={}",
                            taskId,
                            request.getClass().getName());
                    notifier.failure(request, e.getMessage());
                    Logger.debug(false, "Tempus", "Inline failure notifier completed: taskId={}", taskId);
                } catch (Exception notifierError) {
                    Logger.error(
                            false,
                            "Tempus",
                            notifierError,
                            "Inline failure notifier failed: taskId={}, exception={}",
                            taskId,
                            notifierError.getClass().getSimpleName());
                }
            } else if (notifier != null && notificationMode == NotificationMode.AFTER_COMPLETE) {
                return completeThenNotifyFailure(request, e, notifier, taskId);
            }
            throw new RuntimeException("Task execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a success callback before the activity completion is reported to Temporal.
     *
     * @param notifier the callback notifier
     * @param request  the activity input
     * @param result   the execution result
     * @param taskId   the logical task identifier
     */
    protected void notifySuccessBeforeComplete(CallbackNotifier<R> notifier, R request, Object result, String taskId) {
        Logger.debug(
                true,
                "Tempus",
                "Inline success notifier started: taskId={}, resultType={}",
                taskId,
                result == null ? null : result.getClass().getName());
        heartbeatDuring(() -> notifier.success(request, result), "notifying", taskId);
        Logger.debug(false, "Tempus", "Inline success notifier completed: taskId={}", taskId);
    }

    /**
     * Reports a successful activity completion first and executes the success callback afterward.
     *
     * @param request  the activity input
     * @param result   the execution result
     * @param notifier the callback notifier
     * @param taskId   the logical task identifier
     * @return the serialized success response already reported to Temporal
     */
    protected String completeThenNotifySuccess(R request, Object result, CallbackNotifier<R> notifier, String taskId) {
        String response = buildSuccessResponse(taskId, result);
        ActivityExecutionContext context = Activity.getExecutionContext();
        context.doNotCompleteOnReturn();
        ManualActivityCompletionClient completionClient = context.useLocalManualCompletion();
        try {
            Logger.debug(
                    true,
                    "Tempus",
                    "Manual activity success completion started: taskId={}, responseLength={}",
                    taskId,
                    response == null ? 0 : response.length());
            completionClient.complete(response);
            Logger.debug(false, "Tempus", "Manual activity success completion completed: taskId={}", taskId);
        } catch (ActivityCompletionException ex) {
            Logger.error(
                    false,
                    "Tempus",
                    ex,
                    "Manual activity success completion failed: taskId={}, activityId={}, exception={}",
                    taskId,
                    ex.getActivityId().orElse(null),
                    ex.getClass().getSimpleName());
            throw ex;
        }
        try {
            Logger.debug(true, "Tempus", "Post-complete success notifier started: taskId={}", taskId);
            notifier.success(request, result);
            Logger.debug(false, "Tempus", "Post-complete success notifier completed: taskId={}", taskId);
        } catch (Exception ex) {
            Logger.error(
                    false,
                    "Tempus",
                    ex,
                    "Post-complete success notifier failed: taskId={}, exception={}",
                    taskId,
                    ex.getClass().getSimpleName());
        }
        return response;
    }

    /**
     * Reports a failed activity completion first and executes the failure callback afterward.
     *
     * @param request  the activity input
     * @param error    the execution failure
     * @param notifier the callback notifier
     * @param taskId   the logical task identifier
     * @return the return value used by manual-completion mode; callers ignore this value
     */
    protected String completeThenNotifyFailure(
            R request,
            Exception error,
            CallbackNotifier<R> notifier,
            String taskId) {
        RuntimeException completionError = new RuntimeException("Task execution failed: " + error.getMessage(), error);
        ActivityExecutionContext context = Activity.getExecutionContext();
        context.doNotCompleteOnReturn();
        ManualActivityCompletionClient completionClient = context.useLocalManualCompletion();
        try {
            Logger.debug(
                    true,
                    "Tempus",
                    "Manual activity failure completion started: taskId={}, exception={}",
                    taskId,
                    error.getClass().getSimpleName());
            completionClient.fail(completionError);
            Logger.debug(false, "Tempus", "Manual activity failure completion completed: taskId={}", taskId);
        } catch (ActivityCompletionException ex) {
            Logger.error(
                    false,
                    "Tempus",
                    ex,
                    "Manual activity failure completion failed: taskId={}, activityId={}, exception={}",
                    taskId,
                    ex.getActivityId().orElse(null),
                    ex.getClass().getSimpleName());
            throw completionError;
        }
        try {
            Logger.debug(true, "Tempus", "Post-complete failure notifier started: taskId={}", taskId);
            notifier.failure(request, error.getMessage());
            Logger.debug(false, "Tempus", "Post-complete failure notifier completed: taskId={}", taskId);
        } catch (Exception ex) {
            Logger.error(
                    false,
                    "Tempus",
                    ex,
                    "Post-complete failure notifier failed: taskId={}, exception={}",
                    taskId,
                    ex.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * Returns the logical task identifier for the specified request.
     * <p>
     * Subclasses may override this method to expose business identifiers in logs, heartbeats, and responses.
     *
     * @param request the activity input
     * @return the task identifier, or {@code null} if no identifier is available for the request
     */
    protected String getTaskId(R request) {
        return null;
    }

    /**
     * Executes the given action while periodically sending heartbeats to Temporal.
     * <p>
     * A background scheduler sends a heartbeat every 30 seconds during the action execution, preventing heartbeat
     * timeout for long-running post-processing steps such as callback notifications.
     *
     * @param action the action to execute
     * @param status the heartbeat status label
     * @param taskId the logical task identifier
     */
    protected void heartbeatDuring(Runnable action, String status, String taskId) {
        heartbeatDuring(() -> {
            action.run();
            return null;
        }, status, taskId);
    }

    /**
     * Executes the given action while periodically sending heartbeats to Temporal and returns the action result.
     *
     * @param <T>    the action result type
     * @param action the action to execute
     * @param status the heartbeat status label
     * @param taskId the logical task identifier
     * @return the action result
     */
    protected <T> T heartbeatDuring(Callable<T> action, String status, String taskId) {
        ActivityExecutionContext ctx;
        try {
            ctx = Activity.getExecutionContext();
        } catch (Exception e) {
            try {
                return action.call();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        long interval = heartbeatIntervalSeconds();
        Assert.isTrue(interval > 0, "heartbeatIntervalSeconds must be > 0");
        long initialDelay = Math.min(Math.max(1L, heartbeatInitialDelaySeconds()), interval);
        AtomicReference<RuntimeException> heartbeatFailure = new AtomicReference<>();
        ScheduledFuture<?> future = HEARTBEAT_SCHEDULER.scheduleWithFixedDelay(() -> {
            try {
                Map<String, Object> details = MapKit.newHashMap(2);
                details.put("status", status);
                details.put("taskId", taskId);
                ctx.heartbeat(details);
            } catch (Exception e) {
                if (isTerminalHeartbeatFailure(e)) {
                    heartbeatFailure
                            .compareAndSet(null, new RuntimeException("Heartbeat failed: " + e.getMessage(), e));
                }
                Logger.warn(
                        false,
                        "Tempus",
                        e,
                        "Activity heartbeat failed: taskId={}, status={}, exception={}",
                        taskId,
                        status,
                        e.getClass().getSimpleName());
            }
        }, initialDelay, interval, TimeUnit.SECONDS);
        Logger.debug(
                true,
                "Tempus",
                "Activity heartbeat monitor started: taskId={}, status={}, initialDelaySeconds={}, intervalSeconds={}",
                taskId,
                status,
                initialDelay,
                interval);
        try {
            T result = action.call();
            RuntimeException failure = heartbeatFailure.get();
            if (failure != null) {
                throw failure;
            }
            return result;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            future.cancel(false);
            Logger.debug(false, "Tempus", "Activity heartbeat monitor stopped: taskId={}, status={}", taskId, status);
        }
    }

    /**
     * Sends a Temporal heartbeat for the current activity execution.
     *
     * @param status the current execution status
     * @param taskId the logical task identifier
     */
    protected void heartbeat(String status, String taskId) {
        try {
            Map<String, Object> details = MapKit.newHashMap(2);
            details.put("status", status);
            details.put("taskId", taskId);
            Activity.getExecutionContext().heartbeat(details);
        } catch (Exception e) {
            if (isTerminalHeartbeatFailure(e)) {
                throw ExceptionKit.wrapRuntime(e, "Heartbeat failed: %s", e.getMessage());
            }
            Logger.warn(
                    false,
                    "Tempus",
                    e,
                    "Activity heartbeat failed: taskId={}, status={}, exception={}",
                    taskId,
                    status,
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Builds the serialized success response returned to the workflow.
     *
     * @param taskId the logical task identifier
     * @param result the activity execution result
     * @return the serialized success response
     */
    protected String buildSuccessResponse(String taskId, Object result) {
        Logger.debug(
                true,
                "Tempus",
                "Activity success response build started: taskId={}, resultType={}",
                taskId,
                result == null ? null : result.getClass().getName());
        Map<String, Object> payload = MapKit.<String, Object>builder().put("success", true).put("taskId", taskId)
                .put("result", parseSerializedResult(serialize(result))).build();
        String response = JsonKit.toJsonString(payload);
        Logger.debug(
                false,
                "Tempus",
                "Activity success response build completed: taskId={}, responseLength={}",
                taskId,
                response == null ? 0 : response.length());
        return response;
    }

    /**
     * Returns the callback notifier used after execution completes.
     *
     * @return the callback notifier, or {@code null} if callbacks are disabled
     */
    protected CallbackNotifier<R> getCallbackNotifier() {
        return null;
    }

    /**
     * Returns the callback notification mode for this handler.
     *
     * @return the notification mode, defaulting to inline execution before activity completion
     */
    protected NotificationMode getNotificationMode() {
        return NotificationMode.INLINE_BEFORE_COMPLETE;
    }

    /**
     * Returns the heartbeat interval in seconds.
     * <p>
     * Subclasses may override to link this value with configuration.
     *
     * @return heartbeat interval in seconds, default is 30
     */
    protected long heartbeatIntervalSeconds() {
        return 20;
    }

    /**
     * Returns the initial heartbeat delay in seconds for long-running actions.
     *
     * @return the initial delay in seconds, default is 5
     */
    protected long heartbeatInitialDelaySeconds() {
        return 5;
    }

    /**
     * Resolves the first executor that supports the specified request.
     *
     * @param request   the activity input
     * @param executors the candidate executors to inspect
     * @return the first matching executor, or {@code null} if none matches
     */
    protected ActivityExecutor<R, C> resolveFrom(R request, List<ActivityExecutor<R, C>> executors) {
        for (ActivityExecutor<R, C> executor : executors) {
            if (executor.supports(request)) {
                return executor;
            }
        }
        return null;
    }

    /**
     * Creates the shared scheduler used for periodic activity heartbeat emission.
     *
     * @return the heartbeat scheduler
     */
    private static ScheduledExecutorService createHeartbeatScheduler() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                ThreadKit.newNamedThreadFactory("temporal-heartbeat", true));
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    /**
     * Parses a serialized activity result when it represents JSON and falls back to the raw string otherwise.
     *
     * @param serialized the serialized result value
     * @return the parsed object or the original string
     */
    private Object parseSerializedResult(String serialized) {
        if (serialized == null) {
            return null;
        }
        try {
            Object parsed = JsonKit.toPojo(serialized, Object.class);
            Logger.debug(
                    false,
                    "Tempus",
                    "Activity serialized result parsed: serializedLength={}, parsedType={}",
                    serialized.length(),
                    parsed == null ? null : parsed.getClass().getName());
            return parsed;
        } catch (Exception e) {
            Logger.debug(
                    false,
                    "Tempus",
                    "Activity serialized result kept as text: serializedLength={}, exception={}",
                    serialized.length(),
                    e.getClass().getSimpleName());
            return serialized;
        }
    }

    /**
     * Returns {@code true} when a heartbeat failure indicates that the activity should no longer continue.
     *
     * @param error the heartbeat failure
     * @return {@code true} if the failure represents cancellation or invalid completion state
     */
    private boolean isTerminalHeartbeatFailure(Exception error) {
        return ExceptionKit.isCausedBy(error, CanceledFailure.class, ActivityCompletionException.class);
    }

}
