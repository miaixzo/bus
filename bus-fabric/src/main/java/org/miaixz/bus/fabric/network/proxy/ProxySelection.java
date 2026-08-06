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
package org.miaixz.bus.fabric.network.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;

/**
 * Immutable result of resolving one outbound operation's proxy policy.
 *
 * @param requested  original request-level policy before inheritance resolution
 * @param effective  effective direct, explicit proxy, or system-selection policy
 * @param source     configuration boundary that supplied {@code effective}
 * @param target     logical destination used for selection and failure reporting
 * @param candidates non-empty ordered physical route candidates
 * @param selector   system selector used to create candidates, or {@code null} for fixed policies
 * @author Kimi Liu
 * @since Java 21+
 */
public record ProxySelection(ProxyPlan requested, ProxyPlan effective, Source source, Address target,
        List<ProxyPlan> candidates, ProxySelector selector) {

    /**
     * Describes where the effective policy originated.
     */
    public enum Source {

        /**
         * An explicit non-inherited request policy supplied the effective plan.
         */
        REQUEST,

        /**
         * The context-level {@link ProxyPlan#OPTION} supplied the effective plan.
         */
        CONTEXT,

        /**
         * No configured override existed, so the JDK system selector supplied candidates.
         */
        SYSTEM_DEFAULT,

        /**
         * The compatibility {@link ProxyPlan#LEGACY_HTTP_OPTION} supplied the effective plan.
         */
        LEGACY_HTTP_OPTION,

        /**
         * A present compatibility option with a {@code null} value selected direct routing.
         */
        LEGACY_NULL
    }

    /**
     * Validates and snapshots a resolved selection.
     */
    public ProxySelection {
        requested = required(requested, "Requested proxy plan");
        effective = required(effective, "Effective proxy plan");
        source = required(source, "Proxy policy source");
        target = required(target, "Proxy target");
        candidates = List.copyOf(required(candidates, "Proxy candidates"));
        Assert.isFalse(candidates.isEmpty(), () -> new ValidateException("Proxy candidates must not be empty"));
        Assert.isTrue(
                candidates.stream().allMatch(ProxyPlan::isResolved),
                () -> new ValidateException("Proxy candidates must be resolved routes"));
    }

    /**
     * Validates a required selection component.
     *
     * @param value component value
     * @param name  component name used in the validation message
     * @param <T>   component type
     * @return validated non-null component
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T required(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Reports a failed system-selected proxy candidate back to its originating selector.
     * <p>
     * Fixed request or context policies have no selector and therefore require no callback. Selector callback failures
     * are attached as suppressed exceptions so they cannot replace the actual connection failure.
     *
     * @param candidate resolved candidate whose connection attempt failed
     * @param failure   non-null route failure reported to the selector
     */
    public void connectFailed(final ProxyPlan candidate, final RuntimeException failure) {
        final ProxyPlan failedCandidate = required(candidate, "Failed proxy candidate");
        final RuntimeException routeFailure = required(failure, "Proxy route failure");
        if (selector == null || failedCandidate.proxy().isEmpty()) {
            return;
        }
        final Address address = failedCandidate.proxy().orElseThrow();
        try {
            selector.connectFailed(
                    target.toUri(),
                    InetSocketAddress.createUnresolved(address.host(), address.port()),
                    new IOException("Proxy connection failed", routeFailure));
        } catch (final RuntimeException callbackFailure) {
            routeFailure.addSuppressed(callbackFailure);
        }
    }

}
