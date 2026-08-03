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
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;

/**
 * Immutable DNS resource record with pre-encoded RDATA bytes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsRecord {

    /**
     * Internet DNS class code.
     */
    public static final int CLASS_IN = 1;

    /**
     * Owner name.
     */
    private final String name;

    /**
     * Numeric DNS type code.
     */
    private final int typeCode;

    /**
     * Numeric DNS class code.
     */
    private final int recordClass;

    /**
     * Unsigned TTL represented as a Java long.
     */
    private final long ttl;

    /**
     * Immutable RDATA wire bytes.
     */
    private final byte[] wireData;

    /**
     * Creates a resource record.
     *
     * @param name        owner name
     * @param typeCode    unsigned 16-bit DNS type code
     * @param recordClass unsigned 16-bit DNS class code
     * @param ttl         unsigned 32-bit TTL
     * @param wireData    RDATA wire bytes
     */
    private DnsRecord(final String name, final int typeCode, final int recordClass, final long ttl,
            final byte[] wireData) {
        this.name = DnsName.normalize(name);
        this.typeCode = DnsCodec.validateUnsignedShort(typeCode, "DNS type");
        this.recordClass = DnsCodec.validateUnsignedShort(recordClass, "DNS class");
        this.ttl = DnsCodec.validateUnsignedInt(ttl, "DNS ttl");
        this.wireData = copyWireData(wireData);
    }

    /**
     * Creates a raw record.
     *
     * @param name        owner name
     * @param typeCode    unsigned 16-bit DNS type code
     * @param recordClass unsigned 16-bit DNS class code
     * @param ttl         unsigned 32-bit TTL
     * @param wireData    RDATA wire bytes
     * @return immutable resource record
     */
    public static DnsRecord raw(
            final String name,
            final int typeCode,
            final int recordClass,
            final long ttl,
            final byte[] wireData) {
        return new DnsRecord(name, typeCode, recordClass, ttl, wireData);
    }

    /**
     * Creates an IPv4 A record.
     *
     * @param name    owner name
     * @param address IPv4 address
     * @param ttl     unsigned 32-bit TTL
     * @return immutable A record
     */
    public static DnsRecord a(final String name, final InetAddress address, final long ttl) {
        final byte[] bytes = addressBytes(address, 4, "A");
        return new DnsRecord(name, DnsRecordType.A.code(), CLASS_IN, ttl, bytes);
    }

    /**
     * Creates an IPv6 AAAA record.
     *
     * @param name    owner name
     * @param address IPv6 address
     * @param ttl     unsigned 32-bit TTL
     * @return immutable AAAA record
     */
    public static DnsRecord aaaa(final String name, final InetAddress address, final long ttl) {
        final byte[] bytes = addressBytes(address, 16, "AAAA");
        return new DnsRecord(name, DnsRecordType.AAAA.code(), CLASS_IN, ttl, bytes);
    }

    /**
     * Creates a CNAME record.
     *
     * @param name   owner name
     * @param target canonical target name
     * @param ttl    unsigned 32-bit TTL
     * @return immutable CNAME record
     */
    public static DnsRecord cname(final String name, final String target, final long ttl) {
        return nameRecord(name, DnsRecordType.CNAME, target, ttl);
    }

    /**
     * Creates a DNAME record.
     *
     * @param name   owner name
     * @param target delegated target suffix
     * @param ttl    unsigned 32-bit TTL
     * @return immutable DNAME record
     */
    public static DnsRecord dname(final String name, final String target, final long ttl) {
        return nameRecord(name, DnsRecordType.DNAME, target, ttl);
    }

    /**
     * Creates an NS record.
     *
     * @param name   owner name
     * @param target authoritative name server name
     * @param ttl    unsigned 32-bit TTL
     * @return immutable NS record
     */
    public static DnsRecord ns(final String name, final String target, final long ttl) {
        return nameRecord(name, DnsRecordType.NS, target, ttl);
    }

    /**
     * Creates a PTR record.
     *
     * @param name   owner name
     * @param target pointer target name
     * @param ttl    unsigned 32-bit TTL
     * @return immutable PTR record
     */
    public static DnsRecord ptr(final String name, final String target, final long ttl) {
        return nameRecord(name, DnsRecordType.PTR, target, ttl);
    }

    /**
     * Creates an HINFO record.
     *
     * @param name owner name
     * @param cpu  CPU description string
     * @param os   operating-system description string
     * @param ttl  unsigned 32-bit TTL
     * @return immutable HINFO record
     */
    public static DnsRecord hinfo(final String name, final String cpu, final String os, final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        writeCharacterString(bytes, cpu, "HINFO cpu");
        writeCharacterString(bytes, os, "HINFO os");
        return new DnsRecord(name, DnsRecordType.HINFO.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates a TXT record.
     *
     * @param name  owner name
     * @param texts TXT chunks
     * @param ttl   unsigned 32-bit TTL
     * @return immutable TXT record
     */
    public static DnsRecord txt(final String name, final List<String> texts, final long ttl) {
        if (texts == null || texts.isEmpty()) {
            throw new ValidateException("TXT record must contain at least one chunk");
        }
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (final String text : texts) {
            if (text == null) {
                throw new ValidateException("TXT chunk must not be null");
            }
            final byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            if (bytes.length > 255) {
                throw new ValidateException("TXT chunk exceeds 255 bytes");
            }
            output.write(bytes.length);
            output.writeBytes(bytes);
        }
        return new DnsRecord(name, DnsRecordType.TXT.code(), CLASS_IN, ttl, output.toByteArray());
    }

    /**
     * Creates an MX record.
     *
     * @param name       owner name
     * @param preference mail exchanger preference
     * @param exchange   mail exchanger name
     * @param ttl        unsigned 32-bit TTL
     * @return immutable MX record
     */
    public static DnsRecord mx(final String name, final int preference, final String exchange, final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeShort(DnsCodec.validateUnsignedShort(preference, "MX preference"));
            DnsName.write(output, exchange);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode MX record", e);
        }
        return new DnsRecord(name, DnsRecordType.MX.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates an SRV record.
     *
     * @param name     owner name
     * @param priority service priority
     * @param weight   service weight
     * @param port     service port
     * @param target   service target host
     * @param ttl      unsigned 32-bit TTL
     * @return immutable SRV record
     */
    public static DnsRecord srv(
            final String name,
            final int priority,
            final int weight,
            final int port,
            final String target,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeShort(DnsCodec.validateUnsignedShort(priority, "SRV priority"));
            output.writeShort(DnsCodec.validateUnsignedShort(weight, "SRV weight"));
            output.writeShort(DnsCodec.validateUnsignedShort(port, "SRV port"));
            DnsName.write(output, target);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode SRV record", e);
        }
        return new DnsRecord(name, DnsRecordType.SRV.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates a CAA record.
     *
     * @param name  owner name
     * @param flags CAA flags byte
     * @param tag   property tag
     * @param value property value
     * @param ttl   unsigned 32-bit TTL
     * @return immutable CAA record
     */
    public static DnsRecord caa(
            final String name,
            final int flags,
            final String tag,
            final String value,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(DnsCodec.validateUnsignedByte(flags, "CAA flags"));
        writeCharacterString(bytes, tag, "CAA tag");
        final byte[] valueBytes = valueBytes(value, "CAA value");
        bytes.writeBytes(valueBytes);
        return new DnsRecord(name, DnsRecordType.CAA.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates an SSHFP record.
     *
     * @param name            owner name
     * @param algorithm       SSH public key algorithm code
     * @param fingerprintType fingerprint hash algorithm code
     * @param fingerprint     fingerprint bytes
     * @param ttl             unsigned 32-bit TTL
     * @return immutable SSHFP record
     */
    public static DnsRecord sshfp(
            final String name,
            final int algorithm,
            final int fingerprintType,
            final byte[] fingerprint,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(DnsCodec.validateUnsignedByte(algorithm, "SSHFP algorithm"));
        bytes.write(DnsCodec.validateUnsignedByte(fingerprintType, "SSHFP fingerprint type"));
        bytes.writeBytes(copyWireData(fingerprint));
        return new DnsRecord(name, DnsRecordType.SSHFP.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates a TLSA record.
     *
     * @param name            owner name
     * @param usage           certificate usage
     * @param selector        selector code
     * @param matchingType    matching type code
     * @param associationData certificate association data bytes
     * @param ttl             unsigned 32-bit TTL
     * @return immutable TLSA record
     */
    public static DnsRecord tlsa(
            final String name,
            final int usage,
            final int selector,
            final int matchingType,
            final byte[] associationData,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(DnsCodec.validateUnsignedByte(usage, "TLSA certificate usage"));
        bytes.write(DnsCodec.validateUnsignedByte(selector, "TLSA selector"));
        bytes.write(DnsCodec.validateUnsignedByte(matchingType, "TLSA matching type"));
        bytes.writeBytes(copyWireData(associationData));
        return new DnsRecord(name, DnsRecordType.TLSA.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates a URI record.
     *
     * @param name     owner name
     * @param priority URI priority
     * @param weight   URI weight
     * @param target   URI target
     * @param ttl      unsigned 32-bit TTL
     * @return immutable URI record
     */
    public static DnsRecord uri(
            final String name,
            final int priority,
            final int weight,
            final String target,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeShort(DnsCodec.validateUnsignedShort(priority, "URI priority"));
            output.writeShort(DnsCodec.validateUnsignedShort(weight, "URI weight"));
            output.write(valueBytes(target, "URI target"));
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode URI record", e);
        }
        return new DnsRecord(name, DnsRecordType.URI.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates a NAPTR record.
     *
     * @param name        owner name
     * @param order       processing order
     * @param preference  processing preference
     * @param flags       NAPTR flags string
     * @param service     service string
     * @param regexp      regular-expression replacement string
     * @param replacement replacement domain name
     * @param ttl         unsigned 32-bit TTL
     * @return immutable NAPTR record
     */
    public static DnsRecord naptr(
            final String name,
            final int order,
            final int preference,
            final String flags,
            final String service,
            final String regexp,
            final String replacement,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeShort(DnsCodec.validateUnsignedShort(order, "NAPTR order"));
            output.writeShort(DnsCodec.validateUnsignedShort(preference, "NAPTR preference"));
            writeCharacterString(output, flags, "NAPTR flags");
            writeCharacterString(output, service, "NAPTR service");
            writeCharacterString(output, regexp, "NAPTR regexp");
            DnsName.write(output, replacement);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode NAPTR record", e);
        }
        return new DnsRecord(name, DnsRecordType.NAPTR.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Creates an SVCB record.
     *
     * @param name       owner name
     * @param priority   service binding priority
     * @param target     service binding target name
     * @param parameters service binding parameter bytes
     * @param ttl        unsigned 32-bit TTL
     * @return immutable SVCB record
     */
    public static DnsRecord svcb(
            final String name,
            final int priority,
            final String target,
            final byte[] parameters,
            final long ttl) {
        return serviceBinding(name, DnsRecordType.SVCB, priority, target, parameters, ttl);
    }

    /**
     * Creates an HTTPS record.
     *
     * @param name       owner name
     * @param priority   service binding priority
     * @param target     service binding target name
     * @param parameters service binding parameter bytes
     * @param ttl        unsigned 32-bit TTL
     * @return immutable HTTPS record
     */
    public static DnsRecord https(
            final String name,
            final int priority,
            final String target,
            final byte[] parameters,
            final long ttl) {
        return serviceBinding(name, DnsRecordType.HTTPS, priority, target, parameters, ttl);
    }

    /**
     * Creates an SOA record.
     *
     * @param name        owner name
     * @param primary     primary name server
     * @param responsible responsible mailbox name
     * @param serial      zone serial
     * @param refresh     refresh interval
     * @param retry       retry interval
     * @param expire      expire interval
     * @param minimum     negative-cache minimum
     * @param ttl         unsigned 32-bit TTL
     * @return immutable SOA record
     */
    public static DnsRecord soa(
            final String name,
            final String primary,
            final String responsible,
            final long serial,
            final long refresh,
            final long retry,
            final long expire,
            final long minimum,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            DnsName.write(output, primary);
            DnsName.write(output, responsible);
            output.writeInt((int) DnsCodec.validateUnsignedInt(serial, "SOA serial"));
            output.writeInt((int) DnsCodec.validateUnsignedInt(refresh, "SOA refresh"));
            output.writeInt((int) DnsCodec.validateUnsignedInt(retry, "SOA retry"));
            output.writeInt((int) DnsCodec.validateUnsignedInt(expire, "SOA expire"));
            output.writeInt((int) DnsCodec.validateUnsignedInt(minimum, "SOA minimum"));
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode SOA record", e);
        }
        return new DnsRecord(name, DnsRecordType.SOA.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Returns the owner name.
     *
     * @return canonical owner name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the record type.
     *
     * @return known record type, or {@link DnsRecordType#UNKNOWN}
     */
    public DnsRecordType type() {
        return DnsRecordType.fromCode(typeCode);
    }

    /**
     * Returns the numeric type code.
     *
     * @return unsigned 16-bit DNS type code
     */
    public int typeCode() {
        return typeCode;
    }

    /**
     * Returns the record class code.
     *
     * @return unsigned 16-bit DNS class code
     */
    public int recordClass() {
        return recordClass;
    }

    /**
     * Returns the record TTL.
     *
     * @return unsigned 32-bit TTL represented as a Java long
     */
    public long ttl() {
        return ttl;
    }

    /**
     * Returns a defensive copy of the RDATA bytes.
     *
     * @return RDATA wire bytes
     */
    public byte[] wireData() {
        return Arrays.copyOf(wireData, wireData.length);
    }

    /**
     * Returns a copy of this record using another owner name.
     *
     * @param owner replacement owner name
     * @return record with the replacement owner and identical type, class, TTL, and RDATA
     */
    public DnsRecord withName(final String owner) {
        return new DnsRecord(owner, typeCode, recordClass, ttl, wireData);
    }

    /**
     * Decodes this record's RDATA as one DNS name.
     *
     * @return decoded target name
     * @throws ProtocolException if RDATA is not exactly one DNS name
     */
    public String targetName() {
        final DnsName.ReadResult result = DnsName.read(wireData, 0);
        if (result.nextOffset() != wireData.length) {
            throw new ProtocolException("DNS record RDATA contains trailing data after target name");
        }
        return result.name();
    }

    /**
     * Returns whether this record matches a query type and class.
     *
     * @param queryTypeCode  query type code
     * @param queryClassCode query class code
     * @return true when the class matches and the type is exact or ANY
     */
    public boolean matches(final int queryTypeCode, final int queryClassCode) {
        return recordClass == queryClassCode
                && (typeCode == queryTypeCode || queryTypeCode == DnsRecordType.ANY.code());
    }

    /**
     * Creates a single-name RDATA record.
     *
     * @param name   owner name
     * @param type   record type
     * @param target target name encoded as RDATA
     * @param ttl    unsigned 32-bit TTL
     * @return immutable resource record
     */
    private static DnsRecord nameRecord(
            final String name,
            final DnsRecordType type,
            final String target,
            final long ttl) {
        return new DnsRecord(name, type.code(), CLASS_IN, ttl, DnsName.wire(target));
    }

    /**
     * Creates an SVCB-family service binding record.
     *
     * @param name       owner name
     * @param type       service binding record type
     * @param priority   service binding priority
     * @param target     target name
     * @param parameters service binding parameter bytes
     * @param ttl        unsigned 32-bit TTL
     * @return immutable service binding record
     */
    private static DnsRecord serviceBinding(
            final String name,
            final DnsRecordType type,
            final int priority,
            final String target,
            final byte[] parameters,
            final long ttl) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeShort(DnsCodec.validateUnsignedShort(priority, type + " priority"));
            DnsName.write(output, target);
            output.write(copyWireData(parameters));
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode " + type + " record", e);
        }
        return new DnsRecord(name, type.code(), CLASS_IN, ttl, bytes.toByteArray());
    }

    /**
     * Returns validated IP address bytes.
     *
     * @param address        source address
     * @param expectedLength required byte length
     * @param type           record type name for diagnostics
     * @return immutable address bytes
     */
    private static byte[] addressBytes(final InetAddress address, final int expectedLength, final String type) {
        if (address == null) {
            throw new ValidateException(type + " record address must not be null");
        }
        final byte[] bytes = address.getAddress();
        if (bytes.length != expectedLength) {
            throw new ValidateException(type + " record address length is invalid");
        }
        return bytes;
    }

    /**
     * Returns a safe copy of RDATA bytes.
     *
     * @param wireData source RDATA bytes
     * @return copied RDATA bytes
     */
    private static byte[] copyWireData(final byte[] wireData) {
        if (wireData == null) {
            throw new ValidateException("DNS record wire data must not be null");
        }
        if (wireData.length > Normal._65535) {
            throw new ValidateException("DNS record wire data exceeds 65535 bytes");
        }
        return Arrays.copyOf(wireData, wireData.length);
    }

    /**
     * Encodes one DNS character string.
     *
     * @param output target byte stream
     * @param value  source text
     * @param name   diagnostic name
     */
    private static void writeCharacterString(
            final ByteArrayOutputStream output,
            final String value,
            final String name) {
        final byte[] bytes = valueBytes(value, name);
        if (bytes.length > 255) {
            throw new ValidateException(name + " exceeds 255 bytes");
        }
        output.write(bytes.length);
        output.writeBytes(bytes);
    }

    /**
     * Encodes one DNS character string into a data stream.
     *
     * @param output target data stream
     * @param value  source text
     * @param name   diagnostic name
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeCharacterString(final DataOutputStream output, final String value, final String name)
            throws IOException {
        final byte[] bytes = valueBytes(value, name);
        if (bytes.length > 255) {
            throw new ValidateException(name + " exceeds 255 bytes");
        }
        output.writeByte(bytes.length);
        output.write(bytes);
    }

    /**
     * Returns a UTF-8 byte representation of a record text field.
     *
     * @param value source text
     * @param name  diagnostic name
     * @return UTF-8 bytes
     */
    private static byte[] valueBytes(final String value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

}
