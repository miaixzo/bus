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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.miaixz.bus.core.lang.annotation.Transactional.Isolation;
import org.miaixz.bus.core.lang.annotation.Transactional.Propagation;

/**
 * Immutable transaction operations backed by the application's existing transaction manager. Configured variants are
 * independent, so concurrent callers cannot change one another's policy.
 *
 * @author Kimi Liu
 */
public final class Transactions {

    /**
     * Existing transaction manager that owns resource binding, commit, and rollback.
     */
    private final PlatformTransactionManager manager;
    /**
     * Participation rule applied by this configured operations instance.
     */
    private final Propagation propagation;
    /**
     * Isolation level requested when this instance starts a new transaction.
     */
    private final Isolation isolation;
    /**
     * Read-only hint applied to newly created transactions.
     */
    private final boolean readOnly;
    /**
     * Transaction timeout in seconds; -1 retains the transaction manager's default.
     */
    private final int timeout;

    /**
     * Creates immutable transaction operations using the existing application transaction manager.
     *
     * @param manager existing transaction manager
     */
    public Transactions(PlatformTransactionManager manager) {
        this(manager, Propagation.REQUIRED, Isolation.DEFAULT, false, -1);
    }

    /**
     * Creates an independent transaction operations variant with a validated execution policy.
     *
     * @param manager     existing transaction manager
     * @param propagation transaction participation rule
     * @param isolation   isolation level requested for newly created transactions
     * @param readOnly    read-only hint for newly created transactions
     * @param timeout     transaction timeout in seconds, or -1 for the manager default
     */
    private Transactions(PlatformTransactionManager manager, Propagation propagation, Isolation isolation,
            boolean readOnly, int timeout) {
        this.manager = Objects.requireNonNull(manager);
        this.propagation = Objects.requireNonNull(propagation);
        this.isolation = Objects.requireNonNull(isolation);
        this.readOnly = readOnly;
        if (timeout < -1)
            throw new IllegalArgumentException("Invalid transaction timeout");
        this.timeout = timeout;
    }

    /**
     * Returns an independent variant with the requested participation rule.
     *
     * @param value propagation rule
     * @return configured operations
     */
    public Transactions propagation(Propagation value) {
        return new Transactions(manager, value, isolation, readOnly, timeout);
    }

    /**
     * Returns an independent variant with the requested isolation level.
     *
     * @param value isolation level for new transactions
     * @return configured operations
     */
    public Transactions isolation(Isolation value) {
        return new Transactions(manager, propagation, value, readOnly, timeout);
    }

    /**
     * Returns an independent variant with the requested read-only hint.
     *
     * @param value whether new transactions are read-only
     * @return configured operations
     */
    public Transactions readOnly(boolean value) {
        return new Transactions(manager, propagation, isolation, value, timeout);
    }

    /**
     * Returns an independent variant with the requested transaction timeout.
     *
     * @param seconds timeout in seconds, or -1 for the manager default
     * @return configured operations
     */
    public Transactions timeout(int seconds) {
        return new Transactions(manager, propagation, isolation, readOnly, seconds);
    }

    /**
     * Runs work and returns only after the transaction has committed or rolled back.
     *
     * @param work synchronous work using this transaction scope
     * @param <T>  result type
     * @return result after commit or rollback
     */
    public <T> T execute(Function<TransactionScope, T> work) {
        Objects.requireNonNull(work);
        var template = new TransactionTemplate(manager);
        template.setPropagationBehavior(propagation.value());
        template.setIsolationLevel(isolation.value());
        template.setReadOnly(readOnly);
        template.setTimeout(timeout);
        try {
            return template.execute(status -> {
                var scope = new TransactionScope(status);
                try {
                    return work.apply(scope);
                } finally {
                    scope.finish();
                }
            });
        } catch (RuntimeException failure) {
            throw JdbcErrorTranslator.translate(failure);
        }
    }

    /**
     * Runs work with no returned value.
     *
     * @param work synchronous work using this transaction scope
     */
    public void executeWithoutResult(Consumer<TransactionScope> work) {
        Objects.requireNonNull(work);
        execute(scope -> {
            work.accept(scope);
            return null;
        });
    }

    /**
     * Checks whether the current thread owns an active physical transaction.
     *
     * @return whether a real transaction is active on this thread
     */
    public static boolean isActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    /**
     * Reads the current transaction isolation declaration.
     *
     * @return declared isolation metadata for the current transaction
     */
    public static Isolation currentIsolation() {
        return Isolation.of(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel());
    }

    /**
     * Obtains the scope of a declaratively advised transaction. Callback users receive their scope as an argument.
     *
     * @return scope of the currently advised transaction
     */
    public static TransactionScope current() {
        try {
            return new TransactionScope(TransactionAspectSupport.currentTransactionStatus());
        } catch (RuntimeException failure) {
            throw JdbcErrorTranslator.translate(failure);
        }
    }

}
