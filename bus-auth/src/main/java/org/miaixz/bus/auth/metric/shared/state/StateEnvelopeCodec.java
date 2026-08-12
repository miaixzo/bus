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
package org.miaixz.bus.auth.metric.shared.state;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;

/**
 * Encodes and decodes the single authentication state envelope format. The wire layout is a four-byte {@code AUTH}
 * magic, one-byte version, one-byte opaque payload type, four-byte big-endian payload length, payload bytes, and a
 * SHA-256 digest over every preceding byte. Decoding requires the exact version, type, length, digest, and end of
 * input; Java serialization and trailing extension data are not accepted.
 * <p>
 * <strong>Bus dependencies:</strong> {@link Builder#sha256()} supplies the envelope digest, {@link Normal} supplies
 * framework byte counts, and {@link ErrorCode#_100300} supplies the stable parsing failure. JDK
 * {@link MessageDigest#isEqual(byte[], byte[])} performs the bare-digest comparison required by the framework security
 * boundary.
 *
 * @author Kimi Liu
 */
public final class StateEnvelopeCodec {

    /**
     * Four-byte ASCII {@code AUTH} envelope marker.
     */
    private static final int MAGIC = 0x41555448;

    /**
     * Only accepted envelope format version.
     */
    private static final byte VERSION = Normal._1;

    /**
     * Only accepted envelope payload type.
     */
    private static final byte OPAQUE_TYPE = Normal._1;

    /**
     * Prevents instantiation of the stateless codec.
     */
    private StateEnvelopeCodec() {
        // No initialization required.
    }

    /**
     * Encodes one opaque payload into the fixed authenticated envelope.
     *
     * @param payload opaque state payload
     * @return new envelope byte array
     */
    public static byte[] encode(final byte[] payload) {
        final byte[] value = Arrays.copyOf(
                Assert.notNull(payload, () -> new ValidateException("State payload must not be null")),
                payload.length);
        final int signedLength = Math.addExact(Normal._10, value.length);
        final ByteBuffer signed = ByteBuffer.allocate(signedLength).order(ByteOrder.BIG_ENDIAN);
        signed.putInt(MAGIC).put(VERSION).put(OPAQUE_TYPE).putInt(value.length).put(value);
        final byte[] signedBytes = signed.array();
        final byte[] digest = Builder.sha256().digest(signedBytes);
        final ByteBuffer envelope = ByteBuffer.allocate(Math.addExact(signedLength, Normal._32))
                .order(ByteOrder.BIG_ENDIAN);
        envelope.put(signedBytes).put(digest);
        return envelope.array();
    }

    /**
     * Validates and decodes one complete fixed state envelope.
     *
     * @param envelope complete envelope bytes
     * @return new opaque payload byte array
     */
    public static byte[] decode(final byte[] envelope) {
        final byte[] source = Arrays.copyOf(
                Assert.notNull(envelope, () -> new ValidateException("State envelope must not be null")),
                envelope.length);
        require(source.length >= Normal._10 + Normal._32);
        final ByteBuffer input = ByteBuffer.wrap(source).order(ByteOrder.BIG_ENDIAN);
        require(input.getInt() == MAGIC);
        require(input.get() == VERSION);
        require(input.get() == OPAQUE_TYPE);
        final int payloadLength = input.getInt();
        require(payloadLength >= Normal._0);
        final long expectedLength = (long) Normal._10 + payloadLength + Normal._32;
        require(expectedLength == source.length);
        final byte[] payload = new byte[payloadLength];
        input.get(payload);
        final byte[] suppliedDigest = new byte[Normal._32];
        input.get(suppliedDigest);
        require(!input.hasRemaining());
        final byte[] signed = Arrays.copyOf(source, Normal._10 + payloadLength);
        final byte[] expectedDigest = Builder.sha256().digest(signed);
        require(MessageDigest.isEqual(expectedDigest, suppliedDigest));
        return payload;
    }

    /**
     * Requires one envelope invariant.
     *
     * @param condition required invariant
     */
    private static void require(final boolean condition) {
        if (!condition) {
            throw new ProtocolException(ErrorCode._100300);
        }
    }

}
