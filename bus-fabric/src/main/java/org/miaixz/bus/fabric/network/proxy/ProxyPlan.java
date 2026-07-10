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

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.observe.tag.Tags;

/**
 * Immutable proxy plan used before opening a network route.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ProxyPlan {

    /**
     * Shared empty proxy authorization headers.
     */
    private static final Headers EMPTY_AUTHORIZATION = Headers.empty();

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
        this.type = type;
        this.proxy = proxy;
        this.authorization = require(authorization, "Proxy authorization headers");
    }

    /**
     * Returns a direct proxy plan.
     *
     * @return direct plan
     */
    public static ProxyPlan direct() {
        return Instances.get(
                ProxyPlan.class.getName() + ".direct",
                () -> new ProxyPlan(Type.DIRECT, null, EMPTY_AUTHORIZATION));
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
        if (proxy == null || proxy.secure()) {
            throw new ValidateException("HTTP proxy address must be non-null and plain");
        }
        return new ProxyPlan(Type.HTTP, proxy, require(authorization, "Proxy authorization headers"));
    }

    /**
     * Creates a SOCKS proxy plan.
     *
     * @param proxy proxy address
     * @return proxy plan
     */
    public static ProxyPlan socks(final Address proxy) {
        if (proxy == null) {
            throw new ValidateException("SOCKS proxy address must not be null");
        }
        return new ProxyPlan(Type.SOCKS, proxy, Headers.empty());
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
        if (target == null) {
            throw new ValidateException("Target address must not be null");
        }
        return type == Type.HTTP && target.secure();
    }

    /**
     * Returns a copy of this plan with proxy authorization headers.
     *
     * @param authorization proxy authorization headers
     * @return proxy plan
     */
    public ProxyPlan withAuthorization(final Headers authorization) {
        if (type != Type.HTTP) {
            throw new ValidateException("Proxy authorization requires an HTTP proxy");
        }
        return new ProxyPlan(type, proxy, require(authorization, "Proxy authorization headers"));
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
        if (type == Type.DIRECT) {
            return "direct";
        }
        return type.name().toLowerCase(Locale.ROOT) + "://" + proxy.host() + ":" + proxy.port();
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
        if (authorization.size() == 0) {
            return "none";
        }
        final StringBuilder builder = new StringBuilder();
        authorization.asMap().forEach((name, values) -> values.forEach(value -> {
            if (!builder.isEmpty()) {
                builder.append(",");
            }
            builder.append(name).append("=").append(Tags.sanitize(name, value));
        }));
        return builder.toString();
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Proxy type.
     */
    private enum Type {

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
