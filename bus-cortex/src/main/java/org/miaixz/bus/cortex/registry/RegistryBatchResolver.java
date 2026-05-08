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

/**
 * Optional batch lookup hook for resolving existing registry assets before per-entry mutation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface RegistryBatchResolver {

    /**
     * Returns resolver order. Lower values are evaluated first.
     *
     * @return order value
     */
    default int order() {
        return 0;
    }

    /**
     * Returns whether this resolver can handle the operation.
     *
     * @param operation  batch operation
     * @param operations controlled registry operations
     * @return {@code true} if this resolver should be used
     */
    boolean supports(BatchOperation operation, RegistryBatchOperations operations);

    /**
     * Resolves existing assets for the supplied operation.
     *
     * @param operation  batch operation
     * @param operations controlled registry operations
     * @return lookup result
     */
    RegistryBatchLookup resolve(BatchOperation operation, RegistryBatchOperations operations);

}
