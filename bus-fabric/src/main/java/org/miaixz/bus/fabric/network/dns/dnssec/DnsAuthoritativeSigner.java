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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.zone.DnsZone;

/**
 * DNSSEC signer for authoritative-zone response RRsets.
 *
 * @author Kimi Liu
 */
public final class DnsAuthoritativeSigner {

    /**
     * RSA/SHA-1 DNSSEC algorithm.
     */
    private static final int ALGORITHM_RSASHA1 = 5;

    /**
     * RSA/SHA-1 NSEC3 DNSSEC algorithm.
     */
    private static final int ALGORITHM_RSASHA1_NSEC3 = 7;

    /**
     * RSA/SHA-256 DNSSEC algorithm.
     */
    private static final int ALGORITHM_RSASHA256 = 8;

    /**
     * RSA/SHA-512 DNSSEC algorithm.
     */
    private static final int ALGORITHM_RSASHA512 = 10;

    /**
     * ECDSA P-256/SHA-256 DNSSEC algorithm.
     */
    private static final int ALGORITHM_ECDSAP256SHA256 = 13;

    /**
     * ECDSA P-384/SHA-384 DNSSEC algorithm.
     */
    private static final int ALGORITHM_ECDSAP384SHA384 = 14;

    /**
     * Ed25519 DNSSEC algorithm.
     */
    private static final int ALGORITHM_ED25519 = 15;

    /**
     * Ed448 DNSSEC algorithm.
     */
    private static final int ALGORITHM_ED448 = 16;

    /**
     * Default RRSIG lifetime for online signatures.
     */
    private static final long DEFAULT_SIGNATURE_TTL_SECONDS = 300L;

    /**
     * Clock used for online signature inception and expiration.
     */
    private final Clock clock;

    /**
     * Creates an authoritative signer using the system UTC clock.
     */
    public DnsAuthoritativeSigner() {
        this(Clock.systemUTC());
    }

    /**
     * Creates an authoritative signer.
     *
     * @param clock clock used for online signature time fields
     */
    public DnsAuthoritativeSigner(final Clock clock) {
        if (clock == null) {
            throw new ValidateException("DNS authoritative signer clock must not be null");
        }
        this.clock = clock;
    }

    /**
     * Signs or appends existing signatures for response records.
     *
     * @param zone                 matched authoritative zone
     * @param owner                response RRSet owner
     * @param records              records to sign
     * @param recordClass          DNS record class
     * @param dnssecOk             true when the client requested DNSSEC records
     * @param requireOnlineSigning true when dynamic data must be online signed
     * @return records with RRSIG records appended when required
     */
    public List<DnsRecord> sign(
            final DnsZone zone,
            final String owner,
            final List<DnsRecord> records,
            final int recordClass,
            final boolean dnssecOk,
            final boolean requireOnlineSigning) {
        validateZone(zone);
        final String normalizedOwner = DnsName.normalize(owner);
        final List<DnsRecord> checkedRecords = validateRecords(records);
        if (!dnssecOk || checkedRecords.isEmpty() || rrsigOnly(checkedRecords)) {
            return checkedRecords;
        }
        final ArrayList<DnsRecord> signed = new ArrayList<>(checkedRecords);
        for (final List<DnsRecord> rrset : rrsets(checkedRecords)) {
            final List<DnsRecord> existing = existingSignatures(zone, normalizedOwner, rrset, recordClass);
            if (!existing.isEmpty()) {
                signed.addAll(existing);
                continue;
            }
            if (requireOnlineSigning || dnssecEnabled(zone)) {
                final DnsRecord signature = onlineSignature(
                        zone,
                        normalizedOwner,
                        rrset,
                        recordClass,
                        requireOnlineSigning);
                if (signature != null) {
                    signed.add(signature);
                }
            }
        }
        return List.copyOf(signed);
    }

    /**
     * Creates an online RRSIG for one RRSet.
     *
     * @param zone                 matched authoritative zone
     * @param owner                response RRSet owner
     * @param rrset                RRSet to sign
     * @param recordClass          DNS record class
     * @param requireOnlineSigning true when missing keys are fatal
     * @return generated RRSIG record
     */
    private DnsRecord onlineSignature(
            final DnsZone zone,
            final String owner,
            final List<DnsRecord> rrset,
            final int recordClass,
            final boolean requireOnlineSigning) {
        final DnsSigningKey key = activeSigningKey(zone);
        if (key == null) {
            if (requireOnlineSigning || dnssecEnabled(zone)) {
                throw new ValidateException("DNSSEC authoritative signing key is unavailable");
            }
            return null;
        }
        return rrsig(owner, rrset, recordClass, key, signingOwner(zone, owner, rrset.getFirst()), Instant.now(clock));
    }

    /**
     * Returns existing snapshot signatures that cover one RRSet.
     *
     * @param zone        matched authoritative zone
     * @param owner       response RRSet owner
     * @param rrset       RRSet to cover
     * @param recordClass DNS record class
     * @return existing signatures rewritten to response owner when wildcard-sourced
     */
    private static List<DnsRecord> existingSignatures(
            final DnsZone zone,
            final String owner,
            final List<DnsRecord> rrset,
            final int recordClass) {
        final int typeCode = rrset.getFirst().typeCode();
        final ArrayList<DnsRecord> signatures = new ArrayList<>();
        appendExistingSignatures(
                signatures,
                zone.records(owner, DnsRecordType.RRSIG.code(), recordClass),
                typeCode,
                owner);
        if (!signatures.isEmpty()) {
            return List.copyOf(signatures);
        }
        final String wildcardOwner = wildcardSigningOwner(zone, owner, typeCode, recordClass);
        if (wildcardOwner != null) {
            appendExistingSignatures(
                    signatures,
                    zone.records(wildcardOwner, DnsRecordType.RRSIG.code(), recordClass),
                    typeCode,
                    owner);
        }
        return List.copyOf(signatures);
    }

    /**
     * Appends existing signatures that cover a requested type.
     *
     * @param target     signature target list
     * @param candidates candidate RRSIG records
     * @param typeCode   covered type code
     * @param owner      response owner name
     */
    private static void appendExistingSignatures(
            final List<DnsRecord> target,
            final List<DnsRecord> candidates,
            final int typeCode,
            final String owner) {
        for (final DnsRecord signature : candidates) {
            if (rrsigTypeCovered(signature) == typeCode) {
                target.add(signature.name().equals(owner) ? signature : signature.withName(owner));
            }
        }
    }

    /**
     * Builds one generated RRSIG record.
     *
     * @param owner        response owner name
     * @param rrset        covered RRSet
     * @param recordClass  DNS record class
     * @param key          active signing key
     * @param signingOwner original signing owner
     * @param now          current instant
     * @return generated RRSIG record
     */
    private static DnsRecord rrsig(
            final String owner,
            final List<DnsRecord> rrset,
            final int recordClass,
            final DnsSigningKey key,
            final String signingOwner,
            final Instant now) {
        final SignatureFields fields = SignatureFields
                .create(rrset.getFirst().typeCode(), key, signingOwner, originalTtl(rrset), now);
        final byte[] signature = sign(fields, rrset, key);
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.writeBytes(fields.signedPrefix());
        bytes.writeBytes(signature);
        return DnsRecord.raw(owner, DnsRecordType.RRSIG.code(), recordClass, fields.originalTtl(), bytes.toByteArray());
    }

    /**
     * Signs canonical RRSet bytes.
     *
     * @param fields RRSIG fields
     * @param rrset  covered RRSet
     * @param key    active signing key
     * @return DNSSEC wire-format signature bytes
     */
    private static byte[] sign(final SignatureFields fields, final List<DnsRecord> rrset, final DnsSigningKey key) {
        try {
            final Signature signer = Signature.getInstance(signatureAlgorithm(key.algorithm()));
            signer.initSign(privateKey(key));
            signer.update(signedData(fields, rrset));
            return signatureForDnssec(key.algorithm(), signer.sign());
        } catch (final GeneralSecurityException e) {
            throw new ProtocolException("Unable to generate DNSSEC authoritative signature", e);
        }
    }

    /**
     * Builds DNSSEC signed data for one RRSet.
     *
     * @param fields RRSIG fields
     * @param rrset  covered RRSet
     * @return canonical signed data
     */
    private static byte[] signedData(final SignatureFields fields, final List<DnsRecord> rrset) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.writeBytes(fields.signedPrefix());
        final ArrayList<byte[]> records = new ArrayList<>();
        for (final DnsRecord record : rrset) {
            records.add(canonicalRecord(record, fields));
        }
        records.sort(DnsCodec::compareUnsignedBytes);
        for (final byte[] record : records) {
            bytes.writeBytes(record);
        }
        return bytes.toByteArray();
    }

    /**
     * Builds one canonical DNSSEC RRSet member.
     *
     * @param record covered record
     * @param fields RRSIG fields
     * @return canonical record bytes
     */
    private static byte[] canonicalRecord(final DnsRecord record, final SignatureFields fields) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            DnsName.write(output, canonicalOwner(record.name(), fields.labels()));
            output.writeShort(record.typeCode());
            output.writeShort(record.recordClass());
            output.writeInt((int) fields.originalTtl());
            final byte[] data = record.wireData();
            output.writeShort(data.length);
            output.write(data);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to canonicalize DNSSEC authoritative RRSet", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Returns the canonical owner name used by wildcard signatures.
     *
     * @param owner  response owner name
     * @param labels RRSIG labels field
     * @return canonical owner name
     */
    private static String canonicalOwner(final String owner, final int labels) {
        final String normalized = DnsName.normalize(owner);
        if (DnsName.ROOT.equals(normalized)) {
            return normalized;
        }
        final String[] parts = DnsName.labels(normalized);
        if (labels < 0 || labels > parts.length) {
            throw new ProtocolException("DNSSEC RRSIG labels field is invalid");
        }
        if (labels == parts.length) {
            return normalized;
        }
        if (labels == 0) {
            return DnsName.WILDCARD;
        }
        return DnsName.wildcardFromLabels(parts, parts.length - labels, parts.length);
    }

    /**
     * Creates a private key from PKCS#8 key bytes.
     *
     * @param key signing key
     * @return private key
     * @throws GeneralSecurityException if key decoding fails
     */
    private static PrivateKey privateKey(final DnsSigningKey key) throws GeneralSecurityException {
        return KeyFactory.getInstance(keyAlgorithm(key.algorithm()))
                .generatePrivate(new PKCS8EncodedKeySpec(key.privateKeyBytes()));
    }

    /**
     * Returns the JCA key factory algorithm.
     *
     * @param algorithm DNSSEC algorithm code
     * @return JCA key algorithm
     */
    private static String keyAlgorithm(final int algorithm) {
        return switch (algorithm) {
            case ALGORITHM_RSASHA1, ALGORITHM_RSASHA1_NSEC3, ALGORITHM_RSASHA256, ALGORITHM_RSASHA512 -> "RSA";
            case ALGORITHM_ECDSAP256SHA256, ALGORITHM_ECDSAP384SHA384 -> "EC";
            case ALGORITHM_ED25519 -> "Ed25519";
            case ALGORITHM_ED448 -> "Ed448";
            default -> throw new ProtocolException("Unsupported DNSSEC authoritative signing algorithm: " + algorithm);
        };
    }

    /**
     * Returns the JCA signature algorithm.
     *
     * @param algorithm DNSSEC algorithm code
     * @return JCA signature algorithm
     */
    private static String signatureAlgorithm(final int algorithm) {
        return switch (algorithm) {
            case ALGORITHM_RSASHA1, ALGORITHM_RSASHA1_NSEC3 -> "SHA1withRSA";
            case ALGORITHM_RSASHA256 -> "SHA256withRSA";
            case ALGORITHM_RSASHA512 -> "SHA512withRSA";
            case ALGORITHM_ECDSAP256SHA256 -> "SHA256withECDSA";
            case ALGORITHM_ECDSAP384SHA384 -> "SHA384withECDSA";
            case ALGORITHM_ED25519 -> "Ed25519";
            case ALGORITHM_ED448 -> "Ed448";
            default -> throw new ProtocolException("Unsupported DNSSEC authoritative signing algorithm: " + algorithm);
        };
    }

    /**
     * Converts a JCA signature to DNSSEC wire signature format.
     *
     * @param algorithm DNSSEC algorithm code
     * @param signature JCA signature bytes
     * @return DNSSEC wire-format signature
     */
    private static byte[] signatureForDnssec(final int algorithm, final byte[] signature) {
        return switch (algorithm) {
            case ALGORITHM_ECDSAP256SHA256 -> ecdsaWireSignature(signature, 32);
            case ALGORITHM_ECDSAP384SHA384 -> ecdsaWireSignature(signature, 48);
            default -> signature;
        };
    }

    /**
     * Converts DER ECDSA signature bytes into raw DNSSEC R and S bytes.
     *
     * @param signature  DER ECDSA signature bytes
     * @param coordinate coordinate byte length
     * @return raw DNSSEC ECDSA signature
     */
    private static byte[] ecdsaWireSignature(final byte[] signature, final int coordinate) {
        if (signature.length < 8 || signature[0] != 0x30) {
            throw new ProtocolException("DNSSEC ECDSA DER signature is invalid");
        }
        int cursor = 2;
        final byte[] r = derInteger(signature, cursor, coordinate);
        cursor += 2 + DnsCodec.readUnsignedByte(signature, cursor + 1);
        final byte[] s = derInteger(signature, cursor, coordinate);
        final byte[] wire = new byte[coordinate * 2];
        System.arraycopy(r, 0, wire, 0, coordinate);
        System.arraycopy(s, 0, wire, coordinate, coordinate);
        return wire;
    }

    /**
     * Reads one DER INTEGER and pads it to a fixed coordinate length.
     *
     * @param signature  DER signature bytes
     * @param offset     integer offset
     * @param coordinate coordinate byte length
     * @return fixed-width integer bytes
     */
    private static byte[] derInteger(final byte[] signature, final int offset, final int coordinate) {
        if (offset + 2 > signature.length || signature[offset] != 0x02) {
            throw new ProtocolException("DNSSEC ECDSA DER integer is invalid");
        }
        final int length = DnsCodec.readUnsignedByte(signature, offset + 1);
        if (length <= 0 || offset + 2 + length > signature.length) {
            throw new ProtocolException("DNSSEC ECDSA DER integer length is invalid");
        }
        int sourceOffset = offset + 2;
        int sourceLength = length;
        while (sourceLength > 1 && signature[sourceOffset] == 0) {
            sourceOffset++;
            sourceLength--;
        }
        if (sourceLength > coordinate) {
            throw new ProtocolException("DNSSEC ECDSA DER integer exceeds coordinate size");
        }
        final byte[] result = new byte[coordinate];
        System.arraycopy(signature, sourceOffset, result, coordinate - sourceLength, sourceLength);
        return result;
    }

    /**
     * Finds the active signing key for a zone.
     *
     * @param zone authoritative zone
     * @return active key, or {@code null}
     */
    private DnsSigningKey activeSigningKey(final DnsZone zone) {
        final Instant now = Instant.now(clock);
        for (final DnsSigningKey key : zone.signingKeys()) {
            if (key.activeAt(now)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Groups records into same-owner, same-type RRSets.
     *
     * @param records records to group
     * @return grouped RRSets
     */
    private static List<List<DnsRecord>> rrsets(final List<DnsRecord> records) {
        final LinkedHashMap<RrsetKey, List<DnsRecord>> grouped = new LinkedHashMap<>();
        for (final DnsRecord record : records) {
            if (record.typeCode() != DnsRecordType.RRSIG.code()) {
                grouped.computeIfAbsent(
                        new RrsetKey(record.name(), record.typeCode(), record.recordClass()),
                        ignored -> new ArrayList<>()).add(record);
            }
        }
        return grouped.values().stream().map(List::copyOf).toList();
    }

    /**
     * Returns whether all records are RRSIG records.
     *
     * @param records records to inspect
     * @return true when every record is an RRSIG
     */
    private static boolean rrsigOnly(final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            if (record.typeCode() != DnsRecordType.RRSIG.code()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether a zone publishes or owns DNSSEC signing material.
     *
     * @param zone zone to inspect
     * @return true when DNSSEC is enabled for the zone
     */
    private static boolean dnssecEnabled(final DnsZone zone) {
        if (!zone.signingKeys().isEmpty()) {
            return true;
        }
        for (final DnsRecord record : zone.records()) {
            if (dnssecType(record.typeCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a type code is DNSSEC material.
     *
     * @param typeCode DNS type code
     * @return true when type belongs to DNSSEC
     */
    private static boolean dnssecType(final int typeCode) {
        return typeCode == DnsRecordType.DNSKEY.code() || typeCode == DnsRecordType.DS.code()
                || typeCode == DnsRecordType.RRSIG.code() || typeCode == DnsRecordType.NSEC.code()
                || typeCode == DnsRecordType.NSEC3.code() || typeCode == DnsRecordType.NSEC3PARAM.code();
    }

    /**
     * Returns the owner name used to produce an RRSet signature.
     *
     * @param zone   matched authoritative zone
     * @param owner  response owner name
     * @param record covered record
     * @return signing owner name
     */
    private static String signingOwner(final DnsZone zone, final String owner, final DnsRecord record) {
        final String wildcardOwner = wildcardSigningOwner(zone, owner, record.typeCode(), record.recordClass());
        return wildcardOwner == null ? owner : wildcardOwner;
    }

    /**
     * Finds the wildcard owner that produced a synthesized RRSet.
     *
     * @param zone        matched authoritative zone
     * @param owner       response owner name
     * @param typeCode    covered type code
     * @param recordClass DNS record class
     * @return wildcard owner, or {@code null}
     */
    private static String wildcardSigningOwner(
            final DnsZone zone,
            final String owner,
            final int typeCode,
            final int recordClass) {
        final String normalized = DnsName.normalize(owner);
        if (zone.hasName(normalized) || DnsName.ROOT.equals(normalized)) {
            return null;
        }
        final String[] labels = DnsName.labels(normalized);
        for (int index = 1; index < labels.length; index++) {
            final String candidate = DnsName.wildcardFromLabels(labels, index, labels.length);
            if (!zone.records(candidate, typeCode, recordClass).isEmpty()) {
                return DnsName.normalize(candidate);
            }
        }
        return null;
    }

    /**
     * Returns the minimum original TTL for an RRSet.
     *
     * @param rrset RRSet records
     * @return minimum TTL
     */
    private static long originalTtl(final List<DnsRecord> rrset) {
        long ttl = Long.MAX_VALUE;
        for (final DnsRecord record : rrset) {
            ttl = Math.min(ttl, record.ttl());
        }
        return ttl == Long.MAX_VALUE ? 0L : ttl;
    }

    /**
     * Reads the type-covered field from RRSIG RDATA.
     *
     * @param signature RRSIG record
     * @return covered type code, or {@code -1}
     */
    private static int rrsigTypeCovered(final DnsRecord signature) {
        final byte[] data = signature.wireData();
        if (data.length < Short.BYTES) {
            return -1;
        }
        return DnsCodec.readUnsignedShort(data, 0);
    }

    /**
     * Counts DNS owner labels excluding the root label and wildcard label.
     *
     * @param name owner name
     * @return label count
     */
    private static int labels(final String name) {
        final String normalized = DnsName.normalize(name);
        if (DnsName.ROOT.equals(normalized)) {
            return 0;
        }
        final String[] labels = DnsName.labels(normalized);
        return labels.length > 0 && DnsName.WILDCARD.equals(labels[0] + DnsName.ROOT) ? labels.length - 1
                : labels.length;
    }

    /**
     * Validates a zone.
     *
     * @param zone zone to validate
     */
    private static void validateZone(final DnsZone zone) {
        if (zone == null) {
            throw new ValidateException("DNS authoritative signer zone must not be null");
        }
    }

    /**
     * Validates response records.
     *
     * @param records records to validate
     * @return immutable records
     */
    private static List<DnsRecord> validateRecords(final List<DnsRecord> records) {
        if (records == null) {
            throw new ValidateException("DNS authoritative signer records must not be null");
        }
        for (final DnsRecord record : records) {
            if (record == null) {
                throw new ValidateException("DNS authoritative signer records must not contain null");
            }
        }
        return List.copyOf(records);
    }

    /**
     * Immutable RRSet grouping key.
     *
     * @param owner       owner name
     * @param typeCode    DNS type code
     * @param recordClass DNS record class
     * @author Kimi Liu
     */
    private record RrsetKey(String owner, int typeCode, int recordClass) {

        /**
         * Creates an RRSet grouping key.
         *
         * @param owner       owner name
         * @param typeCode    DNS type code
         * @param recordClass DNS record class
         */
        private RrsetKey {
            owner = DnsName.normalize(owner);
        }

    }

    /**
     * Immutable RRSIG fields used for signature generation.
     *
     * @param typeCovered covered RR type
     * @param algorithm   DNSSEC algorithm
     * @param labels      owner label count
     * @param originalTtl original RRSet TTL
     * @param expiration  signature expiration epoch seconds
     * @param inception   signature inception epoch seconds
     * @param keyTag      DNSKEY key tag
     * @param signerName  signer name
     * @author Kimi Liu
     */
    private record SignatureFields(int typeCovered, int algorithm, int labels, long originalTtl, long expiration,
            long inception, int keyTag, String signerName) {

        /**
         * Creates signature fields.
         *
         * @param typeCovered covered RR type
         * @param algorithm   DNSSEC algorithm
         * @param labels      owner label count
         * @param originalTtl original RRSet TTL
         * @param expiration  signature expiration epoch seconds
         * @param inception   signature inception epoch seconds
         * @param keyTag      DNSKEY key tag
         * @param signerName  signer name
         */
        private SignatureFields {
            signerName = DnsName.normalize(signerName);
        }

        /**
         * Creates signature fields for a key and signing owner.
         *
         * @param typeCovered  covered RR type
         * @param key          signing key
         * @param signingOwner original signing owner
         * @param originalTtl  original RRSet TTL
         * @param now          current instant
         * @return signature fields
         */
        private static SignatureFields create(
                final int typeCovered,
                final DnsSigningKey key,
                final String signingOwner,
                final long originalTtl,
                final Instant now) {
            final long inception = now.getEpochSecond();
            final long expiration = Math
                    .min(key.notAfter().getEpochSecond(), inception + DEFAULT_SIGNATURE_TTL_SECONDS);
            if (expiration <= inception) {
                throw new ValidateException("DNSSEC authoritative signing key is expired");
            }
            return new SignatureFields(typeCovered, key.algorithm(), DnsAuthoritativeSigner.labels(signingOwner),
                    originalTtl, expiration, inception, key.keyTag(), key.keyName());
        }

        /**
         * Encodes the RRSIG RDATA prefix.
         *
         * @return RRSIG RDATA prefix bytes
         */
        private byte[] signedPrefix() {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeShort(typeCovered);
                output.writeByte(algorithm);
                output.writeByte(labels);
                output.writeInt((int) originalTtl);
                output.writeInt((int) expiration);
                output.writeInt((int) inception);
                output.writeShort(keyTag);
                DnsName.write(output, signerName);
            } catch (final IOException e) {
                throw new ProtocolException("Unable to encode DNSSEC authoritative RRSIG prefix", e);
            }
            return bytes.toByteArray();
        }

    }

}
