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
package org.miaixz.bus.auth.runtime;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.auth.Authenticator;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.registry.AtomicRegistryState;
import org.miaixz.bus.auth.registry.RegistryView;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Routes and executes capabilities against one atomically published runtime view.
 * <p>
 * This class owns invocation validation and dispatch only. It does not register resources, compile snapshots, perform
 * project authorization, bind accounts, create business sessions, persist data, or emit audit events.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DefaultAuthenticator implements Authenticator {

    private final AtomicRegistryState registryState;

    private final AtomicReference<Lifecycle.State> lifecycle;

    /**
     * Creates a running authenticator over the shared current runtime view.
     *
     * @param registryState atomic runtime view holder
     */
    public DefaultAuthenticator(final AtomicRegistryState registryState) {
        this.registryState = Assert.notNull(registryState, "Authenticator Registry state must not be null");
        this.lifecycle = new AtomicReference<>(Lifecycle.State.RUNNING);
    }

    private static <S> CompletionStage<Outcome<S>> failed(final Errors error, final String description) {
        return CompletableFuture.completedFuture(Outcome.failed(failure(error, description)));
    }

    private static <S> CompletionStage<Outcome<S>> rejected(final Errors error, final String description) {
        return CompletableFuture.completedFuture(Outcome.rejected(failure(error, description)));
    }

    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

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
     * Resolves and invokes one exact capability in the current immutable view.
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Registry.Reference reference,
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        if (lifecycle.get() != Lifecycle.State.RUNNING) {
            return failed(ErrorCode._503, "Authenticator is closed");
        }
        if (reference == null || capability == null || context == null || timeout == null) {
            return rejected(ErrorCode._100100, "Authentication invocation requires all routing and context values");
        }
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Authentication invocation time budget is exhausted");
        }
        final RegistryView view = registryState.current();
        final SourceWorker worker = view.worker(reference).getOrNull();
        if (worker == null) {
            return rejected(ErrorCode._404, "Registry reference is not available in the current revision");
        }
        final Capability<Q, S> declared = declared(worker.manifest(), capability);
        if (declared == null) {
            return rejected(ErrorCode._100101, "Capability is not declared by the selected Registry reference");
        }
        if (request == null ? declared.requestType() != Void.class : !declared.requestType().isInstance(request)) {
            return rejected(ErrorCode._100101, "Capability request does not match its declared request type");
        }
        try {
            final CompletionStage<Outcome<S>> result = worker.invoke(declared, request, context, timeout);
            return result == null ? failed(ErrorCode._500, "Source worker returned no invocation stage") : result;
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Source worker failed before returning an invocation stage");
        }
    }

    @Override
    public Lifecycle.State state() {
        return lifecycle.get();
    }

    @Override
    public void close() {
        lifecycle.compareAndSet(Lifecycle.State.RUNNING, Lifecycle.State.CLOSED);
    }

}
