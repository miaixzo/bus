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

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Validator for DNSSEC NSEC3 negative proofs.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsNsec3ProofValidator {

    /**
     * NSEC3 SHA-1 hash algorithm code.
     */
    private static final int HASH_SHA1 = 1;

    /**
     * NSEC3 opt-out flag.
     */
    private static final int OPT_OUT_FLAG = 0x01;

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
     * Maximum bitmap window length in one NSEC3 type bitmap block.
     */
    private static final int MAX_TYPE_BITMAP_WINDOW_BYTES = 32;

    /**
     * Base32hex alphabet without padding.
     */
    private static final char[] BASE32_HEX = "0123456789ABCDEFGHIJKLMNOPQRSTUV".toCharArray();

    /**
     * Shared validation-result cache.
     */
    private final DnsValidationCache validationCache;

    /**
     * Creates an NSEC3 proof validator.
     *
     * @param validationCache shared validation-result cache
     */
    public DnsNsec3ProofValidator(final DnsValidationCache validationCache) {
        if (validationCache == null) {
            throw new ValidateException("DNSSEC NSEC3 proof cache must not be null");
        }
        this.validationCache = validationCache;
    }

    /**
     * Returns whether a decoded response is a negative response that needs NSEC3 proof validation.
     *
     * @param decoded decoded response
     * @return true when the response is NXDOMAIN or NOERROR without answers
     */
    public boolean negativeResponse(final DnsDecodedResponse decoded) {
        if (decoded == null) {
            throw new ValidateException("DNSSEC NSEC3 response must not be null");
        }
        return decoded.responseCode() == DnsResponseCode.NXDOMAIN
                || decoded.responseCode() == DnsResponseCode.NOERROR && decoded.answers().isEmpty();
    }

    /**
     * Returns whether the decoded negative response carries a valid NSEC3 proof.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when the NSEC3 proof validates
     */
    public boolean provesNegative(final DnsDecodedResponse decoded, final Instant now) {
        if (decoded == null) {
            throw new ValidateException("DNSSEC NSEC3 response must not be null");
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
     * Returns whether NSEC3 records prove an NXDOMAIN response.
     *
     * @param question    original question
     * @param authorities authority-section records
     * @param now         current instant
     * @return true when the closest-encloser, next-closer, and wildcard proofs validate
     */
    public boolean provesNxDomain(final DnsQuestion question, final List<DnsRecord> authorities, final Instant now) {
        validateQuestion(question);
        final List<DnsRecord> proofRecords = proofRecords(authorities);
        if (!proofRecords.isEmpty() && validationCache
                .contains(Kind.NSEC3, question.name(), question.typeCode(), proofRecords, validateNow(now))) {
            return true;
        }
        final List<Nsec3Data> proofs = signedProofs(authorities, now);
        final List<Nsec3Param> parameters = parameters(authorities);
        final boolean result = parametersMatch(proofs, parameters) && provesClosestEncloserNxDomain(question, proofs);
        if (result && !proofRecords.isEmpty()) {
            validationCache.putSuccess(
                    Kind.NSEC3,
                    question.name(),
                    question.typeCode(),
                    proofRecords,
                    nearestRrsigExpiration(proofRecords),
                    now);
        }
        return result;
    }

    /**
     * Returns whether NSEC3 records prove a NOERROR/NODATA response.
     *
     * @param question    original question
     * @param authorities authority-section records
     * @param now         current instant
     * @return true when exact-name or closest-encloser NODATA proof validates
     */
    public boolean provesNoData(final DnsQuestion question, final List<DnsRecord> authorities, final Instant now) {
        validateQuestion(question);
        final List<DnsRecord> proofRecords = proofRecords(authorities);
        if (!proofRecords.isEmpty() && validationCache
                .contains(Kind.NSEC3, question.name(), question.typeCode(), proofRecords, validateNow(now))) {
            return true;
        }
        final List<Nsec3Data> proofs = signedProofs(authorities, now);
        final List<Nsec3Param> parameters = parameters(authorities);
        final boolean result = parametersMatch(proofs, parameters)
                && (provesExactNoData(question, proofs) || provesClosestEncloserNoData(question, proofs));
        if (result && !proofRecords.isEmpty()) {
            validationCache.putSuccess(
                    Kind.NSEC3,
                    question.name(),
                    question.typeCode(),
                    proofRecords,
                    nearestRrsigExpiration(proofRecords),
                    now);
        }
        return result;
    }

    /**
     * Returns whether NSEC3 records prove an opt-out insecure delegation.
     *
     * @param question    original DS question
     * @param authorities authority-section records
     * @param now         current instant
     * @return true when an opt-out NSEC3 interval covers the child delegation
     */
    public boolean provesOptOutDelegation(
            final DnsQuestion question,
            final List<DnsRecord> authorities,
            final Instant now) {
        validateQuestion(question);
        if (question.typeCode() != DnsRecordType.DS.code()) {
            return false;
        }
        final List<Nsec3Data> proofs = signedProofs(authorities, now);
        final List<Nsec3Param> parameters = parameters(authorities);
        return parametersMatch(proofs, parameters) && coversOptOut(question.name(), proofs);
    }

    /**
     * Returns whether a decoded DS response proves insecure delegation with NSEC3.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when a DS NODATA or opt-out NSEC3 proof exists
     */
    public boolean provesInsecureDelegation(final DnsDecodedResponse decoded, final Instant now) {
        if (decoded == null) {
            throw new ValidateException("DNSSEC NSEC3 response must not be null");
        }
        return decoded.responseCode() == DnsResponseCode.NOERROR && decoded.answers().isEmpty()
                && decoded.question().typeCode() == DnsRecordType.DS.code()
                && (provesNoData(decoded.question(), decoded.authorities(), now)
                        || provesOptOutDelegation(decoded.question(), decoded.authorities(), now));
    }

    /**
     * Returns whether closest-encloser NSEC3 proof validates NXDOMAIN.
     *
     * @param question original question
     * @param proofs   parsed NSEC3 proofs
     * @return true when the proof validates
     */
    private static boolean provesClosestEncloserNxDomain(final DnsQuestion question, final List<Nsec3Data> proofs) {
        for (final String closest : closestEncloserCandidates(question.name())) {
            if (exactHashExists(closest, proofs) && coversNextCloser(question.name(), closest, proofs)
                    && provesWildcardAbsence(closest, question.typeCode(), proofs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether closest-encloser NSEC3 proof validates NODATA.
     *
     * @param question original question
     * @param proofs   parsed NSEC3 proofs
     * @return true when the proof validates
     */
    private static boolean provesClosestEncloserNoData(final DnsQuestion question, final List<Nsec3Data> proofs) {
        for (final String closest : closestEncloserCandidates(question.name())) {
            if (exactHashExists(closest, proofs) && coversNextCloser(question.name(), closest, proofs)
                    && provesWildcardAbsence(closest, question.typeCode(), proofs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether exact hashed owner NODATA proof exists.
     *
     * @param question original question
     * @param proofs   parsed NSEC3 proofs
     * @return true when an exact owner lacks the queried type and CNAME
     */
    private static boolean provesExactNoData(final DnsQuestion question, final List<Nsec3Data> proofs) {
        for (final Nsec3Data proof : proofs) {
            if (inZone(question.name(), proof) && proof.ownerHash().equals(hashName(question.name(), proof))
                    && !proof.types().contains(question.typeCode())
                    && !proof.types().contains(DnsRecordType.CNAME.code())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether an opt-out NSEC3 interval covers a delegation name.
     *
     * @param name   delegation name
     * @param proofs parsed NSEC3 proofs
     * @return true when an opt-out interval covers the name hash
     */
    private static boolean coversOptOut(final String name, final List<Nsec3Data> proofs) {
        for (final Nsec3Data proof : proofs) {
            if (proof.optOut() && inZone(name, proof) && coversHash(proof, hashName(name, proof))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a next-closer name is covered.
     *
     * @param queried queried owner name
     * @param closest closest encloser name
     * @param proofs  parsed NSEC3 proofs
     * @return true when an NSEC3 interval covers the next-closer hash
     */
    private static boolean coversNextCloser(final String queried, final String closest, final List<Nsec3Data> proofs) {
        final String nextCloser = nextCloser(queried, closest);
        if (nextCloser == null) {
            return false;
        }
        for (final Nsec3Data proof : proofs) {
            if (inZone(nextCloser, proof) && coversHash(proof, hashName(nextCloser, proof))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether wildcard absence is proven under the closest encloser.
     *
     * @param closest  closest encloser name
     * @param typeCode queried type code
     * @param proofs   parsed NSEC3 proofs
     * @return true when wildcard is covered or exists without the requested type
     */
    private static boolean provesWildcardAbsence(
            final String closest,
            final int typeCode,
            final List<Nsec3Data> proofs) {
        final String normalizedClosest = DnsName.normalize(closest);
        final String wildcard = DnsName.ROOT.equals(normalizedClosest) ? DnsName.WILDCARD
                : DnsName.normalize(DnsName.WILDCARD + normalizedClosest);
        for (final Nsec3Data proof : proofs) {
            if (inZone(wildcard, proof)) {
                final String wildcardHash = hashName(wildcard, proof);
                if (coversHash(proof, wildcardHash) || proof.ownerHash().equals(wildcardHash)
                        && !proof.types().contains(typeCode) && !proof.types().contains(DnsRecordType.CNAME.code())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether exact hashed owner exists.
     *
     * @param name   owner name
     * @param proofs parsed NSEC3 proofs
     * @return true when an exact hash owner exists
     */
    private static boolean exactHashExists(final String name, final List<Nsec3Data> proofs) {
        for (final Nsec3Data proof : proofs) {
            if (inZone(name, proof) && proof.ownerHash().equals(hashName(name, proof))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns signed and parsed NSEC3 proof records.
     *
     * @param records authority-section records
     * @param now     current instant
     * @return parsed signed NSEC3 proof data
     */
    private static List<Nsec3Data> signedProofs(final List<DnsRecord> records, final Instant now) {
        final ArrayList<Nsec3Data> proofs = new ArrayList<>();
        for (final DnsRecord record : validateRecords(records)) {
            if (record.typeCode() == DnsRecordType.NSEC3.code() && signed(record, records, now)) {
                proofs.add(parseNsec3(record));
            }
        }
        return List.copyOf(proofs);
    }

    /**
     * Returns parsed NSEC3PARAM records.
     *
     * @param records authority-section records
     * @return parsed NSEC3PARAM values
     */
    private static List<Nsec3Param> parameters(final List<DnsRecord> records) {
        final ArrayList<Nsec3Param> parameters = new ArrayList<>();
        for (final DnsRecord record : validateRecords(records)) {
            if (record.typeCode() == DnsRecordType.NSEC3PARAM.code()) {
                parameters.add(parseParam(record));
            }
        }
        return List.copyOf(parameters);
    }

    /**
     * Returns whether every proof matches an advertised NSEC3PARAM.
     *
     * @param proofs     parsed NSEC3 proofs
     * @param parameters parsed NSEC3PARAM values
     * @return true when parameters are present and match every proof
     */
    private static boolean parametersMatch(final List<Nsec3Data> proofs, final List<Nsec3Param> parameters) {
        if (proofs.isEmpty() || parameters.isEmpty()) {
            return false;
        }
        for (final Nsec3Data proof : proofs) {
            if (!parameterMatches(proof, parameters)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether one proof matches any NSEC3PARAM.
     *
     * @param proof      parsed NSEC3 proof
     * @param parameters parsed NSEC3PARAM values
     * @return true when a matching parameter exists
     */
    private static boolean parameterMatches(final Nsec3Data proof, final List<Nsec3Param> parameters) {
        for (final Nsec3Param parameter : parameters) {
            if (proof.zone().equals(parameter.owner()) && proof.hashAlgorithm() == parameter.hashAlgorithm()
                    && proof.iterations() == parameter.iterations() && Arrays.equals(proof.salt(), parameter.salt())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses one NSEC3 RDATA payload.
     *
     * @param record NSEC3 record
     * @return parsed NSEC3 data
     */
    private static Nsec3Data parseNsec3(final DnsRecord record) {
        final byte[] data = record.wireData();
        if (data.length < 6) {
            throw new ProtocolException("DNSSEC NSEC3 RDATA is truncated");
        }
        final int hashAlgorithm = DnsCodec.readUnsignedByte(data, 0);
        final int flags = DnsCodec.readUnsignedByte(data, 1);
        final int iterations = DnsCodec.readUnsignedShort(data, 2);
        final int saltLength = DnsCodec.readUnsignedByte(data, 4);
        int cursor = 5;
        if (cursor + saltLength >= data.length) {
            throw new ProtocolException("DNSSEC NSEC3 salt is truncated");
        }
        final byte[] salt = Arrays.copyOfRange(data, cursor, cursor + saltLength);
        cursor += saltLength;
        final int hashLength = DnsCodec.readUnsignedByte(data, cursor++);
        if (hashLength <= 0 || cursor + hashLength >= data.length) {
            throw new ProtocolException("DNSSEC NSEC3 next hash is truncated");
        }
        final byte[] nextHash = Arrays.copyOfRange(data, cursor, cursor + hashLength);
        cursor += hashLength;
        return new Nsec3Data(record.name(), hashAlgorithm, flags, iterations, salt, base32Hex(nextHash),
                typeBitmap(data, cursor));
    }

    /**
     * Parses one NSEC3PARAM RDATA payload.
     *
     * @param record NSEC3PARAM record
     * @return parsed NSEC3 parameters
     */
    private static Nsec3Param parseParam(final DnsRecord record) {
        final byte[] data = record.wireData();
        if (data.length < 5) {
            throw new ProtocolException("DNSSEC NSEC3PARAM RDATA is truncated");
        }
        final int hashAlgorithm = DnsCodec.readUnsignedByte(data, 0);
        final int iterations = DnsCodec.readUnsignedShort(data, 2);
        final int saltLength = DnsCodec.readUnsignedByte(data, 4);
        final int saltOffset = 5;
        if (saltOffset + saltLength != data.length) {
            throw new ProtocolException("DNSSEC NSEC3PARAM salt length is invalid");
        }
        return new Nsec3Param(record.name(), hashAlgorithm, iterations,
                Arrays.copyOfRange(data, saltOffset, data.length));
    }

    /**
     * Parses an NSEC3 type bitmap.
     *
     * @param data   NSEC3 RDATA bytes
     * @param offset type bitmap offset
     * @return set of present type codes
     */
    private static Set<Integer> typeBitmap(final byte[] data, final int offset) {
        final HashSet<Integer> types = new HashSet<>();
        int cursor = offset;
        while (cursor < data.length) {
            if (cursor + 2 > data.length) {
                throw new ProtocolException("DNSSEC NSEC3 type bitmap window is truncated");
            }
            final int window = DnsCodec.readUnsignedByte(data, cursor++);
            final int length = DnsCodec.readUnsignedByte(data, cursor++);
            if (length <= 0 || length > MAX_TYPE_BITMAP_WINDOW_BYTES || cursor + length > data.length) {
                throw new ProtocolException("DNSSEC NSEC3 type bitmap window length is invalid");
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
            throw new ProtocolException("DNSSEC NSEC3 type bitmap is empty");
        }
        return Set.copyOf(types);
    }

    /**
     * Returns whether one NSEC3 record has a current same-owner NSEC3 RRSIG.
     *
     * @param nsec3   NSEC3 record
     * @param records authority-section records
     * @param now     current instant
     * @return true when a usable RRSIG covers the NSEC3 record
     */
    private static boolean signed(final DnsRecord nsec3, final List<DnsRecord> records, final Instant now) {
        for (final DnsRecord signature : validateRecords(records)) {
            if (signature.typeCode() == DnsRecordType.RRSIG.code() && signature.name().equals(nsec3.name())
                    && typeCovered(signature) == DnsRecordType.NSEC3.code() && signatureCurrent(signature, now)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether one NSEC3 interval covers a hashed owner label.
     *
     * @param proof      parsed NSEC3 proof
     * @param targetHash target owner hash label
     * @return true when the target hash is inside the interval
     */
    private static boolean coversHash(final Nsec3Data proof, final String targetHash) {
        final int ownerToNext = proof.ownerHash().compareTo(proof.nextHash());
        final int ownerToTarget = proof.ownerHash().compareTo(targetHash);
        final int targetToNext = targetHash.compareTo(proof.nextHash());
        if (ownerToNext < 0) {
            return ownerToTarget < 0 && targetToNext < 0;
        }
        return ownerToTarget < 0 || targetToNext < 0;
    }

    /**
     * Hashes a name using one NSEC3 proof's parameters.
     *
     * @param name  owner name
     * @param proof parsed NSEC3 proof
     * @return base32hex encoded lowercase hash label
     */
    private static String hashName(final String name, final Nsec3Data proof) {
        if (proof.hashAlgorithm() != HASH_SHA1) {
            throw new ProtocolException("Unsupported DNSSEC NSEC3 hash algorithm: " + proof.hashAlgorithm());
        }
        try {
            byte[] current = DnsName.wire(name);
            for (int round = 0; round <= proof.iterations(); round++) {
                final MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(current);
                digest.update(proof.salt());
                current = digest.digest();
            }
            return base32Hex(current);
        } catch (final GeneralSecurityException e) {
            throw new ProtocolException("Unable to compute DNSSEC NSEC3 SHA-1 hash", e);
        }
    }

    /**
     * Encodes bytes with unpadded base32hex.
     *
     * @param data source bytes
     * @return lowercase base32hex string
     */
    private static String base32Hex(final byte[] data) {
        final StringBuilder output = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bits = 0;
        for (final byte value : data) {
            buffer = (buffer << 8) | (value & 0xff);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                output.append(BASE32_HEX[(buffer >>> bits) & 0x1f]);
            }
        }
        if (bits > 0) {
            output.append(BASE32_HEX[(buffer << (5 - bits)) & 0x1f]);
        }
        return output.toString().toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Returns closest-encloser candidates from nearest suffix to root.
     *
     * @param name queried owner name
     * @return candidate closest enclosers
     */
    private static List<String> closestEncloserCandidates(final String name) {
        final String normalized = DnsName.normalize(name);
        if (DnsName.ROOT.equals(normalized)) {
            return List.of(DnsName.ROOT);
        }
        final String[] labels = DnsName.labels(normalized);
        final ArrayList<String> candidates = new ArrayList<>();
        for (int index = 1; index < labels.length; index++) {
            candidates.add(DnsName.fromLabels(labels, index, labels.length));
        }
        candidates.add(DnsName.ROOT);
        return List.copyOf(candidates);
    }

    /**
     * Returns the next-closer name for a query and closest encloser.
     *
     * @param queried queried owner name
     * @param closest closest encloser name
     * @return next-closer name, or {@code null} when unavailable
     */
    private static String nextCloser(final String queried, final String closest) {
        final String normalizedQuery = DnsName.normalize(queried);
        final String normalizedClosest = DnsName.normalize(closest);
        if (!DnsName.inZone(normalizedQuery, normalizedClosest) || normalizedQuery.equals(normalizedClosest)) {
            return null;
        }
        final String queryWithoutRoot = normalizedQuery.substring(0, normalizedQuery.length() - 1);
        final String closestWithoutRoot = DnsName.ROOT.equals(normalizedClosest) ? ""
                : normalizedClosest.substring(0, normalizedClosest.length() - 1);
        final String prefix = closestWithoutRoot.isEmpty() ? queryWithoutRoot
                : queryWithoutRoot.substring(0, queryWithoutRoot.length() - closestWithoutRoot.length() - 1);
        final int dot = prefix.lastIndexOf('.');
        final String label = dot < 0 ? prefix : prefix.substring(dot + 1);
        return closestWithoutRoot.isEmpty() ? DnsName.normalize(label)
                : DnsName.normalize(label + DnsName.ROOT + closestWithoutRoot);
    }

    /**
     * Returns records that participate in an NSEC3 proof cache key.
     *
     * @param records authority-section records
     * @return NSEC3, NSEC3-covering RRSIG, and NSEC3PARAM records
     */
    private static List<DnsRecord> proofRecords(final List<DnsRecord> records) {
        final ArrayList<DnsRecord> proofRecords = new ArrayList<>();
        for (final DnsRecord record : validateRecords(records)) {
            if (record.typeCode() == DnsRecordType.NSEC3.code() || record.typeCode() == DnsRecordType.NSEC3PARAM.code()
                    || record.typeCode() == DnsRecordType.RRSIG.code()
                            && typeCovered(record) == DnsRecordType.NSEC3.code()) {
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
     * Returns whether a candidate name belongs to one NSEC3 proof's zone.
     *
     * @param name  candidate name
     * @param proof parsed NSEC3 proof
     * @return true when the name is at or below the proof zone
     */
    private static boolean inZone(final String name, final Nsec3Data proof) {
        return DnsName.inZone(name, proof.zone());
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
     * Validates a DNS question.
     *
     * @param question question to validate
     */
    private static void validateQuestion(final DnsQuestion question) {
        if (question == null) {
            throw new ValidateException("DNSSEC NSEC3 question must not be null");
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
            throw new ValidateException("DNSSEC NSEC3 records must not be null");
        }
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNSSEC NSEC3 records must not contain null");
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
            throw new ValidateException("DNSSEC NSEC3 validation instant must not be null");
        }
        return now;
    }

    /**
     * Immutable parsed NSEC3 data.
     *
     * @param owner         owner name
     * @param hashAlgorithm NSEC3 hash algorithm
     * @param flags         NSEC3 flags
     * @param iterations    NSEC3 hash iterations
     * @param salt          NSEC3 salt bytes
     * @param nextHash      next hashed owner label
     * @param types         present type codes
     * @author Kimi Liu
     * @since Java 21+
     */
    private record Nsec3Data(String owner, int hashAlgorithm, int flags, int iterations, byte[] salt, String nextHash,
            Set<Integer> types) {

        /**
         * Creates parsed NSEC3 data.
         *
         * @param owner         owner name
         * @param hashAlgorithm NSEC3 hash algorithm
         * @param flags         NSEC3 flags
         * @param iterations    NSEC3 hash iterations
         * @param salt          NSEC3 salt bytes
         * @param nextHash      next hashed owner label
         * @param types         present type codes
         */
        private Nsec3Data {
            owner = DnsName.normalize(owner);
            validateHashAlgorithm(hashAlgorithm);
            if (iterations < 0 || iterations > DnsCodec.UNSIGNED_SHORT_MAX) {
                throw new ProtocolException("DNSSEC NSEC3 iterations are invalid");
            }
            salt = Arrays.copyOf(validateBytes(salt, "DNSSEC NSEC3 salt"), salt.length);
            nextHash = normalizeHash(nextHash);
            if (types == null || types.isEmpty()) {
                throw new ProtocolException("DNSSEC NSEC3 parsed type bitmap must not be empty");
            }
            types = Set.copyOf(types);
        }

        /**
         * Returns the NSEC3 owner hash label.
         *
         * @return owner hash label
         */
        private String ownerHash() {
            final String withoutRoot = owner.substring(0, owner.length() - 1);
            final int dot = withoutRoot.indexOf('.');
            if (dot <= 0) {
                throw new ProtocolException("DNSSEC NSEC3 owner name does not contain a zone suffix");
            }
            return normalizeHash(withoutRoot.substring(0, dot));
        }

        /**
         * Returns the NSEC3 owner zone.
         *
         * @return owner zone name
         */
        private String zone() {
            final String withoutRoot = owner.substring(0, owner.length() - 1);
            final int dot = withoutRoot.indexOf('.');
            if (dot <= 0) {
                throw new ProtocolException("DNSSEC NSEC3 owner name does not contain a zone suffix");
            }
            return DnsName.normalize(withoutRoot.substring(dot + 1));
        }

        /**
         * Returns whether the opt-out flag is set.
         *
         * @return true when opt-out is enabled
         */
        private boolean optOut() {
            return (flags & OPT_OUT_FLAG) != 0;
        }

        /**
         * Returns a defensive copy of the NSEC3 salt.
         *
         * @return salt bytes
         */
        public byte[] salt() {
            return Arrays.copyOf(salt, salt.length);
        }

    }

    /**
     * Immutable parsed NSEC3PARAM data.
     *
     * @param owner         zone owner name
     * @param hashAlgorithm NSEC3 hash algorithm
     * @param iterations    NSEC3 hash iterations
     * @param salt          NSEC3 salt bytes
     * @author Kimi Liu
     * @since Java 21+
     */
    private record Nsec3Param(String owner, int hashAlgorithm, int iterations, byte[] salt) {

        /**
         * Creates parsed NSEC3PARAM data.
         *
         * @param owner         zone owner name
         * @param hashAlgorithm NSEC3 hash algorithm
         * @param iterations    NSEC3 hash iterations
         * @param salt          NSEC3 salt bytes
         */
        private Nsec3Param {
            owner = DnsName.normalize(owner);
            validateHashAlgorithm(hashAlgorithm);
            if (iterations < 0 || iterations > DnsCodec.UNSIGNED_SHORT_MAX) {
                throw new ProtocolException("DNSSEC NSEC3PARAM iterations are invalid");
            }
            salt = Arrays.copyOf(validateBytes(salt, "DNSSEC NSEC3PARAM salt"), salt.length);
        }

        /**
         * Returns a defensive copy of the NSEC3PARAM salt.
         *
         * @return salt bytes
         */
        public byte[] salt() {
            return Arrays.copyOf(salt, salt.length);
        }

    }

    /**
     * Validates an NSEC3 hash algorithm.
     *
     * @param hashAlgorithm hash algorithm code
     */
    private static void validateHashAlgorithm(final int hashAlgorithm) {
        if (hashAlgorithm != HASH_SHA1) {
            throw new ProtocolException("Unsupported DNSSEC NSEC3 hash algorithm: " + hashAlgorithm);
        }
    }

    /**
     * Validates a byte array.
     *
     * @param data byte array
     * @param name diagnostic name
     * @return validated byte array
     */
    private static byte[] validateBytes(final byte[] data, final String name) {
        if (data == null) {
            throw new ValidateException(name + " must not be null");
        }
        return data;
    }

    /**
     * Normalizes a base32hex hash label.
     *
     * @param value hash label
     * @return lowercase hash label
     */
    private static String normalizeHash(final String value) {
        if (value == null || value.isBlank()) {
            throw new ProtocolException("DNSSEC NSEC3 hash label must not be blank");
        }
        return value.toLowerCase(java.util.Locale.ROOT);
    }

}
