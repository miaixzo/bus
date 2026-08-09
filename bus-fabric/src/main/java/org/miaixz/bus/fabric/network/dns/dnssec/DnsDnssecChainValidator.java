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
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.cache.DnsValidationCache;
import org.miaixz.bus.fabric.network.dns.cache.DnsValidationCache.Kind;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsDecodedResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsResponse;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.zone.DnsTrustAnchor;

/**
 * DNSSEC chain validator from configured trust anchors to a decoded response RRSet.
 *
 * @author Kimi Liu
 */
public final class DnsDnssecChainValidator {

    /**
     * RRSIG RDATA byte offset of the signature expiration field.
     */
    private static final int RRSIG_EXPIRATION_OFFSET = 8;

    /**
     * RRSIG RDATA byte offset of the signature inception field.
     */
    private static final int RRSIG_INCEPTION_OFFSET = 12;

    /**
     * RRSIG RDATA byte offset of the key-tag field.
     */
    private static final int RRSIG_KEY_TAG_OFFSET = 16;

    /**
     * Minimum RRSIG fixed RDATA length.
     */
    private static final int RRSIG_FIXED_BYTES = 18;

    /**
     * DNSKEY fixed RDATA byte length.
     */
    private static final int DNSKEY_FIXED_BYTES = 4;

    /**
     * DS fixed RDATA byte length.
     */
    private static final int DS_FIXED_BYTES = 4;

    /**
     * DNSKEY secure-entry-point flag bit.
     */
    private static final int DNSKEY_SEP_FLAG = 1;

    /**
     * DNSKEY zone-key flag bit.
     */
    private static final int DNSKEY_ZONE_FLAG = 0x0100;

    /**
     * DNSKEY protocol value required by DNSSEC.
     */
    private static final int DNSKEY_PROTOCOL = 3;

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
     * SHA-1 DS digest type.
     */
    private static final int DS_DIGEST_SHA1 = 1;

    /**
     * SHA-256 DS digest type.
     */
    private static final int DS_DIGEST_SHA256 = 2;

    /**
     * SHA-384 DS digest type.
     */
    private static final int DS_DIGEST_SHA384 = 4;

    /**
     * X.509 SubjectPublicKeyInfo prefix for an Ed25519 raw public key.
     */
    private static final byte[] ED25519_X509_PREFIX = new byte[] { 0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70,
            0x03, 0x21, 0x00 };

    /**
     * X.509 SubjectPublicKeyInfo prefix for an Ed448 raw public key.
     */
    private static final byte[] ED448_X509_PREFIX = new byte[] { 0x30, 0x43, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x71,
            0x03, 0x3a, 0x00 };

    /**
     * Clock used for signature time validation.
     */
    private final Clock clock;

    /**
     * Configured trust anchors.
     */
    private final List<DnsTrustAnchor> trustAnchors;

    /**
     * Shared DNSSEC validation cache.
     */
    private final DnsValidationCache validationCache;

    /**
     * NSEC negative-proof validator.
     */
    private final DnsNsecProofValidator nsecProofValidator;

    /**
     * NSEC3 negative-proof validator.
     */
    private final DnsNsec3ProofValidator nsec3ProofValidator;

    /**
     * Creates a chain validator.
     *
     * @param clock           clock used for signature time validation
     * @param trustAnchors    configured trust anchors
     * @param validationCache shared validation cache
     */
    public DnsDnssecChainValidator(final Clock clock, final List<DnsTrustAnchor> trustAnchors,
            final DnsValidationCache validationCache) {
        if (clock == null) {
            throw new ValidateException("DNSSEC chain validator clock must not be null");
        }
        if (trustAnchors == null) {
            throw new ValidateException("DNSSEC chain validator trust anchors must not be null");
        }
        if (validationCache == null) {
            throw new ValidateException("DNSSEC chain validator cache must not be null");
        }
        for (final DnsTrustAnchor trustAnchor : trustAnchors) {
            if (trustAnchor == null) {
                throw new ValidateException("DNSSEC chain validator trust anchors must not contain null");
            }
        }
        this.clock = clock;
        this.trustAnchors = List.copyOf(trustAnchors);
        this.validationCache = validationCache;
        this.nsecProofValidator = new DnsNsecProofValidator(validationCache);
        this.nsec3ProofValidator = new DnsNsec3ProofValidator(validationCache);
    }

    /**
     * Validates a decoded DNSSEC response against the configured chain policy.
     *
     * @param query   original query
     * @param decoded decoded response
     * @return DNS response with AD set only for secure validated data
     */
    public DnsResponse validate(final DnsQuery query, final DnsDecodedResponse decoded) {
        if (query == null) {
            throw new ValidateException("DNSSEC chain query must not be null");
        }
        if (decoded == null) {
            throw new ValidateException("DNSSEC chain response must not be null");
        }
        if (query.checkingDisabled() || !query.dnssecOk() || !containsDnssecMaterial(decoded)) {
            return decoded.toResponse(query);
        }
        final Instant now = Instant.now(clock);
        try {
            if (validationCache.containsResponse(decoded, now)) {
                return authenticated(query, decoded);
            }
            if (nsecProofValidator.negativeResponse(decoded) && !provesNegative(decoded, now)) {
                return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
            }
            if (!secure(decoded, now)) {
                return insecure(query, decoded, now);
            }
            validationCache.putResponseSuccess(decoded, now);
            cacheSectionResults(decoded, now);
            return authenticated(query, decoded);
        } catch (final RuntimeException e) {
            return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
        }
    }

    /**
     * Returns whether a response is secure under the current trust anchors.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when the response is secure
     */
    private boolean secure(final DnsDecodedResponse decoded, final Instant now) {
        final List<DnsRecord> records = records(decoded);
        final List<DnsRecord> dnskeys = recordsOfType(records, DnsRecordType.DNSKEY);
        final List<DnsRecord> dsRecords = recordsOfType(records, DnsRecordType.DS);
        return !trustAnchors.isEmpty() && algorithmsEnabled(records) && signaturesCurrent(records, now)
                && answersCovered(decoded.answers()) && trustAnchorMatches(dnskeys, dsRecords)
                && dsDigestValid(dnskeys, dsRecords) && signingKeysAvailable(records, dnskeys)
                && cryptographicSignaturesValid(records, dnskeys);
    }

    /**
     * Caches section-level validation successes.
     *
     * @param decoded decoded response
     * @param now     current instant
     */
    private void cacheSectionResults(final DnsDecodedResponse decoded, final Instant now) {
        cacheKind(Kind.RRSET, decoded.question().name(), decoded.question().typeCode(), decoded.answers(), now);
        cacheKind(
                Kind.DNSKEY,
                DnsName.ROOT,
                DnsRecordType.DNSKEY.code(),
                recordsOfType(records(decoded), DnsRecordType.DNSKEY),
                now);
        cacheKind(
                Kind.DS,
                DnsName.ROOT,
                DnsRecordType.DS.code(),
                recordsOfType(records(decoded), DnsRecordType.DS),
                now);
    }

    /**
     * Caches one validation-result kind.
     *
     * @param kind    validation cache kind
     * @param owner   owner name
     * @param type    record type code
     * @param records validated records
     * @param now     current instant
     */
    private void cacheKind(
            final Kind kind,
            final String owner,
            final int type,
            final List<DnsRecord> records,
            final Instant now) {
        if (!records.isEmpty()) {
            validationCache.putSuccess(kind, owner, type, records, nearestRrsigExpiration(records), now);
        }
    }

    /**
     * Creates an authenticated response.
     *
     * @param query   original query
     * @param decoded decoded response
     * @return authenticated DNS response
     */
    private static DnsResponse authenticated(final DnsQuery query, final DnsDecodedResponse decoded) {
        return new DnsResponse(query, decoded.responseCode(), false, true, decoded.truncated(), decoded.answers(),
                decoded.authorities(), decoded.additionals(), true, null);
    }

    /**
     * Creates the deterministic validation failure response.
     *
     * @param query   original query
     * @param decoded decoded response
     * @param now     current instant
     * @return SERVFAIL or insecure response
     */
    private DnsResponse insecure(final DnsQuery query, final DnsDecodedResponse decoded, final Instant now) {
        if (provesInsecureDelegation(decoded, now)) {
            return decoded.toResponse(query);
        }
        return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
    }

    /**
     * Returns whether NSEC or NSEC3 records prove a negative response.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when negative proof validates
     */
    private boolean provesNegative(final DnsDecodedResponse decoded, final Instant now) {
        return provesNsecNegative(decoded, now) || provesNsec3Negative(decoded, now);
    }

    /**
     * Returns whether NSEC or NSEC3 records prove insecure delegation.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when insecure delegation proof validates
     */
    private boolean provesInsecureDelegation(final DnsDecodedResponse decoded, final Instant now) {
        return provesNsecInsecureDelegation(decoded, now) || provesNsec3InsecureDelegation(decoded, now);
    }

    /**
     * Returns whether NSEC records prove a negative response.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when NSEC proof validates
     */
    private boolean provesNsecNegative(final DnsDecodedResponse decoded, final Instant now) {
        try {
            return nsecProofValidator.provesNegative(decoded, now);
        } catch (final RuntimeException e) {
            return false;
        }
    }

    /**
     * Returns whether NSEC3 records prove a negative response.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when NSEC3 proof validates
     */
    private boolean provesNsec3Negative(final DnsDecodedResponse decoded, final Instant now) {
        try {
            return nsec3ProofValidator.provesNegative(decoded, now);
        } catch (final RuntimeException e) {
            return false;
        }
    }

    /**
     * Returns whether NSEC records prove insecure delegation.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when NSEC proof validates
     */
    private boolean provesNsecInsecureDelegation(final DnsDecodedResponse decoded, final Instant now) {
        try {
            return nsecProofValidator.provesInsecureDelegation(decoded, now);
        } catch (final RuntimeException e) {
            return false;
        }
    }

    /**
     * Returns whether NSEC3 records prove insecure delegation.
     *
     * @param decoded decoded response
     * @param now     current instant
     * @return true when NSEC3 proof validates
     */
    private boolean provesNsec3InsecureDelegation(final DnsDecodedResponse decoded, final Instant now) {
        try {
            return nsec3ProofValidator.provesInsecureDelegation(decoded, now);
        } catch (final RuntimeException e) {
            return false;
        }
    }

    /**
     * Returns whether decoded sections contain DNSSEC material.
     *
     * @param decoded decoded response
     * @return true when DNSSEC records or AD are present
     */
    private static boolean containsDnssecMaterial(final DnsDecodedResponse decoded) {
        return containsDnssecMaterial(decoded.answers()) || containsDnssecMaterial(decoded.authorities())
                || containsDnssecMaterial(decoded.additionals()) || decoded.authenticData();
    }

    /**
     * Returns whether records contain DNSSEC material.
     *
     * @param records records to scan
     * @return true when DNSSEC material exists
     */
    private static boolean containsDnssecMaterial(final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            if (dnssecType(record.typeCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a type code is DNSSEC material.
     *
     * @param typeCode record type code
     * @return true when the type is DNSSEC related
     */
    private static boolean dnssecType(final int typeCode) {
        return typeCode == DnsRecordType.RRSIG.code() || typeCode == DnsRecordType.DNSKEY.code()
                || typeCode == DnsRecordType.DS.code() || typeCode == DnsRecordType.NSEC.code()
                || typeCode == DnsRecordType.NSEC3.code() || typeCode == DnsRecordType.NSEC3PARAM.code();
    }

    /**
     * Returns all response records.
     *
     * @param decoded decoded response
     * @return response records
     */
    private static List<DnsRecord> records(final DnsDecodedResponse decoded) {
        final ArrayList<DnsRecord> records = new ArrayList<>();
        records.addAll(decoded.answers());
        records.addAll(decoded.authorities());
        records.addAll(decoded.additionals());
        return List.copyOf(records);
    }

    /**
     * Filters records by type.
     *
     * @param records source records
     * @param type    desired type
     * @return records of the desired type
     */
    private static List<DnsRecord> recordsOfType(final List<DnsRecord> records, final DnsRecordType type) {
        final ArrayList<DnsRecord> result = new ArrayList<>();
        for (final DnsRecord record : records) {
            if (record.typeCode() == type.code()) {
                result.add(record);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Returns whether all DNSSEC algorithms in records are enabled.
     *
     * @param records response records
     * @return true when every algorithm is supported
     */
    private static boolean algorithmsEnabled(final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            final int algorithm = algorithm(record);
            if (algorithm >= 0 && !algorithmEnabled(algorithm)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts DNSSEC algorithm from one record.
     *
     * @param record DNSSEC record
     * @return algorithm code, or {@code -1}
     */
    private static int algorithm(final DnsRecord record) {
        final byte[] data = record.wireData();
        if (record.typeCode() == DnsRecordType.RRSIG.code()) {
            return data.length < 3 ? -1 : DnsCodec.readUnsignedByte(data, 2);
        }
        if (record.typeCode() == DnsRecordType.DNSKEY.code()) {
            return data.length < DNSKEY_FIXED_BYTES ? -1 : DnsCodec.readUnsignedByte(data, 3);
        }
        if (record.typeCode() == DnsRecordType.DS.code()) {
            return data.length < DS_FIXED_BYTES ? -1 : DnsCodec.readUnsignedByte(data, 2);
        }
        return -1;
    }

    /**
     * Returns whether an algorithm is enabled.
     *
     * @param algorithm DNSSEC algorithm code
     * @return true when enabled
     */
    private static boolean algorithmEnabled(final int algorithm) {
        return algorithm == ALGORITHM_RSASHA1 || algorithm == ALGORITHM_RSASHA1_NSEC3
                || algorithm == ALGORITHM_RSASHA256 || algorithm == ALGORITHM_RSASHA512
                || algorithm == ALGORITHM_ECDSAP256SHA256 || algorithm == ALGORITHM_ECDSAP384SHA384
                || algorithm == ALGORITHM_ED25519 || algorithm == ALGORITHM_ED448;
    }

    /**
     * Returns whether every signature is currently valid.
     *
     * @param records records to inspect
     * @param now     current instant
     * @return true when all signatures are inside their windows
     */
    private static boolean signaturesCurrent(final List<DnsRecord> records, final Instant now) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.RRSIG.code() && !signatureCurrent(record, now)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether one RRSIG is currently valid.
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
        final long epoch = now.getEpochSecond();
        return inception <= epoch && epoch <= expiration;
    }

    /**
     * Returns whether all non-signature answers are covered by a same-owner RRSIG.
     *
     * @param answers answer section
     * @return true when covered
     */
    private static boolean answersCovered(final List<DnsRecord> answers) {
        for (final DnsRecord answer : answers) {
            if (answer.typeCode() != DnsRecordType.RRSIG.code() && !covered(answer, answers)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether one record is covered by an RRSIG in the same section.
     *
     * @param answer  answer record
     * @param records section records
     * @return true when covered
     */
    private static boolean covered(final DnsRecord answer, final List<DnsRecord> records) {
        for (final DnsRecord signature : records) {
            if (signature.typeCode() == DnsRecordType.RRSIG.code() && signature.name().equals(answer.name())
                    && typeCovered(signature) == answer.typeCode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether configured anchors match response-carried chain material.
     *
     * @param dnskeys   DNSKEY records
     * @param dsRecords DS records
     * @return true when at least one trust anchor matches
     */
    private boolean trustAnchorMatches(final List<DnsRecord> dnskeys, final List<DnsRecord> dsRecords) {
        for (final DnsTrustAnchor trustAnchor : trustAnchors) {
            if (trustAnchor.type() == DnsRecordType.DNSKEY && dnskeyAnchorMatches(trustAnchor, dnskeys)) {
                return true;
            }
            if (trustAnchor.type() == DnsRecordType.DS
                    && (dsAnchorMatches(trustAnchor, dsRecords) || dnskeyMatchesDsAnchor(trustAnchor, dnskeys))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a DNSKEY trust anchor matches a carried DNSKEY.
     *
     * @param trustAnchor DNSKEY trust anchor
     * @param dnskeys     DNSKEY records
     * @return true when matched
     */
    private static boolean dnskeyAnchorMatches(final DnsTrustAnchor trustAnchor, final List<DnsRecord> dnskeys) {
        for (final DnsRecord dnskey : dnskeys) {
            if (dnskey.name().equals(trustAnchor.name()) && dnskey.typeCode() == DnsRecordType.DNSKEY.code()
                    && Arrays.equals(dnskey.wireData(), trustAnchor.data().wireData())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a DS trust anchor matches a carried DS record.
     *
     * @param trustAnchor DS trust anchor
     * @param dsRecords   DS records
     * @return true when matched
     */
    private static boolean dsAnchorMatches(final DnsTrustAnchor trustAnchor, final List<DnsRecord> dsRecords) {
        for (final DnsRecord ds : dsRecords) {
            if (ds.name().equals(trustAnchor.name()) && Arrays.equals(ds.wireData(), trustAnchor.data().wireData())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a DNSKEY validates against a DS trust anchor.
     *
     * @param trustAnchor DS trust anchor
     * @param dnskeys     DNSKEY records
     * @return true when matched
     */
    private static boolean dnskeyMatchesDsAnchor(final DnsTrustAnchor trustAnchor, final List<DnsRecord> dnskeys) {
        for (final DnsRecord dnskey : dnskeys) {
            if (dnskeyMatchesDs(
                    dnskey,
                    trustAnchor.name(),
                    trustAnchor.algorithm(),
                    trustAnchor.keyTag(),
                    trustAnchor.digestType(),
                    trustAnchor.data().wireData())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether carried DS records validate carried DNSKEY records.
     *
     * @param dnskeys   DNSKEY records
     * @param dsRecords DS records
     * @return true when no DS is present or every DS can match a DNSKEY
     */
    private static boolean dsDigestValid(final List<DnsRecord> dnskeys, final List<DnsRecord> dsRecords) {
        for (final DnsRecord ds : dsRecords) {
            if (!dsMatchesAnyKey(ds, dnskeys)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether one DS record matches any DNSKEY.
     *
     * @param ds      DS record
     * @param dnskeys DNSKEY records
     * @return true when matched
     */
    private static boolean dsMatchesAnyKey(final DnsRecord ds, final List<DnsRecord> dnskeys) {
        final byte[] data = ds.wireData();
        if (data.length <= DS_FIXED_BYTES) {
            return false;
        }
        for (final DnsRecord dnskey : dnskeys) {
            if (dnskeyMatchesDs(
                    dnskey,
                    ds.name(),
                    DnsCodec.readUnsignedByte(data, 2),
                    DnsCodec.readUnsignedShort(data, 0),
                    DnsCodec.readUnsignedByte(data, 3),
                    data)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether one DNSKEY matches one DS value.
     *
     * @param dnskey     DNSKEY record
     * @param owner      DS owner
     * @param algorithm  DNSSEC algorithm
     * @param keyTag     DNSKEY key tag
     * @param digestType DS digest type
     * @param dsRdata    DS RDATA bytes
     * @return true when matched
     */
    private static boolean dnskeyMatchesDs(
            final DnsRecord dnskey,
            final String owner,
            final int algorithm,
            final int keyTag,
            final int digestType,
            final byte[] dsRdata) {
        final byte[] keyData = dnskey.wireData();
        return dnskey.typeCode() == DnsRecordType.DNSKEY.code() && dnskey.name().equals(owner)
                && keyData.length > DNSKEY_FIXED_BYTES && DnsCodec.readUnsignedByte(keyData, 3) == algorithm
                && DnsSigningKey.keyTag(keyData) == keyTag
                && MessageDigest.isEqual(
                        dsDigest(dnskey, digestType),
                        Arrays.copyOfRange(dsRdata, DS_FIXED_BYTES, dsRdata.length));
    }

    /**
     * Returns whether every RRSIG has a corresponding DNSKEY by key tag.
     *
     * @param records records containing RRSIGs
     * @param dnskeys DNSKEY records
     * @return true when signing keys are present
     */
    private static boolean signingKeysAvailable(final List<DnsRecord> records, final List<DnsRecord> dnskeys) {
        for (final DnsRecord signature : records) {
            if (signature.typeCode() == DnsRecordType.RRSIG.code() && !signingKeyAvailable(signature, dnskeys)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether one RRSIG has a matching DNSKEY.
     *
     * @param signature RRSIG record
     * @param dnskeys   DNSKEY records
     * @return true when a matching DNSKEY exists
     */
    private static boolean signingKeyAvailable(final DnsRecord signature, final List<DnsRecord> dnskeys) {
        final int keyTag = keyTag(signature);
        final int algorithm = algorithm(signature);
        for (final DnsRecord dnskey : dnskeys) {
            final byte[] data = dnskey.wireData();
            if (data.length > DNSKEY_FIXED_BYTES && (dnskeyFlags(dnskey) & DNSKEY_ZONE_FLAG) != 0
                    && DnsCodec.readUnsignedByte(data, 3) == algorithm && DnsSigningKey.keyTag(data) == keyTag) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether every carried RRSIG cryptographically validates against the available DNSKEY records.
     *
     * @param records all response records
     * @param dnskeys available DNSKEY records
     * @return true when every RRSIG verifies
     */
    private static boolean cryptographicSignaturesValid(final List<DnsRecord> records, final List<DnsRecord> dnskeys) {
        for (final DnsRecord signature : records) {
            if (signature.typeCode() == DnsRecordType.RRSIG.code()
                    && !cryptographicSignatureValid(signature, records, dnskeys)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether one RRSIG validates over its covered RRSet.
     *
     * @param signature RRSIG record
     * @param records   all response records
     * @param dnskeys   available DNSKEY records
     * @return true when a matching DNSKEY verifies the signature
     */
    private static boolean cryptographicSignatureValid(
            final DnsRecord signature,
            final List<DnsRecord> records,
            final List<DnsRecord> dnskeys) {
        final SignatureFields fields = SignatureFields.from(signature);
        final List<DnsRecord> rrset = rrset(signature.name(), signature.recordClass(), fields.typeCovered, records);
        if (rrset.isEmpty()) {
            return false;
        }
        for (final DnsRecord dnskey : dnskeys) {
            if (dnskeyMatches(dnskey, fields) && verifySignature(fields, dnskey, rrset)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the RRSet covered by an RRSIG.
     *
     * @param owner    covered owner name
     * @param rrClass  covered record class
     * @param typeCode covered type code
     * @param records  all response records
     * @return covered RRSet records
     */
    private static List<DnsRecord> rrset(
            final String owner,
            final int rrClass,
            final int typeCode,
            final List<DnsRecord> records) {
        final ArrayList<DnsRecord> result = new ArrayList<>();
        for (final DnsRecord record : records) {
            if (record.name().equals(owner) && record.recordClass() == rrClass && record.typeCode() == typeCode) {
                result.add(record);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Returns whether a DNSKEY matches parsed RRSIG fields.
     *
     * @param key    DNSKEY record
     * @param fields parsed RRSIG fields
     * @return true when owner, protocol, algorithm, and key tag match
     */
    private static boolean dnskeyMatches(final DnsRecord key, final SignatureFields fields) {
        final byte[] data = key.wireData();
        return key.typeCode() == DnsRecordType.DNSKEY.code() && data.length > DNSKEY_FIXED_BYTES
                && key.name().equals(fields.signerName) && DnsCodec.readUnsignedByte(data, 2) == DNSKEY_PROTOCOL
                && DnsCodec.readUnsignedByte(data, 3) == fields.algorithm
                && DnsSigningKey.keyTag(data) == fields.keyTag;
    }

    /**
     * Verifies one RRSIG over one RRSet using a DNSKEY.
     *
     * @param fields parsed RRSIG fields
     * @param key    matching DNSKEY record
     * @param rrset  covered RRSet
     * @return true when the signature verifies
     */
    private static boolean verifySignature(
            final SignatureFields fields,
            final DnsRecord key,
            final List<DnsRecord> rrset) {
        try {
            final Signature verifier = Signature.getInstance(signatureAlgorithm(fields.algorithm));
            verifier.initVerify(publicKey(key, fields.algorithm));
            verifier.update(signedData(fields, rrset));
            return verifier.verify(signatureForJca(fields.algorithm, fields.signature));
        } catch (final GeneralSecurityException e) {
            throw new ProtocolException("Unable to verify DNSSEC signature", e);
        }
    }

    /**
     * Builds DNSSEC signed data for one RRSet.
     *
     * @param fields parsed RRSIG fields
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
     * Builds one canonical RR for DNSSEC signature validation.
     *
     * @param record covered record
     * @param fields parsed RRSIG fields
     * @return canonical RR bytes
     */
    private static byte[] canonicalRecord(final DnsRecord record, final SignatureFields fields) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            DnsName.write(output, canonicalOwner(record.name(), fields.labels));
            output.writeShort(record.typeCode());
            output.writeShort(record.recordClass());
            output.writeInt((int) fields.originalTtl);
            final byte[] data = record.wireData();
            output.writeShort(data.length);
            output.write(data);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to canonicalize DNSSEC RRSet", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Returns the canonical owner name used for wildcard-signed records.
     *
     * @param owner  record owner name
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
     * Creates a public key from DNSKEY RDATA.
     *
     * @param key       DNSKEY record
     * @param algorithm DNSSEC algorithm code
     * @return JCA public key
     * @throws GeneralSecurityException if the key cannot be decoded
     */
    private static PublicKey publicKey(final DnsRecord key, final int algorithm) throws GeneralSecurityException {
        final byte[] data = key.wireData();
        if (data.length <= DNSKEY_FIXED_BYTES) {
            throw new ProtocolException("DNSSEC DNSKEY RDATA is truncated");
        }
        final byte[] publicKey = Arrays.copyOfRange(data, DNSKEY_FIXED_BYTES, data.length);
        return switch (algorithm) {
            case ALGORITHM_RSASHA1, ALGORITHM_RSASHA1_NSEC3, ALGORITHM_RSASHA256, ALGORITHM_RSASHA512 -> rsaPublicKey(
                    publicKey);
            case ALGORITHM_ECDSAP256SHA256 -> ecPublicKey(publicKey, 32, "secp256r1");
            case ALGORITHM_ECDSAP384SHA384 -> ecPublicKey(publicKey, 48, "secp384r1");
            case ALGORITHM_ED25519 -> edPublicKey(publicKey, "Ed25519", ED25519_X509_PREFIX);
            case ALGORITHM_ED448 -> edPublicKey(publicKey, "Ed448", ED448_X509_PREFIX);
            default -> throw new ProtocolException("Unsupported DNSSEC algorithm: " + algorithm);
        };
    }

    /**
     * Creates an RSA public key from DNSKEY public-key bytes.
     *
     * @param publicKey DNSKEY public-key bytes
     * @return RSA public key
     * @throws GeneralSecurityException if the key cannot be decoded
     */
    private static PublicKey rsaPublicKey(final byte[] publicKey) throws GeneralSecurityException {
        if (publicKey.length < 3) {
            throw new ProtocolException("DNSSEC RSA public key is truncated");
        }
        int cursor = 0;
        int exponentLength = DnsCodec.readUnsignedByte(publicKey, cursor++);
        if (exponentLength == 0) {
            if (publicKey.length < 3) {
                throw new ProtocolException("DNSSEC RSA exponent length is truncated");
            }
            exponentLength = DnsCodec.readUnsignedShort(publicKey, cursor);
            cursor += Short.BYTES;
        }
        if (exponentLength <= 0 || cursor + exponentLength >= publicKey.length) {
            throw new ProtocolException("DNSSEC RSA public key length is invalid");
        }
        final BigInteger exponent = new BigInteger(1, Arrays.copyOfRange(publicKey, cursor, cursor + exponentLength));
        cursor += exponentLength;
        final BigInteger modulus = new BigInteger(1, Arrays.copyOfRange(publicKey, cursor, publicKey.length));
        return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }

    /**
     * Creates an elliptic-curve public key from DNSKEY public-key bytes.
     *
     * @param publicKey  DNSKEY public-key bytes
     * @param coordinate coordinate byte length
     * @param curveName  JCA curve name
     * @return elliptic-curve public key
     * @throws GeneralSecurityException if the key cannot be decoded
     */
    private static PublicKey ecPublicKey(final byte[] publicKey, final int coordinate, final String curveName)
            throws GeneralSecurityException {
        if (publicKey.length != coordinate * 2) {
            throw new ProtocolException("DNSSEC ECDSA public key length is invalid");
        }
        final AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec(curveName));
        final ECParameterSpec spec = parameters.getParameterSpec(ECParameterSpec.class);
        final ECPoint point = new ECPoint(new BigInteger(1, Arrays.copyOfRange(publicKey, 0, coordinate)),
                new BigInteger(1, Arrays.copyOfRange(publicKey, coordinate, publicKey.length)));
        return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(point, spec));
    }

    /**
     * Creates an Edwards-curve public key from raw DNSKEY public-key bytes.
     *
     * @param publicKey DNSKEY public-key bytes
     * @param algorithm JCA key algorithm
     * @param prefix    SubjectPublicKeyInfo DER prefix
     * @return Edwards-curve public key
     * @throws GeneralSecurityException if the key cannot be decoded
     */
    private static PublicKey edPublicKey(final byte[] publicKey, final String algorithm, final byte[] prefix)
            throws GeneralSecurityException {
        final byte[] encoded = Arrays.copyOf(prefix, prefix.length + publicKey.length);
        System.arraycopy(publicKey, 0, encoded, prefix.length, publicKey.length);
        return KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(encoded));
    }

    /**
     * Returns the JCA signature algorithm for a DNSSEC algorithm.
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
            default -> throw new ProtocolException("Unsupported DNSSEC algorithm: " + algorithm);
        };
    }

    /**
     * Converts a DNSSEC wire signature to the format expected by JCA.
     *
     * @param algorithm DNSSEC algorithm code
     * @param signature DNSSEC wire signature bytes
     * @return JCA-compatible signature bytes
     */
    private static byte[] signatureForJca(final int algorithm, final byte[] signature) {
        return switch (algorithm) {
            case ALGORITHM_ECDSAP256SHA256 -> ecdsaDerSignature(signature, 32);
            case ALGORITHM_ECDSAP384SHA384 -> ecdsaDerSignature(signature, 48);
            default -> signature;
        };
    }

    /**
     * Converts a raw DNSSEC ECDSA signature to DER.
     *
     * @param signature  raw R and S concatenation
     * @param coordinate coordinate byte length
     * @return DER-encoded ECDSA signature
     */
    private static byte[] ecdsaDerSignature(final byte[] signature, final int coordinate) {
        if (signature.length != coordinate * 2) {
            throw new ProtocolException("DNSSEC ECDSA signature length is invalid");
        }
        final byte[] r = derInteger(Arrays.copyOfRange(signature, 0, coordinate));
        final byte[] s = derInteger(Arrays.copyOfRange(signature, coordinate, signature.length));
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(0x30);
        bytes.write(r.length + s.length);
        bytes.writeBytes(r);
        bytes.writeBytes(s);
        return bytes.toByteArray();
    }

    /**
     * Encodes a positive integer in DER INTEGER format.
     *
     * @param value unsigned big-endian integer bytes
     * @return DER INTEGER bytes
     */
    private static byte[] derInteger(final byte[] value) {
        int offset = 0;
        while (offset < value.length - 1 && value[offset] == 0) {
            offset++;
        }
        final boolean prefixZero = (value[offset] & 0x80) != 0;
        final int length = value.length - offset + (prefixZero ? 1 : 0);
        final byte[] encoded = new byte[length + 2];
        encoded[0] = 0x02;
        encoded[1] = (byte) length;
        int cursor = 2;
        if (prefixZero) {
            encoded[cursor++] = 0;
        }
        System.arraycopy(value, offset, encoded, cursor, value.length - offset);
        return encoded;
    }

    /**
     * Finds the nearest RRSIG expiration among records.
     *
     * @param records records to inspect
     * @return nearest expiration, or {@code null}
     */
    private static Instant nearestRrsigExpiration(final List<DnsRecord> records) {
        Instant nearest = null;
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.RRSIG.code() && record.wireData().length >= 12) {
                final Instant expiration = Instant
                        .ofEpochSecond(DnsCodec.readUnsignedInt(record.wireData(), RRSIG_EXPIRATION_OFFSET));
                nearest = nearest == null || expiration.isBefore(nearest) ? expiration : nearest;
            }
        }
        return nearest;
    }

    /**
     * Computes a DS digest for one DNSKEY.
     *
     * @param dnskey     DNSKEY record
     * @param digestType DS digest type
     * @return digest bytes
     */
    private static byte[] dsDigest(final DnsRecord dnskey, final int digestType) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(dsDigestAlgorithm(digestType));
            digest.update(DnsName.wire(dnskey.name()));
            digest.update(dnskey.wireData());
            return digest.digest();
        } catch (final GeneralSecurityException e) {
            throw new ProtocolException("Unsupported DNSSEC DS digest type: " + digestType, e);
        }
    }

    /**
     * Returns the JCA digest algorithm for a DS digest type.
     *
     * @param digestType DS digest type
     * @return JCA digest algorithm
     */
    private static String dsDigestAlgorithm(final int digestType) {
        return switch (digestType) {
            case DS_DIGEST_SHA1 -> "SHA-1";
            case DS_DIGEST_SHA256 -> "SHA-256";
            case DS_DIGEST_SHA384 -> "SHA-384";
            default -> throw new ProtocolException("Unsupported DNSSEC DS digest type: " + digestType);
        };
    }

    /**
     * Reads the DNSKEY flags field.
     *
     * @param dnskey DNSKEY record
     * @return DNSKEY flags
     */
    private static int dnskeyFlags(final DnsRecord dnskey) {
        final byte[] data = dnskey.wireData();
        if (data.length < DNSKEY_FIXED_BYTES) {
            throw new ProtocolException("DNSSEC DNSKEY RDATA is truncated");
        }
        return DnsCodec.readUnsignedShort(data, 0);
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
     * Reads the key tag from one RRSIG.
     *
     * @param signature RRSIG record
     * @return key tag
     */
    private static int keyTag(final DnsRecord signature) {
        final byte[] data = signature.wireData();
        if (data.length < RRSIG_FIXED_BYTES) {
            throw new ProtocolException("DNSSEC RRSIG RDATA is truncated");
        }
        return DnsCodec.readUnsignedShort(data, RRSIG_KEY_TAG_OFFSET);
    }

    /**
     * Parsed RRSIG RDATA fields used for cryptographic validation.
     *
     * @author Kimi Liu
     */
    private static final class SignatureFields {

        /**
         * Covered RR type.
         */
        private final int typeCovered;

        /**
         * DNSSEC algorithm code.
         */
        private final int algorithm;

        /**
         * RRSIG labels field.
         */
        private final int labels;

        /**
         * Original RRSet TTL.
         */
        private final long originalTtl;

        /**
         * Signature expiration epoch seconds.
         */
        private final long expiration;

        /**
         * Signature inception epoch seconds.
         */
        private final long inception;

        /**
         * DNSKEY key tag.
         */
        private final int keyTag;

        /**
         * Signer DNS name.
         */
        private final String signerName;

        /**
         * Signature bytes from RRSIG RDATA.
         */
        private final byte[] signature;

        /**
         * Creates parsed RRSIG fields.
         *
         * @param typeCovered covered RR type
         * @param algorithm   DNSSEC algorithm code
         * @param labels      RRSIG labels field
         * @param originalTtl original RRSet TTL
         * @param expiration  signature expiration epoch seconds
         * @param inception   signature inception epoch seconds
         * @param keyTag      DNSKEY key tag
         * @param signerName  signer DNS name
         * @param signature   signature bytes from RRSIG RDATA
         */
        private SignatureFields(final int typeCovered, final int algorithm, final int labels, final long originalTtl,
                final long expiration, final long inception, final int keyTag, final String signerName,
                final byte[] signature) {
            this.typeCovered = typeCovered;
            this.algorithm = algorithm;
            this.labels = labels;
            this.originalTtl = originalTtl;
            this.expiration = expiration;
            this.inception = inception;
            this.keyTag = keyTag;
            this.signerName = signerName;
            this.signature = Arrays.copyOf(signature, signature.length);
        }

        /**
         * Parses RRSIG RDATA.
         *
         * @param record RRSIG record
         * @return parsed RRSIG fields
         */
        private static SignatureFields from(final DnsRecord record) {
            final byte[] data = record.wireData();
            if (data.length < RRSIG_FIXED_BYTES) {
                throw new ProtocolException("DNSSEC RRSIG RDATA is truncated");
            }
            final DnsName.ReadResult signer = DnsName.read(data, RRSIG_FIXED_BYTES);
            if (signer.nextOffset() >= data.length) {
                throw new ProtocolException("DNSSEC RRSIG signature bytes are missing");
            }
            return new SignatureFields(DnsCodec.readUnsignedShort(data, 0), DnsCodec.readUnsignedByte(data, 2),
                    DnsCodec.readUnsignedByte(data, 3), DnsCodec.readUnsignedInt(data, 4),
                    DnsCodec.readUnsignedInt(data, RRSIG_EXPIRATION_OFFSET),
                    DnsCodec.readUnsignedInt(data, RRSIG_INCEPTION_OFFSET),
                    DnsCodec.readUnsignedShort(data, RRSIG_KEY_TAG_OFFSET), signer.name(),
                    Arrays.copyOfRange(data, signer.nextOffset(), data.length));
        }

        /**
         * Encodes the RRSIG RDATA prefix used as signed data.
         *
         * @return RRSIG RDATA fields preceding the signature bytes
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
                throw new ProtocolException("Unable to encode DNSSEC RRSIG prefix", e);
            }
            return bytes.toByteArray();
        }

    }

}
