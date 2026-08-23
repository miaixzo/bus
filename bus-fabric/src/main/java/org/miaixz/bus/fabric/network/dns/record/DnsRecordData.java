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
package org.miaixz.bus.fabric.network.dns.record;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;

/**
 * Immutable DNS record-data value used by external snapshot builders before runtime compilation.
 *
 * <p>
 * Implementations are side-effect free, thread-safe, and own defensive copies of wire-format RDATA. The type keeps
 * record data independent from owner name, class, and TTL so external control projects can group values into an
 * {@link DnsRecordSet} and the hot DNS path can still use {@link DnsRecord}.
 * </p>
 *
 * @author Kimi Liu
 */
public interface DnsRecordData {

    /**
     * Returns the known DNS record type.
     *
     * @return known record type, or {@link DnsRecordType#UNKNOWN}
     */
    DnsRecordType type();

    /**
     * Returns the numeric DNS record type.
     *
     * @return unsigned 16-bit DNS record type code
     */
    int typeCode();

    /**
     * Returns a defensive copy of RDATA bytes.
     *
     * @return wire-format RDATA bytes
     */
    byte[] wireData();

    /**
     * Converts this data value into an Internet-class DNS record.
     *
     * @param name owner name
     * @param ttl  unsigned 32-bit record TTL
     * @return immutable DNS record
     */
    default DnsRecord toRecord(final String name, final long ttl) {
        return toRecord(name, DnsRecord.CLASS_IN, ttl);
    }

    /**
     * Converts this data value into a DNS record.
     *
     * @param name        owner name
     * @param recordClass unsigned 16-bit record class
     * @param ttl         unsigned 32-bit record TTL
     * @return immutable DNS record
     */
    default DnsRecord toRecord(final String name, final int recordClass, final long ttl) {
        return DnsRecord.raw(name, typeCode(), recordClass, ttl, wireData());
    }

    /**
     * Creates record data from an existing DNS record.
     *
     * @param record source DNS record
     * @return immutable record data
     */
    static DnsRecordData fromRecord(final DnsRecord record) {
        if (record == null) {
            throw new ValidateException("DNS record must not be null");
        }
        return raw(record.typeCode(), record.wireData());
    }

    /**
     * Creates raw record data for any DNS type, including unknown and pseudo types.
     *
     * @param typeCode unsigned 16-bit DNS record type code
     * @param wireData wire-format RDATA bytes
     * @return immutable record data
     */
    static DnsRecordData raw(final int typeCode, final byte[] wireData) {
        return new Wire(typeCode, wireData);
    }

    /**
     * Creates record data for a known DNS type from raw RDATA bytes.
     *
     * @param type     known DNS record type
     * @param wireData wire-format RDATA bytes
     * @return immutable record data
     */
    static DnsRecordData raw(final DnsRecordType type, final byte[] wireData) {
        if (type == null) {
            throw new ValidateException("DNS record type must not be null");
        }
        return raw(type.code(), wireData);
    }

    /**
     * Creates A record data.
     *
     * @param address IPv4 address
     * @return immutable A record data
     */
    static DnsRecordData a(final InetAddress address) {
        return raw(DnsRecordType.A, addressBytes(address, 4, "A"));
    }

    /**
     * Creates AAAA record data.
     *
     * @param address IPv6 address
     * @return immutable AAAA record data
     */
    static DnsRecordData aaaa(final InetAddress address) {
        return raw(DnsRecordType.AAAA, addressBytes(address, 16, "AAAA"));
    }

    /**
     * Creates DNS-name record data for CNAME, DNAME, NS, or PTR.
     *
     * @param type   DNS record type requiring one domain name as RDATA
     * @param target target DNS name
     * @return immutable DNS-name record data
     */
    static DnsRecordData name(final DnsRecordType type, final String target) {
        if (type != DnsRecordType.CNAME && type != DnsRecordType.DNAME && type != DnsRecordType.NS
                && type != DnsRecordType.PTR) {
            throw new ValidateException("DNS name record data type must be CNAME, DNAME, NS, or PTR");
        }
        return raw(type, DnsName.wire(target));
    }

    /**
     * Creates CNAME record data.
     *
     * @param target canonical target name
     * @return immutable CNAME record data
     */
    static DnsRecordData cname(final String target) {
        return name(DnsRecordType.CNAME, target);
    }

    /**
     * Creates DNAME record data.
     *
     * @param target delegated target suffix
     * @return immutable DNAME record data
     */
    static DnsRecordData dname(final String target) {
        return name(DnsRecordType.DNAME, target);
    }

    /**
     * Creates NS record data.
     *
     * @param target authoritative name server name
     * @return immutable NS record data
     */
    static DnsRecordData ns(final String target) {
        return name(DnsRecordType.NS, target);
    }

    /**
     * Creates PTR record data.
     *
     * @param target pointer target name
     * @return immutable PTR record data
     */
    static DnsRecordData ptr(final String target) {
        return name(DnsRecordType.PTR, target);
    }

    /**
     * Creates TXT record data.
     *
     * @param chunks TXT character-string chunks
     * @return immutable TXT record data
     */
    static DnsRecordData txt(final List<String> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            throw new ValidateException("TXT record data must contain at least one chunk");
        }
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (final String chunk : chunks) {
            if (chunk == null) {
                throw new ValidateException("TXT record chunk must not be null");
            }
            final byte[] data = chunk.getBytes(Charset.UTF_8);
            if (data.length > 255) {
                throw new ValidateException("TXT record chunk exceeds 255 bytes");
            }
            output.write(data.length);
            output.writeBytes(data);
        }
        return raw(DnsRecordType.TXT, output.toByteArray());
    }

    /**
     * Creates MX record data.
     *
     * @param preference mail exchanger preference
     * @param exchange   mail exchanger name
     * @return immutable MX record data
     */
    static DnsRecordData mx(final int preference, final String exchange) {
        return fromRecord(DnsRecord.mx(DnsName.ROOT, preference, exchange, 0L));
    }

    /**
     * Creates SRV record data.
     *
     * @param priority service priority
     * @param weight   service weight
     * @param port     service port
     * @param target   service target host
     * @return immutable SRV record data
     */
    static DnsRecordData srv(final int priority, final int weight, final int port, final String target) {
        return fromRecord(DnsRecord.srv(DnsName.ROOT, priority, weight, port, target, 0L));
    }

    /**
     * Creates CAA record data.
     *
     * @param flags CAA flags byte
     * @param tag   property tag
     * @param value property value
     * @return immutable CAA record data
     */
    static DnsRecordData caa(final int flags, final String tag, final String value) {
        return fromRecord(DnsRecord.caa(DnsName.ROOT, flags, tag, value, 0L));
    }

    /**
     * Creates SSHFP record data.
     *
     * @param algorithm       SSH public key algorithm code
     * @param fingerprintType fingerprint hash algorithm code
     * @param fingerprint     fingerprint bytes
     * @return immutable SSHFP record data
     */
    static DnsRecordData sshfp(final int algorithm, final int fingerprintType, final byte[] fingerprint) {
        return fromRecord(DnsRecord.sshfp(DnsName.ROOT, algorithm, fingerprintType, fingerprint, 0L));
    }

    /**
     * Creates TLSA record data.
     *
     * @param usage           certificate usage
     * @param selector        selector code
     * @param matchingType    matching type code
     * @param associationData certificate association data bytes
     * @return immutable TLSA record data
     */
    static DnsRecordData tlsa(
            final int usage,
            final int selector,
            final int matchingType,
            final byte[] associationData) {
        return fromRecord(DnsRecord.tlsa(DnsName.ROOT, usage, selector, matchingType, associationData, 0L));
    }

    /**
     * Creates URI record data.
     *
     * @param priority URI priority
     * @param weight   URI weight
     * @param target   URI target
     * @return immutable URI record data
     */
    static DnsRecordData uri(final int priority, final int weight, final String target) {
        return fromRecord(DnsRecord.uri(DnsName.ROOT, priority, weight, target, 0L));
    }

    /**
     * Creates NAPTR record data.
     *
     * @param order       processing order
     * @param preference  processing preference
     * @param flags       NAPTR flags string
     * @param service     service string
     * @param regexp      regular-expression replacement string
     * @param replacement replacement domain name
     * @return immutable NAPTR record data
     */
    static DnsRecordData naptr(
            final int order,
            final int preference,
            final String flags,
            final String service,
            final String regexp,
            final String replacement) {
        return fromRecord(DnsRecord.naptr(DnsName.ROOT, order, preference, flags, service, regexp, replacement, 0L));
    }

    /**
     * Creates SVCB record data.
     *
     * @param priority   service binding priority
     * @param target     service binding target name
     * @param parameters service binding parameter bytes
     * @return immutable SVCB record data
     */
    static DnsRecordData svcb(final int priority, final String target, final byte[] parameters) {
        return fromRecord(DnsRecord.svcb(DnsName.ROOT, priority, target, parameters, 0L));
    }

    /**
     * Creates HTTPS record data.
     *
     * @param priority   service binding priority
     * @param target     service binding target name
     * @param parameters service binding parameter bytes
     * @return immutable HTTPS record data
     */
    static DnsRecordData https(final int priority, final String target, final byte[] parameters) {
        return fromRecord(DnsRecord.https(DnsName.ROOT, priority, target, parameters, 0L));
    }

    /**
     * Creates SOA record data.
     *
     * @param primary     primary name server
     * @param responsible responsible mailbox name
     * @param serial      zone serial
     * @param refresh     refresh interval
     * @param retry       retry interval
     * @param expire      expire interval
     * @param minimum     negative-cache minimum
     * @return immutable SOA record data
     */
    static DnsRecordData soa(
            final String primary,
            final String responsible,
            final long serial,
            final long refresh,
            final long retry,
            final long expire,
            final long minimum) {
        return fromRecord(
                DnsRecord.soa(DnsName.ROOT, primary, responsible, serial, refresh, retry, expire, minimum, 0L));
    }

    /**
     * Validates and copies IP address bytes.
     *
     * @param address        source address
     * @param expectedLength expected byte length
     * @param type           diagnostic record type
     * @return address bytes
     */
    private static byte[] addressBytes(final InetAddress address, final int expectedLength, final String type) {
        if (address == null) {
            throw new ValidateException(type + " record address must not be null");
        }
        final byte[] data = address.getAddress();
        if (data.length != expectedLength) {
            throw new ValidateException(type + " record address length is invalid");
        }
        return data;
    }

    /**
     * Immutable wire-format record-data implementation.
     *
     * @author Kimi Liu
     */
    class Wire implements DnsRecordData {

        /**
         * Numeric DNS record type.
         */
        private final int typeCode;

        /**
         * Wire-format RDATA bytes.
         */
        private final byte[] wireData;

        /**
         * Creates immutable wire-format record data.
         *
         * @param typeCode unsigned 16-bit DNS record type code
         * @param wireData wire-format RDATA bytes
         */
        public Wire(final int typeCode, final byte[] wireData) {
            this.typeCode = DnsCodec.validateUnsignedShort(typeCode, "DNS record type");
            this.wireData = copyWireData(wireData);
        }

        /**
         * Returns the known DNS record type.
         *
         * @return known record type, or {@link DnsRecordType#UNKNOWN}
         */
        @Override
        public DnsRecordType type() {
            return DnsRecordType.fromCode(typeCode);
        }

        /**
         * Returns the numeric DNS record type.
         *
         * @return unsigned 16-bit DNS record type code
         */
        @Override
        public int typeCode() {
            return typeCode;
        }

        /**
         * Returns a defensive copy of RDATA bytes.
         *
         * @return wire-format RDATA bytes
         */
        @Override
        public byte[] wireData() {
            return Arrays.copyOf(wireData, wireData.length);
        }

        /**
         * Returns whether another value has identical type and RDATA.
         *
         * @param other candidate value
         * @return true when type and RDATA bytes match
         */
        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Wire wire)) {
                return false;
            }
            return typeCode == wire.typeCode && Arrays.equals(wireData, wire.wireData);
        }

        /**
         * Returns a hash code based on type and RDATA.
         *
         * @return hash code
         */
        @Override
        public int hashCode() {
            int result = Integer.hashCode(typeCode);
            result = 31 * result + Arrays.hashCode(wireData);
            return result;
        }

        /**
         * Returns a diagnostic representation without dumping complete RDATA.
         *
         * @return diagnostic text
         */
        @Override
        public String toString() {
            return "DnsRecordData[type=" + type() + ", typeCode=" + typeCode + ", bytes=" + wireData.length + "]";
        }

        /**
         * Copies RDATA bytes after enforcing DNS RDATA size.
         *
         * @param wireData source RDATA
         * @return copied RDATA
         */
        private static byte[] copyWireData(final byte[] wireData) {
            if (wireData == null) {
                throw new ValidateException("DNS record data bytes must not be null");
            }
            if (wireData.length > Normal._65535) {
                throw new ValidateException("DNS record data bytes exceed 65535 bytes");
            }
            return Arrays.copyOf(wireData, wireData.length);
        }

    }

}
