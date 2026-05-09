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

import org.miaixz.bus.cortex.builtin.batch.BatchOperation;
import org.miaixz.bus.cortex.builtin.batch.BatchResult;

/**
 * Pluggable registry batch execution strategy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface RegistryBatchExecutor {

    /**
     * Returns executor order. Lower values run first.
     *
     * @return order value
     */
    default int order() {
        return 0;
    }

    /**
     * Returns whether this executor can handle one operation.
     *
     * @param operation  batch operation
     * @param operations batch operations
     * @return {@code true} when supported
     */
    boolean supports(BatchOperation operation, RegistryBatchOperations operations);

    /**
     * Executes one batch operation.
     *
     * @param operation  batch operation
     * @param operations batch operations
     * @return batch result
     */
    BatchResult execute(BatchOperation operation, RegistryBatchOperations operations);

}
