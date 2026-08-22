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

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.provider.DnsUpdateCommand;

/**
 * Decoded single-question DNS query.
 *
 * @author Kimi Liu
 */
public class DnsQuery {

    /**
     * Standard query opcode.
     */
    public static final int OPCODE_QUERY = 0;

    /**
     * Dynamic update opcode.
     */
    public static final int OPCODE_UPDATE = 5;

    /**
     * DNS notify opcode.
     */
    public static final int OPCODE_NOTIFY = 4;

    /**
     * DNS message transaction identifier.
     */
    private final int id;

    /**
     * DNS message opcode.
     */
    private final int opcode;

    /**
     * Whether the client requested recursive service.
     */
    private final boolean recursionDesired;

    /**
     * Whether the client disabled DNSSEC validation checking.
     */
    private final boolean checkingDisabled;

    /**
     * Decoded question.
     */
    private final DnsQuestion question;

    /**
     * EDNS UDP payload size, or zero when the query did not contain EDNS.
     */
    private final int ednsUdpPayloadSize;

    /**
     * DNSSEC OK flag from EDNS.
     */
    private final boolean dnssecOk;

    /**
     * EDNS Client Subnet value, or {@code null} when absent.
     */
    private final DnsClientSubnet clientSubnet;

    /**
     * Whether the query contained a TSIG record.
     */
    private final boolean tsigPresent;

    /**
     * Decoded TSIG record, or {@code null} when absent or unavailable.
     */
    private final DnsTsigRecord tsigRecord;

    /**
     * IXFR base SOA serial supplied by the client, or {@code null} when absent.
     */
    private final Long ixfrSerial;

    /**
     * Structured Dynamic Update command, or {@code null} when the query is not UPDATE.
     */
    private final DnsUpdateCommand updateCommand;

    /**
     * Creates a decoded DNS query.
     *
     * @param id               unsigned 16-bit transaction identifier
     * @param recursionDesired recursion desired flag
     * @param question         decoded question
     */
    public DnsQuery(final int id, final boolean recursionDesired, final DnsQuestion question) {
        this(id, OPCODE_QUERY, recursionDesired, question, 0, false, null);
    }

    /**
     * Creates a decoded DNS query.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     */
    public DnsQuery(final int id, final int opcode, final boolean recursionDesired, final DnsQuestion question,
            final int ednsUdpPayloadSize, final boolean dnssecOk) {
        this(id, opcode, recursionDesired, question, ednsUdpPayloadSize, dnssecOk, null);
    }

    /**
     * Creates a decoded DNS query.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     */
    public DnsQuery(final int id, final int opcode, final boolean recursionDesired, final DnsQuestion question,
            final int ednsUdpPayloadSize, final boolean dnssecOk, final DnsClientSubnet clientSubnet) {
        this(id, opcode, recursionDesired, question, ednsUdpPayloadSize, dnssecOk, clientSubnet, false);
    }

    /**
     * Creates a decoded DNS query.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigPresent        whether the query contained a TSIG record
     */
    public DnsQuery(final int id, final int opcode, final boolean recursionDesired, final DnsQuestion question,
            final int ednsUdpPayloadSize, final boolean dnssecOk, final DnsClientSubnet clientSubnet,
            final boolean tsigPresent) {
        this(id, opcode, recursionDesired, question, ednsUdpPayloadSize, dnssecOk, clientSubnet, tsigPresent, null,
                null);
    }

    /**
     * Creates a decoded DNS query with parsed TSIG metadata.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigRecord         decoded TSIG record, or {@code null} when TSIG is absent
     */
    public DnsQuery(final int id, final int opcode, final boolean recursionDesired, final DnsQuestion question,
            final int ednsUdpPayloadSize, final boolean dnssecOk, final DnsClientSubnet clientSubnet,
            final DnsTsigRecord tsigRecord) {
        this(id, opcode, recursionDesired, question, ednsUdpPayloadSize, dnssecOk, clientSubnet, tsigRecord != null,
                tsigRecord, null);
    }

    /**
     * Creates a decoded DNS query with parsed TSIG and IXFR metadata.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigRecord         decoded TSIG record, or {@code null} when TSIG is absent
     * @param ixfrSerial         IXFR base SOA serial, or {@code null} when absent
     */
    DnsQuery(final int id, final int opcode, final boolean recursionDesired, final DnsQuestion question,
            final int ednsUdpPayloadSize, final boolean dnssecOk, final DnsClientSubnet clientSubnet,
            final DnsTsigRecord tsigRecord, final Long ixfrSerial) {
        this(id, opcode, recursionDesired, question, ednsUdpPayloadSize, dnssecOk, clientSubnet, tsigRecord != null,
                tsigRecord, ixfrSerial);
    }

    /**
     * Creates a decoded DNS query.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigPresent        whether the query contained a TSIG record
     * @param tsigRecord         decoded TSIG record, or {@code null} when absent or unavailable
     * @param ixfrSerial         IXFR base SOA serial, or {@code null} when absent
     */
    private DnsQuery(final int id, final int opcode, final boolean recursionDesired, final DnsQuestion question,
            final int ednsUdpPayloadSize, final boolean dnssecOk, final DnsClientSubnet clientSubnet,
            final boolean tsigPresent, final DnsTsigRecord tsigRecord, final Long ixfrSerial) {
        this(id, opcode, recursionDesired, false, question, ednsUdpPayloadSize, dnssecOk, clientSubnet, tsigPresent,
                tsigRecord, ixfrSerial);
    }

    /**
     * Creates a decoded DNS query.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param checkingDisabled   checking-disabled flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigRecord         decoded TSIG record, or {@code null} when TSIG is absent
     * @param ixfrSerial         IXFR base SOA serial, or {@code null} when absent
     */
    DnsQuery(final int id, final int opcode, final boolean recursionDesired, final boolean checkingDisabled,
            final DnsQuestion question, final int ednsUdpPayloadSize, final boolean dnssecOk,
            final DnsClientSubnet clientSubnet, final DnsTsigRecord tsigRecord, final Long ixfrSerial) {
        this(id, opcode, recursionDesired, checkingDisabled, question, ednsUdpPayloadSize, dnssecOk, clientSubnet,
                tsigRecord != null, tsigRecord, ixfrSerial, null);
    }

    /**
     * Creates a decoded DNS query with parsed TSIG, IXFR, and Dynamic Update metadata.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param checkingDisabled   checking-disabled flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigRecord         decoded TSIG record, or {@code null} when TSIG is absent
     * @param ixfrSerial         IXFR base SOA serial, or {@code null} when absent
     * @param updateCommand      structured Dynamic Update command, or {@code null}
     */
    DnsQuery(final int id, final int opcode, final boolean recursionDesired, final boolean checkingDisabled,
            final DnsQuestion question, final int ednsUdpPayloadSize, final boolean dnssecOk,
            final DnsClientSubnet clientSubnet, final DnsTsigRecord tsigRecord, final Long ixfrSerial,
            final DnsUpdateCommand updateCommand) {
        this(id, opcode, recursionDesired, checkingDisabled, question, ednsUdpPayloadSize, dnssecOk, clientSubnet,
                tsigRecord != null, tsigRecord, ixfrSerial, updateCommand);
    }

    /**
     * Creates a decoded DNS query.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param checkingDisabled   checking-disabled flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigPresent        whether the query contained a TSIG record
     * @param tsigRecord         decoded TSIG record, or {@code null} when absent or unavailable
     * @param ixfrSerial         IXFR base SOA serial, or {@code null} when absent
     */
    private DnsQuery(final int id, final int opcode, final boolean recursionDesired, final boolean checkingDisabled,
            final DnsQuestion question, final int ednsUdpPayloadSize, final boolean dnssecOk,
            final DnsClientSubnet clientSubnet, final boolean tsigPresent, final DnsTsigRecord tsigRecord,
            final Long ixfrSerial) {
        this(id, opcode, recursionDesired, checkingDisabled, question, ednsUdpPayloadSize, dnssecOk, clientSubnet,
                tsigPresent, tsigRecord, ixfrSerial, null);
    }

    /**
     * Creates a decoded DNS query.
     *
     * @param id                 unsigned 16-bit transaction identifier
     * @param opcode             DNS opcode
     * @param recursionDesired   recursion desired flag
     * @param checkingDisabled   checking-disabled flag
     * @param question           decoded question
     * @param ednsUdpPayloadSize EDNS UDP payload size, or zero when EDNS is absent
     * @param dnssecOk           DNSSEC OK flag from EDNS
     * @param clientSubnet       EDNS Client Subnet value, or {@code null} when absent
     * @param tsigPresent        whether the query contained a TSIG record
     * @param tsigRecord         decoded TSIG record, or {@code null} when absent or unavailable
     * @param ixfrSerial         IXFR base SOA serial, or {@code null} when absent
     * @param updateCommand      structured Dynamic Update command, or {@code null}
     */
    private DnsQuery(final int id, final int opcode, final boolean recursionDesired, final boolean checkingDisabled,
            final DnsQuestion question, final int ednsUdpPayloadSize, final boolean dnssecOk,
            final DnsClientSubnet clientSubnet, final boolean tsigPresent, final DnsTsigRecord tsigRecord,
            final Long ixfrSerial, final DnsUpdateCommand updateCommand) {
        this.id = DnsCodec.validateUnsignedShort(id, "DNS query id");
        this.opcode = validateOpcode(opcode);
        this.recursionDesired = recursionDesired;
        this.checkingDisabled = checkingDisabled;
        if (question == null) {
            throw new ValidateException("DNS question must not be null");
        }
        this.question = question;
        this.ednsUdpPayloadSize = DnsCodec.validateUnsignedShort(ednsUdpPayloadSize, "DNS EDNS UDP payload size");
        this.dnssecOk = dnssecOk;
        if (clientSubnet != null && this.ednsUdpPayloadSize == 0) {
            throw new ValidateException("DNS client subnet requires EDNS");
        }
        this.clientSubnet = clientSubnet;
        this.tsigRecord = tsigRecord;
        this.tsigPresent = tsigPresent || tsigRecord != null;
        this.ixfrSerial = DnsCodec.validateOptionalUnsignedInt(ixfrSerial, "DNS IXFR serial");
        if (updateCommand != null && this.opcode != OPCODE_UPDATE) {
            throw new ValidateException("DNS update command requires UPDATE opcode");
        }
        this.updateCommand = updateCommand;
    }

    /**
     * Returns the transaction identifier.
     *
     * @return unsigned 16-bit query identifier
     */
    public int id() {
        return id;
    }

    /**
     * Returns the DNS opcode.
     *
     * @return DNS opcode
     */
    public int opcode() {
        return opcode;
    }

    /**
     * Returns whether recursion was requested.
     *
     * @return true when the RD flag is set
     */
    public boolean recursionDesired() {
        return recursionDesired;
    }

    /**
     * Returns whether DNSSEC validation checking was disabled by the client.
     *
     * @return true when the CD flag is set
     */
    public boolean checkingDisabled() {
        return checkingDisabled;
    }

    /**
     * Returns the decoded question.
     *
     * @return DNS question
     */
    public DnsQuestion question() {
        return question;
    }

    /**
     * Returns whether the query contains EDNS.
     *
     * @return true when an OPT pseudo-record was present
     */
    public boolean edns() {
        return ednsUdpPayloadSize > 0;
    }

    /**
     * Returns the EDNS UDP payload size.
     *
     * @return EDNS UDP payload size, or zero when EDNS is absent
     */
    public int ednsUdpPayloadSize() {
        return ednsUdpPayloadSize;
    }

    /**
     * Returns whether the EDNS DNSSEC OK flag was set.
     *
     * @return true when the DO bit was set
     */
    public boolean dnssecOk() {
        return dnssecOk;
    }

    /**
     * Returns the EDNS Client Subnet value.
     *
     * @return EDNS Client Subnet value, or {@code null} when absent
     */
    public DnsClientSubnet clientSubnet() {
        return clientSubnet;
    }

    /**
     * Returns whether the query contained a TSIG record.
     *
     * @return true when TSIG was present
     */
    public boolean tsigPresent() {
        return tsigPresent;
    }

    /**
     * Returns the decoded TSIG record.
     *
     * @return decoded TSIG record, or {@code null} when absent or unavailable
     */
    public DnsTsigRecord tsigRecord() {
        return tsigRecord;
    }

    /**
     * Returns the IXFR base SOA serial supplied by the client.
     *
     * @return IXFR base serial, or {@code null} when the query did not include one
     */
    public Long ixfrSerial() {
        return ixfrSerial;
    }

    /**
     * Returns the structured Dynamic Update command.
     *
     * @return update command, or {@code null} when the query is not UPDATE
     */
    public DnsUpdateCommand updateCommand() {
        return updateCommand;
    }

    /**
     * Validates a DNS opcode.
     *
     * @param value opcode value
     * @return validated opcode
     */
    private static int validateOpcode(final int value) {
        if (value < 0 || value > 0x0f) {
            throw new ValidateException("DNS opcode must be a 4-bit value");
        }
        return value;
    }

}
