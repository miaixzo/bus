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
package org.miaixz.bus.cortex.builtin.batch;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;

import lombok.Getter;
import lombok.Setter;

/**
 * Batch registry operation descriptor.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class BatchOperation {

    /**
     * Creates an empty batch operation descriptor.
     */
    public BatchOperation() {
    }

    /**
     * Type of operation to apply to each entry in the batch.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum OperationType {
        /**
         * Register all entries in the batch.
         */
        REGISTER,
        /**
         * Deregister all entries in the batch.
         */
        DEREGISTER,
        /**
         * Update all entries in the batch.
         */
        UPDATE,
        /**
         * Upsert all entries in the batch.
         */
        UPSERT
    }

    /**
     * Conflict handling policy for batch writes.
     */
    public enum ConflictPolicy {
        /**
         * Keep the existing entry when a conflict is detected.
         */
        SKIP,
        /**
         * Overwrite the existing entry.
         */
        OVERWRITE,
        /**
         * Abort the batch when a conflict is detected.
         */
        FAIL_FAST
    }

    /**
     * Target registry type applied when entries do not specify one explicitly.
     */
    private Type type;
    /**
     * Target namespace applied when entries do not specify one explicitly.
     */
    private String namespace_id;
    /**
     * Optional operator or caller label attached to the batch request.
     */
    private String operator;
    /**
     * Optional request identifier for tracing one batch.
     */
    private String requestId;
    /**
     * Whether the batch should be evaluated without applying durable mutations.
     */
    private boolean dryRun;
    /**
     * Maximum parallelism hint for implementations that support concurrent processing.
     */
    private int parallelism = 1;
    /**
     * Conflict policy applied when entries already exist.
     */
    private ConflictPolicy conflictPolicy = ConflictPolicy.OVERWRITE;
    /**
     * Whether batch execution should continue after one failed entry.
     */
    private boolean continueOnError = true;
    /**
     * Entries to process in this batch.
     */
    private List<Assets> entries = new ArrayList<>();
    /**
     * Operation to apply to the entries.
     */
    private OperationType operationType = OperationType.UPSERT;

}
