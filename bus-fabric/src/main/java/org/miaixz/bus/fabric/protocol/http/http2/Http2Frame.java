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
import java.util.concurrent.atomic.AtomicBoolean;

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
public final class Http2Frame implements AutoCloseable {

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
    private volatile ByteString payload;

    /**
     * Optional internal zero-copy payload owner.
     */
    private final PayloadLease payloadLease;

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
     * Unsigned eight-byte PING payload encoded in a Java {@code long}.
     */
    private final long pingPayload;

    /**
     * Last processed stream identifier carried by a GOAWAY frame.
     */
    private final int lastStreamId;

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
        this(type, streamId, flags, payload, headers, settings, windowDelta, errorCode, promisedStreamId, null, null,
                null);
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
        this(type, streamId, flags, payload, headers, settings, windowDelta, errorCode, promisedStreamId, priority,
                alternateService, null);
    }

    /**
     * Full constructor including an internal leased payload.
     */
    private Http2Frame(final int type, final int streamId, final int flags, final ByteString payload,
            final List<Http2Header> headers, final Http2Settings settings, final long windowDelta, final int errorCode,
            final int promisedStreamId, final Http2Priority priority, final Http2AlternateService alternateService,
            final PayloadLease payloadLease) {
        this.type = type;
        this.streamId = streamId;
        this.flags = flags;
        this.payload = payload == null ? ByteString.EMPTY : payload;
        this.payloadLease = payloadLease;
        this.headers = headers == null ? List.of() : List.copyOf(headers);
        this.settings = settings == null ? null : settings.copy();
        this.windowDelta = windowDelta;
        this.errorCode = errorCode;
        this.promisedStreamId = promisedStreamId;
        this.endStream = (flags & Normal._1) != Normal._0;
        this.priority = priority;
        this.alternateService = alternateService;
        this.pingPayload = type == Normal._6 && this.payload.size() == Normal._8 ? longAt(this.payload, Normal._0)
                : Normal._0;
        this.lastStreamId = type == Normal._7 && this.payload.size() >= Normal._8
                ? intAt(this.payload, Normal._0) & Integer.MAX_VALUE
                : Normal._0;
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
                .notNull(payload, () -> new ValidateException("HTTP/2 Normal._0 payload must not be null"));
        return new Http2Frame(Normal._0, streamId, endStream ? Normal._1 : Normal._0, checkedPayload, List.of(), null,
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
        final int flags = Normal._4 | (endStream ? Normal._1 : Normal._0) | (priority == null ? Normal._0 : Normal._32);
        return new Http2Frame(Normal._1, streamId, flags, ByteString.EMPTY, checkedHeaders, null, Normal._0, Normal._0,
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
        return new Http2Frame(Normal._2, streamId, Normal._0, checkedPriority.encodeBytes(), List.of(), null, Normal._0,
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
        return new Http2Frame(Normal._5, streamId, Normal._4, ByteString.EMPTY, checkedHeaders, null, Normal._0,
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
        return new Http2Frame(Normal._4, Normal._0, Normal._0, settingsPayload(checkedSettings), List.of(),
                checkedSettings, Normal._0, Normal._0, Normal._0);
    }

    /**
     * Creates a SETTINGS ACK frame.
     *
     * @return frame
     */
    public static Http2Frame settingsAck() {
        return new Http2Frame(Normal._4, Normal._0, Normal._1, ByteString.EMPTY, List.of(), null, Normal._0, Normal._0,
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
        return new Http2Frame(Normal._6, Normal._0, ack ? Normal._1 : Normal._0, value.readByteString(), List.of(),
                null, Normal._0, Normal._0, Normal._0);
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
            throw new ValidateException("Invalid HTTP/2 Normal._7 metadata");
        }
        final Buffer payload = new Buffer();
        payload.writeInt(lastStreamId & Integer.MAX_VALUE);
        payload.writeInt(errorCode);
        payload.write(debugData == null ? ByteString.EMPTY : debugData);
        return new Http2Frame(Normal._7, Normal._0, Normal._0, payload.readByteString(), List.of(), null, Normal._0,
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
        return new Http2Frame(Normal._8, streamId, Normal._0, payload.readByteString(), List.of(), null, delta,
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
        return new Http2Frame(Normal._3, streamId, Normal._0, payload.readByteString(), List.of(), null, Normal._0,
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
            throw new ValidateException("HTTP/2 Normal._10 stream id must not be negative");
        }
        final Http2AlternateService checkedAlternateService = Assert
                .notNull(alternateService, () -> new ValidateException("HTTP/2 alternate service must not be null"));
        try {
            Http2AlternateService.validateStreamContext(streamId, checkedAlternateService);
        } catch (final ProtocolException e) {
            throw new ValidateException(e.getMessage(), e);
        }
        return new Http2Frame(Normal._10, streamId, Normal._0, checkedAlternateService.encodeBytes(), List.of(), null,
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
        validateDecoded(type, streamId, flags, payload);
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
        return (flags & Normal._1) != Normal._0;
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
        ByteString current = payload;
        if (current.size() == Normal._0 && payloadLease != null && payloadLease.remaining() != Normal._0) {
            synchronized (this) {
                current = payload;
                if (current.size() == Normal._0) {
                    current = payloadLease.bytes();
                    payload = current;
                    payloadLease.close();
                }
            }
        }
        return current;
    }

    /**
     * Returns headers snapshot.
     *
     * @return headers
     */
    public List<Http2Header> headers() {
        return type == Normal._1 || type == Normal._5 ? List.copyOf(headers) : List.of();
    }

    /**
     * Returns settings snapshot.
     *
     * @return settings or null
     */
    public Http2Settings settings() {
        return type == Normal._4 && settings != null ? settings.copy() : null;
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
        return pingPayload;
    }

    /**
     * Returns the GOAWAY last stream id.
     *
     * @return last stream id
     */
    public int lastStreamId() {
        return lastStreamId;
    }

    /**
     * Returns GOAWAY debug data as immutable bytes.
     *
     * @return debug data
     */
    public ByteString debugDataBytes() {
        if (type != Normal._7 || payload.size() <= Normal._4 * Normal._2) {
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
        return type == Normal._2 || type == Normal._1 ? priority : null;
    }

    /**
     * Returns ALTSVC metadata.
     *
     * @return alternate service metadata, or null when absent
     */
    public Http2AlternateService alternateService() {
        return type == Normal._10 ? alternateService : null;
    }

    /**
     * Releases an internal payload lease at most once.
     */
    @Override
    public void close() {
        if (payloadLease != null) {
            payloadLease.close();
        }
    }

    /**
     * Returns an internal read-only payload slice without materializing ByteString.
     */
    ByteBuffer payloadBuffer() {
        return payloadLease == null ? ByteBuffer.wrap(payload.toByteArray()).asReadOnlyBuffer() : payloadLease.buffer();
    }

    /**
     * Creates a DATA/header-fragment frame backed by an internal bounded lease.
     */
    static Http2Frame decodedLeased(
            final int type,
            final int streamId,
            final int flags,
            final ByteBuffer payload,
            final Runnable releaser) {
        final PayloadLease lease = new PayloadLease(payload, releaser);
        final int length = lease.remaining();
        if (length > 64 * 1024 || payload.capacity() > Math.max(64 * 1024, length << Normal._2)) {
            final ByteString bytes = lease.bytes();
            lease.close();
            return decoded(type, streamId, flags, bytes, List.of());
        }
        if (type != Normal._0 && type != Normal._1 && type != Normal._9) {
            final ByteString bytes = lease.bytes();
            lease.close();
            return decoded(type, streamId, flags, bytes, List.of());
        }
        validateDecodedLength(type, streamId, flags, length);
        return new Http2Frame(type, streamId, flags, ByteString.EMPTY, List.of(), null, Normal._0, Normal._0, Normal._0,
                null, null, lease);
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
     * Validates known decoded frame invariants before interpreting payload bytes.
     */
    private static void validateDecoded(final int type, final int streamId, final int flags, final ByteString payload) {
        if (payload == null) {
            throw new ProtocolException("HTTP/2 frame payload must not be null");
        }
        validateDecodedLength(type, streamId, flags, payload.size());
        if (type == Normal._8 && (intAt(payload, Normal._0) & Integer.MAX_VALUE) == Normal._0) {
            throw new ProtocolException("HTTP/2 WINDOW_UPDATE increment must be non-zero");
        }
    }

    /**
     * Validates frame type/stream/length combinations without allocating.
     */
    private static void validateDecodedLength(final int type, final int streamId, final int flags, final int length) {
        if (streamId < Normal._0 || length < Normal._0) {
            throw new ProtocolException("Invalid HTTP/2 frame metadata");
        }
        final boolean streamRequired = type == Normal._0 || type == Normal._1 || type == Normal._2 || type == Normal._3
                || type == Normal._5 || type == Normal._9;
        final boolean connectionRequired = type == Normal._4 || type == Normal._6 || type == Normal._7;
        if ((streamRequired && streamId == Normal._0) || (connectionRequired && streamId != Normal._0)) {
            throw new ProtocolException("Invalid HTTP/2 frame stream id for type " + type);
        }
        switch (type) {
            case Normal._2 -> exactLength(length, Normal._5, "PRIORITY");
            case Normal._3 -> exactLength(length, Normal._4, "RST_STREAM");
            case Normal._4 -> {
                if ((flags & Normal._1) != Normal._0 ? length != Normal._0 : length % Normal._6 != Normal._0) {
                    throw new ProtocolException("Invalid HTTP/2 SETTINGS payload length");
                }
            }
            case Normal._5 -> {
                if (length < Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 PUSH_PROMISE payload length");
                }
            }
            case Normal._6 -> exactLength(length, Normal._8, "PING");
            case Normal._7 -> {
                if (length < Normal._8) {
                    throw new ProtocolException("Invalid HTTP/2 GOAWAY payload length");
                }
            }
            case Normal._8 -> exactLength(length, Normal._4, "WINDOW_UPDATE");
            default -> {
                // DATA, HEADERS, CONTINUATION and unknown extension frames are variable length.
            }
        }
    }

    /**
     * Requires an exact control-frame payload length.
     */
    private static void exactLength(final int actual, final int expected, final String type) {
        if (actual != expected) {
            throw new ProtocolException("Invalid HTTP/2 " + type + " payload length");
        }
    }

    /**
     * Reads a network-order int directly from immutable bytes.
     */
    private static int intAt(final ByteString bytes, final int offset) {
        return ((bytes.getByte(offset) & 0xff) << 24) | ((bytes.getByte(offset + 1) & 0xff) << 16)
                | ((bytes.getByte(offset + 2) & 0xff) << 8) | (bytes.getByte(offset + 3) & 0xff);
    }

    /**
     * Reads a network-order long directly from immutable bytes.
     */
    private static long longAt(final ByteString bytes, final int offset) {
        return ((long) intAt(bytes, offset) << 32) | Integer.toUnsignedLong(intAt(bytes, offset + Normal._4));
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
            payload.writeInt((int) settings.getLong(id));
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
        if (type != Normal._4 || (flags & Normal._1) != Normal._0 || payload == null || payload.size() == Normal._0) {
            return null;
        }
        final Buffer view = new Buffer().write(payload);
        final Http2Settings settings = Http2Settings.defaults();
        while (view.size() > Normal._0) {
            final int id = view.readShort() & Normal._65535;
            final long value = Integer.toUnsignedLong(view.readInt());
            if (id >= Normal._1 && id <= Normal._6) {
                settings.set(id, value);
            }
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
        if (type != Normal._8 || payload == null || payload.size() != Normal._4) {
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
        if (type == Normal._3 && payload.size() == Normal._4) {
            return new Buffer().write(payload).readInt();
        }
        if (type == Normal._7 && payload.size() >= Normal._4 * Normal._2) {
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
        if (type != Normal._5 || payload == null || payload.size() < Normal._4) {
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
        if (type != Normal._2) {
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
        if (type != Normal._10) {
            return null;
        }
        return Http2AlternateService.decode(payload, streamId);
    }

    /**
     * Internal read-only payload slice with an idempotent parent-buffer release. It never exposes a writable view and
     * is bounded by the frame retention policy.
     */
    static final class PayloadLease implements AutoCloseable {

        /**
         * Read-only payload slice whose position is private to this lease.
         */
        private final ByteBuffer payload;

        /**
         * Parent-buffer release callback invoked at most once.
         */
        private final Runnable releaser;

        /**
         * Idempotent close guard shared by all lease access paths.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a lease over a bounded payload slice.
         *
         * @param payload  payload slice retained from the reader
         * @param releaser callback returning parent-buffer ownership
         */
        private PayloadLease(final ByteBuffer payload, final Runnable releaser) {
            if (payload == null || releaser == null) {
                throw new ValidateException("HTTP/2 payload lease requires payload and releaser");
            }
            this.payload = payload.slice().asReadOnlyBuffer();
            this.releaser = releaser;
        }

        /**
         * Returns the number of payload bytes visible to this lease.
         *
         * @return remaining payload bytes
         */
        private int remaining() {
            return payload.remaining();
        }

        /**
         * Returns an independent read-only view of the retained payload.
         *
         * @return read-only payload view
         * @throws IllegalStateException if this lease is closed
         */
        private ByteBuffer buffer() {
            if (closed.get()) {
                throw new IllegalStateException("HTTP/2 payload lease is closed");
            }
            return payload.asReadOnlyBuffer();
        }

        /**
         * Copies the retained payload into an independently owned byte string.
         *
         * @return copied payload bytes
         */
        private ByteString bytes() {
            final ByteBuffer view = buffer();
            final byte[] copy = new byte[view.remaining()];
            view.get(copy);
            return ByteString.of(copy);
        }

        /**
         * Releases parent-buffer ownership exactly once.
         */
        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                releaser.run();
            }
        }
    }

}
