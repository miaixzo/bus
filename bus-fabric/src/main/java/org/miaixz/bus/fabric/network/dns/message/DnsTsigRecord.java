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

import java.util.Arrays;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Decoded TSIG pseudo-record carried by a DNS query.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsTsigRecord {

    /**
     * TSIG owner name, which is also the key name.
     */
    private final String keyName;

    /**
     * TSIG algorithm DNS name.
     */
    private final String algorithmName;

    /**
     * TSIG record class, normally {@code ANY}.
     */
    private final int recordClass;

    /**
     * TSIG record TTL, normally zero.
     */
    private final long ttl;

    /**
     * Unix epoch seconds carried in the TSIG Time Signed field.
     */
    private final long timeSignedEpochSecond;

    /**
     * Allowed time skew in seconds.
     */
    private final int fudgeSeconds;

    /**
     * Request MAC bytes carried by the TSIG record.
     */
    private final byte[] mac;

    /**
     * Original DNS message identifier from the TSIG record.
     */
    private final int originalId;

    /**
     * TSIG error value.
     */
    private final int error;

    /**
     * TSIG other data bytes.
     */
    private final byte[] otherData;

    /**
     * Original query bytes with the trailing TSIG record removed and ARCOUNT decremented.
     */
    private final byte[] unsignedMessage;

    /**
     * Offset at which the TSIG record started in the original query message.
     */
    private final int recordOffset;

    /**
     * Creates a decoded TSIG record.
     *
     * @param keyName               TSIG owner and key name
     * @param algorithmName         TSIG algorithm DNS name
     * @param recordClass           TSIG record class
     * @param ttl                   TSIG record TTL
     * @param timeSignedEpochSecond Unix epoch seconds from the Time Signed field
     * @param fudgeSeconds          allowed time skew in seconds
     * @param mac                   request MAC bytes
     * @param originalId            original DNS message identifier
     * @param error                 TSIG error value
     * @param otherData             TSIG other data bytes
     * @param unsignedMessage       original query without the trailing TSIG record
     * @param recordOffset          offset at which the TSIG record started
     */
    DnsTsigRecord(final String keyName, final String algorithmName, final int recordClass, final long ttl,
            final long timeSignedEpochSecond, final int fudgeSeconds, final byte[] mac, final int originalId,
            final int error, final byte[] otherData, final byte[] unsignedMessage, final int recordOffset) {
        this.keyName = DnsName.normalize(keyName);
        this.algorithmName = DnsName.normalize(algorithmName);
        this.recordClass = DnsCodec.validateUnsignedShort(recordClass, "DNS TSIG record class");
        this.ttl = DnsCodec.validateUnsignedInt(ttl, "DNS TSIG ttl");
        this.timeSignedEpochSecond = validateNonNegative(timeSignedEpochSecond, "DNS TSIG time signed");
        this.fudgeSeconds = DnsCodec.validateUnsignedShort(fudgeSeconds, "DNS TSIG fudge");
        this.mac = copy(mac, "DNS TSIG MAC");
        this.originalId = DnsCodec.validateUnsignedShort(originalId, "DNS TSIG original id");
        this.error = DnsCodec.validateUnsignedShort(error, "DNS TSIG error");
        this.otherData = copyAllowEmpty(otherData, "DNS TSIG other data");
        this.unsignedMessage = copy(unsignedMessage, "DNS TSIG unsigned message");
        this.recordOffset = validateNonNegative(recordOffset, "DNS TSIG record offset");
    }

    /**
     * Returns the TSIG key name.
     *
     * @return canonical key name ending with a dot
     */
    public String keyName() {
        return keyName;
    }

    /**
     * Returns the TSIG algorithm DNS name.
     *
     * @return canonical algorithm name ending with a dot
     */
    public String algorithmName() {
        return algorithmName;
    }

    /**
     * Returns the TSIG record class.
     *
     * @return unsigned 16-bit record class
     */
    public int recordClass() {
        return recordClass;
    }

    /**
     * Returns the TSIG record TTL.
     *
     * @return unsigned 32-bit TTL
     */
    public long ttl() {
        return ttl;
    }

    /**
     * Returns the Time Signed value.
     *
     * @return Unix epoch seconds
     */
    public long timeSignedEpochSecond() {
        return timeSignedEpochSecond;
    }

    /**
     * Returns the allowed time skew.
     *
     * @return fudge seconds
     */
    public int fudgeSeconds() {
        return fudgeSeconds;
    }

    /**
     * Returns a copy of the request MAC.
     *
     * @return request MAC bytes
     */
    public byte[] mac() {
        return Arrays.copyOf(mac, mac.length);
    }

    /**
     * Returns the original DNS message identifier.
     *
     * @return unsigned 16-bit original id
     */
    public int originalId() {
        return originalId;
    }

    /**
     * Returns the TSIG error value.
     *
     * @return unsigned 16-bit TSIG error
     */
    public int error() {
        return error;
    }

    /**
     * Returns a copy of TSIG other data.
     *
     * @return other data bytes
     */
    public byte[] otherData() {
        return Arrays.copyOf(otherData, otherData.length);
    }

    /**
     * Returns the original query with the trailing TSIG record removed.
     *
     * @return unsigned DNS message bytes
     */
    public byte[] unsignedMessage() {
        return Arrays.copyOf(unsignedMessage, unsignedMessage.length);
    }

    /**
     * Returns the offset at which the TSIG record started in the original query.
     *
     * @return TSIG record start offset
     */
    public int recordOffset() {
        return recordOffset;
    }

    /**
     * Validates a non-negative long value.
     *
     * @param value value to validate
     * @param name  diagnostic name
     * @return validated value
     */
    private static long validateNonNegative(final long value, final String name) {
        if (value < 0L) {
            throw new ValidateException(name + " must be non-negative");
        }
        return value;
    }

    /**
     * Validates a non-negative int value.
     *
     * @param value value to validate
     * @param name  diagnostic name
     * @return validated value
     */
    private static int validateNonNegative(final int value, final String name) {
        if (value < 0) {
            throw new ValidateException(name + " must be non-negative");
        }
        return value;
    }

    /**
     * Copies a non-empty byte array.
     *
     * @param value candidate bytes
     * @param name  diagnostic name
     * @return copied bytes
     */
    private static byte[] copy(final byte[] value, final String name) {
        if (value == null || value.length == 0) {
            throw new ValidateException(name + " must not be empty");
        }
        return Arrays.copyOf(value, value.length);
    }

    /**
     * Copies a byte array that may be empty.
     *
     * @param value candidate bytes
     * @param name  diagnostic name
     * @return copied bytes
     */
    private static byte[] copyAllowEmpty(final byte[] value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return Arrays.copyOf(value, value.length);
    }

}
