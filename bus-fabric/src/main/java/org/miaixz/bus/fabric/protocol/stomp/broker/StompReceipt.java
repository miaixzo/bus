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
package org.miaixz.bus.fabric.protocol.stomp.broker;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * O(1) STOMP receipt future registry.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompReceipt {

    /**
     * Receipt identifiers mapped to the single future currently awaiting each receipt.
     */
    private final ConcurrentHashMap<String, CompletableFuture<Void>> pending;

    /**
     * Creates an empty registry.
     */
    public StompReceipt() {
        this.pending = new ConcurrentHashMap<>();
    }

    /**
     * Registers one receipt identifier and blocks until it is completed, failed, cancelled, or interrupted.
     * <p>
     * The registration and cancellation callback are removed before this method returns or throws.
     * </p>
     *
     * @param receiptId    non-blank, single-line receipt identifier
     * @param cancellation scope that aborts this wait
     * @throws ValidateException     if the identifier is blank or multi-line, or {@code cancellation} is {@code null}
     * @throws StatefulException     if another wait is already registered for the identifier
     * @throws CancellationException if the scope is cancelled or the waiting thread is interrupted
     * @throws CompletionException   if the receipt is failed with a non-cancellation cause
     */
    public void await(final String receiptId, final Cancellation cancellation) {
        final String id = validate(receiptId);
        final Cancellation scope = Assert
                .notNull(cancellation, () -> new ValidateException("STOMP receipt cancellation must not be null"));
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final CompletableFuture<Void> previous = pending.putIfAbsent(id, future);
        if (previous != null) {
            throw new StatefulException("STOMP receipt is already pending: " + id);
        }
        final Runnable unregister = scope.onCancel(() -> {
            if (pending.remove(id, future)) {
                final Throwable cause = scope.cause();
                future.completeExceptionally(
                        cause == null ? new CancellationException("STOMP receipt wait cancelled") : cause);
            }
        });
        try {
            while (!future.isDone()) {
                scope.throwIfCancelled();
                if (!ThreadKit.sleep(1L)) {
                    throw new CancellationException("STOMP receipt wait interrupted");
                }
            }
            future.join();
        } finally {
            unregister.run();
            pending.remove(id, future);
        }
    }

    /**
     * Removes and successfully completes the pending future for a receipt identifier, if present.
     *
     * @param receiptId non-blank, single-line receipt identifier
     * @throws ValidateException if the identifier is blank or multi-line
     */
    public void complete(final String receiptId) {
        final CompletableFuture<Void> future = pending.remove(validate(receiptId));
        if (future != null) {
            future.complete(null);
        }
    }

    /**
     * Removes and exceptionally completes the pending future for a receipt identifier, if present.
     *
     * @param receiptId non-blank, single-line receipt identifier
     * @param cause     failure stored in the pending future
     * @throws ValidateException if the identifier is blank or multi-line, or {@code cause} is {@code null}
     */
    public void fail(final String receiptId, final Throwable cause) {
        Assert.notNull(cause, () -> new ValidateException("STOMP receipt failure cause must not be null"));
        final CompletableFuture<Void> future = pending.remove(validate(receiptId));
        if (future != null) {
            future.completeExceptionally(cause);
        }
    }

    /**
     * Removes and exceptionally completes every receipt that remains pending during the concurrent traversal.
     *
     * @param cause failure stored in each removed future
     * @throws ValidateException if {@code cause} is {@code null}
     */
    public void failAll(final Throwable cause) {
        Assert.notNull(cause, () -> new ValidateException("STOMP receipt failure cause must not be null"));
        for (final Map.Entry<String, CompletableFuture<Void>> entry : pending.entrySet()) {
            if (pending.remove(entry.getKey(), entry.getValue())) {
                entry.getValue().completeExceptionally(cause);
            }
        }
    }

    /**
     * Returns whether a receipt is pending.
     *
     * @param receiptId non-blank, single-line receipt identifier
     * @return {@code true} when a wait is currently registered for the identifier
     * @throws ValidateException if the identifier is blank or multi-line
     */
    public boolean pending(final String receiptId) {
        return pending.containsKey(validate(receiptId));
    }

    /**
     * Returns pending receipt count.
     *
     * @return current number of registered receipt waits
     */
    public int size() {
        return pending.size();
    }

    /**
     * Validates receipt ids.
     *
     * @param value receipt identifier to validate
     * @return unchanged non-blank, single-line identifier
     * @throws ValidateException if the identifier is blank or contains a carriage return or line feed
     */
    private static String validate(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("STOMP receipt id must be non-blank and single-line");
        }
        return value;
    }

}
