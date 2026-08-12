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
package org.miaixz.bus.auth.metric.ldap.codec;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Stateful bounded LDAP BER frame reader over arbitrary raw TCP chunks. The reader accepts only a canonical
 * definite-length outer LDAPMessage sequence, retains at most one partial message between calls, and returns complete
 * independent frames without interpreting their protocol operations.
 *
 * @author Kimi Liu
 */
public final class BerFrameReader {

    /**
     * Maximum supported long-form BER length octets.
     */
    private static final int MAXIMUM_LENGTH_OCTETS = Normal._4;

    /**
     * Buffered unconsumed transport bytes.
     */
    private final Buffer buffer = new Buffer();

    /**
     * Maximum complete frame bytes including its BER header.
     */
    private final int maximumFrameBytes;

    /**
     * Whether end-of-stream validation has completed.
     */
    private boolean ended;

    /**
     * Creates a frame reader with the LDAP codec default message ceiling.
     */
    public BerFrameReader() {
        this(LdapMessageCodec.DEFAULT_MAXIMUM_MESSAGE_BYTES);
    }

    /**
     * Creates a frame reader with an explicit complete-frame ceiling.
     *
     * @param maximumFrameBytes positive complete-frame ceiling
     */
    public BerFrameReader(final int maximumFrameBytes) {
        Assert.isTrue(
                maximumFrameBytes > Normal._0,
                () -> new ValidateException("LDAP maximum frame size must be positive"));
        this.maximumFrameBytes = maximumFrameBytes;
    }

    /**
     * Throws the common LDAP protocol parsing failure.
     */
    private static void reject() {
        throw failure(null);
    }

    /**
     * Creates the common LDAP protocol parsing failure.
     *
     * @param cause optional parsing cause
     * @return protocol failure
     */
    private static ProtocolException failure(final Throwable cause) {
        return cause == null ? new ProtocolException(ErrorCode._100300)
                : new ProtocolException(ErrorCode._100300.getKey(), ErrorCode._100300.getValue(), cause);
    }

    /**
     * Appends one non-empty raw TCP chunk and extracts every complete LDAP message.
     *
     * @param chunk copied raw transport bytes
     * @return immutable complete frames in wire order
     */
    public List<byte[]> append(final byte[] chunk) {
        final byte[] source = Assert
                .notNull(chunk, () -> new ValidateException("LDAP transport chunk must not be null"));
        Assert.isTrue(!ended, () -> new ValidateException("LDAP frame reader has reached end of stream"));
        Assert.isTrue(
                source.length > Normal._0 && source.length <= maximumFrameBytes,
                () -> new ValidateException("LDAP transport chunk size is invalid"));
        buffer.write(source);
        final ArrayList<byte[]> frames = new ArrayList<>();
        while (true) {
            final long frameBytes = frameBytes();
            if (frameBytes < Normal._0 || buffer.size() < frameBytes) {
                break;
            }
            try {
                frames.add(buffer.readByteArray(frameBytes));
            } catch (final EOFException failure) {
                throw failure(failure);
            }
        }
        if (buffer.size() > maximumFrameBytes) {
            reject();
        }
        return List.copyOf(frames);
    }

    /**
     * Marks transport end-of-stream and rejects a truncated final message.
     */
    public void finish() {
        Assert.isTrue(!ended, () -> new ValidateException("LDAP frame reader has reached end of stream"));
        ended = true;
        if (!buffer.exhausted()) {
            reject();
        }
    }

    /**
     * Returns the retained partial-message byte count.
     *
     * @return pending bytes
     */
    public int pendingBytes() {
        return Math.toIntExact(buffer.size());
    }

    /**
     * Determines the complete outer sequence size without consuming buffered bytes.
     *
     * @return complete frame size, or {@code -1} when its header is incomplete
     */
    private long frameBytes() {
        if (buffer.size() < 2) {
            return -1;
        }
        if (Byte.toUnsignedInt(buffer.getByte(Normal._0)) != BerReader.SEQUENCE) {
            reject();
        }
        final int first = Byte.toUnsignedInt(buffer.getByte(Normal._1));
        if ((first & 0x80) == Normal._0) {
            return bounded(2L + first);
        }
        final int count = first & 0x7f;
        if (count < Normal._1 || count > MAXIMUM_LENGTH_OCTETS) {
            reject();
        }
        final int headerBytes = 2 + count;
        if (buffer.size() < headerBytes) {
            return -1;
        }
        if (buffer.getByte(2) == Normal._0) {
            reject();
        }
        long contentBytes = Normal._0;
        for (int index = Normal._0; index < count; index++) {
            contentBytes = contentBytes << Byte.SIZE | Byte.toUnsignedInt(buffer.getByte(2L + index));
        }
        if (contentBytes < Normal._128) {
            reject();
        }
        return bounded(headerBytes + contentBytes);
    }

    /**
     * Enforces the configured complete-frame ceiling.
     *
     * @param frameBytes computed complete frame size
     * @return unchanged frame size
     */
    private long bounded(final long frameBytes) {
        if (frameBytes < 2 || frameBytes > maximumFrameBytes) {
            reject();
        }
        return frameBytes;
    }

}
