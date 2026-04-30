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
package org.miaixz.bus.tempus.temporal.workflow;

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
 * @author Kimi Liu
 * @since Java 21+
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
        this(activityClass, null, null);
    }

    /**
     * Creates a workflow handler backed by the specified activity interface and factories.
     *
     * @param activityClass          the activity interface class
     * @param activityOptionsFactory activity options factory (optional)
     * @param retryOptionsFactory    retry options factory (optional)
     */
    protected AbstractWorkflowHandler(Class<A> activityClass, ActivityOptionsFactory activityOptionsFactory,
            RetryOptionsFactory retryOptionsFactory) {
        Logger.info(
                true,
                "Tempus",
                "Workflow handler initialization started: activityClass={}",
                activityClass == null ? null : activityClass.getName());
        this.activity = Workflow.newActivityStub(
                activityClass,
                createActivityOptions(getActivityName(activityClass), activityOptionsFactory, retryOptionsFactory));
        Logger.info(
                false,
                "Tempus",
                "Workflow handler initialization completed: activityClass={}, activityName={}",
                activityClass == null ? null : activityClass.getName(),
                getActivityName(activityClass));
    }

    /**
     * Executes the workflow for the specified request.
     *
     * @param request the workflow input
     * @return the workflow result
     */
    public String execute(R request) {
        try {
            Logger.info(
                    true,
                    "Tempus",
                    "Workflow execution started: requestType={}, maxDurationHours={}",
                    request == null ? null : request.getClass().getName(),
                    getMaxDurationHours(request));

            Integer maxDurationHours = getMaxDurationHours(request);
            if (maxDurationHours != null && maxDurationHours > 0) {
                A dynamicActivity = Workflow
                        .newActivityStub(getActivityClass(), createDynamicActivityOptions(maxDurationHours));
                String result = invokeActivity(dynamicActivity, request);
                Logger.info(
                        false,
                        "Tempus",
                        "Workflow execution completed: requestType={}, dynamicTimeoutHours={}, resultLength={}",
                        request == null ? null : request.getClass().getName(),
                        maxDurationHours,
                        result == null ? 0 : result.length());
                return result;
            }

            String result = invokeActivity(activity, request);
            Logger.info(
                    false,
                    "Tempus",
                    "Workflow execution completed: requestType={}, resultLength={}",
                    request == null ? null : request.getClass().getName(),
                    result == null ? 0 : result.length());
            return result;

        } catch (Exception e) {
            Logger.error(
                    false,
                    "Tempus",
                    e,
                    "Workflow execution failed: requestType={}, exception={}",
                    request == null ? null : request.getClass().getName(),
                    e.getClass().getSimpleName());
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
     * Returns the heartbeat timeout in seconds used when building default activity options.
     * <p>
     * Subclasses may override to link this value with configuration.
     *
     * @return heartbeat timeout in seconds, default is 30
     */
    protected long getHeartbeatTimeoutSeconds() {
        return 90;
    }

    /**
     * Creates the default Temporal activity options used by this workflow.
     *
     * @return the default activity options
     */
    protected ActivityOptions createDefaultActivityOptions() {
        Logger.debug(
                true,
                "Tempus",
                "Workflow default activity options creation started: heartbeatTimeoutSeconds={}",
                getHeartbeatTimeoutSeconds());
        ActivityOptions options = ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofHours(12))
                .setScheduleToStartTimeout(Duration.ofMinutes(5))
                .setHeartbeatTimeout(Duration.ofSeconds(getHeartbeatTimeoutSeconds()))
                .setRetryOptions(createDefaultRetryOptions()).build();
        Logger.debug(false, "Tempus", "Workflow default activity options creation completed");
        return options;
    }

    /**
     * Creates Temporal activity options with a request-specific execution timeout.
     *
     * @param maxDurationHours the maximum execution duration in hours
     * @return the dynamic activity options
     */
    protected ActivityOptions createDynamicActivityOptions(int maxDurationHours) {
        Logger.debug(
                true,
                "Tempus",
                "Workflow dynamic activity options creation started: maxDurationHours={}, heartbeatTimeoutSeconds={}",
                maxDurationHours,
                getHeartbeatTimeoutSeconds());
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofHours(maxDurationHours))
                .setScheduleToStartTimeout(Duration.ofMinutes(5))
                .setHeartbeatTimeout(Duration.ofSeconds(getHeartbeatTimeoutSeconds()))
                .setRetryOptions(createDefaultRetryOptions()).build();
        Logger.debug(
                false,
                "Tempus",
                "Workflow dynamic activity options creation completed: maxDurationHours={}",
                maxDurationHours);
        return options;
    }

    /**
     * Creates the default retry options applied to activity invocations.
     *
     * @return the default retry options
     */
    protected RetryOptions createDefaultRetryOptions() {
        Logger.debug(true, "Tempus", "Workflow default retry options creation started");
        RetryOptions options = RetryOptions.newBuilder().setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofSeconds(60)).setBackoffCoefficient(2.0).setMaximumAttempts(3).build();
        Logger.debug(
                false,
                "Tempus",
                "Workflow default retry options creation completed: initialIntervalSeconds={}, maximumIntervalSeconds={}, maxAttempts={}",
                1,
                60,
                3);
        return options;
    }

    /**
     * Builds activity options for the given activity name using optional factories.
     *
     * @param activityName           the logical activity name
     * @param activityOptionsFactory the activity options factory (optional)
     * @param retryOptionsFactory    the retry options factory (optional)
     * @return the resolved activity options
     */
    protected ActivityOptions createActivityOptions(
            String activityName,
            ActivityOptionsFactory activityOptionsFactory,
            RetryOptionsFactory retryOptionsFactory) {
        if (activityOptionsFactory != null) {
            Logger.debug(
                    true,
                    "Tempus",
                    "Workflow activity options factory invocation started: activityName={}",
                    activityName);
            ActivityOptions options = activityOptionsFactory.createActivityOptions(activityName);
            if (options != null) {
                Logger.debug(
                        false,
                        "Tempus",
                        "Workflow activity options factory invocation completed: activityName={}, provided=true",
                        activityName);
                return options;
            }
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow activity options factory invocation completed: activityName={}, provided=false",
                    activityName);
        }

        RetryOptions retryOptions = null;
        if (retryOptionsFactory != null) {
            Logger.debug(
                    true,
                    "Tempus",
                    "Workflow retry options factory invocation started: activityName={}",
                    activityName);
            retryOptions = retryOptionsFactory.createRetryOptions(activityName);
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow retry options factory invocation completed: activityName={}, provided={}",
                    activityName,
                    retryOptions != null);
        }

        if (retryOptions != null) {
            ActivityOptions options = ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofHours(12))
                    .setScheduleToStartTimeout(Duration.ofMinutes(5))
                    .setHeartbeatTimeout(Duration.ofSeconds(getHeartbeatTimeoutSeconds())).setRetryOptions(retryOptions)
                    .build();
            Logger.debug(
                    false,
                    "Tempus",
                    "Workflow activity options created with custom retry: activityName={}, heartbeatTimeoutSeconds={}",
                    activityName,
                    getHeartbeatTimeoutSeconds());
            return options;
        }

        Logger.debug(
                false,
                "Tempus",
                "Workflow activity options falling back to defaults: activityName={}",
                activityName);
        return createDefaultActivityOptions();
    }

    /**
     * Resolves the logical activity name used by option factories.
     *
     * @param activityClass the activity interface class
     * @return the logical activity name
     */
    protected String getActivityName(Class<A> activityClass) {
        return activityClass.getSimpleName();
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
