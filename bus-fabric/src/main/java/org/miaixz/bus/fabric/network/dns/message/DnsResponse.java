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
 * DNS response model used by the wire encoder.
 *
 * @author Kimi Liu
 */
public class DnsResponse {

    /**
     * Original query.
     */
    private final DnsQuery query;

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
     * Answer section records.
     */
    private final List<DnsRecord> answers;

    /**
     * Authority section records.
     */
    private final List<DnsRecord> authorities;

    /**
     * Additional section records.
     */
    private final List<DnsRecord> additionals;

    /**
     * EDNS Extended DNS Error, or {@code null} when absent.
     */
    private final DnsExtendedError extendedError;

    /**
     * Creates a DNS response.
     *
     * @param query              original query
     * @param responseCode       response code
     * @param authoritative      authoritative-answer flag
     * @param recursionAvailable recursion-available flag
     * @param truncated          truncated-response flag
     * @param answers            answer records
     * @param authorities        authority records
     * @param additionals        additional records
     */
    public DnsResponse(final DnsQuery query, final DnsResponseCode responseCode, final boolean authoritative,
            final boolean recursionAvailable, final boolean truncated, final List<DnsRecord> answers,
            final List<DnsRecord> authorities, final List<DnsRecord> additionals) {
        this(query, responseCode, authoritative, recursionAvailable, truncated, answers, authorities, additionals,
                null);
    }

    /**
     * Creates a DNS response.
     *
     * @param query              original query
     * @param responseCode       response code
     * @param authoritative      authoritative-answer flag
     * @param recursionAvailable recursion-available flag
     * @param truncated          truncated-response flag
     * @param answers            answer records
     * @param authorities        authority records
     * @param additionals        additional records
     * @param extendedError      EDNS Extended DNS Error, or {@code null} when absent
     */
    public DnsResponse(final DnsQuery query, final DnsResponseCode responseCode, final boolean authoritative,
            final boolean recursionAvailable, final boolean truncated, final List<DnsRecord> answers,
            final List<DnsRecord> authorities, final List<DnsRecord> additionals,
            final DnsExtendedError extendedError) {
        this(query, responseCode, authoritative, recursionAvailable, truncated, answers, authorities, additionals,
                false, extendedError);
    }

    /**
     * Creates a DNS response.
     *
     * @param query              original query
     * @param responseCode       response code
     * @param authoritative      authoritative-answer flag
     * @param recursionAvailable recursion-available flag
     * @param truncated          truncated-response flag
     * @param answers            answer records
     * @param authorities        authority records
     * @param additionals        additional records
     * @param authenticData      authentic-data flag
     * @param extendedError      EDNS Extended DNS Error, or {@code null} when absent
     */
    public DnsResponse(final DnsQuery query, final DnsResponseCode responseCode, final boolean authoritative,
            final boolean recursionAvailable, final boolean truncated, final List<DnsRecord> answers,
            final List<DnsRecord> authorities, final List<DnsRecord> additionals, final boolean authenticData,
            final DnsExtendedError extendedError) {
        if (query == null) {
            throw new ValidateException("DNS response query must not be null");
        }
        if (responseCode == null) {
            throw new ValidateException("DNS response code must not be null");
        }
        this.query = query;
        this.responseCode = responseCode;
        this.authoritative = authoritative;
        this.recursionAvailable = recursionAvailable;
        this.truncated = truncated;
        this.authenticData = authenticData;
        this.answers = immutableRecords(answers, "answer");
        this.authorities = immutableRecords(authorities, "authority");
        this.additionals = immutableRecords(additionals, "additional");
        this.extendedError = extendedError;
    }

    /**
     * Creates a successful authoritative response.
     *
     * @param query   original query
     * @param answers answer records
     * @return DNS response
     */
    public static DnsResponse authoritative(final DnsQuery query, final List<DnsRecord> answers) {
        return new DnsResponse(query, DnsResponseCode.NOERROR, true, false, false, answers, List.of(), List.of());
    }

    /**
     * Creates a response with a response code and no records.
     *
     * @param query         original query
     * @param responseCode  response code
     * @param authoritative authoritative-answer flag
     * @return DNS response
     */
    public static DnsResponse empty(
            final DnsQuery query,
            final DnsResponseCode responseCode,
            final boolean authoritative) {
        return new DnsResponse(query, responseCode, authoritative, false, false, List.of(), List.of(), List.of());
    }

    /**
     * Returns a copy marked as truncated.
     *
     * @return truncated response retaining all sections for TCP re-encoding
     */
    public DnsResponse truncated() {
        return new DnsResponse(query, responseCode, authoritative, recursionAvailable, true, answers, authorities,
                additionals, authenticData, extendedError);
    }

    /**
     * Returns the original query.
     *
     * @return decoded query
     */
    public DnsQuery query() {
        return query;
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
     * Returns whether the response is authoritative.
     *
     * @return true when AA is set
     */
    public boolean authoritative() {
        return authoritative;
    }

    /**
     * Returns whether recursion is available.
     *
     * @return true when RA is set
     */
    public boolean recursionAvailable() {
        return recursionAvailable;
    }

    /**
     * Returns whether the response is truncated.
     *
     * @return true when TC is set
     */
    public boolean truncatedFlag() {
        return truncated;
    }

    /**
     * Returns whether the response carries authenticated data.
     *
     * @return true when AD is set
     */
    public boolean authenticData() {
        return authenticData;
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
     * Returns additional records.
     *
     * @return immutable additional records
     */
    public List<DnsRecord> additionals() {
        return additionals;
    }

    /**
     * Returns the EDNS Extended DNS Error.
     *
     * @return EDE metadata, or {@code null} when absent
     */
    public DnsExtendedError extendedError() {
        return extendedError;
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
            throw new ValidateException("DNS " + section + " records must not be null");
        }
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNS " + section + " records must not contain null");
            }
        }
        return List.copyOf(records);
    }

}
