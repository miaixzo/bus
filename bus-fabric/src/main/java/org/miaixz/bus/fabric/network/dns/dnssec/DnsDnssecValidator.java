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
 * DNSSEC response validator for decoded recursive responses.
 *
 * <p>
 * The validator is immutable, thread-safe, and side-effect free. It validates RRSIG temporal validity and answer RRSet
 * coverage before setting AD on responses that already carry DNSSEC material. It does not allocate sockets, perform
 * network IO, or read persistent state.
 * </p>
 *
 * @author Kimi Liu
 */
public class DnsDnssecValidator {

    /**
     * RRSIG RDATA byte offset of the signature expiration field.
     */
    private static final int RRSIG_EXPIRATION_OFFSET = 8;

    /**
     * RRSIG RDATA byte offset of the signature inception field.
     */
    private static final int RRSIG_INCEPTION_OFFSET = 12;

    /**
     * Minimum RRSIG RDATA bytes through key tag.
     */
    private static final int RRSIG_FIXED_BYTES = 18;

    /**
     * DNSKEY RDATA byte length before public-key bytes.
     */
    private static final int DNSKEY_FIXED_BYTES = 4;

    /**
     * DNSSEC DNSKEY protocol value.
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
     * DS digest type for SHA-1.
     */
    private static final int DS_DIGEST_SHA1 = 1;

    /**
     * DS digest type for SHA-256.
     */
    private static final int DS_DIGEST_SHA256 = 2;

    /**
     * DS digest type for SHA-384.
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
     * Clock used for signature time-window validation.
     */
    private final Clock clock;

    /**
     * DNSKEY and DS trust anchors supplied by the active DNS snapshot.
     */
    private final List<DnsTrustAnchor> trustAnchors;

    /**
     * DNSSEC validation-result cache.
     */
    private final DnsValidationCache validationCache;

    /**
     * Trust-anchor chain validator.
     */
    private final DnsDnssecChainValidator chainValidator;

    /**
     * Creates a validator using the system UTC clock.
     */
    public DnsDnssecValidator() {
        this(Clock.systemUTC(), List.of(), new DnsValidationCache());
    }

    /**
     * Creates a validator using the system UTC clock and explicit trust anchors.
     *
     * @param trustAnchors DNSSEC trust anchors
     */
    public DnsDnssecValidator(final List<DnsTrustAnchor> trustAnchors) {
        this(Clock.systemUTC(), trustAnchors, new DnsValidationCache());
    }

    /**
     * Creates a validator with an explicit clock.
     *
     * @param clock clock used for RRSIG time validation
     */
    public DnsDnssecValidator(final Clock clock) {
        this(clock, List.of(), new DnsValidationCache());
    }

    /**
     * Creates a validator with an explicit clock and DNSSEC trust anchors.
     *
     * @param clock        clock used for RRSIG time validation
     * @param trustAnchors DNSSEC trust anchors
     */
    public DnsDnssecValidator(final Clock clock, final List<DnsTrustAnchor> trustAnchors) {
        this(clock, trustAnchors, new DnsValidationCache());
    }

    /**
     * Creates a validator with an explicit clock, DNSSEC trust anchors, and validation cache.
     *
     * @param clock           clock used for RRSIG time validation
     * @param trustAnchors    DNSSEC trust anchors
     * @param validationCache validation-result cache
     */
    public DnsDnssecValidator(final Clock clock, final List<DnsTrustAnchor> trustAnchors,
            final DnsValidationCache validationCache) {
        if (clock == null) {
            throw new ValidateException("DNSSEC validator clock must not be null");
        }
        if (validationCache == null) {
            throw new ValidateException("DNSSEC validation cache must not be null");
        }
        this.clock = clock;
        this.trustAnchors = immutableTrustAnchors(trustAnchors);
        this.validationCache = validationCache;
        this.chainValidator = new DnsDnssecChainValidator(clock, this.trustAnchors, validationCache);
    }

    /**
     * Validates a decoded response and returns a response suitable for the original query.
     *
     * @param query   original query
     * @param decoded decoded upstream response
     * @return DNS response with AD set only when DNSSEC material validates
     */
    public DnsResponse validate(final DnsQuery query, final DnsDecodedResponse decoded) {
        return chainValidator.validate(query, decoded);
    }

    /**
     * Validates a decoded response using the local single-response DNSSEC checks.
     *
     * @param query   original query
     * @param decoded decoded upstream response
     * @return DNS response with AD set only when local DNSSEC material validates
     */
    public DnsResponse validateLocal(final DnsQuery query, final DnsDecodedResponse decoded) {
        if (query == null) {
            throw new ValidateException("DNSSEC validation query must not be null");
        }
        if (decoded == null) {
            throw new ValidateException("DNSSEC validation response must not be null");
        }
        if (query.checkingDisabled() || !query.dnssecOk() || !containsDnssecMaterial(decoded)) {
            return decoded.toResponse(query);
        }
        final Instant now = Instant.now(clock);
        if (validationCache.containsResponse(decoded, now)) {
            return new DnsResponse(query, decoded.responseCode(), false, true, decoded.truncated(), decoded.answers(),
                    decoded.authorities(), decoded.additionals(), true, null);
        }
        try {
            if (!allSignaturesCurrent(decoded) || !answersCovered(decoded.answers())
                    || !cryptographicSignaturesValid(decoded, trustAnchors)) {
                return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
            }
            validationCache.putResponseSuccess(decoded, now);
            return new DnsResponse(query, decoded.responseCode(), false, true, decoded.truncated(), decoded.answers(),
                    decoded.authorities(), decoded.additionals(), true, null);
        } catch (final RuntimeException e) {
            return DnsResponse.empty(query, DnsResponseCode.SERVFAIL, false);
        }
    }

    /**
     * Returns the validation-result cache owned by this validator.
     *
     * @return validation cache
     */
    public DnsValidationCache validationCache() {
        return validationCache;
    }

    /**
     * Returns whether decoded sections contain DNSSEC records.
     *
     * @param decoded decoded response
     * @return true when DNSSEC material is present
     */
    private static boolean containsDnssecMaterial(final DnsDecodedResponse decoded) {
        return containsDnssecMaterial(decoded.answers()) || containsDnssecMaterial(decoded.authorities())
                || containsDnssecMaterial(decoded.additionals()) || decoded.authenticData();
    }

    /**
     * Returns whether a record list contains DNSSEC records.
     *
     * @param records records to scan
     * @return true when DNSSEC material is present
     */
    private static boolean containsDnssecMaterial(final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            if (dnssecType(record.type())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether every RRSIG in the decoded response is inside its validity window.
     *
     * @param decoded decoded response
     * @return true when all signatures are current
     */
    private boolean allSignaturesCurrent(final DnsDecodedResponse decoded) {
        return signaturesCurrent(decoded.answers()) && signaturesCurrent(decoded.authorities())
                && signaturesCurrent(decoded.additionals());
    }

    /**
     * Returns whether RRSIG records in a list are inside their validity window.
     *
     * @param records records to scan
     * @return true when all signatures are current
     */
    private boolean signaturesCurrent(final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.RRSIG.code() && !signatureCurrent(record)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether a single RRSIG is currently valid.
     *
     * @param record RRSIG record
     * @return true when now is between inception and expiration
     */
    private boolean signatureCurrent(final DnsRecord record) {
        final byte[] data = record.wireData();
        if (data.length < RRSIG_FIXED_BYTES) {
            throw new ProtocolException("DNSSEC RRSIG RDATA is truncated");
        }
        final long expiration = DnsCodec.readUnsignedInt(data, RRSIG_EXPIRATION_OFFSET);
        final long inception = DnsCodec.readUnsignedInt(data, RRSIG_INCEPTION_OFFSET);
        final long now = Instant.now(clock).getEpochSecond();
        return inception <= now && now <= expiration;
    }

    /**
     * Returns whether non-signature answer records have a matching same-owner RRSIG.
     *
     * @param answers answer records
     * @return true when every answer RRSet is covered
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
     * Returns whether one answer record is covered by a same-owner RRSIG.
     *
     * @param answer  answer record
     * @param records answer section records
     * @return true when a matching RRSIG is present
     */
    private static boolean covered(final DnsRecord answer, final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.RRSIG.code() && record.name().equals(answer.name())
                    && typeCovered(record) == answer.typeCode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads the covered RR type from RRSIG RDATA.
     *
     * @param record RRSIG record
     * @return covered DNS type code
     */
    private static int typeCovered(final DnsRecord record) {
        final byte[] data = record.wireData();
        if (data.length < RRSIG_FIXED_BYTES) {
            throw new ProtocolException("DNSSEC RRSIG RDATA is truncated");
        }
        return DnsCodec.readUnsignedShort(data, 0);
    }

    /**
     * Validates and copies configured trust anchors.
     *
     * @param trustAnchors source trust anchors
     * @return immutable trust anchors
     */
    private static List<DnsTrustAnchor> immutableTrustAnchors(final List<DnsTrustAnchor> trustAnchors) {
        if (trustAnchors == null) {
            throw new ValidateException("DNSSEC trust anchors must not be null");
        }
        for (final DnsTrustAnchor trustAnchor : trustAnchors) {
            if (trustAnchor == null) {
                throw new ValidateException("DNSSEC trust anchors must not contain null");
            }
        }
        return List.copyOf(trustAnchors);
    }

    /**
     * Verifies RRSIG signatures when matching DNSKEY records are available in the decoded response.
     *
     * <p>
     * Normal recursive answers do not always include DNSKEY records. This method therefore keeps the existing temporal
     * and RRSet coverage validation when no matching key is present, and upgrades to full JCA signature validation when
     * a matching DNSKEY is carried in any response section. When trust anchors are configured, only DNSKEY trust
     * anchors and response-carried DNSKEY records matching a DS trust anchor are accepted as verifier keys.
     * </p>
     *
     * @param decoded      decoded response
     * @param trustAnchors configured DNSSEC trust anchors
     * @return true when signatures verify under the available key policy
     */
    private static boolean cryptographicSignaturesValid(
            final DnsDecodedResponse decoded,
            final List<DnsTrustAnchor> trustAnchors) {
        final List<DnsRecord> keys = dnskeyRecords(decoded, trustAnchors);
        final boolean requireTrustedKey = !trustAnchors.isEmpty();
        if (keys.isEmpty() && requireTrustedKey) {
            return false;
        }
        if (keys.isEmpty()) {
            return true;
        }
        return cryptographicSectionSignaturesValid(decoded.answers(), keys, requireTrustedKey, true)
                && cryptographicSectionSignaturesValid(decoded.authorities(), keys, requireTrustedKey, false);
    }

    /**
     * Verifies cryptographic signatures within one response section.
     *
     * @param records                section records
     * @param keys                   trusted DNSKEY records
     * @param requireTrustedKey      true when a matching trusted key is required
     * @param requireRecordSignature true when every non-RRSIG record in the section must be signed
     * @return true when all required signatures validate
     */
    private static boolean cryptographicSectionSignaturesValid(
            final List<DnsRecord> records,
            final List<DnsRecord> keys,
            final boolean requireTrustedKey,
            final boolean requireRecordSignature) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.RRSIG.code()) {
                continue;
            }
            if ((requireRecordSignature || signedInSection(record, records))
                    && !cryptographicSignatureValid(record, records, keys, requireTrustedKey)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether a record has a matching RRSIG in the same response section.
     *
     * @param record  section record
     * @param records complete section records
     * @return true when a matching RRSIG is present
     */
    private static boolean signedInSection(final DnsRecord record, final List<DnsRecord> records) {
        for (final DnsRecord signature : records) {
            if (signatureCovers(signature, record)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies the matching signature for one answer RRSet when a DNSKEY is available.
     *
     * @param answer            one record from the answer RRSet
     * @param answers           complete answer section
     * @param keys              DNSKEY records from the decoded response
     * @param requireTrustedKey true when validation must find a trusted matching key
     * @return true when key policy is satisfied and a matching signature verifies
     */
    private static boolean cryptographicSignatureValid(
            final DnsRecord answer,
            final List<DnsRecord> answers,
            final List<DnsRecord> keys,
            final boolean requireTrustedKey) {
        boolean matchingKeyPresent = false;
        for (final DnsRecord signature : answers) {
            if (!signatureCovers(signature, answer)) {
                continue;
            }
            final SignatureFields fields = SignatureFields.from(signature);
            final List<DnsRecord> rrset = rrset(answer, answers);
            for (final DnsRecord key : keys) {
                if (!dnskeyMatches(key, fields)) {
                    continue;
                }
                matchingKeyPresent = true;
                if (verifySignature(fields, key, rrset)) {
                    return true;
                }
            }
        }
        return !matchingKeyPresent && !requireTrustedKey;
    }

    /**
     * Returns whether an RRSIG record covers a DNS answer record.
     *
     * @param signature RRSIG record
     * @param answer    answer record
     * @return true when the RRSIG owner and covered type match the answer
     */
    private static boolean signatureCovers(final DnsRecord signature, final DnsRecord answer) {
        return signature.typeCode() == DnsRecordType.RRSIG.code() && signature.name().equals(answer.name())
                && typeCovered(signature) == answer.typeCode();
    }

    /**
     * Returns all DNSKEY records from a decoded response.
     *
     * @param decoded      decoded response
     * @param trustAnchors configured DNSSEC trust anchors
     * @return DNSKEY records across all response sections
     */
    private static List<DnsRecord> dnskeyRecords(
            final DnsDecodedResponse decoded,
            final List<DnsTrustAnchor> trustAnchors) {
        final ArrayList<DnsRecord> responseKeys = new ArrayList<>();
        collectDnskeys(responseKeys, decoded.answers());
        collectDnskeys(responseKeys, decoded.authorities());
        collectDnskeys(responseKeys, decoded.additionals());
        if (trustAnchors.isEmpty()) {
            return List.copyOf(responseKeys);
        }
        final ArrayList<DnsRecord> keys = trustedResponseDnskeys(responseKeys, trustAnchors);
        collectTrustAnchorDnskeys(keys, trustAnchors);
        return List.copyOf(keys);
    }

    /**
     * Appends DNSKEY records from one response section.
     *
     * @param target  mutable target list
     * @param records section records
     */
    private static void collectDnskeys(final ArrayList<DnsRecord> target, final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.DNSKEY.code()) {
                target.add(record);
            }
        }
    }

    /**
     * Appends DNSKEY trust anchors as verifier keys.
     *
     * @param target       DNSKEY target list
     * @param trustAnchors configured DNSSEC trust anchors
     */
    private static void collectTrustAnchorDnskeys(
            final ArrayList<DnsRecord> target,
            final List<DnsTrustAnchor> trustAnchors) {
        for (final DnsTrustAnchor trustAnchor : trustAnchors) {
            if (trustAnchor.type() == DnsRecordType.DNSKEY) {
                target.add(trustAnchor.toRecord(0L));
            }
        }
    }

    /**
     * Returns response-carried DNSKEY records that match a configured DS trust anchor.
     *
     * @param responseKeys DNSKEY records carried by the response
     * @param trustAnchors configured DNSSEC trust anchors
     * @return trusted DNSKEY records from the response
     */
    private static ArrayList<DnsRecord> trustedResponseDnskeys(
            final List<DnsRecord> responseKeys,
            final List<DnsTrustAnchor> trustAnchors) {
        final ArrayList<DnsRecord> trusted = new ArrayList<>();
        for (final DnsRecord key : responseKeys) {
            if (dnskeyTrustedByDs(key, trustAnchors)) {
                trusted.add(key);
            }
        }
        return trusted;
    }

    /**
     * Returns whether a DNSKEY record matches any DS trust anchor.
     *
     * @param key          DNSKEY record
     * @param trustAnchors configured DNSSEC trust anchors
     * @return true when a DS anchor validates the DNSKEY digest
     */
    private static boolean dnskeyTrustedByDs(final DnsRecord key, final List<DnsTrustAnchor> trustAnchors) {
        for (final DnsTrustAnchor trustAnchor : trustAnchors) {
            if (trustAnchor.type() == DnsRecordType.DS && dnskeyMatchesDs(key, trustAnchor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a DNSKEY record matches one DS trust anchor.
     *
     * @param key         DNSKEY record
     * @param trustAnchor DS trust anchor
     * @return true when owner, algorithm, key tag, digest type, and digest match
     */
    private static boolean dnskeyMatchesDs(final DnsRecord key, final DnsTrustAnchor trustAnchor) {
        if (!key.name().equals(trustAnchor.name())) {
            return false;
        }
        final byte[] data = key.wireData();
        return data.length > DNSKEY_FIXED_BYTES && DnsCodec.readUnsignedByte(data, 3) == trustAnchor.algorithm()
                && DnsSigningKey.keyTag(data) == trustAnchor.keyTag()
                && MessageDigest.isEqual(dsDigest(key, trustAnchor.digestType()), dsDigest(trustAnchor));
    }

    /**
     * Computes the DS digest for a DNSKEY record.
     *
     * @param key        DNSKEY record
     * @param digestType DS digest type
     * @return digest bytes
     */
    private static byte[] dsDigest(final DnsRecord key, final int digestType) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(dsDigestAlgorithm(digestType));
            digest.update(DnsName.wire(key.name()));
            digest.update(key.wireData());
            return digest.digest();
        } catch (final GeneralSecurityException e) {
            throw new ProtocolException("Unsupported DNSSEC DS digest type: " + digestType, e);
        }
    }

    /**
     * Extracts digest bytes from DS trust-anchor RDATA.
     *
     * @param trustAnchor DS trust anchor
     * @return digest bytes
     */
    private static byte[] dsDigest(final DnsTrustAnchor trustAnchor) {
        final byte[] data = trustAnchor.data().wireData();
        return Arrays.copyOfRange(data, 4, data.length);
    }

    /**
     * Returns the JCA digest algorithm for a DS digest type.
     *
     * @param digestType DS digest type
     * @return JCA message-digest algorithm
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
     * Returns the answer RRSet covered by a signature.
     *
     * @param answer  representative answer record
     * @param answers complete answer section
     * @return records with the same owner, type, and class
     */
    private static List<DnsRecord> rrset(final DnsRecord answer, final List<DnsRecord> answers) {
        final ArrayList<DnsRecord> records = new ArrayList<>();
        for (final DnsRecord record : answers) {
            if (record.name().equals(answer.name()) && record.typeCode() == answer.typeCode()
                    && record.recordClass() == answer.recordClass()) {
                records.add(record);
            }
        }
        return List.copyOf(records);
    }

    /**
     * Returns whether a DNSKEY matches RRSIG signer, algorithm, and key tag fields.
     *
     * @param key    DNSKEY record
     * @param fields parsed RRSIG fields
     * @return true when the key can be used for the signature
     */
    private static boolean dnskeyMatches(final DnsRecord key, final SignatureFields fields) {
        final byte[] data = key.wireData();
        return data.length > DNSKEY_FIXED_BYTES && key.name().equals(fields.signerName)
                && DnsCodec.readUnsignedByte(data, 2) == DNSKEY_PROTOCOL
                && DnsCodec.readUnsignedByte(data, 3) == fields.algorithm
                && DnsSigningKey.keyTag(data) == fields.keyTag;
    }

    /**
     * Verifies one RRSIG over one RRSet using a DNSKEY.
     *
     * @param fields parsed RRSIG fields
     * @param key    matching DNSKEY record
     * @param rrset  covered RRSet
     * @return true when the JCA signature verifies
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
     * Builds the DNSSEC signed data for an RRSet.
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
     * Returns whether a type is DNSSEC material.
     *
     * @param type record type
     * @return true when the type belongs to DNSSEC
     */
    private static boolean dnssecType(final DnsRecordType type) {
        return type == DnsRecordType.DS || type == DnsRecordType.DNSKEY || type == DnsRecordType.RRSIG
                || type == DnsRecordType.NSEC || type == DnsRecordType.NSEC3 || type == DnsRecordType.NSEC3PARAM;
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
                    DnsCodec.readUnsignedInt(data, RRSIG_INCEPTION_OFFSET), DnsCodec.readUnsignedShort(data, 16),
                    signer.name(), Arrays.copyOfRange(data, signer.nextOffset(), data.length));
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
