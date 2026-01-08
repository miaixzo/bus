/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.metric.sse;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.SegmentBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;

import java.io.IOException;

/**
 * A reader for Server-Sent Events (SSE), responsible for parsing event stream data. It reads from an input stream,
 * identifies event fields (id, event, data, retry, etc.), and triggers callbacks to process the events.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ServerSentEventReader {

    /**
     * The input stream for reading server-sent event data.
     */
    private final BufferSource source;

    /**
     * The event callback interface for notifying event data and retry time changes.
     */
    private final Callback callback;

    /**
     * The ID of the last event, which may be null.
     */
    private String lastId;

    /**
     * Constructs a new {@code ServerSentEventReader} instance.
     *
     * @param source   The input stream containing server-sent event data.
     * @param callback The event callback interface for processing parsed events.
     */
    public ServerSentEventReader(BufferSource source, Callback callback) {
        this.source = source;
        this.callback = callback;
        this.lastId = null;
    }

    /**
     * Processes the next event. If the data part is non-empty, an {@link Callback#onEvent} call will be triggered.
     * Multiple {@link Callback#onRetryChange} calls may be triggered during event processing.
     *
     * @return {@code false} if the end of the input stream (EOF) is reached; {@code true} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public boolean processNextEvent() throws IOException {
        String id = lastId;
        String type = null;
        Buffer data = new Buffer();

        while (true) {
            int option = source.select(options);
            switch (option) {
                case 0: // "\r\n"
                case 1: // "\r"
                case 2: // "\n"
                    completeEvent(id, type, data);
                    return true;

                case 3: // "data: "
                case 4: // "data:"
                    readData(source, data);
                    break;

                case 5: // "data\r\n"
                case 6: // "data\r"
                case 7: // "data\n"
                    data.writeByte('\n');
                    break;

                case 8: // "id: "
                case 9: // "id:"
                    String idValue = source.readUtf8LineStrict();
                    id = idValue.isEmpty() ? null : idValue;
                    break;

                case 10: // "id\r\n"
                case 11: // "id\r"
                case 12: // "id\n"
                    id = null;
                    break;

                case 13: // "event: "
                case 14: // "event:"
                    String typeValue = source.readUtf8LineStrict();
                    type = typeValue.isEmpty() ? null : typeValue;
                    break;

                case 15: // "event\r\n"
                case 16: // "event\r"
                case 17: // "event\n"
                    type = null;
                    break;

                case 18: // "retry: "
                case 19: // "retry:"
                    long retryMs = readRetryMs(source);
                    if (retryMs != -1L) {
                        callback.onRetryChange(retryMs);
                    }
                    break;

                case -1:
                    long lineEnd = source.indexOfElement(CRLF);
                    if (lineEnd != -1L) {
                        // Skip the current line and newline characters.
                        source.skip(lineEnd);
                        source.select(options);
                    } else {
                        return false; // No more newline characters, indicating EOF.
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        }
    }

    /**
     * Completes the processing of an event. If the data is non-empty, it saves the ID and triggers the callback.
     *
     * @param id   The event ID, which may be null.
     * @param type The event type, which may be null.
     * @param data The event data buffer.
     * @throws IOException if an I/O error occurs.
     */
    private void completeEvent(String id, String type, Buffer data) throws IOException {
        if (data.size() != 0L) {
            lastId = id;
            data.skip(1L); // Skip the leading newline character.
            callback.onEvent(id, type, data.readUtf8());
        }
    }

    /**
     * Options for SSE event fields and newline characters, used for parsing the event stream.
     */
    private static final SegmentBuffer options = SegmentBuffer.of(
            /* 0 */ ByteString.encodeUtf8("\r\n"),
            /* 1 */ ByteString.encodeUtf8("\r"),
            /* 2 */ ByteString.encodeUtf8("\n"),
            /* 3 */ ByteString.encodeUtf8("data: "),
            /* 4 */ ByteString.encodeUtf8("data:"),
            /* 5 */ ByteString.encodeUtf8("data\r\n"),
            /* 6 */ ByteString.encodeUtf8("data\r"),
            /* 7 */ ByteString.encodeUtf8("data\n"),
            /* 8 */ ByteString.encodeUtf8("id: "),
            /* 9 */ ByteString.encodeUtf8("id:"),
            /* 10 */ ByteString.encodeUtf8("id\r\n"),
            /* 11 */ ByteString.encodeUtf8("id\r"),
            /* 12 */ ByteString.encodeUtf8("id\n"),
            /* 13 */ ByteString.encodeUtf8("event: "),
            /* 14 */ ByteString.encodeUtf8("event:"),
            /* 15 */ ByteString.encodeUtf8("event\r\n"),
            /* 16 */ ByteString.encodeUtf8("event\r"),
            /* 17 */ ByteString.encodeUtf8("event\n"),
            /* 18 */ ByteString.encodeUtf8("retry: "),
            /* 19 */ ByteString.encodeUtf8("retry:"));

    /**
     * Carriage return-line feed (CRLF), used to locate line endings in the event stream.
     */
    private static final ByteString CRLF = ByteString.encodeUtf8("\r\n");

    /**
     * Reads the content of a data field from the input stream and appends it to the data buffer.
     *
     * @param source The input stream.
     * @param data   The data buffer.
     * @throws IOException if an I/O error occurs.
     */
    private static void readData(BufferSource source, Buffer data) throws IOException {
        data.writeByte('\n');
        source.readFully(data, source.indexOfElement(CRLF));
        source.select(options); // Skip the newline characters.
    }

    /**
     * Reads the retry time field and parses it into milliseconds.
     *
     * @param source The input stream.
     * @return The retry time in milliseconds, or -1 if parsing fails.
     * @throws IOException if an I/O error occurs.
     */
    private static long readRetryMs(BufferSource source) throws IOException {
        String retryString = source.readUtf8LineStrict();
        try {
            return Long.parseLong(retryString);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    /**
     * Callback interface for handling server-sent events and retry time changes.
     */
    public interface Callback {

        /**
         * Called when a new event is received.
         *
         * @param id   The event ID, which may be null.
         * @param type The event type, which may be null.
         * @param data The event data.
         */
        void onEvent(String id, String type, String data);

        /**
         * Called when the retry time (from the 'retry' field) changes.
         *
         * @param timeMs The retry time in milliseconds.
         */
        void onRetryChange(long timeMs);
    }

}
