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
package org.miaixz.bus.fabric.network.dns.zone;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.dnssec.DnsSigningKey;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordData;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * Immutable DNSSEC trust anchor supplied by the external control project.
 *
 * <p>
 * A trust anchor carries either DS or DNSKEY RDATA for a canonical owner name. The value is passive configuration: it
 * owns no resources, performs no IO, and is safe to share between snapshot refresh and DNS query threads.
 * </p>
 *
 * @author Kimi Liu
 */
public class DnsTrustAnchor {

    /**
     * DNSKEY RDATA byte length before the public key.
     */
    private static final int DNSKEY_FIXED_BYTES = 4;

    /**
     * DS RDATA byte length before the digest.
     */
    private static final int DS_FIXED_BYTES = 4;

    /**
     * Canonical trust-anchor owner name.
     */
    private final String name;

    /**
     * DS or DNSKEY record data.
     */
    private final DnsRecordData data;

    /**
     * Creates a DNSSEC trust anchor.
     *
     * @param name owner name
     * @param data DS or DNSKEY record data
     */
    public DnsTrustAnchor(final String name, final DnsRecordData data) {
        this.name = DnsName.normalize(name);
        this.data = validateData(data);
    }

    /**
     * Creates a DS trust anchor.
     *
     * @param name  owner name
     * @param rdata DS RDATA bytes
     * @return immutable trust anchor
     */
    public static DnsTrustAnchor ds(final String name, final byte[] rdata) {
        return new DnsTrustAnchor(name, DnsRecordData.raw(DnsRecordType.DS, rdata));
    }

    /**
     * Creates a DNSKEY trust anchor.
     *
     * @param name  owner name
     * @param rdata DNSKEY RDATA bytes
     * @return immutable trust anchor
     */
    public static DnsTrustAnchor dnskey(final String name, final byte[] rdata) {
        return new DnsTrustAnchor(name, DnsRecordData.raw(DnsRecordType.DNSKEY, rdata));
    }

    /**
     * Creates a trust anchor from a DS or DNSKEY record.
     *
     * @param record source DNS record
     * @return immutable trust anchor
     */
    public static DnsTrustAnchor fromRecord(final DnsRecord record) {
        if (record == null) {
            throw new ValidateException("DNS trust anchor record must not be null");
        }
        return new DnsTrustAnchor(record.name(), DnsRecordData.fromRecord(record));
    }

    /**
     * Returns the trust-anchor owner name.
     *
     * @return canonical owner name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the trust-anchor record type.
     *
     * @return DS or DNSKEY
     */
    public DnsRecordType type() {
        return data.type();
    }

    /**
     * Returns the numeric trust-anchor record type.
     *
     * @return unsigned 16-bit type code
     */
    public int typeCode() {
        return data.typeCode();
    }

    /**
     * Returns the trust-anchor record data.
     *
     * @return immutable DS or DNSKEY record data
     */
    public DnsRecordData data() {
        return data;
    }

    /**
     * Converts this trust anchor into a DNS record.
     *
     * @param ttl unsigned 32-bit TTL
     * @return immutable DNS record
     */
    public DnsRecord toRecord(final long ttl) {
        return data.toRecord(name, ttl);
    }

    /**
     * Returns the DNSSEC key tag.
     *
     * @return unsigned 16-bit key tag
     */
    public int keyTag() {
        final byte[] rdata = data.wireData();
        if (data.type() == DnsRecordType.DS) {
            return DnsCodec.readUnsignedShort(rdata, 0);
        }
        return DnsSigningKey.keyTag(rdata);
    }

    /**
     * Returns the DNSSEC algorithm code.
     *
     * @return unsigned 8-bit algorithm code
     */
    public int algorithm() {
        final byte[] rdata = data.wireData();
        return data.type() == DnsRecordType.DS ? DnsCodec.readUnsignedByte(rdata, 2)
                : DnsCodec.readUnsignedByte(rdata, 3);
    }

    /**
     * Returns the DS digest type, or {@code -1} for DNSKEY anchors.
     *
     * @return unsigned 8-bit digest type, or {@code -1}
     */
    public int digestType() {
        if (data.type() != DnsRecordType.DS) {
            return -1;
        }
        return DnsCodec.readUnsignedByte(data.wireData(), 3);
    }

    /**
     * Validates trust-anchor record data.
     *
     * @param data candidate data
     * @return validated data
     */
    private static DnsRecordData validateData(final DnsRecordData data) {
        if (data == null) {
            throw new ValidateException("DNS trust anchor data must not be null");
        }
        final byte[] rdata = data.wireData();
        if (data.type() == DnsRecordType.DS) {
            if (rdata.length <= DS_FIXED_BYTES) {
                throw new ValidateException("DNS DS trust anchor must contain a digest");
            }
            return data;
        }
        if (data.type() == DnsRecordType.DNSKEY) {
            if (rdata.length <= DNSKEY_FIXED_BYTES) {
                throw new ValidateException("DNSKEY trust anchor must contain a public key");
            }
            return data;
        }
        throw new ValidateException("DNS trust anchor must be a DS or DNSKEY record");
    }

}
