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
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Destination {

    /** Application protocol used for pooling. */
    private final Protocol protocol;

    /** Remote network address. */
    private final Address address;

    /** Normalized connection-reuse option identity. */
    private final Options options;

    /** Precomputed hash because pool lookups reuse this immutable key several times per acquisition. */
    private final int hashCode;

    /**
     * Creates a connection destination.
     *
     * @param protocol application protocol used for pooling
     * @param address  remote network address
     * @param options  source options reduced to connection-reuse identity
     * @throws ValidateException if a required component is {@code null} or a retained boolean or proxy option has the
     *                           wrong type
     */
    public Destination(final Protocol protocol, final Address address, final Options options) {
        this.protocol = Assert.notNull(protocol, () -> new ValidateException("Connection protocol must not be null"));
        this.address = Assert.notNull(address, () -> new ValidateException("Connection address must not be null"));
        this.options = stableOptions(
                Assert.notNull(options, () -> new ValidateException("Connection options must not be null")));
        this.hashCode = computeHashCode();
    }

    /**
     * Creates a connection destination.
     *
     * @param protocol application protocol used for pooling
     * @param address  remote network address
     * @param options  source options reduced to connection-reuse identity
     * @return immutable normalized connection destination
     * @throws ValidateException if a required argument is {@code null} or a retained boolean or proxy option has the
     *                           wrong type
     */
    public static Destination of(final Protocol protocol, final Address address, final Options options) {
        return new Destination(protocol, address, options);
    }

    /**
     * Returns the protocol.
     *
     * @return application protocol used for pooling
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Returns the address.
     *
     * @return remote network address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the options.
     *
     * @return normalized immutable connection-reuse option subset
     */
    public Options options() {
        return options;
    }

    /**
     * Returns whether this destination describes a secure connection.
     *
     * @return {@code true} when the address is secure or either retained TLS or secure option is explicitly true
     */
    public boolean secure() {
        return address.secure() || Boolean.TRUE.equals(options.get(Builder.OPTION_TLS))
                || Boolean.TRUE.equals(options.get(Builder.OPTION_SECURE));
    }

    /**
     * Returns whether this destination can share a single physical connection across concurrent logical streams.
     *
     * @return explicit multiplex option when present; otherwise {@code true} for HTTP/2 protocol identities
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
     * @return configured positive stream limit, or 100 when no limit is retained
     * @throws ValidateException if the retained stream limit is not positive
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
     * @param source complete source option snapshot
     * @return immutable subset containing only normalized connection-reuse identity options
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
     * Copies one present typed option when its value is non-null.
     *
     * @param source source options inspected for the typed key
     * @param target accumulated stable option subset
     * @param key    typed option key to copy
     * @param <T>    option value type
     * @return target unchanged when absent or null; otherwise a new snapshot containing the value
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
     * @param source    source options inspected for typed and legacy forms
     * @param target    accumulated stable option subset
     * @param key       canonical typed boolean key
     * @param legacyKey legacy string key, or {@code null} when no fallback is supported
     * @return target containing canonical {@code true}, or unchanged when absent, null, or false
     * @throws ValidateException if a selected non-null value is not boolean
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
     * @param source source options inspected for explicit TLS-context presence
     * @param target accumulated stable option subset
     * @return target preserving both context-key presence and its possibly null value
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
     * @param source source options containing an optional immutable settings snapshot
     * @param target accumulated stable option subset
     * @return target containing non-null TLS settings, or unchanged when absent or null
     */
    private static Options copySettings(final Options source, final Options target) {
        final TlsSettings settings = source.get(Builder.OPTION_TLS_SETTINGS);
        return settings == null ? target : target.with(Builder.OPTION_TLS_SETTINGS, settings);
    }

    /**
     * Copies and normalizes typed or legacy proxy identity.
     *
     * @param source source options inspected for canonical or legacy proxy identity
     * @param target accumulated stable option subset
     * @return target containing a trimmed non-direct proxy identifier, or unchanged for absent or direct routing
     * @throws ValidateException if a selected non-null proxy value is not a string
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
     * @param other object compared with this normalized destination
     * @return {@code true} when protocol, address, retained option values, and TLS-context identity are equivalent
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
        return hashCode;
    }

    /** Computes the immutable connection-key hash once during construction. */
    private int computeHashCode() {
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
     * @param other normalized destination whose TLS-context option is compared
     * @return {@code true} when key presence matches and wrapped context identities are the same reference
     */
    private boolean sameContext(final Destination other) {
        return options.contains(Builder.OPTION_TLS_CONTEXT) == other.options.contains(Builder.OPTION_TLS_CONTEXT)
                && contextIdentity(options) == contextIdentity(other.options);
    }

    /**
     * Returns stable wrapped SSL context identity without materializing an option map.
     *
     * @param source normalized options containing an optional TLS context
     * @return wrapped SSL-context identity reference, or {@code null} when no non-null context is retained
     */
    private static Object contextIdentity(final Options source) {
        final TlsContext context = source.get(Builder.OPTION_TLS_CONTEXT);
        return context == null ? null : context.identity();
    }

}
