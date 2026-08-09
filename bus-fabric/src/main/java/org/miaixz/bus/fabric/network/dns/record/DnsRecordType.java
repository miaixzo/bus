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

/**
 * DNS resource record type codes used by the server-side resolver.
 *
 * @author Kimi Liu
 */
public enum DnsRecordType {

    /**
     * IPv4 address record.
     */
    A(1),

    /**
     * Authoritative name server record.
     */
    NS(2),

    /**
     * Canonical name alias record.
     */
    CNAME(5),

    /**
     * Start of authority record.
     */
    SOA(6),

    /**
     * Host information record.
     */
    HINFO(13),

    /**
     * Domain name pointer record.
     */
    PTR(12),

    /**
     * Mail exchange record.
     */
    MX(15),

    /**
     * Text record.
     */
    TXT(16),

    /**
     * IPv6 address record.
     */
    AAAA(28),

    /**
     * Service locator record.
     */
    SRV(33),

    /**
     * Delegation name alias record.
     */
    DNAME(39),

    /**
     * Delegation signer record.
     */
    DS(43),

    /**
     * SSH public key fingerprint record.
     */
    SSHFP(44),

    /**
     * DNSSEC public key record.
     */
    DNSKEY(48),

    /**
     * DNSSEC signature record.
     */
    RRSIG(46),

    /**
     * DNSSEC next secure record.
     */
    NSEC(47),

    /**
     * DNSSEC hashed next secure record.
     */
    NSEC3(50),

    /**
     * DNSSEC NSEC3 parameter record.
     */
    NSEC3PARAM(51),

    /**
     * Naming authority pointer record.
     */
    NAPTR(35),

    /**
     * DNS-based TLS authentication record.
     */
    TLSA(52),

    /**
     * Uniform resource identifier record.
     */
    URI(256),

    /**
     * Certification authority authorization record.
     */
    CAA(257),

    /**
     * Service binding record.
     */
    SVCB(64),

    /**
     * HTTPS service binding record.
     */
    HTTPS(65),

    /**
     * Query pseudo type requesting all records.
     */
    ANY(255),

    /**
     * EDNS option pseudo-record.
     */
    OPT(41),

    /**
     * Transaction signature pseudo-record.
     */
    TSIG(250),

    /**
     * Full zone transfer query type.
     */
    AXFR(252),

    /**
     * Incremental zone transfer query type.
     */
    IXFR(251),

    /**
     * Runtime representation for an unrecognized type code.
     */
    UNKNOWN(0);

    /**
     * Numeric DNS type code.
     */
    private final int code;

    /**
     * Creates a DNS record type constant.
     *
     * @param code numeric DNS type code
     */
    DnsRecordType(final int code) {
        this.code = code;
    }

    /**
     * Returns the numeric DNS type code.
     *
     * @return unsigned 16-bit DNS type code
     */
    public int code() {
        return code;
    }

    /**
     * Resolves a type constant from a wire type code.
     *
     * @param code unsigned 16-bit DNS type code
     * @return matching type constant, or {@link #UNKNOWN}
     */
    public static DnsRecordType fromCode(final int code) {
        for (final DnsRecordType type : values()) {
            if (type.code == code && type != UNKNOWN) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
