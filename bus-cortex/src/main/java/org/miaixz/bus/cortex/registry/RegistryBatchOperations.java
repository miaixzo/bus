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
package org.miaixz.bus.cortex.registry;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.builtin.batch.BatchOperation;

/**
 * Batch-specific registry operations exposed to pluggable batch strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface RegistryBatchOperations extends RegistryOperations {

    /**
     * Prepares a source entry with operation defaults.
     *
     * @param operation batch operation
     * @param source    source entry
     * @return prepared entry
     */
    Assets prepareEntry(BatchOperation operation, Assets source);

    /**
     * Returns whether execution should continue after one failed entry.
     *
     * @param operation batch operation
     * @return continue-on-error flag
     */
    default boolean continueOnError(BatchOperation operation) {
        return operation != null && operation.isContinueOnError();
    }

    /**
     * Returns whether execution should skip durable mutation.
     *
     * @param operation batch operation
     * @return dry-run flag
     */
    default boolean dryRun(BatchOperation operation) {
        return operation != null && operation.isDryRun();
    }

}
