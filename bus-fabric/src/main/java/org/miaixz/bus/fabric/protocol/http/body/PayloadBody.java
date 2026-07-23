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
package org.miaixz.bus.fabric.protocol.http.body;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.ProgressBody;
import org.miaixz.bus.fabric.codec.body.RequestBody;
import org.miaixz.bus.fabric.codec.body.ResponseBody;

/**
 * Immutable payload-backed body that combines HTTP media metadata, length, and progress tracking.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class PayloadBody implements RequestBody, ResponseBody, ProgressBody {

    /**
     * Closed-state updater without allocating an AtomicBoolean per body.
     */
    private static final VarHandle CLOSED;

    static {
        try {
            CLOSED = MethodHandles.lookup().findVarHandle(PayloadBody.class, "closed", int.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Payload reference.
     */
    private Payload payload;

    /**
     * Optional progress tracker.
     */
    private final ProgressBody.Tracker progress;

    /**
     * Media metadata.
     */
    private final MediaType media;

    /**
     * Body length snapshot.
     */
    private final long length;

    /**
     * Maximum bytes allowed when materializing this body.
     */
    private final long materializeMaxBytes;

    /**
     * Closed state.
     */
    private volatile int closed;

    /**
     * Creates a payload body.
     *
     * @param payload body content source
     * @param media   HTTP media metadata
     */
    private PayloadBody(final Payload payload, final MediaType media) {
        this(payload, media, null, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates a payload body.
     *
     * @param payload  body content source
     * @param media    HTTP media metadata
     * @param progress optional progress tracker
     */
    private PayloadBody(final Payload payload, final MediaType media, final ProgressBody.Tracker progress) {
        this(payload, media, progress, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates a payload body.
     *
     * @param payload             body content source
     * @param media               HTTP media metadata
     * @param progress            optional progress tracker
     * @param materializeMaxBytes materialize byte threshold
     */
    private PayloadBody(final Payload payload, final MediaType media, final ProgressBody.Tracker progress,
            final long materializeMaxBytes) {
        this.payload = Assert.notNull(payload, () -> new ValidateException("Payload must not be null"));
        this.media = Assert.notNull(media, () -> new ValidateException("MediaType must not be null"));
        this.length = validateLength(this.payload.length());
        this.progress = progress;
        Payload.validateMaterializeMaxBytes(materializeMaxBytes);
        this.materializeMaxBytes = materializeMaxBytes;
    }

    /**
     * Returns the empty body.
     *
     * @return empty body
     */
    public static PayloadBody empty() {
        return EmptyHolder.INSTANCE;
    }

    /**
     * Creates a payload body.
     *
     * @param payload body content source
     * @param media   HTTP media metadata
     * @return immutable payload body using the default materialization threshold
     */
    public static PayloadBody of(final Payload payload, final MediaType media) {
        if (payload == Payload.empty() && media == MediaType.APPLICATION_OCTET_STREAM_TYPE) {
            return empty();
        }
        return new PayloadBody(payload, media);
    }

    /**
     * Creates a payload body with an explicit materialize threshold.
     *
     * @param payload             body content source
     * @param media               HTTP media metadata
     * @param materializeMaxBytes materialize byte threshold
     * @return immutable payload body using the supplied materialization threshold
     */
    public static PayloadBody of(final Payload payload, final MediaType media, final long materializeMaxBytes) {
        if (payload == Payload.empty() && media == MediaType.APPLICATION_OCTET_STREAM_TYPE
                && materializeMaxBytes == Builder.DEFAULT_MATERIALIZE_MAX_BYTES) {
            return empty();
        }
        return new PayloadBody(payload, media, null, materializeMaxBytes);
    }

    /**
     * Returns a progress-aware copy of this payload body.
     *
     * @param listener callback receiving transferred and total byte counts
     * @return progress-aware payload body
     */
    public PayloadBody progress(final BiConsumer<Long, Long> listener) {
        return new PayloadBody(payload, media, ProgressBody.Tracker.of(payload, listener), materializeMaxBytes);
    }

    /**
     * Returns a copy with a replacement materialize threshold.
     *
     * @param bytes materialize byte threshold
     * @return copied payload body
     */
    public PayloadBody materializeMaxBytes(final long bytes) {
        Payload.validateMaterializeMaxBytes(bytes);
        if (bytes == materializeMaxBytes) {
            return this;
        }
        return new PayloadBody(payload, media, progress, bytes);
    }

    /**
     * Returns the payload.
     *
     * @return original payload or its progress-tracking wrapper
     */
    public Payload payload() {
        return progress == null ? payload : progress.payload();
    }

    /**
     * Replaces a transport payload before the response body is published, retaining media and limits.
     *
     * @param replacement payload with the same declared length
     * @return this body
     */
    public PayloadBody withTransportPayload(final Payload replacement) {
        final Payload current = Assert.notNull(replacement, () -> new ValidateException("Payload must not be null"));
        if (progress != null || current.length() != length) {
            throw new ValidateException("Transport payload replacement must preserve body length and progress state");
        }
        this.payload = current;
        return this;
    }

    /**
     * Returns the immutable owned bytes when this body is repeatable.
     *
     * <p>
     * The returned value is immutable and can therefore be passed directly to an encoder without an intermediate
     * {@code byte[]} copy. Progress-aware bodies retain their tracking path.
     * </p>
     *
     * @return immutable body bytes
     * @throws IllegalStateException when the payload is not repeatable
     */
    public ByteString ownedBytes() {
        if (progress != null) {
            return progress.payload().ownedBytes();
        }
        return payload.ownedBytes();
    }

    /**
     * Returns the media.
     *
     * @return immutable HTTP media metadata
     */
    public MediaType media() {
        return media;
    }

    /**
     * Returns the body length.
     *
     * @return body length
     */
    public long length() {
        return length;
    }

    /**
     * Returns the materialize threshold for this body.
     *
     * @return materialize byte threshold
     */
    public long materializeMaxBytes() {
        return materializeMaxBytes;
    }

    /**
     * Returns whether this body can be read more than once.
     *
     * @return true when repeatable
     */
    @Override
    public boolean repeatable() {
        return payload.repeatable();
    }

    /**
     * Reads all body bytes.
     *
     * @return fully materialized body bytes within this body's threshold
     */
    public byte[] bytes() {
        return payload().bytes(materializeMaxBytes);
    }

    /**
     * Reads all body bytes with an explicit materialize threshold.
     *
     * @param maxBytes maximum bytes to materialize
     * @return fully materialized body bytes within the supplied threshold
     */
    @Override
    public byte[] bytes(final long maxBytes) {
        return payload().bytes(maxBytes);
    }

    /**
     * Reads body text.
     *
     * @param charset character encoding used to decode body bytes
     * @return fully materialized body text within this body's threshold
     */
    public String text(final Charset charset) {
        return text(charset, materializeMaxBytes);
    }

    /**
     * Reads body text with an explicit materialize threshold.
     *
     * @param charset  character encoding used to decode body bytes
     * @param maxBytes maximum bytes to materialize
     * @return fully materialized body text within the supplied threshold
     */
    @Override
    public String text(final Charset charset, final long maxBytes) {
        final Charset checkedCharset = Assert.notNull(charset, () -> new ValidateException("Charset must not be null"));
        return payload().text(checkedCharset, maxBytes);
    }

    /**
     * Closes this body.
     */
    @Override
    public void close() {
        if ((boolean) CLOSED.compareAndSet(this, 0, 1) && payload instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (final IOException e) {
                throw new SocketException("Unable to close payload body", e);
            } catch (final Exception e) {
                throw new InternalException("Unable to close payload body", e);
            }
        }
    }

    /**
     * Returns transferred byte count.
     *
     * @return transferred byte count
     */
    @Override
    public long transferred() {
        return progress == null ? 0L : progress.transferred();
    }

    /**
     * Returns total byte count.
     *
     * @return total byte count
     */
    @Override
    public long total() {
        return length;
    }

    /**
     * Sets callback step in bytes.
     *
     * @param bytes step bytes
     * @return this body
     */
    @Override
    public PayloadBody stepBytes(final long bytes) {
        if (progress == null) {
            ProgressBody.super.stepBytes(bytes);
        } else {
            progress.stepBytes(bytes);
        }
        return this;
    }

    /**
     * Sets callback step as a total-length rate.
     *
     * @param rate step rate
     * @return this body
     */
    @Override
    public PayloadBody stepRate(final double rate) {
        if (progress == null) {
            ProgressBody.super.stepRate(rate);
        } else {
            progress.stepRate(rate);
        }
        return this;
    }

    /**
     * Validates a payload length.
     *
     * @param length candidate payload length, with {@code -1} representing unknown
     * @return validated length of {@code -1} or greater
     */
    private static long validateLength(final long length) {
        Assert.isFalse(length < -1, () -> new ValidateException("Body length must be -1 or greater"));
        return length;
    }

    /**
     * Defers empty body construction without nesting registry updates.
     */
    private static final class EmptyHolder {

        /**
         * Shared empty body.
         */
        private static final PayloadBody INSTANCE = new PayloadBody(Payload.empty(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

    }

}
