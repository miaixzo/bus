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
package org.miaixz.bus.auth.guard;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Enforces the shared state-port programming contract without emulating an atomic operation. Every method validates
 * invocation, key, byte ownership, TTL, delegate stage, and result shape, then invokes exactly one delegate method with
 * the same name. In particular, one-time consumption uses only {@link StateStore#take(Context, String)} and never
 * composes a read with removal; replay admission uses only put-if-absent, and compare-and-set remains one backend
 * linearization point.
 * <p>
 * <strong>Bus dependencies:</strong> {@link StateStore} supplies all atomic semantics, {@link Context} supplies
 * immutable tenant context, and bus-core {@link Assert} and {@link ValidateException} enforce programming contracts.
 * This guard owns no state, clock, executor, cache, or delegate lifecycle.
 *
 * @author Kimi Liu
 */
public final class StateStoreGuard implements org.miaixz.bus.auth.cache.StateStore {

    /**
     * Product-owned atomic state port.
     */
    private final StateStore delegate;

    /**
     * Creates a stateless validating decorator.
     *
     * @param delegate product-owned atomic state port
     */
    public StateStoreGuard(final StateStore delegate) {
        this.delegate = Assert.notNull(delegate, () -> new ValidateException("State store must not be null"));
    }

    /**
     * Validates one operation context.
     *
     * @param invocation source context
     * @return validated context
     */
    private static Context invocation(final Context invocation) {
        return Assert.notNull(invocation, () -> new ValidateException("Context must not be null"));
    }

    /**
     * Validates one non-blank state key without rewriting it.
     *
     * @param key source state key
     * @return validated state key
     */
    private static String key(final String key) {
        return Assert.notBlank(key, () -> new ValidateException("State key must not be blank"));
    }

    /**
     * Copies one required byte value.
     *
     * @param value source bytes
     * @param label safe diagnostic label
     * @return independent byte copy
     */
    private static byte[] bytes(final byte[] value, final String label) {
        final byte[] current = Assert.notNull(value, () -> new ValidateException(label + " must not be null"));
        return Arrays.copyOf(current, current.length);
    }

    /**
     * Validates one strictly positive state lifetime.
     *
     * @param ttl source lifetime
     * @return validated lifetime
     */
    private static Duration ttl(final Duration ttl) {
        final Duration current = Assert.notNull(ttl, () -> new ValidateException("State lifetime must not be null"));
        Assert.isTrue(
                !current.isZero() && !current.isNegative(),
                () -> new ValidateException("State lifetime must be positive"));
        return current;
    }

    /**
     * Validates a single delegate Boolean result stage.
     *
     * @param stage delegate result stage
     * @return stage rejecting a null Boolean result
     */
    private static CompletionStage<Boolean> booleanResult(final CompletionStage<Boolean> stage) {
        final CompletionStage<Boolean> current = Assert
                .notNull(stage, () -> new ValidateException("State store returned a null stage"));
        return current.thenApply(
                value -> Assert.notNull(value, () -> new ValidateException("State store returned a null Boolean")));
    }

    /**
     * Validates a single delegate optional-byte result stage and copies present bytes.
     *
     * @param stage delegate result stage
     * @return stage containing an independent optional byte value
     */
    private static CompletionStage<Optional<byte[]>> bytesResult(final CompletionStage<Optional<byte[]>> stage) {
        final CompletionStage<Optional<byte[]>> current = Assert
                .notNull(stage, () -> new ValidateException("State store returned a null stage"));
        return current.thenApply(
                value -> Assert
                        .notNull(value, () -> new ValidateException("State store returned a null optional value"))
                        .map(bytes -> Arrays.copyOf(bytes, bytes.length)));
    }

    /**
     * Creates a copied value when no unexpired value exists.
     *
     * @param invocation operation context
     * @param key        state key
     * @param value      state bytes
     * @param ttl        positive state lifetime
     * @return validated asynchronous insertion result
     */
    @Override
    public CompletionStage<Boolean> putIfAbsent(
            final Context invocation,
            final String key,
            final byte[] value,
            final Duration ttl) {
        return booleanResult(
                delegate.putIfAbsent(invocation(invocation), key(key), bytes(value, "State value"), ttl(ttl)));
    }

    /**
     * Reads one copied unexpired value.
     *
     * @param invocation operation context
     * @param key        state key
     * @return validated asynchronous optional value
     */
    @Override
    public CompletionStage<Optional<byte[]>> get(final Context invocation, final String key) {
        return bytesResult(delegate.get(invocation(invocation), key(key)));
    }

    /**
     * Atomically consumes one copied unexpired value.
     *
     * @param invocation operation context
     * @param key        state key
     * @return validated asynchronous optional consumed value
     */
    @Override
    public CompletionStage<Optional<byte[]>> take(final Context invocation, final String key) {
        return bytesResult(delegate.take(invocation(invocation), key(key)));
    }

    /**
     * Atomically replaces an expected unexpired value with copied bytes and a positive lifetime.
     *
     * @param invocation operation context
     * @param key        state key
     * @param expected   expected state bytes
     * @param update     replacement state bytes
     * @param ttl        positive replacement lifetime
     * @return validated asynchronous replacement result
     */
    @Override
    public CompletionStage<Boolean> compareAndSet(
            final Context invocation,
            final String key,
            final byte[] expected,
            final byte[] update,
            final Duration ttl) {
        return booleanResult(
                delegate.compareAndSet(
                        invocation(invocation),
                        key(key),
                        bytes(expected, "Expected state value"),
                        bytes(update, "Replacement state value"),
                        ttl(ttl)));
    }

    /**
     * Atomically removes one unexpired value.
     *
     * @param invocation operation context
     * @param key        state key
     * @return validated asynchronous removal result
     */
    @Override
    public CompletionStage<Boolean> remove(final Context invocation, final String key) {
        return booleanResult(delegate.remove(invocation(invocation), key(key)));
    }

}
