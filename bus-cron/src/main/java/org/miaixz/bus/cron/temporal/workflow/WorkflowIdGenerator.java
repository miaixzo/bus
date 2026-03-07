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

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Strategy interface for generating Temporal workflow identifiers.
 * <p>
 * Implementations can enforce naming conventions or identifier composition rules for published workflows.
 */
public interface WorkflowIdGenerator {

    /**
     * Generates a workflow identifier for the specified publication definition.
     * <p>
     * The default implementation uses the workflow type name followed by a colon and a generated object identifier.
     *
     * @param definition the workflow publication definition
     * @return the generated workflow identifier
     */
    default String generateWorkflowId(WorkflowPublisherDefinition definition) {
        return definition.getWorkflowType() + Symbol.C_COLON + ID.objectId();
    }

}
