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
package org.miaixz.bus.auth.vendor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Composes a Vendor manifest, Source options, redirect lifecycle, and already constructed standard protocol clients.
 * <p>
 * Protocol clients and codecs remain owned by their protocol packages. This adapter only associates each declared
 * standard {@link Capability} with the operation supplied by that client and never reimplements protocol wire logic.
 * </p>
 *
 * @author Kimi Liu
 */
public final class StandardAdapter implements VendorAdapter {

    /**
     * Selected immutable platform variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Complete immutable Source deployment options.
     */
    private final VendorOptions<?> options;

    /**
     * Optional shared redirect lifecycle used by browser-oriented composition.
     */
    private final Optional<RedirectManager> redirectManager;

    /**
     * Typed standard protocol operation associations.
     */
    private final List<Binding<?, ?>> bindings;

    /**
     * Exact standard capability subset implemented by the supplied protocol bindings.
     */
    private final Capability.Manifest capabilities;

    /**
     * Creates an adapter from fully constructed standard protocol operations.
     *
     * @param variant         selected platform variant
     * @param options         complete Source options object
     * @param redirectManager optional redirect lifecycle
     * @param bindings        typed protocol operation associations
     * @throws IllegalArgumentException if a component or binding is {@code null}
     * @throws ValidateException        if bindings do not exactly implement the declared manifest
     */
    public StandardAdapter(final VariantManifest.Variant variant, final VendorOptions<?> options,
            final Optional<RedirectManager> redirectManager, final List<Binding<?, ?>> bindings) {
        this.variant = Assert.notNull(variant, "Standard Vendor variant must not be null");
        this.options = Assert.notNull(options, "Standard Vendor options must not be null");
        Assert.notNull(redirectManager, "Standard Vendor redirect manager container must not be null");
        this.redirectManager = Optional.ofNullable(redirectManager.getOrNull());
        Assert.notNull(bindings, "Standard Vendor protocol bindings must not be null");
        final List<Binding<?, ?>> copy = new ArrayList<>(bindings.size());
        final Set<Capability.Key> keys = new HashSet<>(bindings.size());
        for (Binding<?, ?> binding : bindings) {
            final Binding<?, ?> value = Assert.notNull(binding, "Standard Vendor protocol binding must not be null");
            if (!keys.add(value.capability().key())) {
                throw new ValidateException("Standard Vendor protocol bindings contain a duplicate capability");
            }
            copy.add(value);
        }
        final Set<Capability.Key> declared = variant.capabilityManifest().capabilities().stream().map(Capability::key)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!declared.containsAll(keys)) {
            throw new ValidateException("Standard Vendor protocol bindings must be declared by the manifest");
        }
        this.bindings = List.copyOf(copy);
        final List<Capability<?, ?>> capabilities = new ArrayList<>(copy.size());
        for (Binding<?, ?> binding : copy) {
            capabilities.add(binding.capability());
        }
        this.capabilities = new Capability.Manifest(capabilities);
    }

    /**
     * Safely narrows one heterogeneous stored binding to the caller's declared capability types.
     *
     * @param binding    stored typed binding
     * @param capability caller capability
     * @param request    caller request
     * @param context    invocation context
     * @param timeout    time budget
     * @param <Q>        request type
     * @param <S>        success type
     * @return delegated standard outcome
     */
    private static <Q, S> CompletionStage<Outcome<S>> invoke(
            final Binding<?, ?> binding,
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        if (request != null && !capability.requestType().isInstance(request)) {
            return CompletableFuture.completedFuture(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400, "Vendor standard capability request type is invalid",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
        return invokeTyped(binding, capability, request, context, timeout);
    }

    /**
     * Performs the single checked cast justified by equal capability objects.
     *
     * @param binding    stored binding
     * @param capability equal caller capability
     * @param request    caller request
     * @param context    invocation context
     * @param timeout    time budget
     * @param <Q>        request type
     * @param <S>        success type
     * @return delegated outcome
     */
    private static <Q, S> CompletionStage<Outcome<S>> invokeTyped(
            final Binding<?, ?> binding,
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        final Binding<Q, S> typed = (Binding<Q, S>) binding;
        return typed.operation().invoke(request, context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(capability.responseType().cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Returns the exact immutable capability manifest.
     *
     * @return capabilities implemented by the configured standard bindings
     */
    @Override
    public Capability.Manifest manifest() {
        return capabilities;
    }

    /**
     * Invokes the exact typed standard protocol operation associated with a declared capability.
     *
     * @param capability declared standard capability
     * @param request    exact standard request
     * @param context    invocation context
     * @param timeout    end-to-end budget
     * @param <Q>        request type
     * @param <S>        success type
     * @return standard protocol outcome or safe rejection
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        final Capability<Q, S> selected = Assert.notNull(capability, "Standard Vendor capability must not be null");
        Assert.notNull(context, "Standard Vendor context must not be null");
        Assert.notNull(timeout, "Standard Vendor time budget must not be null");
        for (Binding<?, ?> binding : bindings) {
            if (binding.capability().equals(selected)) {
                return invoke(binding, selected, request, context, timeout);
            }
        }
        return CompletableFuture.completedFuture(
                Outcome.rejected(
                        new Outcome.Failure(ErrorCode._400,
                                "Vendor adapter does not implement the requested standard capability",
                                new JsonValue.ObjectValue(Map.of()))));
    }

    /**
     * Invokes one standard protocol client operation without defining a replacement wire contract.
     *
     * @param <Q> request type
     * @param <S> success type
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Operation<Q, S> {

        /**
         * Executes the standard operation within the caller's invocation state.
         *
         * @param request standard request
         * @param context invocation context
         * @param timeout end-to-end budget
         * @return asynchronous standard outcome
         */
        CompletionStage<? extends Outcome<? extends S>> invoke(Q request, Context context, Timeout.Budget timeout);

    }

    /**
     * Associates one exact standard capability with its protocol-client operation.
     *
     * @param capability standard protocol capability
     * @param operation  operation implemented by a protocol client and codec
     * @param <Q>        request type
     * @param <S>        success type
     * @author Kimi Liu
     */
    public record Binding<Q, S>(Capability<Q, S> capability, Operation<Q, ? extends S> operation) {

        /**
         * Validates one typed operation association.
         */
        public Binding {
            capability = Assert.notNull(capability, "Standard Vendor binding capability must not be null");
            operation = Assert.notNull(operation, "Standard Vendor binding operation must not be null");
        }

    }

}
