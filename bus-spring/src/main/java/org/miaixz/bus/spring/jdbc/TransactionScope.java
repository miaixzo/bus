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
package org.miaixz.bus.spring.jdbc;

import org.springframework.transaction.TransactionStatus;

/**
 * A thread-confined handle to the current transaction, without exposing its implementation.
 *
 * @author Kimi Liu
 */
public final class TransactionScope {

    /**
     * Engine transaction status retained by this scope without exposing it to business callers.
     */
    private final TransactionStatus status;
    /**
     * Thread that created this handle and is allowed to change the transaction state.
     */
    private final Thread owner = Thread.currentThread();
    /**
     * Whether the owning callback has not yet released this scope.
     */
    private boolean active = true;

    /**
     * Binds a thread-confined scope to the current engine transaction status.
     *
     * @param status engine status of the owning transaction
     */
    TransactionScope(TransactionStatus status) {
        this.status = status;
    }

    /**
     * Expires this handle when its owning transaction callback exits.
     */
    void finish() {
        active = false;
    }

    /**
     * Rejects a completed scope or access from a thread other than its owner.
     */
    private void requireActive() {
        if (!active || owner != Thread.currentThread() || status.isCompleted())
            throw new IllegalStateException("Transaction scope is no longer active on this thread");
    }

    /**
     * Checks whether this callback handle or its underlying transaction has completed.
     *
     * @return whether this scope has expired or its underlying transaction is complete
     */
    public boolean isCompleted() {
        return !active || status.isCompleted();
    }

    /**
     * Checks whether the current transaction has been marked for rollback.
     *
     * @return whether the current transaction is marked for rollback
     * @throws IllegalStateException if the scope has expired or the current thread is not its owner
     */
    public boolean isRollbackOnly() {
        requireActive();
        return status.isRollbackOnly();
    }

    /**
     * Marks the current transaction for rollback without replacing the operation's result.
     *
     * @throws IllegalStateException if the scope has expired or the current thread is not its owner
     */
    public void setRollbackOnly() {
        requireActive();
        status.setRollbackOnly();
    }

    /**
     * Creates an automatically released savepoint in this transaction.
     *
     * @return savepoint handle owned by the current transaction scope
     * @throws IllegalStateException if the scope has expired or the current thread is not its owner
     */
    public Savepoint savepoint() {
        requireActive();
        try {
            return new Savepoint(status.createSavepoint());
        } catch (RuntimeException failure) {
            throw JdbcErrorTranslator.translate(failure);
        }
    }

    /**
     * A savepoint belongs only to its creating transaction and thread.
     *
     * @author Kimi Liu
     */
    public final class Savepoint implements AutoCloseable {

        /**
         * Engine savepoint token belonging to the enclosing transaction scope.
         */
        private final Object value;
        /**
         * Whether the savepoint has already been released.
         */
        private boolean closed;

        /**
         * Retains the engine savepoint token inside its owning transaction scope.
         *
         * @param value engine savepoint token owned by the enclosing scope
         */
        private Savepoint(Object value) {
            this.value = value;
        }

        /**
         * Rolls back work since this savepoint while keeping the enclosing transaction usable.
         *
         * @throws IllegalStateException if the owning scope is no longer active on this thread
         */
        public void rollback() {
            requireActive();
            if (closed)
                throw new IllegalStateException("Savepoint is already released");
            try {
                status.rollbackToSavepoint(value);
            } catch (RuntimeException failure) {
                throw JdbcErrorTranslator.translate(failure);
            }
        }

        /**
         * Releases this savepoint.
         *
         * @throws IllegalStateException if the owning scope is no longer active on this thread
         */
        @Override
        public void close() {
            requireActive();
            if (!closed) {
                try {
                    status.releaseSavepoint(value);
                    closed = true;
                } catch (RuntimeException failure) {
                    throw JdbcErrorTranslator.translate(failure);
                }
            }
        }

    }

}
