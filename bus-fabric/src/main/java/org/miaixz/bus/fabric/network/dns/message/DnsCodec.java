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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.provider.DnsUpdateCommand;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * DNS wire-format decoder and encoder.
 *
 * @author Kimi Liu
 */
public final class DnsCodec {

    /**
     * Maximum unsigned 16-bit DNS wire value.
     */
    public static final int UNSIGNED_SHORT_MAX = Normal._65535;

    /**
     * Maximum DNS wire message length in bytes.
     */
    public static final int MAX_MESSAGE_BYTES = UNSIGNED_SHORT_MAX;

    /**
     * TCP, DoT, and DoQ stream length-prefix byte count for one DNS message.
     */
    public static final int STREAM_LENGTH_PREFIX_BYTES = 2;

    /**
     * Maximum unsigned 8-bit DNS wire value.
     */
    public static final int UNSIGNED_BYTE_MAX = 0xff;

    /**
     * Maximum unsigned 32-bit DNS wire value.
     */
    public static final long UNSIGNED_INT_MAX = 0xffff_ffffL;

    /**
     * DNS header byte length.
     */
    public static final int HEADER_LENGTH = 12;

    /**
     * Standard response bit.
     */
    private static final int FLAG_QR = 0x8000;

    /**
     * Authoritative answer bit.
     */
    private static final int FLAG_AA = 0x0400;

    /**
     * Opcode bit shift inside DNS flags.
     */
    private static final int OPCODE_SHIFT = 11;

    /**
     * Truncated response bit.
     */
    private static final int FLAG_TC = 0x0200;

    /**
     * Recursion desired bit.
     */
    private static final int FLAG_RD = 0x0100;

    /**
     * Recursion available bit.
     */
    private static final int FLAG_RA = 0x0080;

    /**
     * Authentic data bit.
     */
    private static final int FLAG_AD = 0x0020;

    /**
     * Checking disabled bit.
     */
    private static final int FLAG_CD = 0x0010;

    /**
     * Mask for the low four-bit header RCODE.
     */
    private static final int RCODE_MASK = 0x000f;

    /**
     * EDNS DNSSEC OK flag stored in the OPT TTL field.
     */
    private static final int EDNS_FLAG_DO = 0x8000;

    /**
     * EDNS Client Subnet option code.
     */
    private static final int EDNS_OPTION_CLIENT_SUBNET = 8;

    /**
     * EDNS Extended DNS Error option code.
     */
    private static final int EDNS_OPTION_EXTENDED_DNS_ERROR = 15;

    /**
     * Restricts the class to static operations.
     */
    private DnsCodec() {
        // No initialization required.
    }

    /**
     * Validates an unsigned 8-bit DNS wire value.
     *
     * @param value candidate value
     * @param name  diagnostic name
     * @return validated value
     */
    public static int validateUnsignedByte(final int value, final String name) {
        if (value < Normal._0 || value > UNSIGNED_BYTE_MAX) {
            throw new ValidateException(name + " must be an unsigned 8-bit value");
        }
        return value;
    }

    /**
     * Validates an unsigned 16-bit DNS wire value.
     *
     * @param value candidate value
     * @param name  diagnostic name
     * @return validated value
     */
    public static int validateUnsignedShort(final int value, final String name) {
        if (value < Normal._0 || value > UNSIGNED_SHORT_MAX) {
            throw new ValidateException(name + " must be an unsigned 16-bit value");
        }
        return value;
    }

    /**
     * Validates an unsigned 32-bit DNS wire value.
     *
     * @param value candidate value
     * @param name  diagnostic name
     * @return validated value
     */
    public static long validateUnsignedInt(final long value, final String name) {
        if (value < 0L || value > UNSIGNED_INT_MAX) {
            throw new ValidateException(name + " must be an unsigned 32-bit value");
        }
        return value;
    }

    /**
     * Validates an optional unsigned 32-bit DNS wire value.
     *
     * @param value candidate value, or {@code null}
     * @param name  diagnostic name
     * @return validated value, or {@code null}
     */
    public static Long validateOptionalUnsignedInt(final Long value, final String name) {
        return value == null ? null : validateUnsignedInt(value, name);
    }

    /**
     * Compares two byte arrays as unsigned DNS canonical byte sequences.
     *
     * @param left  left bytes
     * @param right right bytes
     * @return negative, zero, or positive comparison result
     */
    public static int compareUnsignedBytes(final byte[] left, final byte[] right) {
        final int length = Math.min(left.length, right.length);
        for (int index = 0; index < length; index++) {
            final int delta = readUnsignedByte(left, index) - readUnsignedByte(right, index);
            if (delta != Normal._0) {
                return delta;
            }
        }
        return left.length - right.length;
    }

    /**
     * Decodes a single-question DNS query.
     *
     * @param message complete DNS query message
     * @return decoded query
     * @throws ProtocolException if the message is malformed or unsupported
     */
    public static DnsQuery decodeQuery(final byte[] message) {
        if (message == null || message.length < HEADER_LENGTH) {
            throw new ProtocolException("DNS query is shorter than the header");
        }
        final int id = readUnsignedShort(message, 0);
        final int flags = readUnsignedShort(message, 2);
        final int opcode = (flags >>> OPCODE_SHIFT) & 0x0f;
        final int qdCount = readUnsignedShort(message, 4);
        final int anCount = readUnsignedShort(message, 6);
        final int nsCount = readUnsignedShort(message, 8);
        final int arCount = readUnsignedShort(message, 10);
        if ((flags & FLAG_QR) != 0) {
            throw new ProtocolException("DNS query has the response flag set");
        }
        if (qdCount != 1) {
            throw new ProtocolException("DNS query must contain exactly one question");
        }
        if (opcode == DnsQuery.OPCODE_QUERY && anCount != 0) {
            throw new ProtocolException("DNS standard query must not contain answer records");
        }
        final DnsName.ReadResult name = DnsName.read(message, HEADER_LENGTH);
        int offset = name.nextOffset();
        if (offset + 4 > message.length) {
            throw new ProtocolException("DNS question is truncated");
        }
        final int typeCode = readUnsignedShort(message, offset);
        final int recordClass = readUnsignedShort(message, offset + 2);
        offset += 4;
        final DnsQuestion question = new DnsQuestion(name.name(), typeCode, recordClass);
        final EdnsState edns;
        Long ixfrSerial = null;
        DnsUpdateCommand updateCommand = null;
        if (opcode == DnsQuery.OPCODE_QUERY) {
            if (nsCount > 0) {
                if (typeCode != DnsRecordType.IXFR.code() || nsCount != 1) {
                    throw new ProtocolException("DNS standard query must not contain authority records");
                }
                final IxfrState ixfr = readIxfrAuthority(message, offset, name.name());
                ixfrSerial = ixfr.serial;
                offset = ixfr.nextOffset;
            }
            edns = readAdditionals(message, offset, arCount);
        } else if (opcode == DnsQuery.OPCODE_UPDATE) {
            final UpdateCommandState update = readUpdateCommand(message, offset, question, anCount, nsCount, arCount);
            edns = update.edns;
            updateCommand = update.command;
        } else {
            edns = readAdditionals(message, skipRecords(message, offset, anCount + nsCount), arCount);
        }
        return new DnsQuery(id, opcode, (flags & FLAG_RD) != 0, (flags & FLAG_CD) != 0, question, edns.udpPayloadSize,
                edns.dnssecOk, edns.clientSubnet, edns.tsigRecord, ixfrSerial, updateCommand);
    }

    /**
     * Decodes a DNS response including answer, authority, and additional records.
     *
     * <p>
     * Domain names carried in known RDATA fields are canonicalized into uncompressed RDATA so the decoded records can
     * be re-encoded safely by {@link #encodeResponse(DnsResponse)}. The EDNS OPT pseudo-record is consumed for framing
     * validation and is not included in the returned additional records.
     * </p>
     *
     * @param message complete DNS response message
     * @return decoded response
     * @throws ProtocolException if the message is malformed or is not a response
     */
    public static DnsDecodedResponse decodeResponse(final byte[] message) {
        if (message == null || message.length < HEADER_LENGTH) {
            throw new ProtocolException("DNS response is shorter than the header");
        }
        final int id = readUnsignedShort(message, 0);
        final int flags = readUnsignedShort(message, 2);
        if ((flags & FLAG_QR) == 0) {
            throw new ProtocolException("DNS response does not have the response flag set");
        }
        final int questionCount = readUnsignedShort(message, 4);
        if (questionCount != 1) {
            throw new ProtocolException("DNS response must contain exactly one question");
        }
        final int answerCount = readUnsignedShort(message, 6);
        final int authorityCount = readUnsignedShort(message, 8);
        final int additionalCount = readUnsignedShort(message, 10);
        final QuestionState question = readQuestion(message, HEADER_LENGTH);
        final RecordSectionState answers = readSectionRecords(message, question.nextOffset, answerCount, false);
        final RecordSectionState authorities = readSectionRecords(message, answers.nextOffset, authorityCount, false);
        final RecordSectionState additionals = readSectionRecords(
                message,
                authorities.nextOffset,
                additionalCount,
                true);
        if (additionals.nextOffset != message.length) {
            throw new ProtocolException("DNS response contains trailing bytes");
        }
        return new DnsDecodedResponse(id, DnsResponseCode.fromCode(flags & RCODE_MASK), (flags & FLAG_AA) != 0,
                (flags & FLAG_RA) != 0, (flags & FLAG_TC) != 0, (flags & FLAG_AD) != 0, (flags & FLAG_CD) != 0,
                question.question, answers.records, authorities.records, additionals.records);
    }

    /**
     * Encodes a DNS response.
     *
     * @param response response model
     * @return DNS wire-format response bytes
     */
    public static byte[] encodeResponse(final DnsResponse response) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            writeHeader(output, response);
            writeQuestion(output, response.query().question());
            writeRecords(output, response.answers());
            writeRecords(output, response.authorities());
            writeRecords(output, response.additionals());
            writeOpt(output, response);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode DNS response", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Encodes a response for UDP, setting TC and dropping sections when the full answer exceeds the limit.
     *
     * @param response        response model
     * @param maxPayloadBytes maximum UDP payload size
     * @return UDP-safe DNS response bytes
     */
    public static byte[] encodeUdpResponse(final DnsResponse response, final int maxPayloadBytes) {
        final byte[] full = encodeResponse(response);
        if (full.length <= maxPayloadBytes) {
            return full;
        }
        return encodeResponse(
                new DnsResponse(response.query(), response.responseCode(), response.authoritative(),
                        response.recursionAvailable(), true, List.of(), List.of(), List.of(), response.authenticData(),
                        response.extendedError()));
    }

    /**
     * Encodes a minimal format-error response from an invalid query.
     *
     * @param message original DNS message bytes
     * @return DNS FORMERR response containing the original id when present
     */
    public static byte[] encodeFormatError(final byte[] message) {
        final int id = message == null || message.length < 2 ? 0 : readUnsignedShort(message, 0);
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeShort(id);
            output.writeShort(FLAG_QR | DnsResponseCode.FORMERR.code());
            output.writeShort(0);
            output.writeShort(0);
            output.writeShort(0);
            output.writeShort(0);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode DNS format error", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Writes the DNS response header.
     *
     * @param output   target stream
     * @param response response model
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeHeader(final DataOutputStream output, final DnsResponse response) throws IOException {
        int flags = FLAG_QR | (response.query().opcode() << OPCODE_SHIFT) | response.responseCode().code();
        if (response.query().recursionDesired()) {
            flags |= FLAG_RD;
        }
        if (response.authoritative()) {
            flags |= FLAG_AA;
        }
        if (response.recursionAvailable()) {
            flags |= FLAG_RA;
        }
        if (response.authenticData()) {
            flags |= FLAG_AD;
        }
        if (response.query().checkingDisabled()) {
            flags |= FLAG_CD;
        }
        if (response.truncatedFlag()) {
            flags |= FLAG_TC;
        }
        output.writeShort(response.query().id());
        output.writeShort(flags);
        output.writeShort(1);
        output.writeShort(response.answers().size());
        output.writeShort(response.authorities().size());
        output.writeShort(response.additionals().size() + (response.query().edns() ? 1 : 0));
    }

    /**
     * Writes one DNS question.
     *
     * @param output   target stream
     * @param question question to encode
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeQuestion(final DataOutputStream output, final DnsQuestion question) throws IOException {
        DnsName.write(output, question.name());
        output.writeShort(question.typeCode());
        output.writeShort(question.recordClass());
    }

    /**
     * Reads one DNS question from a message.
     *
     * @param message complete DNS message
     * @param offset  question owner offset
     * @return decoded question and next offset
     */
    private static QuestionState readQuestion(final byte[] message, final int offset) {
        final DnsName.ReadResult name = DnsName.read(message, offset);
        final int cursor = name.nextOffset();
        if (cursor + 4 > message.length) {
            throw new ProtocolException("DNS question is truncated");
        }
        return new QuestionState(new DnsQuestion(name.name(), readUnsignedShort(message, cursor),
                readUnsignedShort(message, cursor + 2)), cursor + 4);
    }

    /**
     * Writes one record section.
     *
     * @param output  target stream
     * @param records records to encode
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeRecords(final DataOutputStream output, final List<DnsRecord> records) throws IOException {
        for (final DnsRecord record : records) {
            DnsName.write(output, record.name());
            output.writeShort(record.typeCode());
            output.writeShort(record.recordClass());
            output.writeInt((int) record.ttl());
            final byte[] data = record.wireData();
            output.writeShort(data.length);
            output.write(data);
        }
    }

    /**
     * Reads a resource-record section.
     *
     * @param message complete DNS message
     * @param offset  first section record offset
     * @param count   record count
     * @param skipOpt true to consume EDNS OPT while omitting it from the returned records
     * @return decoded records and next offset
     */
    private static RecordSectionState readSectionRecords(
            final byte[] message,
            final int offset,
            final int count,
            final boolean skipOpt) {
        int cursor = offset;
        final ArrayList<DnsRecord> records = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            final ResourceRecordState state = readResourceRecord(message, cursor, skipOpt);
            cursor = state.nextOffset;
            if (state.record != null) {
                records.add(state.record);
            }
        }
        return new RecordSectionState(List.copyOf(records), cursor);
    }

    /**
     * Reads an RFC 2136 Dynamic Update command from the records following the zone section.
     *
     * @param message           complete DNS message
     * @param offset            first prerequisite record offset
     * @param zone              decoded zone section question
     * @param prerequisiteCount prerequisite record count
     * @param updateCount       update record count
     * @param additionalCount   additional record count
     * @return structured update command and EDNS state
     */
    private static UpdateCommandState readUpdateCommand(
            final byte[] message,
            final int offset,
            final DnsQuestion zone,
            final int prerequisiteCount,
            final int updateCount,
            final int additionalCount) {
        if (zone.typeCode() != DnsRecordType.SOA.code()) {
            throw new ProtocolException("DNS update zone type must be SOA");
        }
        final RecordSectionState prerequisites = readSectionRecords(message, offset, prerequisiteCount, false);
        final RecordSectionState updates = readSectionRecords(message, prerequisites.nextOffset, updateCount, false);
        final EdnsState edns = readAdditionals(message, updates.nextOffset, additionalCount);
        return new UpdateCommandState(new DnsUpdateCommand(zone, null, message,
                updatePrerequisites(prerequisites.records, zone.recordClass()),
                updateOperations(updates.records, zone.recordClass()), edns.records), edns);
    }

    /**
     * Converts prerequisite records into RFC 2136 prerequisite entries.
     *
     * @param records   prerequisite records
     * @param zoneClass zone class from the zone section
     * @return immutable prerequisite entries
     */
    private static List<DnsUpdateCommand.Prerequisite> updatePrerequisites(
            final List<DnsRecord> records,
            final int zoneClass) {
        final ArrayList<DnsUpdateCommand.Prerequisite> result = new ArrayList<>(records.size());
        for (final DnsRecord record : records) {
            result.add(DnsUpdateCommand.prerequisite(record, zoneClass));
        }
        return List.copyOf(result);
    }

    /**
     * Converts update records into RFC 2136 update entries.
     *
     * @param records   update records
     * @param zoneClass zone class from the zone section
     * @return immutable update entries
     */
    private static List<DnsUpdateCommand.Update> updateOperations(final List<DnsRecord> records, final int zoneClass) {
        final ArrayList<DnsUpdateCommand.Update> result = new ArrayList<>(records.size());
        for (final DnsRecord record : records) {
            result.add(DnsUpdateCommand.update(record, zoneClass));
        }
        return List.copyOf(result);
    }

    /**
     * Reads one resource record.
     *
     * @param message complete DNS message
     * @param offset  record owner offset
     * @param skipOpt true to return {@code null} for EDNS OPT records
     * @return decoded record and next offset
     */
    private static ResourceRecordState readResourceRecord(
            final byte[] message,
            final int offset,
            final boolean skipOpt) {
        final DnsName.ReadResult owner = DnsName.read(message, offset);
        int cursor = owner.nextOffset();
        if (cursor + 10 > message.length) {
            throw new ProtocolException("DNS resource record is truncated");
        }
        final int typeCode = readUnsignedShort(message, cursor);
        final int recordClass = readUnsignedShort(message, cursor + 2);
        final long ttl = readUnsignedInt(message, cursor + 4);
        final int length = readUnsignedShort(message, cursor + 8);
        cursor += 10;
        final int end = cursor + length;
        if (end > message.length) {
            throw new ProtocolException("DNS resource RDATA is truncated");
        }
        final DnsRecord record = skipOpt && typeCode == DnsRecordType.OPT.code() ? null
                : DnsRecord.raw(
                        owner.name(),
                        typeCode,
                        recordClass,
                        ttl,
                        canonicalRdata(message, cursor, length, typeCode));
        return new ResourceRecordState(record, end);
    }

    /**
     * Canonicalizes known RDATA fields that may contain compressed names.
     *
     * @param message  complete DNS message
     * @param offset   first RDATA byte
     * @param length   RDATA byte length
     * @param typeCode DNS record type code
     * @return uncompressed RDATA bytes
     */
    private static byte[] canonicalRdata(final byte[] message, final int offset, final int length, final int typeCode) {
        final int end = offset + length;
        final DnsRecordType type = DnsRecordType.fromCode(typeCode);
        return switch (type) {
            case NS, CNAME, DNAME, PTR -> canonicalSingleNameRdata(message, offset, end);
            case SOA -> canonicalSoaRdata(message, offset, end);
            case MX -> canonicalPrefixedNameRdata(message, offset, end, Short.BYTES, "MX");
            case SRV -> canonicalPrefixedNameRdata(message, offset, end, 6, "SRV");
            case NAPTR -> canonicalNaptrRdata(message, offset, end);
            case SVCB, HTTPS -> canonicalPrefixedNameRdata(message, offset, end, Short.BYTES, type.name());
            default -> Arrays.copyOfRange(message, offset, end);
        };
    }

    /**
     * Canonicalizes an RDATA value made of exactly one DNS name.
     *
     * @param message complete DNS message
     * @param offset  first RDATA byte
     * @param end     exclusive RDATA end offset
     * @return uncompressed single-name RDATA
     */
    private static byte[] canonicalSingleNameRdata(final byte[] message, final int offset, final int end) {
        final DnsName.ReadResult name = DnsName.read(message, offset);
        if (name.nextOffset() != end) {
            throw new ProtocolException("DNS single-name RDATA contains trailing bytes");
        }
        return DnsName.wire(name.name());
    }

    /**
     * Canonicalizes SOA RDATA.
     *
     * @param message complete DNS message
     * @param offset  first RDATA byte
     * @param end     exclusive RDATA end offset
     * @return uncompressed SOA RDATA
     */
    private static byte[] canonicalSoaRdata(final byte[] message, final int offset, final int end) {
        final DnsName.ReadResult primary = DnsName.read(message, offset);
        final DnsName.ReadResult responsible = DnsName.read(message, primary.nextOffset());
        if (responsible.nextOffset() + 20 != end) {
            throw new ProtocolException("DNS SOA RDATA length is invalid");
        }
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            DnsName.write(output, primary.name());
            DnsName.write(output, responsible.name());
            output.write(message, responsible.nextOffset(), 20);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to canonicalize DNS SOA RDATA", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Canonicalizes RDATA that contains fixed leading bytes, one DNS name, and optional trailing bytes.
     *
     * @param message     complete DNS message
     * @param offset      first RDATA byte
     * @param end         exclusive RDATA end offset
     * @param prefixBytes number of fixed bytes before the domain name
     * @param typeName    diagnostic record type name
     * @return uncompressed RDATA
     */
    private static byte[] canonicalPrefixedNameRdata(
            final byte[] message,
            final int offset,
            final int end,
            final int prefixBytes,
            final String typeName) {
        if (offset + prefixBytes >= end) {
            throw new ProtocolException("DNS " + typeName + " RDATA is truncated");
        }
        final DnsName.ReadResult target = DnsName.read(message, offset + prefixBytes);
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.write(message, offset, prefixBytes);
            DnsName.write(output, target.name());
            if (target.nextOffset() < end) {
                output.write(message, target.nextOffset(), end - target.nextOffset());
            }
        } catch (final IOException e) {
            throw new ProtocolException("Unable to canonicalize DNS " + typeName + " RDATA", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Canonicalizes NAPTR RDATA.
     *
     * @param message complete DNS message
     * @param offset  first RDATA byte
     * @param end     exclusive RDATA end offset
     * @return uncompressed NAPTR RDATA
     */
    private static byte[] canonicalNaptrRdata(final byte[] message, final int offset, final int end) {
        if (offset + 4 > end) {
            throw new ProtocolException("DNS NAPTR RDATA is truncated");
        }
        final CharacterStringState flags = readCharacterString(message, offset + 4, end);
        final CharacterStringState service = readCharacterString(message, flags.nextOffset, end);
        final CharacterStringState regexp = readCharacterString(message, service.nextOffset, end);
        final DnsName.ReadResult replacement = DnsName.read(message, regexp.nextOffset);
        if (replacement.nextOffset() != end) {
            throw new ProtocolException("DNS NAPTR RDATA contains trailing bytes");
        }
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.write(message, offset, 4);
            output.write(flags.bytes);
            output.write(service.bytes);
            output.write(regexp.bytes);
            DnsName.write(output, replacement.name());
        } catch (final IOException e) {
            throw new ProtocolException("Unable to canonicalize DNS NAPTR RDATA", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Reads one DNS character-string from RDATA.
     *
     * @param message complete DNS message
     * @param offset  first character-string byte
     * @param end     exclusive RDATA end offset
     * @return copied character-string bytes and next offset
     */
    private static CharacterStringState readCharacterString(final byte[] message, final int offset, final int end) {
        if (offset >= end) {
            throw new ProtocolException("DNS character-string is missing");
        }
        final int length = readUnsignedByte(message, offset);
        if (offset + 1 + length > end) {
            throw new ProtocolException("DNS character-string is truncated");
        }
        return new CharacterStringState(Arrays.copyOfRange(message, offset, offset + 1 + length), offset + 1 + length);
    }

    /**
     * Writes the response EDNS OPT pseudo-record when the query used EDNS.
     *
     * @param output   target stream
     * @param response response model
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeOpt(final DataOutputStream output, final DnsResponse response) throws IOException {
        final DnsQuery query = response.query();
        if (!query.edns()) {
            return;
        }
        DnsName.write(output, DnsName.ROOT);
        output.writeShort(DnsRecordType.OPT.code());
        output.writeShort(query.ednsUdpPayloadSize());
        output.writeInt(query.dnssecOk() ? EDNS_FLAG_DO : 0);
        final byte[] options = ednsOptions(response);
        output.writeShort(options.length);
        output.write(options);
    }

    /**
     * Encodes EDNS option data for a response.
     *
     * @param response response model
     * @return EDNS option data
     * @throws IOException if the option buffer rejects bytes
     */
    private static byte[] ednsOptions(final DnsResponse response) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            final DnsQuery query = response.query();
            writeClientSubnetOption(output, query.clientSubnet());
            writeExtendedErrorOption(output, response.extendedError());
        }
        return bytes.toByteArray();
    }

    /**
     * Writes an EDNS Client Subnet option.
     *
     * @param output       target stream
     * @param clientSubnet client subnet value, or {@code null}
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeClientSubnetOption(final DataOutputStream output, final DnsClientSubnet clientSubnet)
            throws IOException {
        if (clientSubnet == null) {
            return;
        }
        final ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(dataBytes)) {
            data.writeShort(clientSubnet.family());
            data.writeByte(clientSubnet.sourcePrefixLength());
            data.writeByte(clientSubnet.scopePrefixLength());
            data.write(clientSubnet.wireAddress());
        }
        final byte[] data = dataBytes.toByteArray();
        output.writeShort(EDNS_OPTION_CLIENT_SUBNET);
        output.writeShort(data.length);
        output.write(data);
    }

    /**
     * Writes an EDNS Extended DNS Error option.
     *
     * @param output        target stream
     * @param extendedError extended error value, or {@code null}
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeExtendedErrorOption(final DataOutputStream output, final DnsExtendedError extendedError)
            throws IOException {
        if (extendedError == null) {
            return;
        }
        final byte[] text = extendedError.text() == null ? Normal.EMPTY_BYTE_ARRAY
                : extendedError.text().getBytes(Charset.UTF_8);
        output.writeShort(EDNS_OPTION_EXTENDED_DNS_ERROR);
        output.writeShort(Short.BYTES + text.length);
        output.writeShort(extendedError.code());
        output.write(text);
    }

    /**
     * Reads additional records and extracts the EDNS OPT pseudo-record.
     *
     * @param message complete DNS message
     * @param offset  first additional record offset
     * @param count   additional record count
     * @return EDNS state
     */
    private static EdnsState readAdditionals(final byte[] message, final int offset, final int count) {
        int cursor = offset;
        boolean present = false;
        int udpPayloadSize = 0;
        boolean dnssecOk = false;
        DnsClientSubnet clientSubnet = null;
        DnsTsigRecord tsigRecord = null;
        final ArrayList<DnsRecord> records = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            final int recordStart = cursor;
            final DnsName.ReadResult name = DnsName.read(message, cursor);
            cursor = name.nextOffset();
            if (cursor + 10 > message.length) {
                throw new ProtocolException("DNS additional record is truncated");
            }
            final int typeCode = readUnsignedShort(message, cursor);
            final int recordClass = readUnsignedShort(message, cursor + 2);
            final long ttl = readUnsignedInt(message, cursor + 4);
            final int length = readUnsignedShort(message, cursor + 8);
            cursor += 10;
            if (cursor + length > message.length) {
                throw new ProtocolException("DNS additional RDATA is truncated");
            }
            if (typeCode == DnsRecordType.OPT.code()) {
                if (!DnsName.ROOT.equals(name.name())) {
                    throw new ProtocolException("DNS EDNS OPT owner must be root");
                }
                if (present) {
                    throw new ProtocolException("DNS query contains multiple EDNS OPT records");
                }
                present = true;
                udpPayloadSize = recordClass;
                dnssecOk = (ttl & EDNS_FLAG_DO) != 0;
                clientSubnet = readEdnsOptions(message, cursor, length);
            } else if (typeCode == DnsRecordType.TSIG.code()) {
                if (tsigRecord != null) {
                    throw new ProtocolException("DNS query contains multiple TSIG records");
                }
                if (index != count - 1) {
                    throw new ProtocolException("DNS TSIG record must be the final additional record");
                }
                tsigRecord = readTsigRecord(message, recordStart, name.name(), recordClass, ttl, cursor, length);
            } else {
                records.add(
                        DnsRecord.raw(
                                name.name(),
                                typeCode,
                                recordClass,
                                ttl,
                                canonicalRdata(message, cursor, length, typeCode)));
            }
            cursor += length;
        }
        if (cursor != message.length) {
            throw new ProtocolException("DNS query contains trailing bytes");
        }
        return new EdnsState(present, udpPayloadSize, dnssecOk, clientSubnet, tsigRecord, List.copyOf(records));
    }

    /**
     * Reads a TSIG additional pseudo-record.
     *
     * @param message     complete DNS message
     * @param recordStart offset at which the TSIG record starts
     * @param keyName     TSIG owner and key name
     * @param recordClass TSIG record class
     * @param ttl         TSIG record TTL
     * @param offset      first RDATA byte
     * @param length      RDATA length
     * @return decoded TSIG record
     */
    private static DnsTsigRecord readTsigRecord(
            final byte[] message,
            final int recordStart,
            final String keyName,
            final int recordClass,
            final long ttl,
            final int offset,
            final int length) {
        final int end = offset + length;
        final DnsName.ReadResult algorithm = DnsName.read(message, offset);
        int cursor = algorithm.nextOffset();
        if (cursor + 16 > end) {
            throw new ProtocolException("DNS TSIG RDATA is truncated");
        }
        final long timeSigned = ((long) readUnsignedShort(message, cursor) << 32)
                | readUnsignedInt(message, cursor + 2);
        cursor += 6;
        final int fudge = readUnsignedShort(message, cursor);
        cursor += 2;
        final int macSize = readUnsignedShort(message, cursor);
        cursor += 2;
        if (cursor + macSize + 6 > end) {
            throw new ProtocolException("DNS TSIG MAC is truncated");
        }
        final byte[] mac = Arrays.copyOfRange(message, cursor, cursor + macSize);
        cursor += macSize;
        final int originalId = readUnsignedShort(message, cursor);
        final int error = readUnsignedShort(message, cursor + 2);
        final int otherLength = readUnsignedShort(message, cursor + 4);
        cursor += 6;
        if (cursor + otherLength != end) {
            throw new ProtocolException("DNS TSIG other data length is invalid");
        }
        final byte[] otherData = Arrays.copyOfRange(message, cursor, end);
        return new DnsTsigRecord(keyName, algorithm.name(), recordClass, ttl, timeSigned, fudge, mac, originalId, error,
                otherData, unsignedMessage(message, recordStart), recordStart);
    }

    /**
     * Copies a DNS message without the final TSIG record and decrements ARCOUNT.
     *
     * @param message     original signed DNS message
     * @param recordStart offset at which the final TSIG record starts
     * @return unsigned DNS message bytes
     */
    private static byte[] unsignedMessage(final byte[] message, final int recordStart) {
        final byte[] unsigned = Arrays.copyOf(message, recordStart);
        final int additionalCount = readUnsignedShort(unsigned, 10);
        if (additionalCount <= 0) {
            throw new ProtocolException("DNS TSIG message has no additional count to decrement");
        }
        writeUnsignedShort(unsigned, 10, additionalCount - 1);
        return unsigned;
    }

    /**
     * Reads EDNS options from an OPT record.
     *
     * @param message complete DNS message
     * @param offset  first option offset
     * @param length  total option data length
     * @return client subnet option, or {@code null}
     */
    private static DnsClientSubnet readEdnsOptions(final byte[] message, final int offset, final int length) {
        int cursor = offset;
        final int end = offset + length;
        DnsClientSubnet clientSubnet = null;
        while (cursor < end) {
            if (cursor + 4 > end) {
                throw new ProtocolException("DNS EDNS option header is truncated");
            }
            final int optionCode = readUnsignedShort(message, cursor);
            final int optionLength = readUnsignedShort(message, cursor + 2);
            cursor += 4;
            if (cursor + optionLength > end) {
                throw new ProtocolException("DNS EDNS option data is truncated");
            }
            if (optionCode == EDNS_OPTION_CLIENT_SUBNET) {
                if (clientSubnet != null) {
                    throw new ProtocolException("DNS query contains multiple EDNS Client Subnet options");
                }
                clientSubnet = readClientSubnetOption(message, cursor, optionLength);
            }
            cursor += optionLength;
        }
        return clientSubnet;
    }

    /**
     * Reads one EDNS Client Subnet option.
     *
     * @param message complete DNS message
     * @param offset  option payload offset
     * @param length  option payload length
     * @return decoded client subnet
     */
    private static DnsClientSubnet readClientSubnetOption(final byte[] message, final int offset, final int length) {
        if (length < 4) {
            throw new ProtocolException("DNS EDNS Client Subnet option is truncated");
        }
        final int family = readUnsignedShort(message, offset);
        final int sourcePrefixLength = readUnsignedByte(message, offset + 2);
        final int scopePrefixLength = readUnsignedByte(message, offset + 3);
        final int addressLength = length - 4;
        final byte[] address = new byte[addressLength];
        System.arraycopy(message, offset + 4, address, 0, addressLength);
        return DnsClientSubnet.fromWire(family, sourcePrefixLength, scopePrefixLength, address);
    }

    /**
     * Reads the authority SOA carried by an IXFR query.
     *
     * @param message  complete DNS message
     * @param offset   first authority record offset
     * @param zoneName query zone name
     * @return parsed IXFR state
     */
    private static IxfrState readIxfrAuthority(final byte[] message, final int offset, final String zoneName) {
        final DnsName.ReadResult owner = DnsName.read(message, offset);
        int cursor = owner.nextOffset();
        if (!owner.name().equals(zoneName)) {
            throw new ProtocolException("DNS IXFR authority SOA owner must match the query zone");
        }
        if (cursor + 10 > message.length) {
            throw new ProtocolException("DNS IXFR authority record is truncated");
        }
        final int typeCode = readUnsignedShort(message, cursor);
        final int recordClass = readUnsignedShort(message, cursor + 2);
        final int length = readUnsignedShort(message, cursor + 8);
        cursor += 10;
        if (typeCode != DnsRecordType.SOA.code()) {
            throw new ProtocolException("DNS IXFR authority record must be SOA");
        }
        if (recordClass != DnsRecord.CLASS_IN) {
            throw new ProtocolException("DNS IXFR authority SOA class must be IN");
        }
        if (cursor + length > message.length) {
            throw new ProtocolException("DNS IXFR authority SOA RDATA is truncated");
        }
        return new IxfrState(readSoaSerial(message, cursor, length), cursor + length);
    }

    /**
     * Reads the serial from SOA RDATA.
     *
     * @param message complete DNS message
     * @param offset  SOA RDATA offset
     * @param length  SOA RDATA length
     * @return unsigned 32-bit SOA serial
     */
    private static long readSoaSerial(final byte[] message, final int offset, final int length) {
        final int end = offset + length;
        final DnsName.ReadResult primary = DnsName.read(message, offset);
        final DnsName.ReadResult responsible = DnsName.read(message, primary.nextOffset());
        if (responsible.nextOffset() + 20 > end) {
            throw new ProtocolException("DNS SOA RDATA is truncated");
        }
        return readUnsignedInt(message, responsible.nextOffset());
    }

    /**
     * Skips resource records and verifies that no trailing bytes remain.
     *
     * @param message complete DNS message
     * @param offset  first record offset
     * @param count   record count
     */
    private static int skipRecords(final byte[] message, final int offset, final int count) {
        int cursor = offset;
        for (int index = 0; index < count; index++) {
            final DnsName.ReadResult name = DnsName.read(message, cursor);
            cursor = name.nextOffset();
            if (cursor + 10 > message.length) {
                throw new ProtocolException("DNS resource record is truncated");
            }
            final int length = readUnsignedShort(message, cursor + 8);
            cursor += 10;
            if (cursor + length > message.length) {
                throw new ProtocolException("DNS resource RDATA is truncated");
            }
            cursor += length;
        }
        return cursor;
    }

    /**
     * Reads an unsigned int from a byte array.
     *
     * @param bytes  source bytes
     * @param offset source offset
     * @return unsigned 32-bit value represented as a Java long
     */
    public static long readUnsignedInt(final byte[] bytes, final int offset) {
        return ((long) readUnsignedShort(bytes, offset) << 16) | readUnsignedShort(bytes, offset + 2);
    }

    /**
     * Reads an unsigned short from a byte array.
     *
     * @param bytes  source bytes
     * @param offset source offset
     * @return unsigned 16-bit value
     */
    public static int readUnsignedShort(final byte[] bytes, final int offset) {
        return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
    }

    /**
     * Reads an unsigned byte from a byte array.
     *
     * @param bytes  source bytes
     * @param offset source offset
     * @return unsigned 8-bit value
     */
    public static int readUnsignedByte(final byte[] bytes, final int offset) {
        return bytes[offset] & 0xff;
    }

    /**
     * Writes an unsigned short to a byte array.
     *
     * @param bytes  target bytes
     * @param offset target offset
     * @param value  unsigned 16-bit value
     */
    public static void writeUnsignedShort(final byte[] bytes, final int offset, final int value) {
        validateUnsignedShort(value, "DNS unsigned short");
        bytes[offset] = (byte) ((value >>> 8) & 0xff);
        bytes[offset + 1] = (byte) (value & 0xff);
    }

    /**
     * Decoded question state with the next parser offset.
     *
     * @author Kimi Liu
     */
    private static final class QuestionState {

        /**
         * Decoded DNS question.
         */
        private final DnsQuestion question;

        /**
         * Next parser offset after the question.
         */
        private final int nextOffset;

        /**
         * Creates decoded question state.
         *
         * @param question   decoded DNS question
         * @param nextOffset next parser offset after the question
         */
        private QuestionState(final DnsQuestion question, final int nextOffset) {
            this.question = question;
            this.nextOffset = nextOffset;
        }

    }

    /**
     * Decoded record section state with the next parser offset.
     *
     * @author Kimi Liu
     */
    private static final class RecordSectionState {

        /**
         * Decoded records.
         */
        private final List<DnsRecord> records;

        /**
         * Next parser offset after the section.
         */
        private final int nextOffset;

        /**
         * Creates record section state.
         *
         * @param records    decoded records
         * @param nextOffset next parser offset after the section
         */
        private RecordSectionState(final List<DnsRecord> records, final int nextOffset) {
            this.records = records;
            this.nextOffset = nextOffset;
        }

    }

    /**
     * Decoded resource-record state with the next parser offset.
     *
     * @author Kimi Liu
     */
    private static final class ResourceRecordState {

        /**
         * Decoded record, or {@code null} when an omitted pseudo-record was consumed.
         */
        private final DnsRecord record;

        /**
         * Next parser offset after the record.
         */
        private final int nextOffset;

        /**
         * Creates resource-record state.
         *
         * @param record     decoded record, or {@code null}
         * @param nextOffset next parser offset after the record
         */
        private ResourceRecordState(final DnsRecord record, final int nextOffset) {
            this.record = record;
            this.nextOffset = nextOffset;
        }

    }

    /**
     * Decoded character-string state with the next parser offset.
     *
     * @author Kimi Liu
     */
    private static final class CharacterStringState {

        /**
         * Complete character-string bytes, including the leading length octet.
         */
        private final byte[] bytes;

        /**
         * Next parser offset after the character-string.
         */
        private final int nextOffset;

        /**
         * Creates character-string state.
         *
         * @param bytes      complete character-string bytes
         * @param nextOffset next parser offset after the character-string
         */
        private CharacterStringState(final byte[] bytes, final int nextOffset) {
            this.bytes = bytes;
            this.nextOffset = nextOffset;
        }

    }

    /**
     * Decoded EDNS state extracted from a query.
     *
     * @author Kimi Liu
     */
    private static final class EdnsState {

        /**
         * Whether an OPT pseudo-record was present.
         */
        private final boolean present;

        /**
         * EDNS UDP payload size.
         */
        private final int udpPayloadSize;

        /**
         * DNSSEC OK flag.
         */
        private final boolean dnssecOk;

        /**
         * EDNS Client Subnet value.
         */
        private final DnsClientSubnet clientSubnet;

        /**
         * Parsed TSIG record, or {@code null} when absent.
         */
        private final DnsTsigRecord tsigRecord;

        /**
         * Non-EDNS and non-TSIG additional records.
         */
        private final List<DnsRecord> records;

        /**
         * Creates EDNS state.
         *
         * @param present        whether an OPT pseudo-record was present
         * @param udpPayloadSize EDNS UDP payload size
         * @param dnssecOk       DNSSEC OK flag
         * @param clientSubnet   EDNS Client Subnet value, or {@code null}
         * @param tsigRecord     parsed TSIG record, or {@code null}
         * @param records        non-EDNS and non-TSIG additional records
         */
        private EdnsState(final boolean present, final int udpPayloadSize, final boolean dnssecOk,
                final DnsClientSubnet clientSubnet, final DnsTsigRecord tsigRecord, final List<DnsRecord> records) {
            this.present = present;
            this.udpPayloadSize = udpPayloadSize;
            this.dnssecOk = dnssecOk;
            this.clientSubnet = clientSubnet;
            this.tsigRecord = tsigRecord;
            this.records = records;
        }

        /**
         * Returns an absent EDNS state.
         *
         * @return absent EDNS state
         */
        private static EdnsState absent() {
            return new EdnsState(false, 0, false, null, null, List.of());
        }

    }

    /**
     * Decoded Dynamic Update command state.
     *
     * @author Kimi Liu
     */
    private static final class UpdateCommandState {

        /**
         * Structured Dynamic Update command.
         */
        private final DnsUpdateCommand command;

        /**
         * EDNS and TSIG metadata extracted while reading additionals.
         */
        private final EdnsState edns;

        /**
         * Creates a Dynamic Update command state.
         *
         * @param command structured Dynamic Update command
         * @param edns    EDNS and TSIG metadata
         */
        private UpdateCommandState(final DnsUpdateCommand command, final EdnsState edns) {
            this.command = command;
            this.edns = edns;
        }

    }

    /**
     * Decoded IXFR authority state extracted from a query.
     *
     * @author Kimi Liu
     */
    private static final class IxfrState {

        /**
         * Client base SOA serial.
         */
        private final long serial;

        /**
         * Next parser offset after the IXFR authority record.
         */
        private final int nextOffset;

        /**
         * Creates IXFR authority state.
         *
         * @param serial     client base SOA serial
         * @param nextOffset next parser offset after the IXFR authority record
         */
        private IxfrState(final long serial, final int nextOffset) {
            this.serial = serial;
            this.nextOffset = nextOffset;
        }

    }

}
