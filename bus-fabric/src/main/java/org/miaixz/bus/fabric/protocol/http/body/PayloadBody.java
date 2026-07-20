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
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

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
     * Payload reference.
     */
    private final Payload payload;

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
    private final AtomicBoolean closed;

    /**
     * Creates a payload body.
     *
     * @param payload payload
     * @param media   media
     */
    private PayloadBody(final Payload payload, final MediaType media) {
        this(payload, media, null, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates a payload body.
     *
     * @param payload  payload
     * @param media    media
     * @param progress optional progress tracker
     */
    private PayloadBody(final Payload payload, final MediaType media, final ProgressBody.Tracker progress) {
        this(payload, media, progress, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Creates a payload body.
     *
     * @param payload             payload
     * @param media               media
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
        this.closed = new AtomicBoolean();
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
     * Defers empty body construction without nesting registry updates.
     */
    private static final class EmptyHolder {

        /**
         * Shared empty body.
         */
        private static final PayloadBody INSTANCE = new PayloadBody(Payload.empty(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

    }

    /**
     * Creates a payload body.
     *
     * @param payload payload
     * @param media   media
     * @return payload body
     */
    public static PayloadBody of(final Payload payload, final MediaType media) {
        return new PayloadBody(payload, media);
    }

    /**
     * Creates a payload body with an explicit materialize threshold.
     *
     * @param payload             payload
     * @param media               media
     * @param materializeMaxBytes materialize byte threshold
     * @return payload body
     */
    public static PayloadBody of(final Payload payload, final MediaType media, final long materializeMaxBytes) {
        return new PayloadBody(payload, media, null, materializeMaxBytes);
    }

    /**
     * Returns a progress-aware copy of this payload body.
     *
     * @param listener listener
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
        return new PayloadBody(payload, media, progress, bytes);
    }

    /**
     * Returns the payload.
     *
     * @return payload
     */
    public Payload payload() {
        return progress == null ? payload : progress.payload();
    }

    /**
     * Returns the media.
     *
     * @return media
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
     * @return bytes
     */
    public byte[] bytes() {
        return Payload.materialize(payload(), materializeMaxBytes, "PayloadBody.bytes()");
    }

    /**
     * Reads all body bytes with an explicit materialize threshold.
     *
     * @param maxBytes maximum bytes to materialize
     * @return bytes
     */
    @Override
    public byte[] bytes(final long maxBytes) {
        return Payload.materialize(payload(), maxBytes, "PayloadBody.bytes(long)");
    }

    /**
     * Reads body text.
     *
     * @param charset charset
     * @return text
     */
    public String text(final Charset charset) {
        return text(charset, materializeMaxBytes);
    }

    /**
     * Reads body text with an explicit materialize threshold.
     *
     * @param charset  charset
     * @param maxBytes maximum bytes to materialize
     * @return text
     */
    @Override
    public String text(final Charset charset, final long maxBytes) {
        final Charset checkedCharset = Assert.notNull(charset, () -> new ValidateException("Charset must not be null"));
        return new String(bytes(maxBytes), checkedCharset);
    }

    /**
     * Closes this body.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true) && payload instanceof AutoCloseable closeable) {
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
     * @param length length
     * @return length
     */
    private static long validateLength(final long length) {
        Assert.isFalse(length < -1, () -> new ValidateException("Body length must be -1 or greater"));
        return length;
    }

}
