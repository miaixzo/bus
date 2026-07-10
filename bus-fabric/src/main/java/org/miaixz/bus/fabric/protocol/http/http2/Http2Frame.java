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

import java.nio.ByteBuffer;
import java.util.List;

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
    public static final int DATA = 0;

    /**
     * HEADERS frame type.
     */
    public static final int HEADERS = 1;

    /**
     * PRIORITY frame type.
     */
    public static final int PRIORITY = 2;

    /**
     * RST_STREAM frame type.
     */
    public static final int RST_STREAM = 3;

    /**
     * SETTINGS frame type.
     */
    public static final int SETTINGS = 4;

    /**
     * PUSH_PROMISE frame type.
     */
    public static final int PUSH_PROMISE = 5;

    /**
     * PING frame type.
     */
    public static final int PING = 6;

    /**
     * GOAWAY frame type.
     */
    public static final int GOAWAY = 7;

    /**
     * WINDOW_UPDATE frame type.
     */
    public static final int WINDOW_UPDATE = 8;

    /**
     * CONTINUATION frame type.
     */
    public static final int CONTINUATION = 9;

    /**
     * ALTSVC frame type.
     */
    public static final int ALTSVC = 10;

    /**
     * END_STREAM flag.
     */
    public static final int END_STREAM = 0x1;

    /**
     * END_HEADERS flag.
     */
    public static final int END_HEADERS = 0x4;

    /**
     * ACK flag used by SETTINGS and PING.
     */
    public static final int ACK = 0x1;

    /**
     * PRIORITY flag used by HEADERS.
     */
    public static final int PRIORITY_FLAG = 0x20;

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
    private final ByteBuffer payload;

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
    private Http2Frame(final int type, final int streamId, final int flags, final ByteBuffer payload,
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
    private Http2Frame(final int type, final int streamId, final int flags, final ByteBuffer payload,
            final List<Http2Header> headers, final Http2Settings settings, final long windowDelta, final int errorCode,
            final int promisedStreamId, final Http2Priority priority, final Http2AlternateService alternateService) {
        this.type = type;
        this.streamId = streamId;
        this.flags = flags;
        this.payload = snapshot(payload);
        this.headers = headers == null ? List.of() : List.copyOf(headers);
        this.settings = settings == null ? null : settings.copy();
        this.windowDelta = windowDelta;
        this.errorCode = errorCode;
        this.promisedStreamId = promisedStreamId;
        this.endStream = (flags & END_STREAM) != 0;
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
    public static Http2Frame data(final int streamId, final ByteBuffer payload, final boolean endStream) {
        positiveStream(streamId);
        if (payload == null) {
            throw new ValidateException("HTTP/2 DATA payload must not be null");
        }
        return new Http2Frame(DATA, streamId, endStream ? END_STREAM : 0, payload, List.of(), null, 0L, 0, 0);
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
        if (headers == null || headers.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidateException("HTTP/2 headers must not contain null values");
        }
        validatePriorityOwner(streamId, priority);
        final int flags = END_HEADERS | (endStream ? END_STREAM : 0) | (priority == null ? 0 : PRIORITY_FLAG);
        return new Http2Frame(HEADERS, streamId, flags, ByteBuffer.allocate(0), headers, null, 0L, 0, 0, priority,
                null);
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
        if (priority == null) {
            throw new ValidateException("HTTP/2 priority must not be null");
        }
        validatePriorityOwner(streamId, priority);
        return new Http2Frame(PRIORITY, streamId, 0, priority.encode(), List.of(), null, 0L, 0, 0, priority, null);
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
        if (headers == null || headers.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidateException("HTTP/2 push headers must not contain null values");
        }
        return new Http2Frame(PUSH_PROMISE, streamId, END_HEADERS, ByteBuffer.allocate(0), headers, null, 0L, 0,
                promisedStreamId);
    }

    /**
     * Creates a SETTINGS frame.
     *
     * @param settings settings
     * @return frame
     */
    public static Http2Frame settings(final Http2Settings settings) {
        if (settings == null) {
            throw new ValidateException("HTTP/2 settings must not be null");
        }
        return new Http2Frame(SETTINGS, 0, 0, settingsPayload(settings), List.of(), settings, 0L, 0, 0);
    }

    /**
     * Creates a SETTINGS ACK frame.
     *
     * @return frame
     */
    public static Http2Frame settingsAck() {
        return new Http2Frame(SETTINGS, 0, ACK, ByteBuffer.allocate(0), List.of(), null, 0L, 0, 0);
    }

    /**
     * Creates a PING frame.
     *
     * @param payload opaque 8-byte payload represented as a long
     * @param ack     true for PING ACK
     * @return frame
     */
    public static Http2Frame ping(final long payload, final boolean ack) {
        final ByteBuffer value = ByteBuffer.allocate(Long.BYTES).putLong(payload);
        value.flip();
        return new Http2Frame(PING, 0, ack ? ACK : 0, value, List.of(), null, 0L, 0, 0);
    }

    /**
     * Creates a GOAWAY frame.
     *
     * @param lastStreamId last peer-initiated stream id that may have been processed
     * @param errorCode    error code
     * @param debugData    optional debug data
     * @return frame
     */
    public static Http2Frame goAway(final int lastStreamId, final int errorCode, final ByteBuffer debugData) {
        if (lastStreamId < 0 || errorCode < 0) {
            throw new ValidateException("Invalid HTTP/2 GOAWAY metadata");
        }
        final ByteBuffer debug = debugData == null ? ByteBuffer.allocate(0) : debugData.asReadOnlyBuffer();
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES + Integer.BYTES + debug.remaining());
        payload.putInt(lastStreamId & 0x7fffffff);
        payload.putInt(errorCode);
        payload.put(debug);
        payload.flip();
        return new Http2Frame(GOAWAY, 0, 0, payload, List.of(), null, 0L, errorCode, 0);
    }

    /**
     * Creates a WINDOW_UPDATE frame.
     *
     * @param streamId stream id
     * @param delta    delta
     * @return frame
     */
    public static Http2Frame windowUpdate(final int streamId, final long delta) {
        if (streamId < 0 || delta <= 0 || delta > 0x7fffffffL) {
            throw new ValidateException("Invalid HTTP/2 window update");
        }
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES).putInt((int) delta);
        payload.flip();
        return new Http2Frame(WINDOW_UPDATE, streamId, 0, payload, List.of(), null, delta, 0, 0);
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
        if (errorCode < 0) {
            throw new ValidateException("HTTP/2 error code must be non-negative");
        }
        final ByteBuffer payload = ByteBuffer.allocate(4).putInt(errorCode);
        payload.flip();
        return new Http2Frame(RST_STREAM, streamId, 0, payload, List.of(), null, 0L, errorCode, 0);
    }

    /**
     * Creates an ALTSVC frame.
     *
     * @param streamId         stream id, 0 for connection scoped ALTSVC
     * @param alternateService alternate service metadata
     * @return frame
     */
    public static Http2Frame alternateService(final int streamId, final Http2AlternateService alternateService) {
        if (streamId < 0) {
            throw new ValidateException("HTTP/2 ALTSVC stream id must not be negative");
        }
        if (alternateService == null) {
            throw new ValidateException("HTTP/2 alternate service must not be null");
        }
        try {
            Http2AlternateService.validateStreamContext(streamId, alternateService);
        } catch (final ProtocolException e) {
            throw new ValidateException(e.getMessage(), e);
        }
        return new Http2Frame(ALTSVC, streamId, 0, alternateService.encode(), List.of(), null, 0L, 0, 0, null,
                alternateService);
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
            final ByteBuffer payload,
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
            final ByteBuffer payload,
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
        return (flags & ACK) != 0;
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
     * Returns payload snapshot.
     *
     * @return payload
     */
    public ByteBuffer payload() {
        return payload.asReadOnlyBuffer();
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
        if (type != PING || payload.remaining() != Long.BYTES) {
            return 0L;
        }
        return payload.asReadOnlyBuffer().getLong();
    }

    /**
     * Returns the GOAWAY last stream id.
     *
     * @return last stream id
     */
    public int lastStreamId() {
        if (type != GOAWAY || payload.remaining() < Integer.BYTES * 2) {
            return 0;
        }
        return payload.asReadOnlyBuffer().getInt() & 0x7fffffff;
    }

    /**
     * Returns GOAWAY debug data.
     *
     * @return debug data
     */
    public ByteBuffer debugData() {
        if (type != GOAWAY || payload.remaining() <= Integer.BYTES * 2) {
            return ByteBuffer.allocate(0).asReadOnlyBuffer();
        }
        final ByteBuffer view = payload.asReadOnlyBuffer();
        view.position(Integer.BYTES * 2);
        return view.slice().asReadOnlyBuffer();
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
        if (streamId <= 0) {
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
     * Encodes settings payload.
     *
     * @param settings settings
     * @return payload
     */
    private static ByteBuffer settingsPayload(final Http2Settings settings) {
        final int[] ids = settings.ids();
        final ByteBuffer payload = ByteBuffer.allocate(ids.length * 6);
        for (final int id : ids) {
            payload.putShort((short) id);
            payload.putInt(settings.get(id));
        }
        payload.flip();
        return payload;
    }

    /**
     * Decodes a SETTINGS payload.
     *
     * @param type    type
     * @param flags   flags
     * @param payload payload
     * @return settings or null
     */
    private static Http2Settings decodedSettings(final int type, final int flags, final ByteBuffer payload) {
        if (type != SETTINGS || (flags & ACK) != 0 || payload == null || payload.remaining() == 0) {
            return null;
        }
        final ByteBuffer view = payload.asReadOnlyBuffer();
        final Http2Settings settings = Http2Settings.defaults();
        while (view.hasRemaining()) {
            settings.set(view.getShort() & 0xffff, view.getInt());
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
    private static long decodedWindowDelta(final int type, final ByteBuffer payload) {
        if (type != WINDOW_UPDATE || payload == null || payload.remaining() != Integer.BYTES) {
            return 0L;
        }
        return payload.asReadOnlyBuffer().getInt() & 0x7fffffffL;
    }

    /**
     * Decodes error code.
     *
     * @param type    type
     * @param payload payload
     * @return error code
     */
    private static int decodedErrorCode(final int type, final ByteBuffer payload) {
        if (payload == null) {
            return 0;
        }
        if (type == RST_STREAM && payload.remaining() == Integer.BYTES) {
            return payload.asReadOnlyBuffer().getInt();
        }
        if (type == GOAWAY && payload.remaining() >= Integer.BYTES * 2) {
            final ByteBuffer view = payload.asReadOnlyBuffer();
            view.getInt();
            return view.getInt();
        }
        return 0;
    }

    /**
     * Decodes a promised stream id.
     *
     * @param type    type
     * @param payload payload
     * @return promised stream id
     */
    private static int decodedPromisedStreamId(final int type, final ByteBuffer payload) {
        if (type != PUSH_PROMISE || payload == null || payload.remaining() < Integer.BYTES) {
            return 0;
        }
        final int id = payload.asReadOnlyBuffer().getInt() & 0x7fffffff;
        if (id <= 0) {
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
    private static Http2Priority decodedPriority(final int type, final int streamId, final ByteBuffer payload) {
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
            final ByteBuffer payload) {
        if (type != ALTSVC) {
            return null;
        }
        return Http2AlternateService.decode(payload, streamId);
    }

    /**
     * Creates a read-only payload snapshot.
     *
     * @param source source
     * @return payload
     */
    private static ByteBuffer snapshot(final ByteBuffer source) {
        if (source == null) {
            return ByteBuffer.allocate(0).asReadOnlyBuffer();
        }
        final ByteBuffer view = source.asReadOnlyBuffer();
        final byte[] bytes = new byte[view.remaining()];
        view.get(bytes);
        return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
    }

}
