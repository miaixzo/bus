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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable HTTP/2 frame value.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Frame {

    /**
     * DATA frame type.
     */
    public static final int DATA = Normal._0;

    /**
     * HEADERS frame type.
     */
    public static final int HEADERS = Normal._1;

    /**
     * PRIORITY frame type.
     */
    public static final int PRIORITY = Normal._2;

    /**
     * RST_STREAM frame type.
     */
    public static final int RST_STREAM = Normal._3;

    /**
     * SETTINGS frame type.
     */
    public static final int SETTINGS = Normal._4;

    /**
     * PUSH_PROMISE frame type.
     */
    public static final int PUSH_PROMISE = Normal._5;

    /**
     * PING frame type.
     */
    public static final int PING = Normal._6;

    /**
     * GOAWAY frame type.
     */
    public static final int GOAWAY = Normal._7;

    /**
     * WINDOW_UPDATE frame type.
     */
    public static final int WINDOW_UPDATE = Normal._8;

    /**
     * CONTINUATION frame type.
     */
    public static final int CONTINUATION = Normal._9;

    /**
     * ALTSVC frame type.
     */
    public static final int ALTSVC = Normal._10;

    /**
     * END_STREAM flag.
     */
    public static final int END_STREAM = Normal._1;

    /**
     * END_HEADERS flag.
     */
    public static final int END_HEADERS = Normal._4;

    /**
     * ACK flag used by SETTINGS and PING.
     */
    public static final int ACK = Normal._1;

    /**
     * PRIORITY flag used by HEADERS.
     */
    public static final int PRIORITY_FLAG = Normal._32;

    /**
     * Frame type.
     */
    private final int type;

    /**
     * Stream id.
     */
    private final int streamId;

    /**
     * Flags.
     */
    private final int flags;

    /**
     * Payload.
     */
    private final ByteString payload;

    /**
     * Headers.
     */
    private final List<Http2Header> headers;

    /**
     * Settings snapshot.
     */
    private final Http2Settings settings;

    /**
     * Window update delta.
     */
    private final long windowDelta;

    /**
     * Error code.
     */
    private final int errorCode;

    /**
     * Promised stream id for PUSH_PROMISE.
     */
    private final int promisedStreamId;

    /**
     * End stream flag.
     */
    private final boolean endStream;

    /**
     * Priority metadata for PRIORITY frames or HEADERS with PRIORITY flag.
     */
    private final Http2Priority priority;

    /**
     * ALTSVC metadata.
     */
    private final Http2AlternateService alternateService;

    /**
     * Creates a frame.
     *
     * @param type             type
     * @param streamId         stream id
     * @param flags            flags
     * @param payload          payload
     * @param headers          headers
     * @param settings         settings
     * @param windowDelta      window delta
     * @param errorCode        error code
     * @param promisedStreamId promised stream id
     */
    private Http2Frame(final int type, final int streamId, final int flags, final ByteString payload,
                       final List<Http2Header> headers, final Http2Settings settings, final long windowDelta, final int errorCode,
                       final int promisedStreamId) {
        this(type, streamId, flags, payload, headers, settings, windowDelta, errorCode, promisedStreamId, null, null);
    }

    /**
     * Creates a frame.
     *
     * @param type             type
     * @param streamId         stream id
     * @param flags            flags
     * @param payload          payload
     * @param headers          headers
     * @param settings         settings
     * @param windowDelta      window delta
     * @param errorCode        error code
     * @param promisedStreamId promised stream id
     * @param priority         priority metadata
     * @param alternateService alternate service metadata
     */
    private Http2Frame(final int type, final int streamId, final int flags, final ByteString payload,
                       final List<Http2Header> headers, final Http2Settings settings, final long windowDelta, final int errorCode,
                       final int promisedStreamId, final Http2Priority priority, final Http2AlternateService alternateService) {
        this.type = type;
        this.streamId = streamId;
        this.flags = flags;
        this.payload = payload == null ? ByteString.EMPTY : payload;
        this.headers = headers == null ? List.of() : List.copyOf(headers);
        this.settings = settings == null ? null : settings.copy();
        this.windowDelta = windowDelta;
        this.errorCode = errorCode;
        this.promisedStreamId = promisedStreamId;
        this.endStream = (flags & END_STREAM) != Normal._0;
        this.priority = priority;
        this.alternateService = alternateService;
    }

    /**
     * Creates a DATA frame.
     *
     * @param streamId  stream id
     * @param payload   payload
     * @param endStream end stream flag
     * @return frame
     */
    public static Http2Frame data(final int streamId, final ByteString payload, final boolean endStream) {
        positiveStream(streamId);
        final ByteString checkedPayload = Assert
                .notNull(payload, () -> new ValidateException("HTTP/2 DATA payload must not be null"));
        return new Http2Frame(DATA, streamId, endStream ? END_STREAM : Normal._0, checkedPayload, List.of(), null,
                Normal._0, Normal._0, Normal._0);
    }

    /**
     * Creates a HEADERS frame.
     *
     * @param streamId  stream id
     * @param headers   headers
     * @param endStream end stream flag
     * @return frame
     */
    public static Http2Frame headers(final int streamId, final List<Http2Header> headers, final boolean endStream) {
        return headers(streamId, headers, endStream, null);
    }

    /**
     * Creates a HEADERS frame with optional priority metadata.
     *
     * @param streamId  stream id
     * @param headers   headers
     * @param endStream end stream flag
     * @param priority  priority metadata
     * @return frame
     */
    public static Http2Frame headers(
            final int streamId,
            final List<Http2Header> headers,
            final boolean endStream,
            final Http2Priority priority) {
        positiveStream(streamId);
        final List<Http2Header> checkedHeaders = headersSnapshot(
                headers,
                "HTTP/2 headers must not contain null values");
        validatePriorityOwner(streamId, priority);
        final int flags = END_HEADERS | (endStream ? END_STREAM : Normal._0)
                | (priority == null ? Normal._0 : PRIORITY_FLAG);
        return new Http2Frame(HEADERS, streamId, flags, ByteString.EMPTY, checkedHeaders, null, Normal._0, Normal._0,
                Normal._0, priority, null);
    }

    /**
     * Creates a PRIORITY frame.
     *
     * @param streamId stream id
     * @param priority priority metadata
     * @return frame
     */
    public static Http2Frame priority(final int streamId, final Http2Priority priority) {
        positiveStream(streamId);
        final Http2Priority checkedPriority = Assert
                .notNull(priority, () -> new ValidateException("HTTP/2 priority must not be null"));
        validatePriorityOwner(streamId, checkedPriority);
        return new Http2Frame(PRIORITY, streamId, Normal._0, checkedPriority.encodeBytes(), List.of(), null, Normal._0,
                Normal._0, Normal._0, checkedPriority, null);
    }

    /**
     * Creates a PRIORITY frame.
     *
     * @param streamId           stream id
     * @param dependencyStreamId dependency stream id
     * @param weight             weight
     * @param exclusive          exclusive flag
     * @return frame
     */
    public static Http2Frame priority(
            final int streamId,
            final int dependencyStreamId,
            final int weight,
            final boolean exclusive) {
        return priority(streamId, Http2Priority.of(dependencyStreamId, weight, exclusive));
    }

    /**
     * Creates a PUSH_PROMISE frame.
     *
     * @param streamId         associated stream id
     * @param promisedStreamId promised stream id
     * @param headers          pushed request headers
     * @return frame
     */
    public static Http2Frame pushPromise(
            final int streamId,
            final int promisedStreamId,
            final List<Http2Header> headers) {
        positiveStream(streamId);
        positiveStream(promisedStreamId);
        final List<Http2Header> checkedHeaders = headersSnapshot(
                headers,
                "HTTP/2 push headers must not contain null values");
        return new Http2Frame(PUSH_PROMISE, streamId, END_HEADERS, ByteString.EMPTY, checkedHeaders, null, Normal._0,
                Normal._0, promisedStreamId);
    }

    /**
     * Creates a SETTINGS frame.
     *
     * @param settings settings
     * @return frame
     */
    public static Http2Frame settings(final Http2Settings settings) {
        final Http2Settings checkedSettings = Assert
                .notNull(settings, () -> new ValidateException("HTTP/2 settings must not be null"));
        return new Http2Frame(SETTINGS, Normal._0, Normal._0, settingsPayload(checkedSettings), List.of(),
                checkedSettings, Normal._0, Normal._0, Normal._0);
    }

    /**
     * Creates a SETTINGS ACK frame.
     *
     * @return frame
     */
    public static Http2Frame settingsAck() {
        return new Http2Frame(SETTINGS, Normal._0, ACK, ByteString.EMPTY, List.of(), null, Normal._0, Normal._0,
                Normal._0);
    }

    /**
     * Creates a PING frame.
     *
     * @param payload opaque 8-byte payload represented as a long
     * @param ack     true for PING ACK
     * @return frame
     */
    public static Http2Frame ping(final long payload, final boolean ack) {
        final Buffer value = new Buffer();
        value.writeLong(payload);
        return new Http2Frame(PING, Normal._0, ack ? ACK : Normal._0, value.readByteString(), List.of(), null,
                Normal._0, Normal._0, Normal._0);
    }

    /**
     * Creates a GOAWAY frame.
     *
     * @param lastStreamId last peer-initiated stream id that may have been processed
     * @param errorCode    error code
     * @param debugData    optional debug data
     * @return frame
     */
    public static Http2Frame goAway(final int lastStreamId, final int errorCode, final ByteString debugData) {
        if (lastStreamId < Normal._0 || errorCode < Normal._0) {
            throw new ValidateException("Invalid HTTP/2 GOAWAY metadata");
        }
        final Buffer payload = new Buffer();
        payload.writeInt(lastStreamId & Integer.MAX_VALUE);
        payload.writeInt(errorCode);
        payload.write(debugData == null ? ByteString.EMPTY : debugData);
        return new Http2Frame(GOAWAY, Normal._0, Normal._0, payload.readByteString(), List.of(), null, Normal._0,
                errorCode, Normal._0);
    }

    /**
     * Creates a WINDOW_UPDATE frame.
     *
     * @param streamId stream id
     * @param delta    delta
     * @return frame
     */
    public static Http2Frame windowUpdate(final int streamId, final long delta) {
        if (streamId < Normal._0 || delta <= Normal._0 || delta > Integer.MAX_VALUE) {
            throw new ValidateException("Invalid HTTP/2 window update");
        }
        final Buffer payload = new Buffer();
        payload.writeInt((int) delta);
        return new Http2Frame(WINDOW_UPDATE, streamId, Normal._0, payload.readByteString(), List.of(), null, delta,
                Normal._0, Normal._0);
    }

    /**
     * Creates an RST_STREAM frame.
     *
     * @param streamId  stream id
     * @param errorCode error code
     * @return frame
     */
    public static Http2Frame rstStream(final int streamId, final int errorCode) {
        positiveStream(streamId);
        if (errorCode < Normal._0) {
            throw new ValidateException("HTTP/2 error code must be non-negative");
        }
        final Buffer payload = new Buffer();
        payload.writeInt(errorCode);
        return new Http2Frame(RST_STREAM, streamId, Normal._0, payload.readByteString(), List.of(), null, Normal._0,
                errorCode, Normal._0);
    }

    /**
     * Creates an ALTSVC frame.
     *
     * @param streamId         stream id, 0 for connection scoped ALTSVC
     * @param alternateService alternate service metadata
     * @return frame
     */
    public static Http2Frame alternateService(final int streamId, final Http2AlternateService alternateService) {
        if (streamId < Normal._0) {
            throw new ValidateException("HTTP/2 ALTSVC stream id must not be negative");
        }
        final Http2AlternateService checkedAlternateService = Assert
                .notNull(alternateService, () -> new ValidateException("HTTP/2 alternate service must not be null"));
        try {
            Http2AlternateService.validateStreamContext(streamId, checkedAlternateService);
        } catch (final ProtocolException e) {
            throw new ValidateException(e.getMessage(), e);
        }
        return new Http2Frame(ALTSVC, streamId, Normal._0, checkedAlternateService.encodeBytes(), List.of(), null,
                Normal._0, Normal._0, Normal._0, null, checkedAlternateService);
    }

    /**
     * Creates a decoded frame.
     *
     * @param type     type
     * @param streamId stream id
     * @param flags    flags
     * @param payload  payload
     * @param headers  headers
     * @return frame
     */
    static Http2Frame decoded(
            final int type,
            final int streamId,
            final int flags,
            final ByteString payload,
            final List<Http2Header> headers) {
        return decoded(type, streamId, flags, payload, headers, null, null);
    }

    /**
     * Creates a decoded frame with pre-parsed extension metadata.
     *
     * @param type             type
     * @param streamId         stream id
     * @param flags            flags
     * @param payload          payload
     * @param headers          headers
     * @param priority         priority metadata
     * @param alternateService alternate service metadata
     * @return frame
     */
    static Http2Frame decoded(
            final int type,
            final int streamId,
            final int flags,
            final ByteString payload,
            final List<Http2Header> headers,
            final Http2Priority priority,
            final Http2AlternateService alternateService) {
        final Http2Priority decodedPriority = priority == null ? decodedPriority(type, streamId, payload) : priority;
        final Http2AlternateService decodedAlternateService = alternateService == null
                ? decodedAlternateService(type, streamId, payload)
                : alternateService;
        return new Http2Frame(type, streamId, flags, payload, headers, decodedSettings(type, flags, payload),
                decodedWindowDelta(type, payload), decodedErrorCode(type, payload),
                decodedPromisedStreamId(type, payload), decodedPriority, decodedAlternateService);
    }

    /**
     * Returns type.
     *
     * @return type
     */
    public int type() {
        return type;
    }

    /**
     * Returns stream id.
     *
     * @return stream id
     */
    public int streamId() {
        return streamId;
    }

    /**
     * Returns flags.
     *
     * @return flags
     */
    public int flags() {
        return flags;
    }

    /**
     * Returns whether ACK flag is set.
     *
     * @return true when ACK is set
     */
    public boolean ack() {
        return (flags & ACK) != Normal._0;
    }

    /**
     * Returns whether END_STREAM is set.
     *
     * @return true when set
     */
    public boolean endStream() {
        return endStream;
    }

    /**
     * Returns immutable payload bytes.
     *
     * @return payload bytes
     */
    public ByteString payloadBytes() {
        return payload;
    }

    /**
     * Returns headers snapshot.
     *
     * @return headers
     */
    public List<Http2Header> headers() {
        return type == HEADERS || type == PUSH_PROMISE ? List.copyOf(headers) : List.of();
    }

    /**
     * Returns settings snapshot.
     *
     * @return settings or null
     */
    public Http2Settings settings() {
        return type == SETTINGS && settings != null ? settings.copy() : null;
    }

    /**
     * Returns window delta.
     *
     * @return delta
     */
    public long windowDelta() {
        return windowDelta;
    }

    /**
     * Returns error code.
     *
     * @return error code
     */
    public int errorCode() {
        return errorCode;
    }

    /**
     * Returns the PING opaque payload.
     *
     * @return ping payload
     */
    public long pingPayload() {
        if (type != PING || payload.size() != Normal._8) {
            return Normal._0;
        }
        return new Buffer().write(payload).readLong();
    }

    /**
     * Returns the GOAWAY last stream id.
     *
     * @return last stream id
     */
    public int lastStreamId() {
        if (type != GOAWAY || payload.size() < Normal._4 * Normal._2) {
            return Normal._0;
        }
        return new Buffer().write(payload).readInt() & Integer.MAX_VALUE;
    }

    /**
     * Returns GOAWAY debug data as immutable bytes.
     *
     * @return debug data
     */
    public ByteString debugDataBytes() {
        if (type != GOAWAY || payload.size() <= Normal._4 * Normal._2) {
            return ByteString.EMPTY;
        }
        return payload.substring(Normal._4 * Normal._2);
    }

    /**
     * Returns promised stream id.
     *
     * @return promised stream id, or 0 when absent
     */
    public int promisedStreamId() {
        return promisedStreamId;
    }

    /**
     * Returns priority metadata.
     *
     * @return priority metadata, or null when absent
     */
    public Http2Priority priority() {
        return type == PRIORITY || type == HEADERS ? priority : null;
    }

    /**
     * Returns ALTSVC metadata.
     *
     * @return alternate service metadata, or null when absent
     */
    public Http2AlternateService alternateService() {
        return type == ALTSVC ? alternateService : null;
    }

    /**
     * Validates positive stream id.
     *
     * @param streamId stream id
     */
    private static void positiveStream(final int streamId) {
        if (streamId <= Normal._0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
    }

    /**
     * Validates that a stream does not depend on itself.
     *
     * @param streamId stream id
     * @param priority priority metadata
     */
    private static void validatePriorityOwner(final int streamId, final Http2Priority priority) {
        if (priority != null && priority.dependencyStreamId() == streamId) {
            throw new ValidateException("HTTP/2 stream cannot depend on itself");
        }
    }

    /**
     * Validates and snapshots headers.
     *
     * @param headers headers
     * @param message failure message
     * @return immutable snapshot
     */
    private static List<Http2Header> headersSnapshot(final List<Http2Header> headers, final String message) {
        final List<Http2Header> checkedHeaders = Assert.notNull(headers, () -> new ValidateException(message));
        for (final Http2Header header : checkedHeaders) {
            Assert.notNull(header, () -> new ValidateException(message));
        }
        return List.copyOf(checkedHeaders);
    }

    /**
     * Encodes settings payload.
     *
     * @param settings settings
     * @return payload
     */
    private static ByteString settingsPayload(final Http2Settings settings) {
        final int[] ids = settings.ids();
        final Buffer payload = new Buffer();
        for (final int id : ids) {
            payload.writeShort(id);
            payload.writeInt(settings.get(id));
        }
        return payload.readByteString();
    }

    /**
     * Decodes a SETTINGS payload.
     *
     * @param type    type
     * @param flags   flags
     * @param payload payload
     * @return settings or null
     */
    private static Http2Settings decodedSettings(final int type, final int flags, final ByteString payload) {
        if (type != SETTINGS || (flags & ACK) != Normal._0 || payload == null || payload.size() == Normal._0) {
            return null;
        }
        final Buffer view = new Buffer().write(payload);
        final Http2Settings settings = Http2Settings.defaults();
        while (view.size() > Normal._0) {
            settings.set(view.readShort() & Normal._65535, view.readInt());
        }
        return settings;
    }

    /**
     * Decodes window delta.
     *
     * @param type    type
     * @param payload payload
     * @return delta
     */
    private static long decodedWindowDelta(final int type, final ByteString payload) {
        if (type != WINDOW_UPDATE || payload == null || payload.size() != Normal._4) {
            return Normal._0;
        }
        return new Buffer().write(payload).readInt() & Integer.MAX_VALUE;
    }

    /**
     * Decodes error code.
     *
     * @param type    type
     * @param payload payload
     * @return error code
     */
    private static int decodedErrorCode(final int type, final ByteString payload) {
        if (payload == null) {
            return Normal._0;
        }
        if (type == RST_STREAM && payload.size() == Normal._4) {
            return new Buffer().write(payload).readInt();
        }
        if (type == GOAWAY && payload.size() >= Normal._4 * Normal._2) {
            final Buffer view = new Buffer().write(payload);
            view.readInt();
            return view.readInt();
        }
        return Normal._0;
    }

    /**
     * Decodes a promised stream id.
     *
     * @param type    type
     * @param payload payload
     * @return promised stream id
     */
    private static int decodedPromisedStreamId(final int type, final ByteString payload) {
        if (type != PUSH_PROMISE || payload == null || payload.size() < Normal._4) {
            return Normal._0;
        }
        final int id = new Buffer().write(payload).readInt() & Integer.MAX_VALUE;
        if (id <= Normal._0) {
            throw new ProtocolException("Invalid HTTP/2 promised stream id");
        }
        return id;
    }

    /**
     * Decodes priority metadata.
     *
     * @param type     type
     * @param streamId stream id
     * @param payload  payload
     * @return priority or null
     */
    private static Http2Priority decodedPriority(final int type, final int streamId, final ByteString payload) {
        if (type != PRIORITY) {
            return null;
        }
        return Http2Priority.decode(payload, streamId);
    }

    /**
     * Decodes alternate-service metadata.
     *
     * @param type     type
     * @param streamId stream id
     * @param payload  payload
     * @return alternate service or null
     */
    private static Http2AlternateService decodedAlternateService(
            final int type,
            final int streamId,
            final ByteString payload) {
        if (type != ALTSVC) {
            return null;
        }
        return Http2AlternateService.decode(payload, streamId);
    }

}
