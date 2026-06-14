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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.tuning.PollerBehaviorSimpleMaximum;

/**
 * Builds Temporal SDK workflow, activity, worker, and worker factory options from {@link WorkflowBindingOptions}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultWorkflowOptionsFactory implements WorkflowOptionsFactory {

    /**
     * Strategy used to generate workflow identifiers.
     */
    private final WorkflowIdGenerator generator;

    /**
     * Factory used to create activity retry options.
     */
    private final RetryOptionsFactory retryOptionsFactory;

    /**
     * Creates a workflow options factory.
     *
     * @param generator           workflow identifier generator
     * @param retryOptionsFactory retry options factory
     */
    public DefaultWorkflowOptionsFactory(WorkflowIdGenerator generator, RetryOptionsFactory retryOptionsFactory) {
        Assert.notNull(generator, "generator must not be null");
        Assert.notNull(retryOptionsFactory, "retryOptionsFactory must not be null");
        this.generator = generator;
        this.retryOptionsFactory = retryOptionsFactory;
    }

    /**
     * Creates workflow options from unified workflow binding options.
     *
     * @param options workflow binding options
     * @return workflow options
     */
    @Override
    public WorkflowOptions createWorkflowOptions(WorkflowBindingOptions options) {
        WorkflowBindingOptions effective = effective(options);
        Assert.isTrue(StringKit.hasText(effective.getTaskQueue()), "taskQueue must not be blank");
        Assert.isTrue(StringKit.hasText(effective.getWorkflowType()), "workflowType must not be blank");
        Logger.debug(
                true,
                "Tempus",
                "Workflow options creation started: workflowType={}, taskQueue={}, workflowIdPresent={}, stableKeyPresent={}",
                effective.getWorkflowType(),
                effective.getTaskQueue(),
                StringKit.hasText(effective.getWorkflowId()),
                StringKit.hasText(effective.getStableKey()));
        String workflowId = effective.getWorkflowId();
        if (!StringKit.hasText(workflowId)) {
            workflowId = generator.workflowId(effective.getWorkflowType(), effective.getStableKey());
        }
        WorkflowOptions workflowOptions = WorkflowOptions.newBuilder().setTaskQueue(effective.getTaskQueue())
                .setWorkflowId(workflowId)
                .setWorkflowExecutionTimeout(Duration.ofDays(effective.resolveWorkflowExecutionTimeoutDays()))
                .setWorkflowRunTimeout(Duration.ofHours(effective.resolveWorkflowRunTimeoutHours()))
                .setWorkflowTaskTimeout(Duration.ofMinutes(effective.resolveWorkflowTaskTimeoutMinutes())).build();
        Logger.debug(
                false,
                "Tempus",
                "Workflow options creation completed: workflowType={}, taskQueue={}, workflowId={}, executionTimeoutDays={}, runTimeoutHours={}, taskTimeoutMinutes={}",
                effective.getWorkflowType(),
                effective.getTaskQueue(),
                workflowId,
                effective.resolveWorkflowExecutionTimeoutDays(),
                effective.resolveWorkflowRunTimeoutHours(),
                effective.resolveWorkflowTaskTimeoutMinutes());
        return workflowOptions;
    }

    /**
     * Creates activity options from unified workflow binding options.
     *
     * @param options      workflow binding options
     * @param activityName activity name
     * @return activity options
     */
    @Override
    public ActivityOptions createActivityOptions(WorkflowBindingOptions options, String activityName) {
        return createActivityOptions(options, activityName, null);
    }

    /**
     * Creates activity options from unified workflow binding options with a timeout override.
     *
     * @param options          workflow binding options
     * @param activityName     activity name
     * @param maxDurationHours maximum activity duration in hours
     * @return activity options
     */
    @Override
    public ActivityOptions createActivityOptions(
            WorkflowBindingOptions options,
            String activityName,
            Integer maxDurationHours) {
        WorkflowBindingOptions effective = effective(options);
        int startToCloseHours = maxDurationHours != null && maxDurationHours > 0 ? maxDurationHours
                : effective.resolveActivityStartToCloseHours();
        Logger.debug(
                true,
                "Tempus",
                "Activity options creation started: activityName={}, startToCloseHours={}, heartbeatSeconds={}",
                activityName,
                startToCloseHours,
                effective.resolveActivityHeartbeatTimeoutSeconds());
        ActivityOptions activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofHours(startToCloseHours))
                .setScheduleToStartTimeout(Duration.ofMinutes(effective.resolveActivityScheduleToStartMinutes()))
                .setHeartbeatTimeout(Duration.ofSeconds(effective.resolveActivityHeartbeatTimeoutSeconds()))
                .setRetryOptions(retryOptionsFactory.createRetryOptions(effective, activityName)).build();
        Logger.debug(
                false,
                "Tempus",
                "Activity options creation completed: activityName={}, scheduleToStartMinutes={}",
                activityName,
                effective.resolveActivityScheduleToStartMinutes());
        return activityOptions;
    }

    /**
     * Creates worker options from unified workflow binding options.
     *
     * @param options       workflow binding options
     * @param maxConcurrent maximum concurrent activity and workflow task executions
     * @return worker options
     */
    @Override
    public WorkerOptions createWorkerOptions(WorkflowBindingOptions options, int maxConcurrent) {
        WorkflowBindingOptions effective = effective(options);
        Assert.isTrue(maxConcurrent > 0, "maxConcurrent must be > 0");
        Logger.debug(
                true,
                "Tempus",
                "Worker options creation started: maxConcurrent={}, workflowPollers={}, activityPollers={}, workerActivitiesPerSecond={}, taskQueueActivitiesPerSecond={}",
                maxConcurrent,
                effective.resolveMaxWorkflowTaskPollers(),
                effective.resolveMaxActivityTaskPollers(),
                effective.resolveMaxWorkerActivitiesPerSecond(),
                effective.resolveMaxTaskQueueActivitiesPerSecond());
        WorkerOptions.Builder builder = WorkerOptions.newBuilder().setMaxConcurrentActivityExecutionSize(maxConcurrent)
                .setMaxConcurrentWorkflowTaskExecutionSize(maxConcurrent)
                .setActivityTaskPollersBehavior(
                        new PollerBehaviorSimpleMaximum(effective.resolveMaxActivityTaskPollers()))
                .setWorkflowTaskPollersBehavior(
                        new PollerBehaviorSimpleMaximum(effective.resolveMaxWorkflowTaskPollers()));
        if (effective.resolveMaxWorkerActivitiesPerSecond() > 0D) {
            builder.setMaxWorkerActivitiesPerSecond(effective.resolveMaxWorkerActivitiesPerSecond());
        }
        if (effective.resolveMaxTaskQueueActivitiesPerSecond() > 0D) {
            builder.setMaxTaskQueueActivitiesPerSecond(effective.resolveMaxTaskQueueActivitiesPerSecond());
        }
        WorkerOptions workerOptions = builder.validateAndBuildWithDefaults();
        Logger.debug(
                false,
                "Tempus",
                "Worker options creation completed: maxConcurrent={}, workflowPollers={}, activityPollers={}",
                maxConcurrent,
                effective.resolveMaxWorkflowTaskPollers(),
                effective.resolveMaxActivityTaskPollers());
        return workerOptions;
    }

    /**
     * Creates worker factory options from unified workflow binding options.
     *
     * @param options workflow binding options
     * @return worker factory options
     */
    @Override
    public WorkerFactoryOptions createWorkerFactoryOptions(WorkflowBindingOptions options) {
        WorkflowBindingOptions effective = effective(options);
        Logger.debug(
                true,
                "Tempus",
                "Worker factory options creation started: workflowCacheSize={}, maxWorkflowThreadCount={}",
                effective.resolveWorkflowCacheSize(),
                effective.resolveMaxWorkflowThreadCount());
        WorkerFactoryOptions factoryOptions = WorkerFactoryOptions.newBuilder()
                .setWorkflowCacheSize(effective.resolveWorkflowCacheSize())
                .setMaxWorkflowThreadCount(effective.resolveMaxWorkflowThreadCount()).validateAndBuildWithDefaults();
        Logger.debug(
                false,
                "Tempus",
                "Worker factory options creation completed: workflowCacheSize={}, maxWorkflowThreadCount={}",
                effective.resolveWorkflowCacheSize(),
                effective.resolveMaxWorkflowThreadCount());
        return factoryOptions;
    }

    /**
     * Resolves null workflow binding options to defaults.
     *
     * @param options workflow binding options
     * @return effective workflow binding options
     */
    private static WorkflowBindingOptions effective(WorkflowBindingOptions options) {
        return options == null ? WorkflowBindingOptions.defaults() : options;
    }

}
