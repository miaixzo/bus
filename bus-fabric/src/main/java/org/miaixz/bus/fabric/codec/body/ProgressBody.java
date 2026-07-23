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
package org.miaixz.bus.fabric.codec.body;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.io.TransferObserver;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Payload;

/**
 * Body capability for progress-aware reads or writes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ProgressBody extends Body {

    /**
     * Reads all progress body bytes.
     *
     * @return newly materialized payload bytes within the default safety limit
     */
    @Override
    default byte[] bytes() {
        return Payload.materialize(payload(), Builder.DEFAULT_MATERIALIZE_MAX_BYTES, "ProgressBody.bytes()");
    }

    /**
     * Reads all progress body bytes with an explicit materialize threshold.
     *
     * @param maxBytes maximum payload bytes permitted during materialization
     * @return newly materialized payload bytes
     */
    @Override
    default byte[] bytes(final long maxBytes) {
        return Payload.materialize(payload(), maxBytes, "ProgressBody.bytes(long)");
    }

    /**
     * Reads progress body text.
     *
     * @param charset character set used to decode the materialized bytes
     * @return payload text decoded within the default materialization limit
     */
    @Override
    default String text(final Charset charset) {
        return new String(bytes(), Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
    }

    /**
     * Reads progress body text with an explicit materialize threshold.
     *
     * @param charset  character set used to decode the materialized bytes
     * @param maxBytes maximum payload bytes permitted during materialization
     * @return decoded payload text
     */
    @Override
    default String text(final Charset charset, final long maxBytes) {
        return new String(bytes(maxBytes),
                Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
    }

    /**
     * Returns transferred byte count.
     *
     * @return cumulative observed byte count, or {@code 0} when tracking is not implemented
     */
    default long transferred() {
        return 0L;
    }

    /**
     * Returns total byte count.
     *
     * @return declared body length, or {@code -1} when unknown
     */
    default long total() {
        return length();
    }

    /**
     * Sets callback step in bytes.
     *
     * @param bytes positive number of transferred bytes between callbacks
     * @return this body
     * @throws ValidateException if {@code bytes} is not positive
     */
    default ProgressBody stepBytes(final long bytes) {
        Tracker.validateStepBytes(bytes);
        return this;
    }

    /**
     * Sets callback step as a total-length rate.
     *
     * @param rate finite fraction greater than {@code 0} and at most {@code 1} of the known total length
     * @return this body
     * @throws ValidateException if the rate is outside the valid range or the total length is unknown
     */
    default ProgressBody stepRate(final double rate) {
        Tracker.validateStepRate(rate, total());
        return this;
    }

    /**
     * Shared progress tracker used by concrete body implementations.
     */
    final class Tracker implements TransferObserver {

        /**
         * Original payload whose opened sources are observed.
         */
        private final Payload original;

        /**
         * Callback receiving cumulative transferred and declared total byte counts.
         */
        private final BiConsumer<Long, Long> listener;

        /**
         * Stable payload view that creates progress-reporting sources.
         */
        private final Payload payload;

        /**
         * Saturating cumulative byte count shared by all opened wrapped sources.
         */
        private final AtomicLong transferred = new AtomicLong();

        /**
         * Positive byte interval between threshold callbacks.
         */
        private final AtomicLong stepBytes = new AtomicLong(Normal._8192);

        /**
         * Next cumulative byte count that triggers a callback.
         */
        private final AtomicLong nextStep = new AtomicLong(Normal._8192);

        /**
         * Guard ensuring the known-length completion callback is emitted at most once.
         */
        private final AtomicBoolean doneCalled = new AtomicBoolean();

        /**
         * Creates a tracker.
         *
         * @param original non-null payload to observe
         * @param listener non-null cumulative progress callback
         */
        private Tracker(final Payload original, final BiConsumer<Long, Long> listener) {
            this.original = require(original, "Progress payload");
            this.listener = require(listener, "Progress listener");
            this.payload = new ProgressPayload();
        }

        /**
         * Creates a tracker.
         *
         * @param payload  non-null payload to observe
         * @param listener non-null cumulative progress callback
         * @return tracker exposing a progress-aware payload view
         */
        public static Tracker of(final Payload payload, final BiConsumer<Long, Long> listener) {
            return new Tracker(payload, listener);
        }

        /**
         * Returns wrapped payload.
         *
         * @return stable progress-aware wrapper around the original payload
         */
        public Payload payload() {
            return payload;
        }

        /**
         * Returns transferred byte count.
         *
         * @return saturating cumulative byte count observed across wrapped sources
         */
        public long transferred() {
            return transferred.get();
        }

        /**
         * Returns total byte count.
         *
         * @return original payload length, or {@code -1} when unknown
         */
        public long total() {
            return original.length();
        }

        /**
         * Sets callback step in bytes.
         *
         * @param bytes positive number of transferred bytes between callbacks
         * @return this tracker
         * @throws ValidateException if {@code bytes} is not positive
         */
        public Tracker stepBytes(final long bytes) {
            validateStepBytes(bytes);
            stepBytes.set(bytes);
            nextStep.set(nextThreshold(transferred.get(), bytes));
            return this;
        }

        /**
         * Sets callback step as a total-length rate.
         *
         * @param rate finite fraction greater than {@code 0} and at most {@code 1} of the known original length
         * @return this tracker
         * @throws ValidateException if the rate is outside the valid range or the original length is unknown
         */
        public Tracker stepRate(final double rate) {
            validateStepRate(rate, original.length());
            stepBytes(Math.max(1L, (long) Math.ceil(original.length() * rate)));
            return this;
        }

        /**
         * Validates step bytes.
         *
         * @param bytes candidate callback interval in bytes
         * @throws ValidateException if {@code bytes} is not positive
         */
        static void validateStepBytes(final long bytes) {
            Assert.isTrue(bytes > 0, () -> new ValidateException("Progress step bytes must be positive"));
        }

        /**
         * Validates step rate.
         *
         * @param rate  candidate fraction of the total length
         * @param total declared total byte count
         * @throws ValidateException if the rate is outside {@code (0, 1]} or the total is unknown
         */
        static void validateStepRate(final double rate, final long total) {
            Assert.isTrue(
                    Double.isFinite(rate) && rate > 0 && rate <= 1,
                    () -> new ValidateException("Progress step rate must be greater than 0 and at most 1"));
            Assert.isTrue(
                    total >= 0,
                    () -> new ValidateException("Progress step rate requires a known payload length"));
        }

        /**
         * Starts observing a core transfer.
         */
        @Override
        public void start() {
            // Progress bodies emit callbacks only when byte thresholds are crossed.
        }

        /**
         * Observes a cumulative core transfer progress update.
         *
         * @param total       transfer-reported total byte count, ignored in favor of the original payload length
         * @param transferred cumulative transferred byte count reported by the core transfer
         */
        @Override
        public void progress(final long total, final long transferred) {
            final long previous = this.transferred.get();
            if (transferred > previous) {
                progress(transferred - previous);
            }
        }

        /**
         * Finishes observing a core transfer.
         */
        @Override
        public void finish() {
            // Existing payload progress semantics emit the final callback from progress(long).
        }

        /**
         * Updates progress and emits callbacks when thresholds are crossed.
         *
         * @param count positive byte-count increment to add
         */
        private void progress(final long count) {
            if (count <= 0) {
                return;
            }
            final long current = addTransferred(count);
            final long total = original.length();
            long lastNotified = -1L;
            long threshold = nextStep.get();
            while (current >= threshold) {
                if (threshold == Long.MAX_VALUE) {
                    break;
                }
                final long next = saturatedAdd(threshold, stepBytes.get());
                if (nextStep.compareAndSet(threshold, next)) {
                    notifyListener(threshold, total);
                    lastNotified = threshold;
                    threshold = nextStep.get();
                } else {
                    threshold = nextStep.get();
                }
            }
            if (total >= 0 && current >= total && doneCalled.compareAndSet(false, true)) {
                if (current != lastNotified) {
                    notifyListener(current, total);
                }
            }
        }

        /**
         * Notifies listener.
         *
         * @param current cumulative bytes to report
         * @param total   declared original payload length, or {@code -1}
         * @throws InternalException if the listener throws a runtime exception
         */
        private void notifyListener(final long current, final long total) {
            try {
                listener.accept(current, total);
            } catch (final RuntimeException e) {
                throw new InternalException("Progress listener failed", e);
            }
        }

        /**
         * Returns the next threshold after the transferred count.
         *
         * @param current current cumulative byte count
         * @param step    positive callback interval
         * @return first aligned threshold strictly greater than {@code current}, saturated at {@link Long#MAX_VALUE}
         */
        private static long nextThreshold(final long current, final long step) {
            final long remainder = current % step;
            final long delta = remainder == 0L ? step : step - remainder;
            return saturatedAdd(current, delta);
        }

        /**
         * Adds transferred bytes with saturation at {@link Long#MAX_VALUE}.
         *
         * @param count positive byte count
         * @return updated transferred count
         */
        private long addTransferred(final long count) {
            while (true) {
                final long current = transferred.get();
                final long next = saturatedAdd(current, count);
                if (transferred.compareAndSet(current, next)) {
                    return next;
                }
            }
        }

        /**
         * Adds two non-negative values with saturation.
         *
         * @param first  first value
         * @param second second value
         * @return saturated sum
         */
        private static long saturatedAdd(final long first, final long second) {
            return first > Long.MAX_VALUE - second ? Long.MAX_VALUE : first + second;
        }

        /**
         * Validates a required value.
         *
         * @param value reference to validate
         * @param name  logical field name included in the validation error
         * @param <T>   reference type
         * @return validated non-null reference
         * @throws ValidateException if {@code value} is {@code null}
         */
        private static <T> T require(final T value, final String name) {
            return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
        }

        /**
         * Progress-aware payload.
         */
        private final class ProgressPayload implements Payload {

            /**
             * Returns the wrapped payload length.
             *
             * @return original payload length, or {@code -1} when unknown
             */
            @Override
            public long length() {
                return original.length();
            }

            /**
             * Opens a progress-reporting source over the wrapped payload.
             *
             * @return new source that reports positive reads to the shared tracker
             */
            @Override
            public Source source() {
                return new ProgressSource(original.source());
            }

            /**
             * Materializes the payload using the default threshold.
             *
             * @return newly materialized bytes within the default safety limit
             */
            @Override
            public byte[] bytes() {
                return bytes(Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Materializes the payload using an explicit threshold.
             *
             * @param maxBytes maximum payload bytes permitted during materialization
             * @return newly materialized bytes read through a progress-reporting source
             */
            @Override
            public byte[] bytes(final long maxBytes) {
                return Payload.materialize(this, maxBytes, "ProgressBody.ProgressPayload.bytes(long)");
            }

            /**
             * Reads payload text using the default threshold.
             *
             * @param charset character set used to decode materialized bytes
             * @return decoded payload text within the default materialization limit
             */
            @Override
            public String text(final Charset charset) {
                return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
            }

            /**
             * Reads payload text using an explicit threshold.
             *
             * @param charset  character set used to decode materialized bytes
             * @param maxBytes maximum payload bytes permitted during materialization
             * @return decoded payload text
             */
            @Override
            public String text(final Charset charset, final long maxBytes) {
                return new String(bytes(maxBytes),
                        Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
            }

            /**
             * Returns whether the wrapped payload is repeatable.
             *
             * @return {@code true} when the original payload can open independent sources repeatedly
             */
            @Override
            public boolean repeatable() {
                return original.repeatable();
            }

        }

        /**
         * Progress-aware source.
         */
        private final class ProgressSource implements Source {

            /**
             * Delegate source.
             */
            private final Source input;

            /**
             * Creates a progress source.
             *
             * @param input source opened from the original payload
             */
            private ProgressSource(final Source input) {
                this.input = input;
            }

            /**
             * Reads bytes from the delegate and reports progress for positive byte counts.
             *
             * @param sink      destination buffer
             * @param byteCount maximum bytes to read
             * @return bytes read, or -1 at end of stream
             * @throws IOException when the delegate read fails
             */
            @Override
            public long read(final Buffer sink, final long byteCount) throws IOException {
                final long read = input.read(sink, byteCount);
                if (read > 0) {
                    progress(read);
                }
                return read;
            }

            /**
             * Returns the delegate timeout.
             *
             * @return timeout policy of the delegate source
             */
            @Override
            public Timeout timeout() {
                return input.timeout();
            }

            /**
             * Closes the delegate source.
             *
             * @throws IOException when the delegate close fails
             */
            @Override
            public void close() throws IOException {
                input.close();
            }

        }

    }

}
