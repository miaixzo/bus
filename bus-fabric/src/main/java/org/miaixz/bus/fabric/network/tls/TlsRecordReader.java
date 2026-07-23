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
package org.miaixz.bus.fabric.network.tls;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngineResult;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Conduit;

/**
 * Reusable TLS record reader for the stable application-data path.
 *
 * <p>
 * Encrypted leftovers and produced plaintext remain in connection-owned direct buffers between calls. Underflow reads
 * only the missing packet capacity and overflow grows from provider-reported hints.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class TlsRecordReader {

    /**
     * Engine adapter that unwraps encrypted records and supplies buffer-size hints.
     */
    private final TlsEngine engine;

    /**
     * Connection conduit supplying encrypted network bytes.
     */
    private final Conduit transport;

    /**
     * Reusable direct encrypted input maintained in read mode between unwrap operations.
     */
    private ByteBuffer encrypted;

    /**
     * Reusable direct plaintext output maintained in read mode between caller reads.
     */
    private ByteBuffer plaintext;

    /**
     * Number of calls made to the TLS engine's unwrap operation.
     */
    private long unwrapCount;

    /**
     * Number of conduit reads that produced at least one encrypted byte.
     */
    private long readCount;

    /**
     * Whether the engine reported CLOSED after receiving a clean TLS shutdown.
     */
    private boolean closed;

    /**
     * Creates a record reader with provider-sized direct buffers.
     *
     * @param engine    non-null TLS engine adapter
     * @param transport non-null conduit supplying encrypted bytes
     */
    TlsRecordReader(final TlsEngine engine, final Conduit transport) {
        if (engine == null || transport == null) {
            throw new ValidateException("TLS record reader collaborators must not be null");
        }
        this.engine = engine;
        this.transport = transport;
        this.encrypted = ByteBuffer.allocateDirect(engine.packetBufferSize());
        this.encrypted.flip();
        this.plaintext = ByteBuffer.allocateDirect(engine.applicationBufferSize());
        this.plaintext.flip();
    }

    /**
     * Reads up to a requested plaintext count.
     *
     * @param target destination buffer
     * @param limit  maximum plaintext bytes
     * @return bytes read, or {@code -1} after clean close
     */
    long read(final Buffer target, final long limit) {
        if (target == null || limit < 0L) {
            throw new ValidateException("Invalid TLS plaintext read");
        }
        if (limit == 0L) {
            return 0L;
        }
        for (;;) {
            if (plaintext.hasRemaining()) {
                final int count = (int) Math.min(limit, plaintext.remaining());
                final int originalLimit = plaintext.limit();
                plaintext.limit(plaintext.position() + count);
                try {
                    target.write(plaintext);
                } catch (final IOException e) {
                    throw new SocketException("TLS plaintext transfer failed", e);
                } finally {
                    plaintext.limit(originalLimit);
                }
                return count;
            }
            if (closed) {
                return -1L;
            }
            if (!encrypted.hasRemaining() && !fillEncrypted()) {
                throw new SocketException("TLS transport ended without close_notify");
            }
            plaintext.clear();
            final SSLEngineResult result = engine.unwrap(encrypted, plaintext);
            unwrapCount++;
            plaintext.flip();
            switch (result.getStatus()) {
                case OK -> {
                    if (result.bytesConsumed() == 0 && result.bytesProduced() == 0) {
                        if (!fillEncrypted()) {
                            throw new SocketException("TLS unwrap made no progress before EOF");
                        }
                    }
                }
                case BUFFER_UNDERFLOW -> {
                    if (!fillEncrypted()) {
                        throw new SocketException("Truncated TLS record");
                    }
                }
                case BUFFER_OVERFLOW -> {
                    plaintext = grow(plaintext, engine.applicationBufferSize());
                    plaintext.flip();
                }
                case CLOSED -> closed = true;
            }
        }
    }

    /**
     * Returns the number of engine unwrap operations.
     *
     * @return engine unwrap call count
     */
    long unwrapCount() {
        return unwrapCount;
    }

    /**
     * Returns the number of productive encrypted transport reads.
     *
     * @return encrypted transport read count
     */
    long readCount() {
        return readCount;
    }

    /**
     * Returns whether a clean TLS close was observed.
     *
     * @return true after the engine reports CLOSED
     */
    boolean closed() {
        return closed;
    }

    /**
     * Compacts encrypted leftovers and reads only available capacity.
     *
     * @return whether encrypted bytes are available after the read
     */
    private boolean fillEncrypted() {
        encrypted.compact();
        if (!encrypted.hasRemaining()) {
            encrypted = grow(encrypted, engine.packetBufferSize());
        }
        final int read;
        try {
            read = transport.readSynchronously(encrypted);
        } catch (final IOException e) {
            throw new SocketException("TLS record read failed", e);
        }
        if (read > 0) {
            readCount++;
        }
        encrypted.flip();
        return read >= 0L && encrypted.hasRemaining();
    }

    /**
     * Grows a direct buffer while preserving bytes currently stored in write mode.
     *
     * @param current direct buffer in write mode whose stored bytes are preserved
     * @param hint    provider-reported minimum capacity candidate
     * @return larger direct buffer in write mode with preserved bytes preceding its position
     */
    private static ByteBuffer grow(final ByteBuffer current, final int hint) {
        final int capacity = Math.max(hint, current.capacity() << 1);
        if (capacity <= current.capacity()) {
            throw new ProtocolException("TLS buffer cannot grow safely");
        }
        current.flip();
        final ByteBuffer grown = ByteBuffer.allocateDirect(capacity);
        grown.put(current);
        return grown;
    }

}
