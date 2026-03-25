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

import java.util.List;

import org.miaixz.bus.cortex.Assets;

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
        UPDATE
    }

    /**
     * Entries to process in this batch.
     */
    private List<Assets> entries;
    /**
     * Operation to apply to the entries.
     */
    private OperationType operationType;

}
