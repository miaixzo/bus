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
package org.miaixz.bus.fabric.network.dns.resolve;

import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;

/**
 * Internal DNS resolution result before response encoding.
 *
 * @author Kimi Liu
 */
public class DnsResolution {

    /**
     * Response code.
     */
    private final DnsResponseCode responseCode;

    /**
     * Authoritative-answer flag.
     */
    private final boolean authoritative;

    /**
     * Answer records.
     */
    private final List<DnsRecord> answers;

    /**
     * Authority records.
     */
    private final List<DnsRecord> authorities;

    /**
     * Creates a resolution result.
     *
     * @param responseCode  response code
     * @param authoritative authoritative-answer flag
     * @param answers       answer records
     * @param authorities   authority records
     */
    public DnsResolution(final DnsResponseCode responseCode, final boolean authoritative, final List<DnsRecord> answers,
            final List<DnsRecord> authorities) {
        if (responseCode == null) {
            throw new ValidateException("DNS resolution response code must not be null");
        }
        this.responseCode = responseCode;
        this.authoritative = authoritative;
        this.answers = immutableRecords(answers, "answer");
        this.authorities = immutableRecords(authorities, "authority");
    }

    /**
     * Creates a successful authoritative result.
     *
     * @param answers answer records
     * @return resolution result
     */
    public static DnsResolution answer(final List<DnsRecord> answers) {
        return new DnsResolution(DnsResponseCode.NOERROR, true, answers, List.of());
    }

    /**
     * Creates an empty response.
     *
     * @param code          response code
     * @param authoritative authoritative-answer flag
     * @param authorities   authority records
     * @return resolution result
     */
    public static DnsResolution empty(
            final DnsResponseCode code,
            final boolean authoritative,
            final List<DnsRecord> authorities) {
        return new DnsResolution(code, authoritative, List.of(), authorities);
    }

    /**
     * Returns the response code.
     *
     * @return response code
     */
    public DnsResponseCode responseCode() {
        return responseCode;
    }

    /**
     * Returns whether the answer is authoritative.
     *
     * @return true when AA should be set
     */
    public boolean authoritative() {
        return authoritative;
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
     * Validates and copies records.
     *
     * @param records source records
     * @param section diagnostic section
     * @return immutable records
     */
    private static List<DnsRecord> immutableRecords(final List<DnsRecord> records, final String section) {
        if (records == null) {
            throw new ValidateException("DNS resolution " + section + " records must not be null");
        }
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNS resolution " + section + " records must not contain null");
            }
        }
        return List.copyOf(records);
    }

}
