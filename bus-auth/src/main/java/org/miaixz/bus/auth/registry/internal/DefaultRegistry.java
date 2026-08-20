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
package org.miaixz.bus.auth.registry.internal;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.registry.spi.RegistryView;
import org.miaixz.bus.auth.shared.internal.RuntimeProvider;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the only public execution gateway to compiled Source capabilities.
 * <p>
 * Each invocation captures one complete immutable view, verifies lifecycle, remaining budget, exact Manifest
 * membership, and request type, and then delegates without exposing the selected runtime provider. Closing is
 * idempotent and stops accepting new invocations without closing externally owned runtime components.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DefaultRegistry implements Registry {

    /**
     * Atomically published immutable Registry state shared with the reload service.
     */
    private final AtomicRegistryState registryState;

    /**
     * Independent Registry lifecycle state used to reject calls after close.
     */
    private final AtomicReference<Lifecycle.State> lifecycle;

    /**
     * Creates a running Registry over an already complete initial view.
     *
     * @param registryState atomic state initialized by runtime assembly
     * @throws IllegalArgumentException if the state holder is {@code null}
     */
    public DefaultRegistry(final AtomicRegistryState registryState) {
        this.registryState = Assert.notNull(registryState, "Atomic Registry state must not be null");
        this.lifecycle = new AtomicReference<>(Lifecycle.State.RUNNING);
    }

    /**
     * Creates an already completed operational failure with no sensitive structured detail.
     *
     * @param error       shared Bus error code
     * @param description safe diagnostic description
     * @param <S>         expected success type
     * @return completed failed outcome stage
     */
    private static <S> CompletionStage<Outcome<S>> failed(final Errors error, final String description) {
        return CompletableFuture.completedFuture(Outcome.failed(failure(error, description)));
    }

    /**
     * Creates an already completed request rejection with no sensitive structured detail.
     *
     * @param error       shared Bus error code
     * @param description safe diagnostic description
     * @param <S>         expected success type
     * @return completed rejected outcome stage
     */
    private static <S> CompletionStage<Outcome<S>> rejected(final Errors error, final String description) {
        return CompletableFuture.completedFuture(Outcome.rejected(failure(error, description)));
    }

    /**
     * Creates one safe internal failure value using the shared provider-neutral JSON model.
     *
     * @param error       shared Bus error code
     * @param description safe diagnostic description
     * @return immutable internal failure value
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Resolves the concrete capability declaration owned by the selected runtime provider.
     * <p>
     * Routing uses the stable capability key and exact Q/S, direction, and security contract. Interaction metadata is
     * intentionally supplied by the runtime declaration because a concrete Source may implement only redirect, device,
     * or direct interaction while callers use the canonical application-level operation.
     * </p>
     *
     * @param manifest  runtime provider manifest
     * @param requested canonical capability requested by the caller
     * @param <Q>       request type
     * @param <S>       success type
     * @return matching concrete declaration, or {@code null} when no compatible declaration exists
     */
    @SuppressWarnings("unchecked")
    private static <Q, S> Capability<Q, S> declared(
            final Capability.Manifest manifest,
            final Capability<Q, S> requested) {
        if (manifest == null) {
            return null;
        }
        for (Capability<?, ?> candidate : manifest.capabilities()) {
            if (candidate.key().equals(requested.key()) && candidate.requestType().equals(requested.requestType())
                    && candidate.responseType().equals(requested.responseType())
                    && candidate.direction() == requested.direction() && candidate.security() == requested.security()) {
                return (Capability<Q, S>) candidate;
            }
        }
        return null;
    }

    /**
     * Routes one exact declared capability through the current immutable view.
     *
     * @param reference  registered Source reference
     * @param capability strongly typed capability requested by the caller
     * @param request    request matching the capability request class
     * @param context    current non-secret invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        capability request type
     * @param <S>        capability success type
     * @return asynchronous internal outcome; closed, expired, missing, or mismatched calls are completed values
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Registry.Reference reference,
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        if (lifecycle.get() != Lifecycle.State.RUNNING) {
            return failed(ErrorCode._503, "Registry is closed");
        }
        if (reference == null || capability == null || context == null || timeout == null) {
            return rejected(ErrorCode._100100, "Registry invocation requires all routing and context values");
        }
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Registry invocation time budget is exhausted");
        }
        final RegistryView view = registryState.current();
        final RuntimeProvider runtime = view.runtime(reference).getOrNull();
        if (runtime == null) {
            return rejected(ErrorCode._404, "Registry reference is not available in the current revision");
        }
        final Capability.Manifest manifest = runtime.manifest();
        final Capability<Q, S> declared = declared(manifest, capability);
        if (declared == null) {
            return rejected(ErrorCode._100101, "Capability is not declared by the selected Registry reference");
        }
        if (request == null ? declared.requestType() != Void.class : !declared.requestType().isInstance(request)) {
            return rejected(ErrorCode._100101, "Capability request does not match its declared request type");
        }
        try {
            final CompletionStage<Outcome<S>> result = runtime.invoke(declared, request, context, timeout);
            return result == null ? failed(ErrorCode._500, "Runtime provider returned no invocation stage") : result;
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Runtime provider failed before returning an invocation stage");
        }
    }

    /**
     * Returns the complete snapshot from the current immutable view.
     *
     * @return current complete registration snapshot
     */
    @Override
    public Registry.Snapshot snapshot() {
        return registryState.current().snapshot();
    }

    /**
     * Returns the revision from the current immutable view.
     *
     * @return current committed revision
     */
    @Override
    public Registry.Revision revision() {
        return registryState.current().revision();
    }

    /**
     * Returns whether this Registry accepts new invocations.
     *
     * @return {@link Lifecycle.State#RUNNING} before close, otherwise {@link Lifecycle.State#CLOSED}
     */
    @Override
    public Lifecycle.State state() {
        return lifecycle.get();
    }

    /**
     * Idempotently stops new Registry invocations without closing externally owned components.
     */
    @Override
    public void close() {
        lifecycle.compareAndSet(Lifecycle.State.RUNNING, Lifecycle.State.CLOSED);
    }

}
