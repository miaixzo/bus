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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;

/**
 * Declares a Bus transaction boundary. Runtime exceptions and errors roll back by default; explicit rollback and
 * no-rollback rules use the nearest matching exception type.
 *
 * @author Kimi Liu
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

    /**
     * Selects an existing transaction manager by bean name; empty uses the default manager.
     *
     * @return transaction manager bean name, or an empty value for the default manager
     */
    String transactionManager() default Normal.EMPTY;

    /**
     * Defines whether this operation joins, creates, suspends, or requires a transaction.
     *
     * @return participation rule for this transaction declaration
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * Defines the isolation of a newly created transaction; DEFAULT keeps the manager default.
     *
     * @return isolation level requested by this transaction declaration
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * Limits a new transaction in seconds; -1 uses the manager default.
     *
     * @return transaction timeout in seconds, or -1 for the manager default
     */
    int timeout() default Normal.__1;

    /**
     * Requests read-only handling for a newly created transaction.
     *
     * @return whether newly created transactions should receive the read-only hint
     */
    boolean readOnly() default false;

    /**
     * Adds exception types whose failures must roll back the transaction.
     *
     * @return exception classes whose failures require rollback
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * Adds exception types whose failures may preserve the transaction changes.
     *
     * @return exception classes whose failures are excluded from rollback
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * Transaction participation rules, independent of the underlying transaction engine.
     *
     * @author Kimi Liu
     */
    public enum Propagation {

        /**
         * Joins an existing transaction or creates one when none exists.
         */
        REQUIRED(0),
        /**
         * Joins an existing transaction and otherwise runs without creating one.
         */
        SUPPORTS(1),
        /**
         * Requires an existing transaction and rejects calls made without one.
         */
        MANDATORY(2),
        /**
         * Suspends any existing transaction and starts an independent transaction.
         */
        REQUIRES_NEW(3),
        /**
         * Suspends any existing transaction and runs without a transaction.
         */
        NOT_SUPPORTED(4),
        /**
         * Rejects an existing transaction and otherwise runs without one.
         */
        NEVER(5),
        /**
         * Uses a nested savepoint when supported, or starts a transaction when none exists.
         */
        NESTED(6);

        /**
         * Standard participation code interpreted by the transaction adapter.
         */
        private final int value;

        /**
         * Associates this participation rule with its standard transaction code.
         *
         * @param value standard transaction participation code
         */
        Propagation(int value) {
            this.value = value;
        }

        /**
         * Returns the standard participation code used by the transaction adapter.
         *
         * @return underlying standard propagation code
         */
        public int value() {
            return value;
        }

    }

    /**
     * Standard JDBC transaction isolation levels.
     *
     * @author Kimi Liu
     */
    public enum Isolation {

        /**
         * Uses the default isolation level of the existing transaction manager.
         */
        DEFAULT(-1),
        /**
         * Allows reads of changes not yet committed by another transaction.
         */
        READ_UNCOMMITTED(1),
        /**
         * Restricts each statement to changes committed before that statement's snapshot.
         */
        READ_COMMITTED(2),
        /**
         * Keeps repeated reads consistent with the database's repeatable-read transaction semantics.
         */
        REPEATABLE_READ(4),
        /**
         * Requests database isolation equivalent to a serial ordering of concurrent transactions.
         */
        SERIALIZABLE(8);

        /**
         * Standard JDBC isolation code, or -1 when the transaction manager chooses its default.
         */
        private final int value;

        /**
         * Associates this isolation level with its standard transaction code.
         *
         * @param value standard JDBC isolation code, or -1 for the manager default
         */
        Isolation(int value) {
            this.value = value;
        }

        /**
         * Returns the standard isolation code used by the transaction adapter.
         *
         * @return standard JDBC isolation code
         */
        public int value() {
            return value;
        }

        /**
         * Resolves transaction metadata; no declared isolation is represented by DEFAULT.
         *
         * @param value value to validate, normalize, or project under this method's contract
         * @return isolation level requested by this transaction declaration
         */
        public static Isolation of(Integer value) {
            if (value == null)
                return DEFAULT;
            for (Isolation level : values())
                if (level.value == value)
                    return level;
            throw new IllegalArgumentException("Unsupported isolation level: " + value);
        }

    }

}
