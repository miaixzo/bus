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
package org.miaixz.bus.mapper.feature.schema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Schema execution report.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@Accessors(fluent = true)
public class SchemaReport {

    /**
     * SQL statements executed successfully.
     */
    private List<String> executedSqls = new ArrayList<>();

    /**
     * SQL statements or messages skipped during execution.
     */
    private List<String> skippedSqls = new ArrayList<>();

    /**
     * Schema differences that failed during execution.
     */
    private List<SchemaDiff> failedDiffs = new ArrayList<>();

    /**
     * Differences found by final verification, or original target differences in non-applying modes.
     */
    private List<SchemaDiff> unresolvedDiffs = new ArrayList<>();

    /**
     * Comparison facts that could not be established from available metadata.
     */
    private List<String> unknowns = new ArrayList<>();

    /**
     * Ordered diagnostics describing object scope, processing phase, and disposition.
     */
    private List<Diagnostic> diagnostics = new ArrayList<>();

    /**
     * Whether the entire checked scope has no remaining differences, unknown facts, or failures. Excluded or unchecked
     * objects and script/dry-run modes prevent this flag from being set.
     */
    private boolean verified;

    /**
     * Constructs a new SchemaReport instance.
     */
    public SchemaReport() {
        // No initialization required.
    }

    /**
     * Appends an observation without changing the execution counters or final verification flag.
     *
     * @param table     table or excluded entity coordinate
     * @param operation operation name, or an empty string for object-wide observations
     * @param stage     processing phase
     * @param status    diagnostic disposition
     * @param reason    human-readable explanation
     */
    public void diagnose(String table, String operation, Stage stage, Status status, String reason) {
        diagnostics.add(new Diagnostic(table, operation, stage, status, reason));
    }

    /**
     * One structured observation; textual reasons are explanatory, while phase and status carry the disposition.
     *
     * @param table     table coordinate, or entity coordinate when excluded before mapping
     * @param operation schema operation name, or an empty string for an object-wide observation
     * @param stage     processing phase that produced this observation
     * @param status    disposition reached in that phase
     * @param reason    human-readable explanation
     */
    public record Diagnostic(String table, String operation, Stage stage, Status status, String reason) {

    }

    /**
     * Phase in which a diagnostic was recorded; SQL preparation is separate from execution.
     */
    public enum Stage {
        /**
         * Entity selection and initial metadata discovery.
         */
        READ,

        /**
         * Capability, configuration, dependency, and SQL preparation checks.
         */
        PREPARE,

        /**
         * Application of prepared SQL to the database.
         */
        EXECUTE,

        /**
         * Metadata reread and comparison after applying the plan.
         */
        VERIFY
    }

    /**
     * Diagnostic disposition. Individual statuses do not replace the report-wide verification result.
     */
    public enum Status {
        /**
         * Object omitted by an explicit include/exclude rule.
         */
        EXCLUDED,

        /**
         * Object left unchecked, for example an existing table in CREATE mode.
         */
        NOT_CHECKED,

        /**
         * Inspection occurred; remaining differences and the verified flag still determine convergence.
         */
        VALIDATED,

        /**
         * SQL generated for a script or dry run without application.
         */
        PROPOSED,

        /**
         * An execution permission, risk switch, or data prerequisite prevented the operation.
         */
        CONFIGURATION_BLOCKED,

        /**
         * The dialect does not support the operation or cannot produce its SQL.
         */
        UNSUPPORTED,

        /**
         * A required structure operation was blocked or failed.
         */
        DEPENDENCY_BLOCKED,

        /**
         * The difference is still present in the final database snapshot.
         */
        UNRESOLVED,

        /**
         * Preparation or execution failed.
         */
        FAILED,

        /**
         * SQL executed successfully; this alone does not prove final structure convergence.
         */
        APPLIED,

        /**
         * Available metadata cannot establish the required comparison fact.
         */
        UNKNOWN
    }

}
