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
package org.miaixz.bus.fabric.protocol.sse.event;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.protocol.sse.SseEvent;

/**
 * Streaming SSE line reader.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseReader implements AutoCloseable {

    /**
     * Unknown field.
     */
    private static final int FIELD_UNKNOWN = Normal._0;

    /**
     * Data field.
     */
    private static final int FIELD_DATA = Normal._1;

    /**
     * Event field.
     */
    private static final int FIELD_EVENT = Normal._2;

    /**
     * Id field.
     */
    private static final int FIELD_ID = Normal._3;

    /**
     * Retry field.
     */
    private static final int FIELD_RETRY = Normal._4;

    /**
     * Underlying source.
     */
    private final Source input;

    /**
     * Reusable source buffer.
     */
    private final Buffer sourceBuffer;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Reusable line buffer.
     */
    private byte[] line;

    /**
     * Reusable input buffer.
     */
    private final byte[] inputBuffer;

    /**
     * Current input buffer position.
     */
    private int inputPosition;

    /**
     * Current input buffer limit.
     */
    private int inputLimit;

    /**
     * Current line length.
     */
    private int lineLength;

    /**
     * One pushed-back byte.
     */
    private int pushed = Normal.__1;

    /**
     * Callback for low-allocation SSE field delivery.
     */
    public interface Events {

        /**
         * Receives an event.
         *
         * @param id    event identifier
         * @param event event type
         * @param data  event data
         */
        void event(String id, String event, String data);

        /**
         * Receives a retry directive.
         *
         * @param retry retry directive
         */
        default void retry(final Duration retry) {
            // No initialization required.
        }

    }

    /**
     * Creates a reader over a UTF-8 SSE source.
     *
     * @param input input source
     */
    public SseReader(final Source input) {
        this.input = Assert.notNull(input, () -> new ValidateException("SSE input source must not be null"));
        this.sourceBuffer = new Buffer();
        this.state = new AtomicReference<>(Status.OPENED);
        this.line = new byte[Normal._128];
        this.inputBuffer = new byte[Normal._8192];
    }

    /**
     * Reads the next event from the stream.
     *
     * @return event or null at EOF
     */
    public SseEvent next() {
        ensureReadable();
        String data = null;
        StringBuilder dataBuilder = null;
        String id = null;
        String event = null;
        Duration retry = null;
        boolean seen = false;
        while (opened()) {
            final int length = readLine();
            if (length < Normal._0) {
                return seen ? event(id, event, data, dataBuilder, retry) : null;
            }
            if (length == Normal._0) {
                if (seen) {
                    return event(id, event, data, dataBuilder, retry);
                }
                continue;
            }
            if (line[Normal._0] == Symbol.C_COLON) {
                continue;
            }
            int field = commonField(length);
            int valueStart;
            if (field == FIELD_UNKNOWN) {
                final int colon = colon(length);
                final int nameEnd = colon < Normal._0 ? length : colon;
                valueStart = colon < Normal._0 ? length : colon + Normal._1;
                field = field(nameEnd);
            } else {
                valueStart = commonValueStart(field);
            }
            if (valueStart < length && line[valueStart] == Symbol.C_SPACE) {
                valueStart += Normal._1;
            }
            switch (field) {
                case FIELD_DATA -> {
                    seen = true;
                    final String value = value(valueStart, length);
                    if (data == null && dataBuilder == null) {
                        data = value;
                    } else {
                        if (dataBuilder == null) {
                            dataBuilder = new StringBuilder(data.length() + value.length() + 1);
                            dataBuilder.append(data);
                            data = null;
                        }
                        appendData(dataBuilder, value);
                    }
                }
                case FIELD_EVENT -> {
                    seen = true;
                    event = value(valueStart, length);
                }
                case FIELD_ID -> {
                    seen = true;
                    final String value = value(valueStart, length);
                    id = value.indexOf('\0') >= Normal._0 ? id : value;
                }
                case FIELD_RETRY -> {
                    final Duration parsed = parseRetry(valueStart, length);
                    if (parsed != null) {
                        seen = true;
                        retry = parsed;
                    }
                }
                default -> {
                    // Unknown SSE fields are explicitly ignored by the format.
                }
            }
        }
        return null;
    }

    /**
     * Reads events until EOF or close.
     *
     * @param handler event handler
     */
    public void readLoop(final Consumer<SseEvent> handler) {
        Assert.notNull(handler, () -> new ValidateException("SSE event handler must not be null"));
        try {
            SseEvent event;
            while (opened() && (event = next()) != null) {
                handler.accept(event);
            }
        } catch (final ProtocolException | SocketException e) {
            close();
            throw e;
        } catch (final RuntimeException e) {
            close();
            throw new InternalException("SSE event handler failed", e);
        }
    }

    /**
     * Reads events until EOF or close with low-allocation callbacks.
     *
     * @param handler event handler
     */
    public void readEvents(final Events handler) {
        Assert.notNull(handler, () -> new ValidateException("SSE event handler must not be null"));
        try {
            readCallbacks(handler);
        } catch (final ProtocolException | SocketException e) {
            close();
            throw e;
        } catch (final RuntimeException e) {
            close();
            throw new InternalException("SSE event handler failed", e);
        }
    }

    /**
     * Closes this reader.
     */
    @Override
    public void close() {
        if (!state.compareAndSet(Status.OPENED, Status.CLOSED) && !state.compareAndSet(Status.RUNNING, Status.CLOSED)
                && !state.compareAndSet(Status.CLOSING, Status.CLOSED)) {
            return;
        }
        try {
            input.close();
        } catch (final IOException e) {
            throw new SocketException("Unable to close SSE reader", e);
        }
    }

    /**
     * Returns whether the reader remains open.
     *
     * @return true when open
     */
    private boolean opened() {
        final Status current = state.get();
        return current == Status.OPENED || current == Status.RUNNING;
    }

    /**
     * Ensures reads are allowed.
     */
    private void ensureReadable() {
        if (!opened()) {
            return;
        }
        state.compareAndSet(Status.OPENED, Status.RUNNING);
    }

    /**
     * Reads one line and maps transport failures.
     *
     * @return line length or -1 at EOF
     */
    private int readLine() {
        try {
            lineLength = 0;
            boolean read = false;
            while (true) {
                if (pushed >= Normal._0) {
                    final int current = pushed;
                    pushed = Normal.__1;
                    read = true;
                    if (current == Symbol.C_LF) {
                        return lineLength;
                    }
                    if (current == Symbol.C_CR) {
                        final int next = readByte();
                        if (next >= Normal._0 && next != Symbol.C_LF) {
                            pushed = next;
                        }
                        return lineLength;
                    }
                    append(current);
                    continue;
                }
                if (inputPosition >= inputLimit) {
                    inputLimit = fillInput();
                    inputPosition = Normal._0;
                    if (inputLimit <= Normal._0) {
                        return read ? lineLength : Normal.__1;
                    }
                }
                final int start = inputPosition;
                while (inputPosition < inputLimit) {
                    final int current = inputBuffer[inputPosition++] & 0xff;
                    read = true;
                    if (current == Symbol.C_LF) {
                        append(inputBuffer, start, inputPosition - 1);
                        return lineLength;
                    }
                    if (current == Symbol.C_CR) {
                        append(inputBuffer, start, inputPosition - 1);
                        final int next = readByte();
                        if (next >= Normal._0 && next != Symbol.C_LF) {
                            pushed = next;
                        }
                        return lineLength;
                    }
                }
                append(inputBuffer, start, inputPosition);
            }
        } catch (final IOException e) {
            throw new SocketException("Unable to read SSE stream", e);
        }
    }

    /**
     * Reads events through the callback path.
     *
     * @param handler event handler
     */
    private void readCallbacks(final Events handler) {
        ensureReadable();
        String data = null;
        StringBuilder dataBuilder = null;
        String id = null;
        String event = null;
        boolean seenData = false;
        while (true) {
            final int length = readLine();
            if (length < Normal._0) {
                if (seenData) {
                    handler.event(id, event, dataBuilder == null ? data : dataBuilder.toString());
                }
                return;
            }
            if (length == Normal._0) {
                if (seenData) {
                    handler.event(id, event, dataBuilder == null ? data : dataBuilder.toString());
                    data = null;
                    dataBuilder = null;
                    id = null;
                    event = null;
                    seenData = false;
                }
                continue;
            }
            if (line[Normal._0] == Symbol.C_COLON) {
                continue;
            }
            int field = commonField(length);
            int valueStart;
            if (field == FIELD_UNKNOWN) {
                final int colon = colon(length);
                final int nameEnd = colon < Normal._0 ? length : colon;
                valueStart = colon < Normal._0 ? length : colon + Normal._1;
                field = field(nameEnd);
            } else {
                valueStart = commonValueStart(field);
            }
            if (valueStart < length && line[valueStart] == Symbol.C_SPACE) {
                valueStart += Normal._1;
            }
            switch (field) {
                case FIELD_DATA -> {
                    seenData = true;
                    final String value = value(valueStart, length);
                    if (data == null && dataBuilder == null) {
                        data = value;
                    } else {
                        if (dataBuilder == null) {
                            dataBuilder = new StringBuilder(data.length() + value.length() + 1);
                            dataBuilder.append(data);
                            data = null;
                        }
                        appendData(dataBuilder, value);
                    }
                }
                case FIELD_EVENT -> event = value(valueStart, length);
                case FIELD_ID -> {
                    final String value = value(valueStart, length);
                    id = value.indexOf('\0') >= Normal._0 ? id : value;
                }
                case FIELD_RETRY -> {
                    final Duration parsed = parseRetry(valueStart, length);
                    if (parsed != null) {
                        handler.retry(parsed);
                    }
                }
                default -> {
                    // Unknown SSE fields are explicitly ignored by the format.
                }
            }
        }
    }

    /**
     * Reads one byte.
     *
     * @return byte value or -1
     * @throws IOException when reading fails
     */
    private int readByte() throws IOException {
        if (pushed >= Normal._0) {
            final int current = pushed;
            pushed = Normal.__1;
            return current;
        }
        if (inputPosition >= inputLimit) {
            inputLimit = fillInput();
            inputPosition = Normal._0;
            if (inputLimit <= Normal._0) {
                return Normal.__1;
            }
        }
        return inputBuffer[inputPosition++] & 0xff;
    }

    /**
     * Refills the reusable input buffer from the source.
     *
     * @return byte count, or -1 at EOF
     * @throws IOException when reading fails
     */
    private int fillInput() throws IOException {
        final long read = input.read(sourceBuffer, inputBuffer.length);
        if (read <= Normal.LONG_ZERO) {
            return (int) read;
        }
        return sourceBuffer.read(inputBuffer, Normal._0, (int) read);
    }

    /**
     * Appends one byte to the reusable line buffer.
     *
     * @param value byte value
     */
    private void append(final int value) {
        ensureLine(1);
        line[lineLength++] = (byte) value;
    }

    /**
     * Appends a byte range to the reusable line buffer.
     *
     * @param source source bytes
     * @param start  start index
     * @param end    end index
     */
    private void append(final byte[] source, final int start, final int end) {
        final int length = end - start;
        if (length <= 0) {
            return;
        }
        ensureLine(length);
        System.arraycopy(source, start, line, lineLength, length);
        lineLength += length;
    }

    /**
     * Ensures the reusable line buffer has free space.
     *
     * @param additional additional bytes
     */
    private void ensureLine(final int additional) {
        final int required = lineLength + additional;
        if (required <= line.length) {
            return;
        }
        int capacity = line.length << 1;
        while (capacity < required) {
            capacity <<= 1;
        }
        final byte[] copy = new byte[capacity];
        System.arraycopy(line, 0, copy, 0, lineLength);
        line = copy;
    }

    /**
     * Finds the field separator.
     *
     * @param length line length
     * @return separator index or -1
     */
    private int colon(final int length) {
        for (int i = Normal._0; i < length; i++) {
            if (line[i] == Symbol.C_COLON) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the common SSE field code for colon-terminated field names.
     *
     * @param length line length
     * @return field code
     */
    private int commonField(final int length) {
        return switch (line[0]) {
            case 'd' -> length > 4 && line[1] == 'a' && line[2] == 't' && line[3] == 'a' && line[4] == Symbol.C_COLON
                    ? FIELD_DATA
                    : FIELD_UNKNOWN;
            case 'e' -> length > 5 && line[1] == 'v' && line[2] == 'e' && line[3] == 'n' && line[4] == 't'
                    && line[5] == Symbol.C_COLON ? FIELD_EVENT : FIELD_UNKNOWN;
            case 'i' -> length > 2 && line[1] == 'd' && line[2] == Symbol.C_COLON ? FIELD_ID : FIELD_UNKNOWN;
            case 'r' -> length > 5 && line[1] == 'e' && line[2] == 't' && line[3] == 'r' && line[4] == 'y'
                    && line[5] == Symbol.C_COLON ? FIELD_RETRY : FIELD_UNKNOWN;
            default -> FIELD_UNKNOWN;
        };
    }

    /**
     * Returns the value start offset for common colon-terminated fields.
     *
     * @param field field code
     * @return value start offset
     */
    private static int commonValueStart(final int field) {
        return switch (field) {
            case FIELD_DATA -> Normal._5;
            case FIELD_EVENT, FIELD_RETRY -> Normal._6;
            case FIELD_ID -> Normal._3;
            default -> Normal._0;
        };
    }

    /**
     * Returns the field code.
     *
     * @param nameEnd field name end
     * @return field code
     */
    private int field(final int nameEnd) {
        return switch (nameEnd) {
            case Normal._2 -> line[Normal._0] == 'i' && line[Normal._1] == 'd' ? FIELD_ID : FIELD_UNKNOWN;
            case Normal._4 -> line[Normal._0] == 'd' && line[Normal._1] == 'a' && line[Normal._2] == 't'
                    && line[Normal._3] == 'a' ? FIELD_DATA : FIELD_UNKNOWN;
            case Normal._5 -> field5();
            default -> FIELD_UNKNOWN;
        };
    }

    /**
     * Returns a five-byte field code.
     *
     * @return field code
     */
    private int field5() {
        if (line[Normal._0] == 'e' && line[Normal._1] == 'v' && line[Normal._2] == 'e' && line[Normal._3] == 'n'
                && line[Normal._4] == 't') {
            return FIELD_EVENT;
        }
        return line[Normal._0] == 'r' && line[Normal._1] == 'e' && line[Normal._2] == 't' && line[Normal._3] == 'r'
                && line[Normal._4] == 'y' ? FIELD_RETRY : FIELD_UNKNOWN;
    }

    /**
     * Decodes a UTF-8 field value.
     *
     * @param start start index
     * @param end   end index
     * @return decoded value
     */
    private String value(final int start, final int end) {
        if (start >= end) {
            return Normal.EMPTY;
        }
        boolean ascii = true;
        for (int i = start; i < end; i++) {
            if (line[i] < 0) {
                ascii = false;
                break;
            }
        }
        if (ascii) {
            return new String(line, start, end - start, StandardCharsets.ISO_8859_1);
        }
        return ByteString.of(line, start, end - start).string(StandardCharsets.UTF_8);
    }

    /**
     * Appends one data field.
     *
     * @param data  current data
     * @param value field value
     */
    private static void appendData(final StringBuilder data, final String value) {
        if (!data.isEmpty()) {
            data.append(Symbol.C_LF);
        }
        data.append(value);
    }

    /**
     * Creates an event from accumulated fields.
     *
     * @param id          id
     * @param event       event type
     * @param data        single data line
     * @param dataBuilder multi-line data
     * @param retry       retry
     * @return event
     */
    private static SseEvent event(
            final String id,
            final String event,
            final String data,
            final StringBuilder dataBuilder,
            final Duration retry) {
        return SseEvent.of(id, event, dataBuilder == null ? data : dataBuilder.toString(), retry);
    }

    /**
     * Parses retry milliseconds. Invalid values are ignored by SSE parsing.
     *
     * @param start start index
     * @param end   end index
     * @return retry delay
     */
    private Duration parseRetry(final int start, final int end) {
        if (start >= end) {
            return null;
        }
        long millis = Normal.LONG_ZERO;
        for (int i = start; i < end; i++) {
            final int digit = line[i] - '0';
            if (digit < Normal._0 || digit > Normal._9 || millis > (Long.MAX_VALUE - digit) / Normal._10) {
                return null;
            }
            millis = millis * Normal._10 + digit;
        }
        return Duration.ofMillis(millis);
    }

}
