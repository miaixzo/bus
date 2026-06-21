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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;

import io.temporal.activity.ActivityOptions;

/**
 * Provides a reusable execution template for Temporal workflows.
 * <p>
 * This base class creates a default activity stub and delegates the concrete workflow invocation to subclasses. It also
 * exposes a protected extension point for request-specific timeout customization.
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
     * Activity interface class.
     */
    private final Class<A> activityClass;

    /**
     * Temporal options factory.
     */
    private final WorkflowOptionsFactory workflowOptionsFactory;

    /**
     * Unified workflow binding options.
     */
    private final WorkflowBindingOptions bindingOptions;

    /**
     * Creates a workflow handler backed by the specified activity interface and workflow options factory.
     *
     * @param activityClass          the activity interface class
     * @param workflowOptionsFactory workflow options factory
     * @param bindingOptions         workflow binding options
     */
    protected AbstractWorkflowHandler(Class<A> activityClass, WorkflowOptionsFactory workflowOptionsFactory,
            WorkflowBindingOptions bindingOptions) {
        Assert.notNull(activityClass, "activityClass must not be null");
        Assert.notNull(workflowOptionsFactory, "workflowOptionsFactory must not be null");
        Logger.info(
                true,
                "Tempus",
                "Workflow handler initialization started: activityClass={}",
                activityClass.getName());
        this.activityClass = activityClass;
        this.workflowOptionsFactory = workflowOptionsFactory;
        this.bindingOptions = bindingOptions == null ? WorkflowBindingOptions.defaults() : bindingOptions;
        this.activity = NativeWorkflowAdapter.newActivityStub(
                activityClass,
                workflowOptionsFactory.createActivityOptions(this.bindingOptions, getActivityName(activityClass)));
        Logger.info(
                false,
                "Tempus",
                "Workflow handler initialization completed: activityClass={}, activityName={}",
                activityClass.getName(),
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
                ActivityOptions options = workflowOptionsFactory
                        .createActivityOptions(bindingOptions, getActivityName(getActivityClass()), maxDurationHours);
                A dynamicActivity = NativeWorkflowAdapter.newActivityStub(getActivityClass(), options);
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
    protected Class<A> getActivityClass() {
        return activityClass;
    }

}
