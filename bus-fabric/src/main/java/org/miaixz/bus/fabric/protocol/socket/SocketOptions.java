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

import java.net.SocketOption;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.instance.Instances;
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
 * @param threadNum        AIO read worker count
 * @param socketOptions    JDK socket options passed to client channels
 * @param retainReadBuffer true to reuse one read buffer per session
 * @param connectTimeout   connection timeout
 * @param idleTimeout      operation-time idle timeout
 * @author Kimi Liu
 * @since Java 21+
 */
public record SocketOptions(int readBufferSize, int writeChunkSize, int writeChunkCount, int backlog, int threadNum,
        Map<SocketOption<?>, Object> socketOptions, boolean retainReadBuffer, Duration connectTimeout,
        Duration idleTimeout) {

    /**
     * Option key for per-session read buffer size.
     */
    public static final String READ_BUFFER_SIZE = "socket.readBufferSize";

    /**
     * Option key for maximum bytes submitted in one low-level write chunk.
     */
    public static final String WRITE_CHUNK_SIZE = "socket.writeChunkSize";

    /**
     * Option key for retained write chunk count hint.
     */
    public static final String WRITE_CHUNK_COUNT = "socket.writeChunkCount";

    /**
     * Option key for TCP server listen backlog.
     */
    public static final String BACKLOG = "socket.backlog";

    /**
     * Option key for AIO read worker count.
     */
    public static final String THREAD_NUM = "socket.threadNum";

    /**
     * Option key for JDK socket options applied to client channels.
     */
    public static final String SOCKET_OPTIONS = "socket.socketOptions";

    /**
     * Option key for retaining one reusable read buffer per session.
     */
    public static final String RETAIN_READ_BUFFER = "socket.retainReadBuffer";

    /**
     * Option key for connection timeout.
     */
    public static final String CONNECT_TIMEOUT = "socket.connectTimeout";

    /**
     * Option key for operation-time idle timeout.
     */
    public static final String IDLE_TIMEOUT = "socket.idleTimeout";

    /**
     * Default per-session read buffer size used when no option map value is supplied.
     */
    private static final int DEFAULT_READ_BUFFER_SIZE = 8192;

    /**
     * Default maximum bytes submitted in one low-level socket write.
     */
    private static final int DEFAULT_WRITE_CHUNK_SIZE = 8192;

    /**
     * Default retained write-chunk count used by socket write buffering.
     */
    private static final int DEFAULT_WRITE_CHUNK_COUNT = 16;

    /**
     * Default TCP server backlog for socket listeners.
     */
    private static final int DEFAULT_BACKLOG = 1000;

    /**
     * Creates validated options.
     */
    public SocketOptions {
        readBufferSize = positive(readBufferSize, "Read buffer size");
        writeChunkSize = positive(writeChunkSize, "Write chunk size");
        writeChunkCount = positive(writeChunkCount, "Write chunk count");
        backlog = positive(backlog, "Backlog");
        threadNum = positive(threadNum, "Thread count");
        socketOptions = snapshotSocketOptions(socketOptions);
        connectTimeout = timeout(connectTimeout, "Connect timeout");
        idleTimeout = timeout(idleTimeout, "Idle timeout");
    }

    /**
     * Returns defaults.
     *
     * @return defaults
     */
    public static SocketOptions defaults() {
        return Instances.get(
                SocketOptions.class.getName() + ".defaults",
                () -> builder().threadNum(Math.max(1, Runtime.getRuntime().availableProcessors())).build());
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
        if (options == null) {
            throw new ValidateException("Options must not be null");
        }
        final Builder builder = builder();
        builder.readBufferSize(number(options, READ_BUFFER_SIZE, DEFAULT_READ_BUFFER_SIZE));
        builder.writeChunkSize(number(options, WRITE_CHUNK_SIZE, DEFAULT_WRITE_CHUNK_SIZE));
        builder.writeChunkCount(number(options, WRITE_CHUNK_COUNT, DEFAULT_WRITE_CHUNK_COUNT));
        builder.backlog(number(options, BACKLOG, DEFAULT_BACKLOG));
        builder.threadNum(number(options, THREAD_NUM, Math.max(1, Runtime.getRuntime().availableProcessors())));
        final Object rawSocketOptions = options.get(SOCKET_OPTIONS);
        if (rawSocketOptions instanceof Map<?, ?> map) {
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof SocketOption<?> option)) {
                    throw new ValidateException("Socket option key must be a SocketOption");
                }
                builder.socketOption((SocketOption<Object>) option, entry.getValue());
            }
        } else if (rawSocketOptions != null) {
            throw new ValidateException("Socket options value must be a map");
        }
        builder.retainReadBuffer(bool(options, RETAIN_READ_BUFFER, false));
        builder.connectTimeout(duration(options, CONNECT_TIMEOUT, java.time.Duration.ofSeconds(10)));
        builder.idleTimeout(duration(options, IDLE_TIMEOUT, java.time.Duration.ZERO));
        return builder.build();
    }

    /**
     * Converts to generic fabric options.
     *
     * @return options
     */
    public Options toOptions() {
        return Options.empty().with(READ_BUFFER_SIZE, readBufferSize).with(WRITE_CHUNK_SIZE, writeChunkSize)
                .with(WRITE_CHUNK_COUNT, writeChunkCount).with(BACKLOG, backlog).with(THREAD_NUM, threadNum)
                .with(SOCKET_OPTIONS, socketOptions).with(RETAIN_READ_BUFFER, retainReadBuffer)
                .with(CONNECT_TIMEOUT, connectTimeout).with(IDLE_TIMEOUT, idleTimeout);
    }

    /**
     * Builder for current socket runtime options.
     */
    public static final class Builder {

        /**
         * Mutable read buffer size candidate.
         */
        private int readBufferSize = DEFAULT_READ_BUFFER_SIZE;

        /**
         * Mutable write chunk size candidate.
         */
        private int writeChunkSize = DEFAULT_WRITE_CHUNK_SIZE;

        /**
         * Mutable retained write chunk count candidate.
         */
        private int writeChunkCount = DEFAULT_WRITE_CHUNK_COUNT;

        /**
         * Mutable TCP server backlog candidate.
         */
        private int backlog = DEFAULT_BACKLOG;

        /**
         * Mutable AIO worker count candidate.
         */
        private int threadNum = Math.max(1, Runtime.getRuntime().availableProcessors());

        /**
         * Mutable JDK socket options collected before the immutable snapshot is built.
         */
        private final LinkedHashMap<SocketOption<?>, Object> socketOptions = new LinkedHashMap<>();

        /**
         * Mutable read-buffer retention flag candidate.
         */
        private boolean retainReadBuffer;

        /**
         * Mutable connection timeout candidate.
         */
        private Duration connectTimeout = Duration.ofSeconds(10);

        /**
         * Mutable idle timeout candidate.
         */
        private Duration idleTimeout = Duration.ZERO;

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
         * Sets the AIO read worker count.
         *
         * @param value worker count
         * @return this builder
         */
        public Builder threadNum(final int value) {
            threadNum = positive(value, "Thread count");
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
            if (option == null || value == null) {
                throw new ValidateException("Socket option and value must not be null");
            }
            socketOptions.put(option, value);
            return this;
        }

        /**
         * Adds JDK socket options for client channels.
         *
         * @param values socket option values
         * @return this builder
         */
        public Builder socketOptions(final Map<SocketOption<?>, ?> values) {
            if (values == null) {
                throw new ValidateException("Socket options must not be null");
            }
            values.forEach((option, value) -> {
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
         * Sets the connection timeout.
         *
         * @param value timeout
         * @return this builder
         */
        public Builder connectTimeout(final Duration value) {
            connectTimeout = timeout(value, "Connect timeout");
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
         * Builds immutable socket options.
         *
         * @return socket options
         */
        public SocketOptions build() {
            return new SocketOptions(readBufferSize, writeChunkSize, writeChunkCount, backlog, threadNum, socketOptions,
                    retainReadBuffer, connectTimeout, idleTimeout);
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
        if (value <= 0) {
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
        if (value == null || value.isNegative()) {
            throw new ValidateException(name + " must be non-null and non-negative");
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
    private static int number(final Options options, final String key, final int fallback) {
        final Object value = options.get(key);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Number number)) {
            throw new ValidateException(key + " must be numeric");
        }
        return number.intValue();
    }

    /**
     * Reads a boolean socket option from a generic fabric option map.
     *
     * @param options  option map
     * @param key      option key
     * @param fallback fallback when absent
     * @return option value
     */
    private static boolean bool(final Options options, final String key, final boolean fallback) {
        final Object value = options.get(key);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Boolean bool)) {
            throw new ValidateException(key + " must be boolean");
        }
        return bool;
    }

    /**
     * Reads a duration socket option from a generic fabric option map.
     *
     * @param options  option map
     * @param key      option key
     * @param fallback fallback when absent
     * @return option value
     */
    private static Duration duration(final Options options, final String key, final Duration fallback) {
        final Object value = options.get(key);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Duration duration)) {
            throw new ValidateException(key + " must be a Duration");
        }
        return duration;
    }

}
