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

import java.net.ProxySelector;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Options;

/**
 * The single policy-resolution boundary shared by every outbound protocol.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ProxyPolicyResolver {

    /**
     * Creates a stateless proxy-policy resolver.
     */
    public ProxyPolicyResolver() {
        // No initialization required.
    }

    /**
     * Resolves request, context, legacy, and system policies in deterministic priority order.
     * <p>
     * Explicit request policy has highest priority, followed by {@link ProxyPlan#OPTION}, the legacy HTTP option, and
     * finally the current JDK system selector. A present legacy option with a {@code null} value means direct routing.
     *
     * @param requestPlan non-null request policy, normally {@link ProxyPlan#inherit()}
     * @param options     non-null context options consulted for inherited policy
     * @param target      non-null logical target used for system selector lookup and failure reporting
     * @return immutable effective policy and ordered resolved route candidates
     * @throws ValidateException if an argument is {@code null} or resolution produces an unsupported policy state
     */
    public ProxySelection resolve(final ProxyPlan requestPlan, final Options options, final Address target) {
        final ProxyPlan requested = required(requestPlan, "Request proxy plan");
        final Options configured = required(options, "Context options");
        final Address destination = required(target, "Proxy target");
        ProxyPlan effective = requested;
        ProxySelection.Source source = ProxySelection.Source.REQUEST;
        if (effective.isInherit()) {
            if (configured.contains(ProxyPlan.OPTION)) {
                effective = configured.get(ProxyPlan.OPTION);
                source = effective == null ? ProxySelection.Source.LEGACY_NULL : ProxySelection.Source.CONTEXT;
            } else if (configured.contains(ProxyPlan.LEGACY_HTTP_OPTION)) {
                effective = configured.get(ProxyPlan.LEGACY_HTTP_OPTION);
                source = effective == null ? ProxySelection.Source.LEGACY_NULL
                        : ProxySelection.Source.LEGACY_HTTP_OPTION;
            } else {
                effective = ProxyPlan.system();
                source = ProxySelection.Source.SYSTEM_DEFAULT;
            }
            if (effective == null) {
                effective = ProxyPlan.direct();
            } else if (effective.isInherit()) {
                effective = ProxyPlan.system();
            }
        }
        if (!effective.isSystem()) {
            Assert.isTrue(effective.isResolved(), () -> new ValidateException("Unsupported proxy policy state"));
            return new ProxySelection(requested, effective, source, destination, List.of(effective), null);
        }
        final ProxySelector selector = ProxySelector.getDefault();
        final List<ProxyPlan> candidates = selector == null ? List.of(ProxyPlan.direct())
                : ProxySelectorAdapter.of(selector).select(destination.toUri());
        return new ProxySelection(requested, effective, source, destination, candidates, selector);
    }

    /**
     * Validates one required resolver input.
     *
     * @param value input value
     * @param name  diagnostic input name
     * @param <T>   input type
     * @return validated non-null value
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T required(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
