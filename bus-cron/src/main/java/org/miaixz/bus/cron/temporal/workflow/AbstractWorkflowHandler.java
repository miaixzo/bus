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
package org.miaixz.bus.cron.temporal.workflow;

import java.time.Duration;

import org.miaixz.bus.logger.Logger;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

/**
 * Provides a reusable execution template for Temporal workflows.
 * <p>
 * This base class creates a default activity stub and delegates the concrete workflow invocation to subclasses. It also
 * exposes protected extension points for request-specific timeout customization and retry configuration.
 *
 * @param <A> the activity interface type
 * @param <R> the workflow input type
 */
public abstract class AbstractWorkflowHandler<A, R> {

    /**
     * Default activity stub used when no request-specific timeout override is provided.
     */
    private final A activity;

    /**
     * Creates a workflow handler backed by the specified activity interface.
     *
     * @param activityClass the activity interface class
     */
    protected AbstractWorkflowHandler(Class<A> activityClass) {
        this.activity = Workflow.newActivityStub(activityClass, createDefaultActivityOptions());
    }

    /**
     * Executes the workflow for the specified request.
     *
     * @param request the workflow input
     * @return the workflow result
     */
    public String execute(R request) {
        try {
            Logger.info("[TaskWorkflow] Starting workflow, request: {}", request);

            Integer maxDurationHours = getMaxDurationHours(request);
            if (maxDurationHours != null && maxDurationHours > 0) {
                A dynamicActivity = Workflow
                        .newActivityStub(getActivityClass(), createDynamicActivityOptions(maxDurationHours));
                return invokeActivity(dynamicActivity, request);
            }

            String result = invokeActivity(activity, request);
            Logger.info("[TaskWorkflow] Workflow completed, request: {}", request);
            return result;

        } catch (Exception e) {
            Logger.error("[TaskWorkflow] Workflow failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the maximum activity execution duration in hours for the request.
     * <p>
     * Subclasses may override this method to create activity stubs with request-specific timeouts.
     *
     * @param request the workflow input
     * @return the maximum duration in hours, or {@code null} if the default options should be used
     */
    protected Integer getMaxDurationHours(R request) {
        return null;
    }

    /**
     * Creates the default Temporal activity options used by this workflow.
     *
     * @return the default activity options
     */
    protected ActivityOptions createDefaultActivityOptions() {
        return ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofHours(12))
                .setScheduleToStartTimeout(Duration.ofMinutes(5)).setHeartbeatTimeout(Duration.ofSeconds(30))
                .setRetryOptions(createDefaultRetryOptions()).build();
    }

    /**
     * Creates Temporal activity options with a request-specific execution timeout.
     *
     * @param maxDurationHours the maximum execution duration in hours
     * @return the dynamic activity options
     */
    protected ActivityOptions createDynamicActivityOptions(int maxDurationHours) {
        return ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofHours(maxDurationHours))
                .setScheduleToStartTimeout(Duration.ofMinutes(5)).setHeartbeatTimeout(Duration.ofSeconds(30))
                .setRetryOptions(createDefaultRetryOptions()).build();
    }

    /**
     * Creates the default retry options applied to activity invocations.
     *
     * @return the default retry options
     */
    protected RetryOptions createDefaultRetryOptions() {
        return RetryOptions.newBuilder().setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofSeconds(60)).setBackoffCoefficient(2.0).setMaximumAttempts(3).build();
    }

    /**
     * Invokes the target activity for the specified request.
     *
     * @param activity the activity stub to invoke
     * @param request  the workflow input
     * @return the activity result
     */
    protected abstract String invokeActivity(A activity, R request);

    /**
     * Returns the activity interface class used to create request-specific stubs.
     *
     * @return the activity interface class
     */
    protected abstract Class<A> getActivityClass();

}
