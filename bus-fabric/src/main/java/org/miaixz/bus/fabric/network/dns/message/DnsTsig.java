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
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.server.DnsTsigKey;

/**
 * TSIG verification and response-signing utilities.
 *
 * @author Kimi Liu
 */
public final class DnsTsig {

    /**
     * DNS class value used by TSIG pseudo-records.
     */
    private static final int CLASS_ANY = 255;

    /**
     * TTL value required by TSIG pseudo-records.
     */
    private static final long TSIG_TTL = 0L;

    /**
     * Default DNS TSIG time fudge in seconds.
     */
    private static final int DEFAULT_FUDGE_SECONDS = 300;

    /**
     * Restricts the class to static operations.
     */
    private DnsTsig() {
        // No initialization required.
    }

    /**
     * Verifies a signed query against configured keys and returns the matching key.
     *
     * @param query decoded query
     * @param keys  configured TSIG keys
     * @return verified key, or {@code null} when verification fails or no key matches
     */
    public static DnsTsigKey verify(final DnsQuery query, final List<DnsTsigKey> keys) {
        if (query == null || !query.tsigPresent() || keys == null || keys.isEmpty()) {
            return null;
        }
        final DnsTsigRecord record = query.tsigRecord();
        for (final DnsTsigKey key : keys) {
            if (key != null && key.matches(record.keyName(), record.algorithmName()) && verify(query, key)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Signs a response corresponding to a verified TSIG query.
     *
     * @param response unsigned response wire bytes
     * @param query    decoded signed query
     * @param key      verified TSIG key
     * @return response wire bytes with a trailing TSIG pseudo-record
     */
    public static byte[] signResponse(final byte[] response, final DnsQuery query, final DnsTsigKey key) {
        if (response == null || response.length < DnsCodec.HEADER_LENGTH) {
            throw new ProtocolException("DNS TSIG response is shorter than the header");
        }
        if (query == null || query.tsigRecord() == null || key == null) {
            return response == null ? Normal.EMPTY_BYTE_ARRAY : Arrays.copyOf(response, response.length);
        }
        final DnsTsigRecord requestTsig = query.tsigRecord();
        final long timeSigned = Instant.now().getEpochSecond();
        final int fudge = DEFAULT_FUDGE_SECONDS;
        final byte[] mac = responseMac(response, requestTsig, key, timeSigned, fudge);
        final byte[] tsig = encodeRecord(key, query.id(), timeSigned, fudge, mac);
        final byte[] signed = Arrays.copyOf(response, response.length + tsig.length);
        System.arraycopy(tsig, 0, signed, response.length, tsig.length);
        DnsCodec.writeUnsignedShort(signed, 10, DnsCodec.readUnsignedShort(signed, 10) + 1);
        return signed;
    }

    /**
     * Verifies a query with a matched TSIG key.
     *
     * @param query decoded query
     * @param key   matched TSIG key
     * @return true when the MAC, algorithm, owner, and time are valid
     */
    private static boolean verify(final DnsQuery query, final DnsTsigKey key) {
        final DnsTsigRecord record = query.tsigRecord();
        if (record.recordClass() != CLASS_ANY || record.ttl() != TSIG_TTL || record.originalId() != query.id()) {
            return false;
        }
        if (!timeValid(record)) {
            return false;
        }
        final byte[] expected = requestMac(record, key);
        return MessageDigest.isEqual(expected, record.mac());
    }

    /**
     * Returns whether the TSIG time lies within the requested fudge window.
     *
     * @param record decoded TSIG record
     * @return true when the client time is acceptable
     */
    private static boolean timeValid(final DnsTsigRecord record) {
        final long now = Instant.now().getEpochSecond();
        final long delta = Math.abs(now - record.timeSignedEpochSecond());
        return delta <= record.fudgeSeconds();
    }

    /**
     * Calculates the expected request MAC.
     *
     * @param record decoded TSIG record
     * @param key    matched TSIG key
     * @return expected MAC bytes
     */
    private static byte[] requestMac(final DnsTsigRecord record, final DnsTsigKey key) {
        final Mac mac = mac(key);
        mac.update(record.unsignedMessage());
        mac.update(
                tsigVariables(
                        record.keyName(),
                        record.recordClass(),
                        record.ttl(),
                        record.algorithmName(),
                        record.timeSignedEpochSecond(),
                        record.fudgeSeconds(),
                        record.error(),
                        record.otherData()));
        return truncate(mac.doFinal(), key.macLengthBytes());
    }

    /**
     * Calculates the response MAC.
     *
     * @param response    unsigned response wire bytes
     * @param requestTsig verified request TSIG record
     * @param key         matched TSIG key
     * @param timeSigned  response TSIG time
     * @param fudge       response TSIG fudge seconds
     * @return response MAC bytes
     */
    private static byte[] responseMac(
            final byte[] response,
            final DnsTsigRecord requestTsig,
            final DnsTsigKey key,
            final long timeSigned,
            final int fudge) {
        final Mac mac = mac(key);
        writeMacPrefix(mac, requestTsig.mac());
        mac.update(response);
        mac.update(
                tsigVariables(
                        key.name(),
                        CLASS_ANY,
                        TSIG_TTL,
                        key.algorithmName(),
                        timeSigned,
                        fudge,
                        DnsResponseCode.NOERROR.code(),
                        Normal.EMPTY_BYTE_ARRAY));
        return truncate(mac.doFinal(), key.macLengthBytes());
    }

    /**
     * Writes a previous message MAC in TSIG chained-MAC format.
     *
     * @param mac      active MAC instance
     * @param previous previous message MAC bytes
     */
    private static void writeMacPrefix(final Mac mac, final byte[] previous) {
        mac.update((byte) ((previous.length >>> Byte.SIZE) & DnsCodec.UNSIGNED_BYTE_MAX));
        mac.update((byte) (previous.length & DnsCodec.UNSIGNED_BYTE_MAX));
        mac.update(previous);
    }

    /**
     * Encodes TSIG variables used as MAC input.
     *
     * @param keyName       TSIG owner and key name
     * @param recordClass   TSIG record class
     * @param ttl           TSIG record TTL
     * @param algorithmName TSIG algorithm DNS name
     * @param timeSigned    TSIG time as Unix epoch seconds
     * @param fudge         allowed time skew seconds
     * @param error         TSIG error value
     * @param otherData     TSIG other data bytes
     * @return encoded TSIG MAC variables
     */
    private static byte[] tsigVariables(
            final String keyName,
            final int recordClass,
            final long ttl,
            final String algorithmName,
            final long timeSigned,
            final int fudge,
            final int error,
            final byte[] otherData) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            DnsName.write(output, keyName);
            output.writeShort(recordClass);
            output.writeInt((int) ttl);
            DnsName.write(output, algorithmName);
            writeTime(output, timeSigned);
            output.writeShort(fudge);
            output.writeShort(error);
            output.writeShort(otherData.length);
            output.write(otherData);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode DNS TSIG variables", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Encodes the trailing TSIG pseudo-record for a response.
     *
     * @param key        matched TSIG key
     * @param originalId original DNS message identifier
     * @param timeSigned TSIG time as Unix epoch seconds
     * @param fudge      allowed time skew seconds
     * @param mac        response MAC bytes
     * @return encoded TSIG pseudo-record
     */
    private static byte[] encodeRecord(
            final DnsTsigKey key,
            final int originalId,
            final long timeSigned,
            final int fudge,
            final byte[] mac) {
        final byte[] rdata = encodeRdata(key, originalId, timeSigned, fudge, mac);
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            DnsName.write(output, key.name());
            output.writeShort(DnsRecordType.TSIG.code());
            output.writeShort(CLASS_ANY);
            output.writeInt(0);
            output.writeShort(rdata.length);
            output.write(rdata);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode DNS TSIG response record", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Encodes TSIG response RDATA.
     *
     * @param key        matched TSIG key
     * @param originalId original DNS message identifier
     * @param timeSigned TSIG time as Unix epoch seconds
     * @param fudge      allowed time skew seconds
     * @param mac        response MAC bytes
     * @return encoded TSIG RDATA
     */
    private static byte[] encodeRdata(
            final DnsTsigKey key,
            final int originalId,
            final long timeSigned,
            final int fudge,
            final byte[] mac) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            DnsName.write(output, key.algorithmName());
            writeTime(output, timeSigned);
            output.writeShort(fudge);
            output.writeShort(mac.length);
            output.write(mac);
            output.writeShort(originalId);
            output.writeShort(DnsResponseCode.NOERROR.code());
            output.writeShort(0);
        } catch (final IOException e) {
            throw new ProtocolException("Unable to encode DNS TSIG response RDATA", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Writes a TSIG 48-bit time value.
     *
     * @param output     target stream
     * @param timeSigned Unix epoch seconds
     * @throws IOException if the target stream rejects bytes
     */
    private static void writeTime(final DataOutputStream output, final long timeSigned) throws IOException {
        output.writeShort((int) ((timeSigned >>> Integer.SIZE) & DnsCodec.UNSIGNED_SHORT_MAX));
        output.writeInt((int) (timeSigned & DnsCodec.UNSIGNED_INT_MAX));
    }

    /**
     * Creates a MAC instance initialized with the TSIG key.
     *
     * @param key TSIG key
     * @return initialized MAC
     */
    private static Mac mac(final DnsTsigKey key) {
        try {
            final Mac mac = Mac.getInstance(key.macAlgorithm());
            mac.init(new SecretKeySpec(key.secret(), key.macAlgorithm()));
            return mac;
        } catch (final GeneralSecurityException e) {
            throw new ProtocolException("Unable to initialize DNS TSIG MAC", e);
        }
    }

    /**
     * Truncates a MAC to the algorithm wire length.
     *
     * @param mac       full MAC
     * @param maxLength maximum wire length
     * @return truncated or original MAC
     */
    private static byte[] truncate(final byte[] mac, final int maxLength) {
        return mac.length > maxLength ? Arrays.copyOf(mac, maxLength) : mac;
    }

}
