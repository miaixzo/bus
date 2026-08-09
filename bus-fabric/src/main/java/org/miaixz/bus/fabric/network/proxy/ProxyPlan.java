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

import java.util.Objects;
import java.util.Optional;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.network.Transport;
import org.miaixz.bus.fabric.observe.tags.Tags;

/**
 * Immutable proxy plan used before opening a network route.
 *
 * @author Kimi Liu
 */
public final class ProxyPlan {

    /**
     * Typed option for the process-independent network proxy policy.
     */
    public static final Options.Key<ProxyPlan> OPTION = Options.key("network.proxy", ProxyPlan.class);

    /**
     * Legacy HTTP-only option retained while callers migrate to {@link #OPTION}.
     */
    public static final Options.Key<ProxyPlan> LEGACY_HTTP_OPTION = Options.key("http.proxy", ProxyPlan.class);

    /**
     * Shared immutable request policy that inherits the context policy.
     */
    private static final ProxyPlan INHERIT = new ProxyPlan(Type.INHERIT, null, Headers.empty());

    /**
     * Shared immutable policy that delegates selection to the JDK system selector.
     */
    private static final ProxyPlan SYSTEM = new ProxyPlan(Type.SYSTEM, null, Headers.empty());

    /**
     * Shared immutable direct route.
     */
    private static final ProxyPlan DIRECT = new ProxyPlan(Type.DIRECT, null, Headers.empty());

    /**
     * Proxy type.
     */
    private final Type type;

    /**
     * Proxy address.
     */
    private final Address proxy;

    /**
     * Proxy authorization headers.
     */
    private final Headers authorization;

    /**
     * Creates a proxy plan.
     *
     * @param type          proxy type
     * @param proxy         proxy address
     * @param authorization proxy authorization headers
     */
    private ProxyPlan(final Type type, final Address proxy, final Headers authorization) {
        this.type = Assert.notNull(type, () -> new ValidateException("Proxy type must not be null"));
        this.proxy = proxy;
        this.authorization = Assert
                .notNull(authorization, () -> new ValidateException("Proxy authorization headers must not be null"));
    }

    /**
     * Returns the request policy that inherits the context network proxy setting.
     *
     * @return shared immutable inheritance policy
     */
    public static ProxyPlan inherit() {
        return INHERIT;
    }

    /**
     * Returns the policy that explicitly delegates to the system proxy selector.
     *
     * @return shared immutable system-selection policy
     */
    public static ProxyPlan system() {
        return SYSTEM;
    }

    /**
     * Returns a direct proxy plan.
     *
     * @return direct plan
     */
    public static ProxyPlan direct() {
        return DIRECT;
    }

    /**
     * Creates an HTTP proxy plan.
     *
     * @param proxy proxy address
     * @return proxy plan
     */
    public static ProxyPlan http(final Address proxy) {
        return http(proxy, Headers.empty());
    }

    /**
     * Creates an HTTP proxy plan with authorization headers.
     *
     * @param proxy         proxy address
     * @param authorization proxy authorization headers
     * @return proxy plan
     */
    public static ProxyPlan http(final Address proxy, final Headers authorization) {
        final Address checkedProxy = Assert
                .notNull(proxy, () -> new ValidateException("HTTP proxy address must be non-null and plain"));
        Assert.isFalse(
                checkedProxy.secure(),
                () -> new ValidateException("HTTP proxy address must be non-null and plain"));
        Assert.isTrue(
                Protocol.HTTP.name.equals(checkedProxy.scheme()),
                () -> new ValidateException("HTTP proxy address must use the http scheme"));
        return new ProxyPlan(Type.HTTP, checkedProxy, Assert
                .notNull(authorization, () -> new ValidateException("Proxy authorization headers must not be null")));
    }

    /**
     * Creates a SOCKS proxy plan.
     *
     * @param proxy proxy address
     * @return proxy plan
     */
    public static ProxyPlan socks(final Address proxy) {
        final Address checked = Assert
                .notNull(proxy, () -> new ValidateException("SOCKS proxy address must not be null"));
        Assert.isTrue(
                !checked.secure() && Transport.fromScheme(checked.scheme()).connectionOriented(),
                () -> new ValidateException("SOCKS proxy address must use a plain stream transport"));
        return new ProxyPlan(Type.SOCKS, checked, Headers.empty());
    }

    /**
     * Returns the proxy address when one exists.
     *
     * @return proxy address
     */
    public Optional<Address> proxy() {
        return Optional.ofNullable(proxy);
    }

    /**
     * Returns whether this policy inherits its enclosing context.
     *
     * @return {@code true} when policy resolution must continue at the context boundary
     */
    public boolean isInherit() {
        return type == Type.INHERIT;
    }

    /**
     * Returns whether this policy explicitly delegates to the system selector.
     *
     * @return {@code true} when the current JDK proxy selector must provide candidates
     */
    public boolean isSystem() {
        return type == Type.SYSTEM;
    }

    /**
     * Returns whether this plan can be consumed by a physical route connector.
     *
     * @return {@code true} for direct, HTTP, and SOCKS plans
     */
    public boolean isResolved() {
        return type == Type.DIRECT || type == Type.HTTP || type == Type.SOCKS;
    }

    /**
     * Returns whether this plan is direct.
     *
     * @return true when direct
     */
    public boolean isDirect() {
        return type == Type.DIRECT;
    }

    /**
     * Returns whether this plan uses an HTTP proxy.
     *
     * @return true when HTTP proxy
     */
    public boolean isHttp() {
        return type == Type.HTTP;
    }

    /**
     * Returns whether this plan uses a SOCKS proxy.
     *
     * @return true when SOCKS proxy
     */
    public boolean isSocks() {
        return type == Type.SOCKS;
    }

    /**
     * Returns whether a tunnel is needed for the target.
     *
     * @param target target address
     * @return true when CONNECT tunnel is needed
     */
    public boolean requiresTunnel(final Address target) {
        Assert.notNull(target, () -> new ValidateException("Target address must not be null"));
        return type == Type.HTTP && target.secure();
    }

    /**
     * Returns a copy of this plan with proxy authorization headers.
     *
     * @param authorization proxy authorization headers
     * @return proxy plan
     */
    public ProxyPlan withAuthorization(final Headers authorization) {
        Assert.isTrue(type == Type.HTTP, () -> new ValidateException("Proxy authorization requires an HTTP proxy"));
        return new ProxyPlan(type, proxy, Assert
                .notNull(authorization, () -> new ValidateException("Proxy authorization headers must not be null")));
    }

    /**
     * Returns proxy authorization headers.
     *
     * @return authorization headers
     */
    public Headers authorization() {
        return authorization;
    }

    /**
     * Returns a redacted route identifier for logs and metrics.
     *
     * @return route identifier
     */
    public String id() {
        final String prefix = switch (type) {
            case INHERIT -> Builder.PROXY_PLAN_INHERIT_ID;
            case SYSTEM -> Builder.PROXY_PLAN_SYSTEM_ID;
            case DIRECT -> Builder.PROXY_PLAN_DIRECT_ID;
            case HTTP -> Builder.PROXY_PLAN_HTTP_ID;
            case SOCKS -> Builder.PROXY_PLAN_SOCKS_ID;
        };
        return proxy == null ? prefix
                : prefix + Symbol.COLON + Symbol.FORWARDSLASH + proxy.host() + Symbol.COLON + proxy.port();
    }

    /**
     * Compares proxy plans by routing-relevant values.
     *
     * @param other other object
     * @return true when equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProxyPlan that)) {
            return false;
        }
        return type == that.type && Objects.equals(proxy, that.proxy)
                && authorization.asMap().equals(that.authorization.asMap());
    }

    /**
     * Returns a stable hash over routing-relevant values.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, proxy, authorization.asMap());
    }

    /**
     * Returns a diagnostic representation with sanitized credentials.
     *
     * @return redacted proxy plan text
     */
    @Override
    public String toString() {
        return "ProxyPlan[type=" + type + ", id=" + id() + ", authorization=" + redactedAuthorization() + "]";
    }

    /**
     * Formats proxy authorization headers for diagnostics without exposing credentials.
     *
     * @return redacted authorization summary
     */
    private String redactedAuthorization() {
        if (authorization.size() == Normal._0) {
            return Normal.NONE;
        }
        final StringBuilder builder = new StringBuilder();
        authorization.asMap().forEach((name, values) -> values.forEach(value -> {
            if (!builder.isEmpty()) {
                builder.append(Symbol.COMMA);
            }
            builder.append(name).append(Symbol.EQUAL).append(Tags.sanitize(name, value));
        }));
        return builder.toString();
    }

    /**
     * Proxy type.
     */
    private enum Type {

        /**
         * Inherit the context proxy policy.
         */
        INHERIT,

        /**
         * Delegate to the system proxy selector.
         */
        SYSTEM,

        /**
         * Direct connection.
         */
        DIRECT,

        /**
         * HTTP proxy.
         */
        HTTP,

        /**
         * SOCKS proxy.
         */
        SOCKS

    }

}
