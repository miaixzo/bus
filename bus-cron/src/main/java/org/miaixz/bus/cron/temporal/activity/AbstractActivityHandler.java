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
package org.miaixz.bus.cron.temporal.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cron.temporal.notifier.CallbackNotifier;
import org.miaixz.bus.logger.Logger;

import io.temporal.activity.Activity;

/**
 * Provides a reusable execution template for Temporal activities.
 * <p>
 * Implementations are responsible for creating an execution context, resolving a matching executor, and optionally
 * providing callback notifications. This base class coordinates heartbeat reporting, success response generation, and
 * failure handling.
 *
 * @param <R> the activity input type
 * @param <C> the activity context type
 */
public abstract class AbstractActivityHandler<R, C>
        implements ActivityContextFactory<R, C>, ActivityExecutorResolver<R, C>, ActivityResultSerializer {

    /**
     * Executes the activity for the specified request.
     *
     * @param request the activity input
     * @return the serialized success response
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws RuntimeException         if activity execution fails
     */
    public String execute(R request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        String taskId = getTaskId(request);

        try {
            Logger.info("[TaskActivity] Starting task, taskId: {}", taskId);
            heartbeat("processing", taskId);

            C context = create(request);
            ActivityExecutor<R, C> executor = resolve(request);
            if (executor == null) {
                throw new IllegalStateException("No executor found for request: " + request);
            }

            Object result = executor.execute(request, context);
            heartbeat("completed", taskId);

            CallbackNotifier<R> notifier = getCallbackNotifier();
            if (notifier != null) {
                notifier.success(request, result);
            }

            Logger.info("[TaskActivity] Task completed, taskId: {}", taskId);
            return buildSuccessResponse(taskId, result);

        } catch (Exception e) {
            Logger.error("[TaskActivity] Task failed, taskId: {}, error: {}", taskId, e.getMessage(), e);

            CallbackNotifier<R> notifier = getCallbackNotifier();
            if (notifier != null) {
                notifier.failure(request, e.getMessage());
            }

            throw new RuntimeException("Task execution failed: " + e.getMessage(), e);
        }
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
     * Sends a Temporal heartbeat for the current activity execution.
     *
     * @param status the current execution status
     * @param taskId the logical task identifier
     */
    protected void heartbeat(String status, String taskId) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("status", status);
            details.put("taskId", taskId);
            Activity.getExecutionContext().heartbeat(details);
        } catch (Exception e) {
            Logger.warn("[TaskActivity] Heartbeat failed: {}", e.getMessage());
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
        return String.format("{\"success\":true,\"taskId\":\"%s\",\"result\":%s}", taskId, serialize(result));
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

}
