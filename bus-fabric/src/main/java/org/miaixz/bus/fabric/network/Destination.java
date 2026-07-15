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
import org.miaixz.bus.fabric.Options;

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
        options = Options.from(
                Assert.notNull(options, () -> new ValidateException("Connection options must not be null")).asMap());
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
        return address.secure() || Boolean.TRUE.equals(options.get("tls"))
                || Boolean.TRUE.equals(options.get("secure"));
    }

    /**
     * Returns whether this destination can share a single physical connection across concurrent logical streams.
     *
     * @return true when multiplex capable
     */
    public boolean multiplex() {
        final Object explicit = options.get("multiplex");
        if (explicit instanceof Boolean enabled) {
            return enabled;
        }
        if (protocol == Protocol.HTTP_2 || protocol == Protocol.H2_PRIOR_KNOWLEDGE) {
            return true;
        }
        final Object selected = options.get("protocol");
        return selected != null
                && ("h2".equalsIgnoreCase(selected.toString()) || "http/2".equalsIgnoreCase(selected.toString())
                || "h2_prior_knowledge".equalsIgnoreCase(selected.toString()));
    }

    /**
     * Returns maximum concurrent logical streams for a multiplex destination.
     *
     * @return stream capacity
     */
    public int maxMultiplexStreams() {
        final Object value = options.get("maxMultiplexStreams") == null ? options.get("maxConcurrentStreams")
                : options.get("maxMultiplexStreams");
        if (value == null) {
            return Normal._100;
        }
        final int parsed = value instanceof Number number ? number.intValue() : Integer.parseInt(value.toString());
        Assert.isTrue(parsed > Normal._0, () -> new ValidateException("Max multiplex streams must be positive"));
        return parsed;
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
                && options.asMap().equals(that.options.asMap());
    }

    /**
     * Returns a stable hash over all destination fields.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(protocol, address, options.asMap());
    }

}
