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
package org.miaixz.bus.fabric.network;

import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.network.tls.TlsSettings;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;

/**
 * Immutable destination for grouping reusable network connections.
 *
 * @param protocol bus protocol
 * @param address  target address
 * @param options  immutable options
 * @author Kimi Liu
 * @since Java 21+
 */
public record Destination(Protocol protocol, Address address, Options options) {

    /**
     * Creates a connection destination.
     */
    public Destination {
        protocol = Assert.notNull(protocol, () -> new ValidateException("Connection protocol must not be null"));
        address = Assert.notNull(address, () -> new ValidateException("Connection address must not be null"));
        options = stableOptions(
                Assert.notNull(options, () -> new ValidateException("Connection options must not be null")));
    }

    /**
     * Creates a connection destination.
     *
     * @param protocol protocol
     * @param address  address
     * @param options  options
     * @return connection destination
     */
    public static Destination of(final Protocol protocol, final Address address, final Options options) {
        return new Destination(protocol, address, options);
    }

    /**
     * Returns the protocol.
     *
     * @return protocol
     */
    @Override
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Returns the address.
     *
     * @return address
     */
    @Override
    public Address address() {
        return address;
    }

    /**
     * Returns the options.
     *
     * @return options
     */
    @Override
    public Options options() {
        return options;
    }

    /**
     * Returns whether this destination describes a secure connection.
     *
     * @return true when secure
     */
    public boolean secure() {
        return address.secure() || Boolean.TRUE.equals(options.get(Builder.OPTION_TLS))
                || Boolean.TRUE.equals(options.get(Builder.OPTION_SECURE));
    }

    /**
     * Returns whether this destination can share a single physical connection across concurrent logical streams.
     *
     * @return true when multiplex capable
     */
    public boolean multiplex() {
        final Boolean explicit = options.get(Builder.OPTION_MULTIPLEX);
        if (explicit != null) {
            return explicit;
        }
        if (protocol == Protocol.HTTP_2 || protocol == Protocol.H2_PRIOR_KNOWLEDGE) {
            return true;
        }
        final String selected = options.get(Builder.OPTION_PROTOCOL);
        return selected != null && ("h2".equalsIgnoreCase(selected) || "http/2".equalsIgnoreCase(selected)
                || "h2_prior_knowledge".equalsIgnoreCase(selected));
    }

    /**
     * Returns maximum concurrent logical streams for a multiplex destination.
     *
     * @return stream capacity
     */
    public int maxMultiplexStreams() {
        final Integer value = options.get(Builder.OPTION_MAX_MULTIPLEX_STREAMS);
        if (value == null) {
            return Normal._100;
        }
        Assert.isTrue(value > Normal._0, () -> new ValidateException("Max multiplex streams must be positive"));
        return value;
    }

    /**
     * Builds the stable option subset used as part of a connection reuse key.
     *
     * @param source source options
     * @return stable options
     */
    private static Options stableOptions(final Options source) {
        Options stable = Options.empty();
        stable = copyBoolean(source, stable, Builder.OPTION_TLS, Builder.OPTION_TLS.name());
        stable = copyBoolean(source, stable, Builder.OPTION_SECURE, null);
        stable = copyBoolean(source, stable, Builder.OPTION_MULTIPLEX, null);
        stable = copy(source, stable, Builder.OPTION_PROTOCOL);
        stable = copy(source, stable, Builder.OPTION_MAX_MULTIPLEX_STREAMS);
        stable = copyContext(source, stable);
        stable = copySettings(source, stable);
        stable = copyProxy(source, stable);
        return copyBoolean(source, stable, Builder.OPTION_ROUTE_TUNNEL, "tunnel");
    }

    /**
     * Copies one present typed option while preserving explicit null.
     *
     * @param source source options
     * @param target target options
     * @param key    typed key
     * @param <T>    option type
     * @return updated target
     */
    private static <T> Options copy(final Options source, final Options target, final Options.Key<T> key) {
        if (!source.contains(key)) {
            return target;
        }
        final T value = source.get(key);
        return value == null ? target : target.with(key, value);
    }

    /**
     * Copies a canonical true boolean from a typed key or legacy string key.
     *
     * @param source    source options
     * @param target    target options
     * @param key       typed key
     * @param legacyKey legacy key or null
     * @return updated target
     */
    private static Options copyBoolean(
            final Options source,
            final Options target,
            final Options.Key<Boolean> key,
            final String legacyKey) {
        final Object value = source.contains(key) ? source.get(key)
                : legacyKey != null && source.contains(legacyKey) ? source.get(legacyKey) : null;
        if (value == null || Boolean.FALSE.equals(value)) {
            return target;
        }
        if (!(value instanceof Boolean current)) {
            throw new ValidateException("Connection option " + key.name() + " must be boolean");
        }
        return current ? target.with(key, Boolean.TRUE) : target;
    }

    /**
     * Copies TLS context presence while preserving explicit disablement.
     *
     * @param source source options
     * @param target target options
     * @return updated target
     */
    private static Options copyContext(final Options source, final Options target) {
        if (!source.contains(Builder.OPTION_TLS_CONTEXT)) {
            return target;
        }
        return target.with(Builder.OPTION_TLS_CONTEXT, source.get(Builder.OPTION_TLS_CONTEXT));
    }

    /**
     * Copies a non-null immutable TLS settings identity.
     *
     * @param source source options
     * @param target target options
     * @return updated target
     */
    private static Options copySettings(final Options source, final Options target) {
        final TlsSettings settings = source.get(Builder.OPTION_TLS_SETTINGS);
        return settings == null ? target : target.with(Builder.OPTION_TLS_SETTINGS, settings);
    }

    /**
     * Copies and normalizes typed or legacy proxy identity.
     *
     * @param source source options
     * @param target target options
     * @return updated target
     */
    private static Options copyProxy(final Options source, final Options target) {
        final Object value = source.contains(Builder.OPTION_ROUTE_PROXY) ? source.get(Builder.OPTION_ROUTE_PROXY)
                : source.contains("proxy") ? source.get("proxy") : null;
        if (value == null) {
            return target;
        }
        if (!(value instanceof String proxy)) {
            throw new ValidateException("Connection route proxy must be a string");
        }
        final String normalized = proxy.trim();
        if (normalized.isEmpty() || Builder.PROXY_PLAN_DIRECT_ID.equalsIgnoreCase(normalized)) {
            return target;
        }
        return target.with(Builder.OPTION_ROUTE_PROXY, normalized);
    }

    /**
     * Compares destinations by value, including option snapshots.
     *
     * @param other other object
     * @return true when equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Destination that)) {
            return false;
        }
        return protocol == that.protocol && address.equals(that.address)
                && Objects.equals(options.get(Builder.OPTION_TLS), that.options.get(Builder.OPTION_TLS))
                && Objects.equals(options.get(Builder.OPTION_SECURE), that.options.get(Builder.OPTION_SECURE))
                && Objects.equals(options.get(Builder.OPTION_MULTIPLEX), that.options.get(Builder.OPTION_MULTIPLEX))
                && Objects.equals(options.get(Builder.OPTION_PROTOCOL), that.options.get(Builder.OPTION_PROTOCOL))
                && Objects.equals(
                        options.get(Builder.OPTION_MAX_MULTIPLEX_STREAMS),
                        that.options.get(Builder.OPTION_MAX_MULTIPLEX_STREAMS))
                && sameContext(that)
                && Objects
                        .equals(options.get(Builder.OPTION_TLS_SETTINGS), that.options.get(Builder.OPTION_TLS_SETTINGS))
                && Objects.equals(options.get(Builder.OPTION_ROUTE_PROXY), that.options.get(Builder.OPTION_ROUTE_PROXY))
                && Objects.equals(
                        options.get(Builder.OPTION_ROUTE_TUNNEL),
                        that.options.get(Builder.OPTION_ROUTE_TUNNEL));
    }

    /**
     * Returns a stable hash over all destination fields.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = 31 * protocol.hashCode() + address.hashCode();
        result = 31 * result + Objects.hashCode(options.get(Builder.OPTION_TLS));
        result = 31 * result + Objects.hashCode(options.get(Builder.OPTION_SECURE));
        result = 31 * result + Objects.hashCode(options.get(Builder.OPTION_MULTIPLEX));
        result = 31 * result + Objects.hashCode(options.get(Builder.OPTION_PROTOCOL));
        result = 31 * result + Objects.hashCode(options.get(Builder.OPTION_MAX_MULTIPLEX_STREAMS));
        result = 31 * result + Boolean.hashCode(options.contains(Builder.OPTION_TLS_CONTEXT));
        result = 31 * result + System.identityHashCode(contextIdentity(options));
        result = 31 * result + Objects.hashCode(options.get(Builder.OPTION_TLS_SETTINGS));
        result = 31 * result + Objects.hashCode(options.get(Builder.OPTION_ROUTE_PROXY));
        return 31 * result + Objects.hashCode(options.get(Builder.OPTION_ROUTE_TUNNEL));
    }

    /**
     * Compares TLS context presence and stable wrapped-context identity.
     *
     * @param other other destination
     * @return true when context identity is equivalent
     */
    private boolean sameContext(final Destination other) {
        return options.contains(Builder.OPTION_TLS_CONTEXT) == other.options.contains(Builder.OPTION_TLS_CONTEXT)
                && contextIdentity(options) == contextIdentity(other.options);
    }

    /**
     * Returns stable wrapped SSL context identity without materializing an option map.
     *
     * @param source options
     * @return context identity or null
     */
    private static Object contextIdentity(final Options source) {
        final TlsContext context = source.get(Builder.OPTION_TLS_CONTEXT);
        return context == null ? null : context.identity();
    }

}
