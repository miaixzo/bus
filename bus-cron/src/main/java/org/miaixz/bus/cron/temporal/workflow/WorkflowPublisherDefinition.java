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

/**
 * Describes the metadata required to publish a Temporal workflow.
 * <p>
 * Implementations supply the destination endpoint, task queue, and workflow type used by publisher-side framework
 * components.
 */
public interface WorkflowPublisherDefinition {

    /**
     * Returns the Temporal server endpoint that should receive the workflow.
     *
     * @return the Temporal server endpoint
     */
    String getEndpoint();

    /**
     * Returns the task queue targeted by the workflow execution.
     *
     * @return the task queue name
     */
    String getTaskQueue();

    /**
     * Returns the workflow type name used to start the workflow.
     *
     * @return the workflow type name
     */
    String getWorkflowType();

}
