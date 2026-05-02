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

import org.miaixz.bus.cortex.Type;

import lombok.Getter;
import lombok.Setter;

/**
 * Result summary of a batch registry operation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class BatchResult {

    /**
     * Creates an empty batch result aggregate.
     */
    public BatchResult() {
    }

    /**
     * Total number of processed entries.
     */
    private int total;
    /**
     * Number of entries processed successfully.
     */
    private int successCount;
    /**
     * Number of entries that failed to process.
     */
    private int failCount;
    /**
     * Number of entries inserted.
     */
    private int insertedCount;
    /**
     * Number of entries updated.
     */
    private int updatedCount;
    /**
     * Number of entries deleted.
     */
    private int deletedCount;
    /**
     * Number of entries skipped because no mutation was required.
     */
    private int skippedCount;
    /**
     * Total elapsed time in milliseconds.
     */
    private long elapsedMs;
    /**
     * Warning messages collected during batch execution.
     */
    private List<String> warnings = new ArrayList<>();
    /**
     * Error items for failed entries.
     */
    private List<Failure> errors = new ArrayList<>();

    /**
     * Records one inserted entry.
     */
    public void recordInsert() {
        total++;
        successCount++;
        insertedCount++;
    }

    /**
     * Records one updated entry.
     */
    public void recordUpdate() {
        total++;
        successCount++;
        updatedCount++;
    }

    /**
     * Records one deleted entry.
     */
    public void recordDelete() {
        total++;
        successCount++;
        deletedCount++;
    }

    /**
     * Records one skipped entry.
     */
    public void recordSkip() {
        total++;
        successCount++;
        skippedCount++;
    }

    /**
     * Records one failed entry.
     *
     * @param failure failure item
     */
    public void recordFailure(Failure failure) {
        total++;
        failCount++;
        if (failure != null) {
            errors.add(failure);
        }
    }

    /**
     * Merges one result into the current aggregate result.
     *
     * @param other other batch result
     */
    public void merge(BatchResult other) {
        if (other == null) {
            return;
        }
        total += other.total;
        successCount += other.successCount;
        failCount += other.failCount;
        insertedCount += other.insertedCount;
        updatedCount += other.updatedCount;
        deletedCount += other.deletedCount;
        skippedCount += other.skippedCount;
        elapsedMs += other.elapsedMs;
        if (other.errors != null && !other.errors.isEmpty()) {
            errors.addAll(other.errors);
        }
        if (other.warnings != null && !other.warnings.isEmpty()) {
            warnings.addAll(other.warnings);
        }
    }

    /**
     * One failed batch entry.
     */
    @Getter
    @Setter
    public static class Failure {

        /**
         * Creates an empty failure descriptor.
         */
        public Failure() {
        }

        /**
         * Target registry type.
         */
        private Type type;
        /**
         * Target namespace.
         */
        private String namespace_id;
        /**
         * Entry ID when available.
         */
        private String id;
        /**
         * Route method when available.
         */
        private String method;
        /**
         * Route version when available.
         */
        private String version;
        /**
         * Failure message.
         */
        private String errorMessage;

        /**
         * Creates one failure item.
         *
         * @param type         target registry type
         * @param namespace    target namespace
         * @param id           entry ID
         * @param method       route method
         * @param version      route version
         * @param errorMessage failure message
         * @return failure item
         */
        public static Failure of(
                Type type,
                String namespace,
                String id,
                String method,
                String version,
                String errorMessage) {
            Failure failure = new Failure();
            failure.setType(type);
            failure.setNamespace_id(namespace);
            failure.setId(id);
            failure.setMethod(method);
            failure.setVersion(version);
            failure.setErrorMessage(errorMessage);
            return failure;
        }

    }

}
