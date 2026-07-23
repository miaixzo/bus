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
 * Reusable TLS record writer for the stable application-data path.
 *
 * <p>
 * Plaintext and ciphertext direct buffers are connection-owned and reused. The writer continues wrapping and writing
 * until the requested plaintext has been consumed, including native partial writes.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class TlsRecordWriter {

    /**
     * Engine adapter that wraps plaintext and supplies buffer-size hints.
     */
    private final TlsEngine engine;

    /**
     * Connection conduit receiving encrypted TLS records.
     */
    private final Conduit transport;

    /**
     * Reusable direct staging buffer filled from caller-owned plaintext.
     */
    private ByteBuffer plaintext;

    /**
     * Reusable direct staging buffer receiving records produced by the engine.
     */
    private ByteBuffer ciphertext;

    /**
     * Bridge buffer drained by synchronous conduit writes, including partial writes.
     */
    private final Buffer encryptedOutput = new Buffer();

    /**
     * Number of calls made to the TLS engine's wrap operation.
     */
    private long wrapCount;

    /**
     * Number of productive synchronous writes made to the encrypted transport.
     */
    private long writeCount;

    /**
     * Creates a record writer.
     *
     * @param engine    non-null negotiated TLS engine adapter
     * @param transport non-null conduit receiving encrypted records
     */
    TlsRecordWriter(final TlsEngine engine, final Conduit transport) {
        if (engine == null || transport == null) {
            throw new ValidateException("TLS record writer collaborators must not be null");
        }
        this.engine = engine;
        this.transport = transport;
        this.plaintext = ByteBuffer.allocateDirect(engine.applicationBufferSize());
        this.ciphertext = ByteBuffer.allocateDirect(engine.packetBufferSize());
    }

    /**
     * Wraps and writes exactly the requested plaintext bytes.
     *
     * @param source non-null buffer whose leading plaintext bytes are consumed by ownership transfer
     * @param count  non-negative number of plaintext bytes to consume, not exceeding the source size
     * @return the requested count after all corresponding ciphertext has been written
     */
    long write(final Buffer source, final long count) {
        if (source == null || count < 0L || count > source.size()) {
            throw new ValidateException("Invalid TLS plaintext write");
        }
        long remaining = count;
        while (remaining != 0L) {
            plaintext.clear();
            final int chunk = (int) Math.min(remaining, plaintext.remaining());
            source.readTo(plaintext, chunk);
            plaintext.flip();
            while (plaintext.hasRemaining()) {
                ciphertext.clear();
                final SSLEngineResult result = engine.wrap(plaintext, ciphertext);
                wrapCount++;
                switch (result.getStatus()) {
                    case OK -> writeCiphertext();
                    case BUFFER_OVERFLOW -> ciphertext = grow(ciphertext, engine.packetBufferSize());
                    case BUFFER_UNDERFLOW -> throw new ProtocolException("TLS wrap returned BUFFER_UNDERFLOW");
                    case CLOSED -> throw new SocketException("TLS outbound side is closed");
                }
                if (result.bytesConsumed() == 0 && result.bytesProduced() == 0
                        && result.getStatus() == SSLEngineResult.Status.OK) {
                    throw new SocketException("TLS wrap made no progress");
                }
            }
            remaining -= chunk;
        }
        return count;
    }

    /**
     * Returns the number of engine wrap operations.
     *
     * @return engine wrap call count
     */
    long wrapCount() {
        return wrapCount;
    }

    /**
     * Returns the number of productive encrypted transport writes.
     *
     * @return encrypted transport write call count
     */
    long writeCount() {
        return writeCount;
    }

    /**
     * Transfers all ciphertext currently produced in the direct staging buffer through partial conduit writes.
     */
    private void writeCiphertext() {
        ciphertext.flip();
        try {
            encryptedOutput.write(ciphertext);
            while (encryptedOutput.size() != 0L) {
                final long before = encryptedOutput.size();
                final long written = transport.writeSynchronously(encryptedOutput, before);
                if (written <= 0L || written > before) {
                    throw new SocketException("TLS transport write made invalid progress");
                }
                writeCount++;
            }
        } catch (final IOException e) {
            throw new SocketException("TLS record write failed", e);
        }
    }

    /**
     * Allocates a larger empty direct buffer using the greater of the provider hint and doubled capacity.
     *
     * @param current buffer whose capacity establishes the growth baseline
     * @param hint    provider-reported minimum capacity candidate
     * @return larger empty direct buffer in write mode
     */
    private static ByteBuffer grow(final ByteBuffer current, final int hint) {
        final int capacity = Math.max(hint, current.capacity() << 1);
        if (capacity <= current.capacity()) {
            throw new SocketException("TLS packet buffer cannot grow safely");
        }
        return ByteBuffer.allocateDirect(capacity);
    }

}
