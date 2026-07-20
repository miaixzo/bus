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
package org.miaixz.bus.fabric.protocol.socket;

import static org.miaixz.bus.fabric.Builder.*;

import java.net.SocketOption;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;

/**
 * Current socket runtime options used by {@link SocketX}, {@link SocketRunner}, {@link SocketSession}, and server
 * adapters that create socket sessions.
 *
 * @param readBufferSize   read buffer size
 * @param writeChunkSize   maximum bytes submitted in one low-level write
 * @param writeChunkCount  retained write chunk count hint
 * @param backlog          TCP server backlog
 * @param ioThreads        AIO read I/O thread count
 * @param socketOptions    JDK socket options passed to client channels
 * @param retainReadBuffer true to reuse one read buffer per session
 * @param idleTimeout      operation-time idle timeout
 * @param kcpWireVersion   KCP wire-format version, either {@code 1} or {@code 2}
 * @author Kimi Liu
 * @since Java 21+
 */
public record SocketOptions(int readBufferSize, int writeChunkSize, int writeChunkCount, int backlog, int ioThreads,
        Map<SocketOption<?>, Object> socketOptions, boolean retainReadBuffer, Duration idleTimeout,
        int kcpWireVersion) {

    /**
     * Creates validated options.
     */
    public SocketOptions {
        readBufferSize = positive(readBufferSize, "Read buffer size");
        writeChunkSize = positive(writeChunkSize, "Write chunk size");
        writeChunkCount = positive(writeChunkCount, "Write chunk count");
        backlog = positive(backlog, "Backlog");
        ioThreads = positive(ioThreads, "I/O thread count");
        socketOptions = snapshotSocketOptions(socketOptions);
        idleTimeout = timeout(idleTimeout, "Idle timeout");
        kcpWireVersion = wireVersion(kcpWireVersion);
    }

    /**
     * Returns defaults.
     *
     * @return defaults
     */
    public static SocketOptions defaults() {
        return Instances.get(
                SocketOptions.class.getName() + ".defaults",
                () -> builder().ioThreads(Math.max(Normal._1, Runtime.getRuntime().availableProcessors())).build());
    }

    /**
     * Creates a builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Recreates socket options from a generic fabric option map.
     *
     * @param options options
     * @return socket options
     */
    @SuppressWarnings("unchecked")
    public static SocketOptions from(final Options options) {
        final Options checkedOptions = Assert.notNull(options, () -> new ValidateException("Options must not be null"));
        final Builder builder = builder();
        builder.readBufferSize(number(checkedOptions, OPTION_SOCKET_READ_BUFFER_SIZE, Normal._8192));
        builder.writeChunkSize(number(checkedOptions, OPTION_SOCKET_WRITE_CHUNK_SIZE, Normal._8192));
        builder.writeChunkCount(number(checkedOptions, OPTION_SOCKET_WRITE_CHUNK_COUNT, Normal._16));
        builder.backlog(number(checkedOptions, OPTION_SOCKET_BACKLOG, _1000));
        builder.ioThreads(
                number(
                        checkedOptions,
                        OPTION_SOCKET_IO_THREADS,
                        Math.max(Normal._1, Runtime.getRuntime().availableProcessors())));
        final Map<?, ?> rawSocketOptions = checkedOptions.get(OPTION_SOCKET_OPTIONS);
        if (rawSocketOptions != null) {
            for (final Map.Entry<?, ?> entry : rawSocketOptions.entrySet()) {
                if (!(entry.getKey() instanceof SocketOption<?> option)) {
                    throw new ValidateException("Socket option key must be a SocketOption");
                }
                builder.socketOption((SocketOption<Object>) option, entry.getValue());
            }
        }
        builder.retainReadBuffer(bool(checkedOptions, OPTION_SOCKET_RETAIN_READ_BUFFER, false));
        builder.idleTimeout(duration(checkedOptions, OPTION_SOCKET_IDLE_TIMEOUT, Duration.ZERO));
        return builder.build();
    }

    /**
     * Converts to generic fabric options.
     *
     * @return options
     */
    public Options toOptions() {
        return Options.empty().with(OPTION_SOCKET_READ_BUFFER_SIZE, readBufferSize)
                .with(OPTION_SOCKET_WRITE_CHUNK_SIZE, writeChunkSize)
                .with(OPTION_SOCKET_WRITE_CHUNK_COUNT, writeChunkCount).with(OPTION_SOCKET_BACKLOG, backlog)
                .with(OPTION_SOCKET_IO_THREADS, ioThreads).with(OPTION_SOCKET_OPTIONS, socketOptions)
                .with(OPTION_SOCKET_RETAIN_READ_BUFFER, retainReadBuffer).with(OPTION_SOCKET_IDLE_TIMEOUT, idleTimeout);
    }

    /**
     * Builder for current socket runtime options.
     */
    public static final class Builder {

        /**
         * Mutable read buffer size candidate.
         */
        private int readBufferSize = Normal._8192;

        /**
         * Mutable write chunk size candidate.
         */
        private int writeChunkSize = Normal._8192;

        /**
         * Mutable retained write chunk count candidate.
         */
        private int writeChunkCount = Normal._16;

        /**
         * Mutable TCP server backlog candidate.
         */
        private int backlog = _1000;

        /**
         * Mutable AIO I/O thread count candidate.
         */
        private int ioThreads = Math.max(Normal._1, Runtime.getRuntime().availableProcessors());

        /**
         * Mutable JDK socket options collected before the immutable snapshot is built.
         */
        private final LinkedHashMap<SocketOption<?>, Object> socketOptions = new LinkedHashMap<>();

        /**
         * Mutable read-buffer retention flag candidate.
         */
        private boolean retainReadBuffer;

        /**
         * Mutable idle timeout candidate.
         */
        private Duration idleTimeout = Duration.ZERO;

        /**
         * Mutable KCP wire-format version candidate.
         */
        private int kcpWireVersion = Normal._1;

        /**
         * Creates a builder seeded with socket defaults.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets the per-session read buffer size.
         *
         * @param value byte size
         * @return this builder
         */
        public Builder readBufferSize(final int value) {
            readBufferSize = positive(value, "Read buffer size");
            return this;
        }

        /**
         * Sets the maximum bytes submitted in one low-level write chunk.
         *
         * @param value byte size
         * @return this builder
         */
        public Builder writeChunkSize(final int value) {
            writeChunkSize = positive(value, "Write chunk size");
            return this;
        }

        /**
         * Sets the retained write chunk count hint.
         *
         * @param value chunk count
         * @return this builder
         */
        public Builder writeChunkCount(final int value) {
            writeChunkCount = positive(value, "Write chunk count");
            return this;
        }

        /**
         * Sets the TCP server listen backlog.
         *
         * @param value backlog
         * @return this builder
         */
        public Builder backlog(final int value) {
            backlog = positive(value, "Backlog");
            return this;
        }

        /**
         * Sets the AIO read I/O thread count.
         *
         * @param value I/O thread count
         * @return this builder
         */
        public Builder ioThreads(final int value) {
            ioThreads = positive(value, "I/O thread count");
            return this;
        }

        /**
         * Adds a JDK socket option for client channels.
         *
         * @param option socket option
         * @param value  option value
         * @param <T>    option value type
         * @return this builder
         */
        public <T> Builder socketOption(final SocketOption<T> option, final T value) {
            final SocketOption<T> checkedOption = Assert
                    .notNull(option, () -> new ValidateException("Socket option must not be null"));
            final T checkedValue = Assert
                    .notNull(value, () -> new ValidateException("Socket option value must not be null"));
            socketOptions.put(checkedOption, checkedValue);
            return this;
        }

        /**
         * Adds JDK socket options for client channels.
         *
         * @param values socket option values
         * @return this builder
         */
        public Builder socketOptions(final Map<SocketOption<?>, ?> values) {
            final Map<SocketOption<?>, ?> checkedValues = Assert
                    .notNull(values, () -> new ValidateException("Socket options must not be null"));
            checkedValues.forEach((option, value) -> {
                if (option == null || value == null) {
                    throw new ValidateException("Socket option and value must not be null");
                }
                socketOptions.put(option, value);
            });
            return this;
        }

        /**
         * Sets whether each session retains a reusable read buffer.
         *
         * @param value true to retain the buffer
         * @return this builder
         */
        public Builder retainReadBuffer(final boolean value) {
            retainReadBuffer = value;
            return this;
        }

        /**
         * Sets the operation-time idle timeout.
         *
         * @param value timeout
         * @return this builder
         */
        public Builder idleTimeout(final Duration value) {
            idleTimeout = timeout(value, "Idle timeout");
            return this;
        }

        /**
         * Sets the KCP wire-format version.
         *
         * @param value wire-format version, either {@code 1} or {@code 2}
         * @return this builder
         */
        public Builder kcpWireVersion(final int value) {
            kcpWireVersion = wireVersion(value);
            return this;
        }

        /**
         * Builds immutable socket options.
         *
         * @return socket options
         */
        public SocketOptions build() {
            return new SocketOptions(readBufferSize, writeChunkSize, writeChunkCount, backlog, ioThreads, socketOptions,
                    retainReadBuffer, idleTimeout, kcpWireVersion);
        }

    }

    /**
     * Validates strictly positive numeric socket options.
     *
     * @param value option value
     * @param name  option name used in validation messages
     * @return validated value
     */
    private static int positive(final int value, final String name) {
        if (value <= Normal._0) {
            throw new ValidateException(name + " must be positive");
        }
        return value;
    }

    /**
     * Validates timeout options while allowing zero to disable idle enforcement.
     *
     * @param value timeout value
     * @param name  option name used in validation messages
     * @return validated timeout
     */
    private static Duration timeout(final Duration value, final String name) {
        final Duration checkedValue = Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
        if (checkedValue.isNegative()) {
            throw new ValidateException(name + " must be non-negative");
        }
        return checkedValue;
    }

    /**
     * Validates a KCP wire-format version.
     *
     * @param value wire-format version
     * @return validated wire-format version
     */
    private static int wireVersion(final int value) {
        if (value != Normal._1 && value != Normal._2) {
            throw new ValidateException("KCP wire version must be 1 or 2");
        }
        return value;
    }

    /**
     * Copies caller-provided JDK socket options into an immutable map.
     *
     * @param values socket option values
     * @return immutable option snapshot
     */
    private static Map<SocketOption<?>, Object> snapshotSocketOptions(final Map<SocketOption<?>, Object> values) {
        if (values == null) {
            return Map.of();
        }
        final LinkedHashMap<SocketOption<?>, Object> copy = new LinkedHashMap<>();
        values.forEach((option, value) -> {
            if (option == null || value == null) {
                throw new ValidateException("Socket option and value must not be null");
            }
            copy.put(option, value);
        });
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Reads an integer socket option from a generic fabric option map.
     *
     * @param options  option map
     * @param key      option key
     * @param fallback fallback when absent
     * @return option value
     */
    private static int number(final Options options, final Options.Key<Integer> key, final int fallback) {
        final Integer value = options.get(key);
        if (value == null) {
            if (options.contains(key)) {
                throw new ValidateException("Numeric socket option must not be null");
            }
            return fallback;
        }
        return value;
    }

    /**
     * Reads a boolean socket option from a generic fabric option map.
     *
     * @param options  option map
     * @param key      option key
     * @param fallback fallback when absent
     * @return option value
     */
    private static boolean bool(final Options options, final Options.Key<Boolean> key, final boolean fallback) {
        final Boolean value = options.get(key);
        if (value == null) {
            if (options.contains(key)) {
                throw new ValidateException("Boolean socket option must not be null");
            }
            return fallback;
        }
        return value;
    }

    /**
     * Reads a duration socket option from a generic fabric option map.
     *
     * @param options  option map
     * @param key      option key
     * @param fallback fallback when absent
     * @return option value
     */
    private static Duration duration(final Options options, final Options.Key<Duration> key, final Duration fallback) {
        final Duration value = options.get(key);
        if (value == null) {
            if (options.contains(key)) {
                throw new ValidateException("Duration socket option must not be null");
            }
            return fallback;
        }
        return value;
    }

}
