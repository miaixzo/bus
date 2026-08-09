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
package org.miaixz.bus.fabric.network.dns.message;

import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;

/**
 * Decoded DNS response produced from a wire-format upstream or recursive reply.
 *
 * <p>
 * This type is immutable and side-effect free. It does not own network resources and can be shared between resolver
 * stages after construction. It is intentionally located in a non-exported package so public DNS Server API remains
 * small while recursive, DNSSEC, cache, and forwarding internals can inspect response sections without external DNS
 * libraries.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DnsDecodedResponse {

    /**
     * DNS message identifier.
     */
    private final int id;

    /**
     * Response code.
     */
    private final DnsResponseCode responseCode;

    /**
     * Authoritative-answer flag.
     */
    private final boolean authoritative;

    /**
     * Recursion-available flag.
     */
    private final boolean recursionAvailable;

    /**
     * Truncated-response flag.
     */
    private final boolean truncated;

    /**
     * Authentic-data flag.
     */
    private final boolean authenticData;

    /**
     * Checking-disabled flag.
     */
    private final boolean checkingDisabled;

    /**
     * Response question.
     */
    private final DnsQuestion question;

    /**
     * Answer section records.
     */
    private final List<DnsRecord> answers;

    /**
     * Authority section records.
     */
    private final List<DnsRecord> authorities;

    /**
     * Additional section records excluding the EDNS OPT pseudo-record.
     */
    private final List<DnsRecord> additionals;

    /**
     * Creates a decoded DNS response.
     *
     * @param id                 DNS message identifier
     * @param responseCode       response code
     * @param authoritative      authoritative-answer flag
     * @param recursionAvailable recursion-available flag
     * @param truncated          truncated-response flag
     * @param authenticData      authentic-data flag
     * @param checkingDisabled   checking-disabled flag
     * @param question           decoded response question
     * @param answers            answer records
     * @param authorities        authority records
     * @param additionals        additional records excluding EDNS OPT
     */
    public DnsDecodedResponse(final int id, final DnsResponseCode responseCode, final boolean authoritative,
            final boolean recursionAvailable, final boolean truncated, final boolean authenticData,
            final boolean checkingDisabled, final DnsQuestion question, final List<DnsRecord> answers,
            final List<DnsRecord> authorities, final List<DnsRecord> additionals) {
        DnsCodec.validateUnsignedShort(id, "DNS response id");
        if (responseCode == null) {
            throw new ValidateException("DNS response code must not be null");
        }
        if (question == null) {
            throw new ValidateException("DNS response question must not be null");
        }
        this.id = id;
        this.responseCode = responseCode;
        this.authoritative = authoritative;
        this.recursionAvailable = recursionAvailable;
        this.truncated = truncated;
        this.authenticData = authenticData;
        this.checkingDisabled = checkingDisabled;
        this.question = question;
        this.answers = immutableRecords(answers, "answer");
        this.authorities = immutableRecords(authorities, "authority");
        this.additionals = immutableRecords(additionals, "additional");
    }

    /**
     * Returns the DNS message identifier.
     *
     * @return unsigned 16-bit identifier
     */
    public int id() {
        return id;
    }

    /**
     * Returns the response code.
     *
     * @return DNS response code
     */
    public DnsResponseCode responseCode() {
        return responseCode;
    }

    /**
     * Returns whether the authoritative-answer flag is set.
     *
     * @return true when AA is set
     */
    public boolean authoritative() {
        return authoritative;
    }

    /**
     * Returns whether the recursion-available flag is set.
     *
     * @return true when RA is set
     */
    public boolean recursionAvailable() {
        return recursionAvailable;
    }

    /**
     * Returns whether the truncated-response flag is set.
     *
     * @return true when TC is set
     */
    public boolean truncated() {
        return truncated;
    }

    /**
     * Returns whether the authentic-data flag is set.
     *
     * @return true when AD is set
     */
    public boolean authenticData() {
        return authenticData;
    }

    /**
     * Returns whether the checking-disabled flag is set.
     *
     * @return true when CD is set
     */
    public boolean checkingDisabled() {
        return checkingDisabled;
    }

    /**
     * Returns the decoded question.
     *
     * @return response question
     */
    public DnsQuestion question() {
        return question;
    }

    /**
     * Returns answer records.
     *
     * @return immutable answer records
     */
    public List<DnsRecord> answers() {
        return answers;
    }

    /**
     * Returns authority records.
     *
     * @return immutable authority records
     */
    public List<DnsRecord> authorities() {
        return authorities;
    }

    /**
     * Returns additional records excluding the EDNS OPT pseudo-record.
     *
     * @return immutable additional records
     */
    public List<DnsRecord> additionals() {
        return additionals;
    }

    /**
     * Converts this decoded response into the server response model for a caller-owned query.
     *
     * @param query original query that should be echoed in the encoded response
     * @return response model using this decoded response's flags and sections
     */
    public DnsResponse toResponse(final DnsQuery query) {
        return new DnsResponse(query, responseCode, authoritative, recursionAvailable, truncated, answers, authorities,
                additionals, authenticData && !query.checkingDisabled(), null);
    }

    /**
     * Returns the DNS cache TTL carried by this response.
     *
     * <p>
     * The value is derived from DNS resource-record TTL values rather than from server configuration. Positive
     * responses use the minimum TTL across all returned records so related additional data cannot outlive the answer.
     * Negative responses use the SOA negative-cache TTL from the authority section when present. Responses that should
     * not be cached, such as SERVFAIL, return zero.
     * </p>
     *
     * @return cache TTL in seconds, or zero when the response must not be cached
     */
    public long cacheTtlSeconds() {
        if (responseCode != DnsResponseCode.NOERROR && responseCode != DnsResponseCode.NXDOMAIN) {
            return 0L;
        }
        final long ttlSeconds = answers.isEmpty() ? negativeCacheTtlSeconds() : minimumTtl(Long.MAX_VALUE, answers);
        final long withAdditionals = minimumTtl(ttlSeconds, additionals);
        return withAdditionals == Long.MAX_VALUE ? 0L : withAdditionals;
    }

    /**
     * Returns the negative-cache TTL for a response without answer records.
     *
     * @return negative-cache TTL in seconds, or {@link Long#MAX_VALUE} when no TTL is available
     */
    private long negativeCacheTtlSeconds() {
        long ttlSeconds = Long.MAX_VALUE;
        for (final DnsRecord record : authorities) {
            if (record.typeCode() == org.miaixz.bus.fabric.network.dns.record.DnsRecordType.SOA.code()) {
                ttlSeconds = Math.min(ttlSeconds, soaNegativeTtl(record));
            } else {
                ttlSeconds = Math.min(ttlSeconds, record.ttl());
            }
        }
        return ttlSeconds;
    }

    /**
     * Returns the negative-cache TTL represented by an SOA record.
     *
     * @param record SOA record
     * @return minimum of the SOA record TTL and SOA minimum field
     */
    private static long soaNegativeTtl(final DnsRecord record) {
        final byte[] data = record.wireData();
        final DnsName.ReadResult primary = DnsName.read(data, 0);
        final DnsName.ReadResult responsible = DnsName.read(data, primary.nextOffset());
        if (responsible.nextOffset() + 20 > data.length) {
            return record.ttl();
        }
        return Math.min(record.ttl(), DnsCodec.readUnsignedInt(data, responsible.nextOffset() + 16));
    }

    /**
     * Returns the minimum TTL across a record list.
     *
     * @param current current minimum TTL
     * @param records records being inspected
     * @return updated minimum TTL
     */
    private static long minimumTtl(final long current, final List<DnsRecord> records) {
        long ttlSeconds = current;
        for (final DnsRecord record : records) {
            ttlSeconds = Math.min(ttlSeconds, record.ttl());
        }
        return ttlSeconds;
    }

    /**
     * Validates and copies a record list.
     *
     * @param records source records
     * @param section diagnostic section name
     * @return immutable record list
     */
    private static List<DnsRecord> immutableRecords(final List<DnsRecord> records, final String section) {
        if (records == null) {
            throw new ValidateException("DNS decoded " + section + " records must not be null");
        }
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNS decoded " + section + " records must not contain null");
            }
        }
        return List.copyOf(records);
    }

}
