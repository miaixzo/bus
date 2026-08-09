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
package org.miaixz.bus.fabric.network.dns.cache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsClientSubnet;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;

/**
 * Immutable DNS response-cache key.
 *
 * @author Kimi Liu
 */
public final class DnsCacheKey {

    /**
     * Lowercase fully-qualified query name.
     */
    private final String name;

    /**
     * Numeric query type code.
     */
    private final int typeCode;

    /**
     * Numeric query class code.
     */
    private final int recordClass;

    /**
     * Whether the response is a stream-safe full response.
     */
    private final boolean stream;

    /**
     * Selected DNS view name.
     */
    private final String viewName;

    /**
     * EDNS Client Subnet scope partition.
     */
    private final String ecsScope;

    /**
     * Creates a cache key.
     *
     * @param name         lowercase fully-qualified query name
     * @param typeCode     numeric query type code
     * @param recordClass  numeric query class code
     * @param stream       whether the response is stream-safe
     * @param viewName     selected DNS view name
     * @param clientSubnet EDNS Client Subnet value, or {@code null}
     */
    public DnsCacheKey(final String name, final int typeCode, final int recordClass, final boolean stream,
            final String viewName, final DnsClientSubnet clientSubnet) {
        this.name = DnsName.normalize(name);
        this.typeCode = DnsCodec.validateUnsignedShort(typeCode, "DNS cache key type");
        this.recordClass = DnsCodec.validateUnsignedShort(recordClass, "DNS cache key class");
        this.stream = stream;
        this.viewName = viewName == null ? Normal.EMPTY : viewName;
        this.ecsScope = ecsScope(clientSubnet);
    }

    /**
     * Creates a cache key from a decoded query.
     *
     * @param query    decoded query
     * @param stream   whether the response is stream-safe
     * @param viewName selected DNS view name
     * @return immutable cache key
     */
    public static DnsCacheKey from(final DnsQuery query, final boolean stream, final String viewName) {
        if (query == null) {
            throw new ValidateException("DNS cache key query must not be null");
        }
        final DnsQuestion question = query.question();
        return new DnsCacheKey(question.name(), question.typeCode(), question.recordClass(), stream, viewName,
                query.clientSubnet());
    }

    /**
     * Returns lowercase fully-qualified query name.
     *
     * @return query name
     */
    public String name() {
        return name;
    }

    /**
     * Returns numeric query type code.
     *
     * @return query type code
     */
    public int typeCode() {
        return typeCode;
    }

    /**
     * Returns numeric query class code.
     *
     * @return query class code
     */
    public int recordClass() {
        return recordClass;
    }

    /**
     * Returns whether the response is stream-safe.
     *
     * @return true for TCP-style full responses
     */
    public boolean stream() {
        return stream;
    }

    /**
     * Returns selected DNS view name.
     *
     * @return view name
     */
    public String viewName() {
        return viewName;
    }

    /**
     * Returns ECS scope partition.
     *
     * @return ECS scope partition
     */
    public String ecsScope() {
        return ecsScope;
    }

    /**
     * Returns whether another object has the same cache key fields.
     *
     * @param other object being compared
     * @return true when all key fields match
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DnsCacheKey key)) {
            return false;
        }
        return typeCode == key.typeCode && recordClass == key.recordClass && stream == key.stream
                && name.equals(key.name) && viewName.equals(key.viewName) && ecsScope.equals(key.ecsScope);
    }

    /**
     * Returns stable hash code.
     *
     * @return hash code based on key fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, typeCode, recordClass, stream, viewName, ecsScope);
    }

    /**
     * Builds the ECS scope partition.
     *
     * @param clientSubnet client subnet, or {@code null}
     * @return scope partition
     */
    private static String ecsScope(final DnsClientSubnet clientSubnet) {
        if (clientSubnet == null) {
            return Normal.EMPTY;
        }
        final byte[] scoped = mask(clientSubnet.address().getAddress(), clientSubnet.scopePrefixLength());
        try {
            return InetAddress.getByAddress(scoped).getHostAddress() + Symbol.SLASH + clientSubnet.scopePrefixLength();
        } catch (final UnknownHostException e) {
            throw new ValidateException("DNS cache key ECS address is invalid", e);
        }
    }

    /**
     * Masks address bytes to a prefix.
     *
     * @param address address bytes
     * @param prefix  prefix length in bits
     * @return masked address bytes
     */
    private static byte[] mask(final byte[] address, final int prefix) {
        final byte[] copy = Arrays.copyOf(address, address.length);
        int remaining = prefix;
        for (int index = 0; index < copy.length; index++) {
            if (remaining >= 8) {
                remaining -= 8;
            } else if (remaining > 0) {
                copy[index] = (byte) (copy[index] & (0xff << (8 - remaining)));
                remaining = 0;
            } else {
                copy[index] = 0;
            }
        }
        return copy;
    }

}
