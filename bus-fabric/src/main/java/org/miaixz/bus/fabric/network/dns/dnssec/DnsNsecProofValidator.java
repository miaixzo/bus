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
package org.miaixz.bus.fabric.network.dns.dnssec;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.cache.DnsValidationCache;
import org.miaixz.bus.fabric.network.dns.cache.DnsValidationCache.Kind;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsDecodedResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * Validator for DNSSEC NSEC negative proofs.
 *
 * @author Kimi Liu
 */
public class DnsNsecProofValidator {

    /**
     * RRSIG RDATA byte offset of the signature expiration field.
     */
    private static final int RRSIG_EXPIRATION_OFFSET = 8;

    /**
     * RRSIG RDATA byte offset of the signature inception field.
     */
    private static final int RRSIG_INCEPTION_OFFSET = 12;

    /**
     * Minimum RRSIG fixed RDATA length.
     */
    private static final int RRSIG_FIXED_BYTES = 18;

    /**
     * Maximum bitmap window length in one NSEC type bitmap block.
     */
    private static final int MAX_TYPE_BITMAP_WINDOW_BYTES = 32;

    /**
     * Shared validation-result cache.
     */
    private final DnsValidationCache validationCache;

    /**
     * Creates an NSEC proof validator.
     *
     * @param validationCache shared validation-result cache
     */
    public DnsNsecProofValidator(final DnsValidationCache validationCache) {
        if (validationCache == null) {
            throw new ValidateException("DNSSEC NSEC proof cache must not be null");
        }
        this.validationCache = validationCache;
    }

    /**
     * Returns whether a decoded response is a negative response that needs NSEC proof validation.
     *
     * @param decoded decoded response
     * @return true when the response is NXDOMAIN or NOERROR without answers
     */
    public boolean negativeResponse(final DnsDecodedResponse decoded) {
        if (decoded == null) {
            throw new ValidateException("DNSSEC NSEC response must not be null");
        }
        return decoded.responseCode() == DnsResponseCode.NXDOMAIN
                || decoded.responseCode() == DnsResponseCode.NOERROR && decoded.answers().isEmpty();
    }

    /**
     * Returns whether the decoded negative response carries a valid NSEC proof.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when the NSEC proof validates
     */
    public boolean provesNegative(final DnsDecodedResponse decoded, final Instant now) {
        if (decoded == null) {
            throw new ValidateException("DNSSEC NSEC response must not be null");
        }
        if (decoded.responseCode() == DnsResponseCode.NXDOMAIN) {
            return provesNxDomain(decoded.question(), decoded.authorities(), now);
        }
        if (decoded.responseCode() == DnsResponseCode.NOERROR && decoded.answers().isEmpty()) {
            return provesNoData(decoded.question(), decoded.authorities(), now);
        }
        return false;
    }

    /**
     * Returns whether NSEC records prove an NXDOMAIN response.
     *
     * @param question    original question
     * @param authorities authority-section records
     * @param now         current instant
     * @return true when the NSEC set proves NXDOMAIN and wildcard absence
     */
    public boolean provesNxDomain(final DnsQuestion question, final List<DnsRecord> authorities, final Instant now) {
        validateQuestion(question);
        final List<DnsRecord> proofRecords = proofRecords(authorities);
        if (!proofRecords.isEmpty() && validationCache
                .contains(Kind.NSEC, question.name(), question.typeCode(), proofRecords, validateNow(now))) {
            return true;
        }
        final List<NsecData> proofs = signedProofs(authorities, now);
        final boolean result = coversName(proofs, question.name()) && provesWildcardAbsence(question, authorities, now);
        if (result && !proofRecords.isEmpty()) {
            validationCache.putSuccess(
                    Kind.NSEC,
                    question.name(),
                    question.typeCode(),
                    proofRecords,
                    nearestRrsigExpiration(proofRecords),
                    now);
        }
        return result;
    }

    /**
     * Returns whether NSEC records prove a NOERROR/NODATA response.
     *
     * @param question    original question
     * @param authorities authority-section records
     * @param now         current instant
     * @return true when the NSEC set proves the requested type does not exist
     */
    public boolean provesNoData(final DnsQuestion question, final List<DnsRecord> authorities, final Instant now) {
        validateQuestion(question);
        final List<DnsRecord> proofRecords = proofRecords(authorities);
        if (!proofRecords.isEmpty() && validationCache
                .contains(Kind.NSEC, question.name(), question.typeCode(), proofRecords, validateNow(now))) {
            return true;
        }
        final List<NsecData> proofs = signedProofs(authorities, now);
        final boolean result = provesExactNoData(proofs, question.name(), question.typeCode())
                || provesWildcardNoData(question, proofs);
        if (result && !proofRecords.isEmpty()) {
            validationCache.putSuccess(
                    Kind.NSEC,
                    question.name(),
                    question.typeCode(),
                    proofRecords,
                    nearestRrsigExpiration(proofRecords),
                    now);
        }
        return result;
    }

    /**
     * Returns whether NSEC records prove that a matching wildcard does not exist.
     *
     * @param question    original question
     * @param authorities authority-section records
     * @param now         current instant
     * @return true when a wildcard owner is covered or explicitly lacks the requested type
     */
    public boolean provesWildcardAbsence(
            final DnsQuestion question,
            final List<DnsRecord> authorities,
            final Instant now) {
        validateQuestion(question);
        final List<NsecData> proofs = signedProofs(authorities, now);
        for (final String wildcard : wildcardCandidates(question.name())) {
            if (coversName(proofs, wildcard) || provesExactNoData(proofs, wildcard, question.typeCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a decoded DS response proves insecure delegation with NSEC.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when a DS NODATA NSEC proof exists
     */
    public boolean provesInsecureDelegation(final DnsDecodedResponse decoded, final Instant now) {
        if (decoded == null) {
            throw new ValidateException("DNSSEC NSEC response must not be null");
        }
        return decoded.responseCode() == DnsResponseCode.NOERROR && decoded.answers().isEmpty()
                && decoded.question().typeCode() == DnsRecordType.DS.code()
                && provesNoData(decoded.question(), decoded.authorities(), now);
    }

    /**
     * Returns signed and parsed NSEC proof records.
     *
     * @param records authority-section records
     * @param now     current instant
     * @return signed NSEC proof data
     */
    private static List<NsecData> signedProofs(final List<DnsRecord> records, final Instant now) {
        final ArrayList<NsecData> proofs = new ArrayList<>();
        for (final DnsRecord record : validateRecords(records)) {
            if (record.typeCode() == DnsRecordType.NSEC.code() && signed(record, records, now)) {
                proofs.add(parse(record));
            }
        }
        return List.copyOf(proofs);
    }

    /**
     * Returns whether parsed NSEC data covers a queried owner.
     *
     * @param proofs parsed NSEC proofs
     * @param name   queried owner
     * @return true when one NSEC interval covers the name
     */
    private static boolean coversName(final List<NsecData> proofs, final String name) {
        final String normalized = DnsName.normalize(name);
        for (final NsecData proof : proofs) {
            if (!proof.owner().equals(normalized) && covers(proof.owner(), proof.next(), normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether parsed NSEC data proves exact owner NODATA.
     *
     * @param proofs   parsed NSEC proofs
     * @param owner    owner name
     * @param typeCode queried type code
     * @return true when an exact owner exists without the queried type or CNAME
     */
    private static boolean provesExactNoData(final List<NsecData> proofs, final String owner, final int typeCode) {
        final String normalized = DnsName.normalize(owner);
        for (final NsecData proof : proofs) {
            if (proof.owner().equals(normalized) && !proof.types().contains(typeCode)
                    && !proof.types().contains(DnsRecordType.CNAME.code())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether NSEC records prove matching wildcard NODATA.
     *
     * @param question original question
     * @param proofs   parsed NSEC proofs
     * @return true when a covered wildcard lacks the requested type
     */
    private static boolean provesWildcardNoData(final DnsQuestion question, final List<NsecData> proofs) {
        for (final String wildcard : wildcardCandidates(question.name())) {
            if (provesExactNoData(proofs, wildcard, question.typeCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses one NSEC RDATA payload.
     *
     * @param record NSEC record
     * @return parsed NSEC data
     */
    private static NsecData parse(final DnsRecord record) {
        final byte[] data = record.wireData();
        final DnsName.ReadResult next = DnsName.read(data, 0);
        if (next.nextOffset() >= data.length) {
            throw new ProtocolException("DNSSEC NSEC type bitmap is missing");
        }
        return new NsecData(record.name(), next.name(), typeBitmap(data, next.nextOffset()));
    }

    /**
     * Parses an NSEC type bitmap.
     *
     * @param data   NSEC RDATA bytes
     * @param offset type bitmap offset
     * @return set of present type codes
     */
    private static Set<Integer> typeBitmap(final byte[] data, final int offset) {
        final HashSet<Integer> types = new HashSet<>();
        int cursor = offset;
        while (cursor < data.length) {
            if (cursor + 2 > data.length) {
                throw new ProtocolException("DNSSEC NSEC type bitmap window is truncated");
            }
            final int window = DnsCodec.readUnsignedByte(data, cursor++);
            final int length = DnsCodec.readUnsignedByte(data, cursor++);
            if (length <= 0 || length > MAX_TYPE_BITMAP_WINDOW_BYTES || cursor + length > data.length) {
                throw new ProtocolException("DNSSEC NSEC type bitmap window length is invalid");
            }
            for (int octet = 0; octet < length; octet++) {
                final int value = DnsCodec.readUnsignedByte(data, cursor + octet);
                for (int bit = 0; bit < Byte.SIZE; bit++) {
                    if ((value & (0x80 >>> bit)) != 0) {
                        types.add(window * 256 + octet * Byte.SIZE + bit);
                    }
                }
            }
            cursor += length;
        }
        if (types.isEmpty()) {
            throw new ProtocolException("DNSSEC NSEC type bitmap is empty");
        }
        return Set.copyOf(types);
    }

    /**
     * Returns whether one NSEC record has a current same-owner NSEC RRSIG.
     *
     * @param nsec    NSEC record
     * @param records authority-section records
     * @param now     current instant
     * @return true when a usable RRSIG covers the NSEC record
     */
    private static boolean signed(final DnsRecord nsec, final List<DnsRecord> records, final Instant now) {
        for (final DnsRecord signature : validateRecords(records)) {
            if (signature.typeCode() == DnsRecordType.RRSIG.code() && signature.name().equals(nsec.name())
                    && typeCovered(signature) == DnsRecordType.NSEC.code() && signatureCurrent(signature, now)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether an NSEC interval covers a DNS name using canonical ordering.
     *
     * @param owner interval owner
     * @param next  interval next owner
     * @param name  candidate name
     * @return true when the candidate is inside the interval
     */
    private static boolean covers(final String owner, final String next, final String name) {
        final int ownerToNext = compareNames(owner, next);
        final int ownerToName = compareNames(owner, name);
        final int nameToNext = compareNames(name, next);
        if (ownerToNext < 0) {
            return ownerToName < 0 && nameToNext < 0;
        }
        return ownerToName < 0 || nameToNext < 0;
    }

    /**
     * Builds wildcard candidates from nearest suffix to root suffix.
     *
     * @param name queried owner name
     * @return wildcard owner candidates
     */
    private static List<String> wildcardCandidates(final String name) {
        final String normalized = DnsName.normalize(name);
        if (DnsName.ROOT.equals(normalized)) {
            return List.of();
        }
        final String[] labels = DnsName.labels(normalized);
        final ArrayList<String> candidates = new ArrayList<>();
        for (int index = 1; index < labels.length; index++) {
            candidates.add(DnsName.wildcardFromLabels(labels, index, labels.length));
        }
        candidates.add(DnsName.WILDCARD);
        return List.copyOf(candidates);
    }

    /**
     * Returns records that participate in an NSEC proof cache key.
     *
     * @param records authority-section records
     * @return NSEC and NSEC-covering RRSIG records
     */
    private static List<DnsRecord> proofRecords(final List<DnsRecord> records) {
        final ArrayList<DnsRecord> proofRecords = new ArrayList<>();
        for (final DnsRecord record : validateRecords(records)) {
            if (record.typeCode() == DnsRecordType.NSEC.code() || record.typeCode() == DnsRecordType.RRSIG.code()
                    && typeCovered(record) == DnsRecordType.NSEC.code()) {
                proofRecords.add(record);
            }
        }
        return List.copyOf(proofRecords);
    }

    /**
     * Finds the nearest RRSIG expiration among proof records.
     *
     * @param records proof records
     * @return nearest RRSIG expiration, or {@code null}
     */
    private static Instant nearestRrsigExpiration(final List<DnsRecord> records) {
        Instant nearest = null;
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.RRSIG.code()) {
                final Instant expiration = Instant
                        .ofEpochSecond(DnsCodec.readUnsignedInt(record.wireData(), RRSIG_EXPIRATION_OFFSET));
                nearest = nearest == null || expiration.isBefore(nearest) ? expiration : nearest;
            }
        }
        return nearest;
    }

    /**
     * Returns whether one RRSIG is inside its validity window.
     *
     * @param record RRSIG record
     * @param now    current instant
     * @return true when current
     */
    private static boolean signatureCurrent(final DnsRecord record, final Instant now) {
        final byte[] data = record.wireData();
        if (data.length < RRSIG_FIXED_BYTES) {
            throw new ProtocolException("DNSSEC RRSIG RDATA is truncated");
        }
        final long expiration = DnsCodec.readUnsignedInt(data, RRSIG_EXPIRATION_OFFSET);
        final long inception = DnsCodec.readUnsignedInt(data, RRSIG_INCEPTION_OFFSET);
        final long epoch = validateNow(now).getEpochSecond();
        return inception <= epoch && epoch <= expiration;
    }

    /**
     * Reads the covered type from one RRSIG.
     *
     * @param signature RRSIG record
     * @return covered type code
     */
    private static int typeCovered(final DnsRecord signature) {
        final byte[] data = signature.wireData();
        if (data.length < RRSIG_FIXED_BYTES) {
            throw new ProtocolException("DNSSEC RRSIG RDATA is truncated");
        }
        return DnsCodec.readUnsignedShort(data, 0);
    }

    /**
     * Compares two normalized names in DNS canonical wire ordering.
     *
     * @param left  left name
     * @param right right name
     * @return negative, zero, or positive comparison result
     */
    private static int compareNames(final String left, final String right) {
        return DnsCodec.compareUnsignedBytes(DnsName.wire(left), DnsName.wire(right));
    }

    /**
     * Validates a DNS question.
     *
     * @param question question to validate
     */
    private static void validateQuestion(final DnsQuestion question) {
        if (question == null) {
            throw new ValidateException("DNSSEC NSEC question must not be null");
        }
    }

    /**
     * Validates an authority-section record list.
     *
     * @param records records to validate
     * @return immutable validated records
     */
    private static List<DnsRecord> validateRecords(final List<DnsRecord> records) {
        if (records == null) {
            throw new ValidateException("DNSSEC NSEC records must not be null");
        }
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNSSEC NSEC records must not contain null");
            }
        }
        return List.copyOf(records);
    }

    /**
     * Validates a current instant.
     *
     * @param now current instant
     * @return validated instant
     */
    private static Instant validateNow(final Instant now) {
        if (now == null) {
            throw new ValidateException("DNSSEC NSEC validation instant must not be null");
        }
        return now;
    }

    /**
     * Immutable parsed NSEC data.
     *
     * @param owner owner name
     * @param next  next owner name
     * @param types present type codes
     * @author Kimi Liu
     */
    private record NsecData(String owner, String next, Set<Integer> types) {

        /**
         * Creates parsed NSEC data.
         *
         * @param owner owner name
         * @param next  next owner name
         * @param types present type codes
         */
        private NsecData {
            owner = DnsName.normalize(owner);
            next = DnsName.normalize(next);
            if (types == null || types.isEmpty()) {
                throw new ProtocolException("DNSSEC NSEC parsed type bitmap must not be empty");
            }
            types = Set.copyOf(types);
        }

    }

}
