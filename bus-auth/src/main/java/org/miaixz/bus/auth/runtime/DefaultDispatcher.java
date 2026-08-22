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

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
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
final class DefaultDispatcher implements Dispatcher {

    /**
     * Atomic cell containing the currently published runtime container.
     */
    private final RuntimeContainer.Cell containers;

    /**
     * Runtime lifecycle gate preventing dispatch before startup or after shutdown.
     */
    private final RuntimeLifecycle lifecycle;

    /**
     * Creates a running dispatcher over the shared current runtime view.
     *
     * @param containers atomic runtime container cell
     * @param lifecycle  runtime lifecycle gate
     */
    DefaultDispatcher(final RuntimeContainer.Cell containers, final RuntimeLifecycle lifecycle) {
        this.containers = Assert.notNull(containers, "Dispatcher runtime containers must not be null");
        this.lifecycle = Assert.notNull(lifecycle, "Dispatcher runtime lifecycle must not be null");
    }

    /**
     * Creates an already completed operational failure stage.
     *
     * @param <S>         expected capability response type
     * @param error       stable failure classification
     * @param description safe failure description
     * @return completed failed outcome stage
     */
    private static <S> CompletionStage<Outcome<S>> failed(final Errors error, final String description) {
        return CompletableFuture.completedFuture(Outcome.failed(failure(error, description)));
    }

    /**
     * Creates an already completed expected rejection stage.
     *
     * @param <S>         expected capability response type
     * @param error       stable rejection classification
     * @param description safe rejection description
     * @return completed rejected outcome stage
     */
    private static <S> CompletionStage<Outcome<S>> rejected(final Errors error, final String description) {
        return CompletableFuture.completedFuture(Outcome.rejected(failure(error, description)));
    }

    /**
     * Creates one safe protocol-neutral failure value without sensitive details.
     *
     * @param error       stable error classification
     * @param description safe failure description
     * @return immutable failure value
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Checks the authentication boundary declared by one compiled capability.
     * <p>
     * The invocation boundary owns this check because it is the last framework-controlled point before arbitrary
     * Source-worker code executes. Registry remains responsible only for registration state and lookup.
     * </p>
     *
     * @param security declared minimum authentication boundary
     * @param context  trusted invocation context
     * @return {@code null} when invocation is allowed, otherwise a safe rejection description
     */
    private static String securityRejection(final Capability.Security security, final Context context) {
        return switch (security) {
            case PUBLIC -> null;
            case CLIENT_AUTHENTICATED -> context.clientId().isPresent() ? null
                    : "Capability requires an authenticated protocol client";
            case SUBJECT_AUTHENTICATED -> context.authenticatedSubject().isPresent()
                    && context.authentication().isPresent() ? null : "Capability requires an authenticated subject";
        };
    }

    /**
     * Finds the exact capability declaration matching a caller-supplied capability descriptor.
     *
     * @param <Q>       capability request type
     * @param <S>       capability response type
     * @param manifest  compiled Source capability manifest
     * @param requested caller-supplied capability descriptor
     * @return matching compiled declaration, or {@code null}
     */
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

    @Override
    public boolean available(final Registry.Reference reference) {
        if (!lifecycle.running() || reference == null) {
            return false;
        }
        final RuntimeContainer.Lease lease = containers.acquire();
        if (lease == null) {
            return false;
        }
        try {
            return lease.container().worker(reference).isPresent();
        } finally {
            lease.close();
        }
    }

    @Override
    public Optional<Capability.Manifest> manifest(final Registry.Reference reference) {
        if (!lifecycle.running() || reference == null) {
            return Optional.empty();
        }
        final RuntimeContainer.Lease lease = containers.acquire();
        if (lease == null) {
            return Optional.empty();
        }
        try {
            final SourceWorker worker = lease.container().worker(reference).getOrNull();
            return Optional.ofNullable(worker == null ? null : worker.manifest());
        } finally {
            lease.close();
        }
    }

    /**
     * Resolves and invokes one exact capability in the current runtime container.
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Registry.Reference reference,
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        if (!lifecycle.running()) {
            return failed(ErrorCode._503, "Dispatcher is closed");
        }
        if (reference == null || capability == null || context == null || timeout == null) {
            return rejected(ErrorCode._100100, "Authentication invocation requires all routing and context values");
        }
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Authentication invocation timeout is exhausted");
        }
        final RuntimeLifecycle.Lease operation = lifecycle.enter();
        if (operation == null) {
            return failed(ErrorCode._503, "Authentication runtime is closing");
        }
        final RuntimeContainer.Lease containerLease = containers.acquire();
        if (containerLease == null) {
            operation.close();
            return failed(ErrorCode._503, "Authentication runtime container is retired");
        }
        final SourceWorker worker = containerLease.container().worker(reference).getOrNull();
        if (worker == null) {
            containerLease.close();
            operation.close();
            return rejected(ErrorCode._404, "Registry reference is not available in the current revision");
        }
        final Capability<Q, S> declared = declared(worker.manifest(), capability);
        if (declared == null) {
            containerLease.close();
            operation.close();
            return rejected(ErrorCode._100101, "Capability is not declared by the selected Registry reference");
        }
        if (request == null ? declared.requestType() != Void.class : !declared.requestType().isInstance(request)) {
            containerLease.close();
            operation.close();
            return rejected(ErrorCode._100101, "Capability request does not match its declared request type");
        }
        final String securityRejection = securityRejection(declared.security(), context);
        if (securityRejection != null) {
            containerLease.close();
            operation.close();
            return rejected(ErrorCode._401, securityRejection);
        }
        try {
            final CompletionStage<Outcome<S>> result = worker.invoke(declared, request, context, timeout);
            if (result == null) {
                containerLease.close();
                operation.close();
                return failed(ErrorCode._500, "Source worker returned no invocation stage");
            }
            return result.handle((outcome, cause) -> {
                try {
                    if (cause != null || outcome == null) {
                        return Outcome.failed(failure(ErrorCode._500, "Source worker invocation failed"));
                    }
                    if (outcome instanceof Outcome.Succeeded<?> success) {
                        final Object value = success.value();
                        final boolean matches = declared.responseType() == Void.class ? value == null
                                : value != null && declared.responseType().isInstance(value);
                        if (!matches) {
                            return Outcome.failed(
                                    failure(
                                            ErrorCode._500,
                                            "Source worker result does not match the declared response type"));
                        }
                    }
                    return outcome;
                } finally {
                    containerLease.close();
                    operation.close();
                }
            });
        } catch (RuntimeException ignored) {
            containerLease.close();
            operation.close();
            return failed(ErrorCode._500, "Source worker failed before returning an invocation stage");
        }
    }

}
