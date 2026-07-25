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
     * Reader-owned unpadded DATA payload.
     */
    private Buffer dataPayload;

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
     * @param type             HTTP/2 frame type code
     * @param streamId         stream id
     * @param flags            frame flags valid for the selected type
     * @param payload          encoded frame payload bytes
     * @param headers          decoded header fields, or {@code null}
     * @param settings         decoded settings values, or {@code null}
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
     * @param type             HTTP/2 frame type code
     * @param streamId         stream id
     * @param flags            frame flags valid for the selected type
     * @param payload          encoded frame payload bytes
     * @param headers          decoded header fields, or {@code null}
     * @param settings         decoded settings values, or {@code null}
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
     *
     * @param type             HTTP/2 frame type code
     * @param streamId         stream identifier, or {@code 0} for connection frames
     * @param flags            frame flags valid for the selected type
     * @param payload          owned immutable payload bytes
     * @param headers          decoded header fields, or {@code null}
     * @param settings         decoded settings values, or {@code null}
     * @param windowDelta      decoded flow-control increment
     * @param errorCode        decoded HTTP/2 error code
     * @param promisedStreamId decoded promised stream identifier
     * @param priority         decoded priority metadata, or {@code null}
     * @param alternateService decoded alternate-service metadata, or {@code null}
     * @param payloadLease     bounded payload lease, or {@code null} for owned payload bytes
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
     * @param payload   DATA bytes carried by the frame
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
     * @param headers   header fields to encode in the frame
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
     * @param headers   trailer fields to encode in the frame
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
     * @param weight             HTTP/2 priority weight in the valid wire range
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
     * @param settings settings values to encode
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
        return new Http2Frame(Normal._6, Normal._0, ack ? Normal._1 : Normal._0, longPayload(payload), List.of(), null,
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
            throw new ValidateException("Invalid HTTP/2 Normal._7 metadata");
        }
        final ByteString payload = goAwayPayload(lastStreamId, errorCode, debugData);
        return new Http2Frame(Normal._7, Normal._0, Normal._0, payload, List.of(), null, Normal._0, errorCode,
                Normal._0);
    }

    /**
     * Creates a WINDOW_UPDATE frame.
     *
     * @param streamId stream id
     * @param delta    positive flow-control increment
     * @return frame
     */
    public static Http2Frame windowUpdate(final int streamId, final long delta) {
        if (streamId < Normal._0 || delta <= Normal._0 || delta > Integer.MAX_VALUE) {
            throw new ValidateException("Invalid HTTP/2 window update");
        }
        return new Http2Frame(Normal._8, streamId, Normal._0, intPayload((int) delta), List.of(), null, delta,
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
        return new Http2Frame(Normal._3, streamId, Normal._0, intPayload(errorCode), List.of(), null, Normal._0,
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
     * @param type     decoded HTTP/2 frame type code
     * @param streamId stream id
     * @param flags    decoded frame flags
     * @param payload  decoded frame payload bytes
     * @param headers  decoded header fields, or {@code null}
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
     * @param type             decoded HTTP/2 frame type code
     * @param streamId         stream id
     * @param flags            decoded frame flags
     * @param payload          decoded frame payload bytes
     * @param headers          decoded header fields, or {@code null}
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
        if (dataPayload != null)
            return dataPayload.snapshot();
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
     * Returns the decoded DATA payload size.
     *
     * @return payload size in bytes
     */
    int payloadSize() {
        return dataPayload == null ? payloadBytes().size() : (int) dataPayload.size();
    }

    /**
     * Transfers ownership of the decoded DATA payload to the caller.
     *
     * @return decoded DATA buffer, or {@code null} when no direct payload is retained
     */
    Buffer takeDataPayload() {
        final Buffer direct = dataPayload;
        dataPayload = null;
        return direct;
    }

    /**
     * Returns headers snapshot.
     *
     * @return headers
     */
    public List<Http2Header> headers() {
        return type == Normal._1 || type == Normal._5 ? headers : List.of();
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
        dataPayload = null;
        if (payloadLease != null) {
            payloadLease.close();
        }
    }

    /**
     * Returns an internal read-only payload slice without materializing ByteString.
     *
     * @return read-only view of the frame payload
     */
    ByteBuffer payloadBuffer() {
        return payloadLease == null ? payload.asByteBuffer() : payloadLease.buffer();
    }

    /**
     * Creates a DATA/header-fragment frame backed by an internal bounded lease.
     *
     * @param type     decoded frame type
     * @param streamId decoded stream identifier
     * @param flags    decoded frame flags
     * @param payload  bounded payload buffer owned by the lease
     * @param releaser action returning the leased storage
     * @return decoded frame backed by the lease when safe, otherwise by copied bytes
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
     * Creates an unpadded DATA frame whose buffer ownership transfers to the dispatcher.
     *
     * @param streamId target stream identifier
     * @param flags    DATA frame flags
     * @param payload  decoded DATA payload whose ownership transfers to the frame
     * @return decoded DATA frame
     */
    static Http2Frame decodedData(final int streamId, final int flags, final Buffer payload) {
        positiveStream(streamId);
        if (payload == null)
            throw new ValidateException("HTTP/2 DATA buffer must not be null");
        validateDecodedLength(Normal._0, streamId, flags, (int) payload.size());
        final Http2Frame frame = new Http2Frame(Normal._0, streamId, flags, ByteString.EMPTY, List.of(), null,
                Normal._0, Normal._0, Normal._0);
        frame.dataPayload = payload;
        return frame;
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
     *
     * @param type     decoded frame type
     * @param streamId decoded stream identifier
     * @param flags    decoded frame flags
     * @param payload  decoded payload bytes to validate
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
     *
     * @param type     decoded frame type
     * @param streamId decoded stream identifier
     * @param flags    decoded frame flags
     * @param length   decoded payload length
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
     *
     * @param actual   actual payload length
     * @param expected required payload length
     * @param type     frame type used in protocol diagnostics
     */
    private static void exactLength(final int actual, final int expected, final String type) {
        if (actual != expected) {
            throw new ProtocolException("Invalid HTTP/2 " + type + " payload length");
        }
    }

    /**
     * Reads a network-order int directly from immutable bytes.
     *
     * @param bytes  immutable source bytes
     * @param offset index of the first encoded byte
     * @return decoded signed 32-bit bit pattern
     */
    private static int intAt(final ByteString bytes, final int offset) {
        return ((bytes.getByte(offset) & 0xff) << 24) | ((bytes.getByte(offset + 1) & 0xff) << 16)
                | ((bytes.getByte(offset + 2) & 0xff) << 8) | (bytes.getByte(offset + 3) & 0xff);
    }

    /**
     * Encodes one network-order integer into an owned immutable payload.
     *
     * @param value integer bit pattern to encode
     * @return four-byte network-order payload
     */
    private static ByteString intPayload(final int value) {
        final byte[] payload = new byte[Normal._4];
        writeInt(payload, Normal._0, value);
        return new ByteString(payload);
    }

    /**
     * Encodes one network-order long into an owned immutable payload.
     *
     * @param value long bit pattern to encode
     * @return eight-byte network-order payload
     */
    private static ByteString longPayload(final long value) {
        final byte[] payload = new byte[Normal._8];
        writeInt(payload, Normal._0, (int) (value >>> 32));
        writeInt(payload, Normal._4, (int) value);
        return new ByteString(payload);
    }

    /**
     * Encodes GOAWAY metadata and optional immutable debug bytes in one allocation.
     *
     * @param lastStreamId last peer-initiated stream identifier processed
     * @param errorCode    HTTP/2 connection error code
     * @param debugData    optional immutable diagnostic bytes
     * @return encoded GOAWAY payload
     */
    private static ByteString goAwayPayload(final int lastStreamId, final int errorCode, final ByteString debugData) {
        final ByteString debug = debugData == null ? ByteString.EMPTY : debugData;
        final byte[] payload = new byte[Normal._8 + debug.size()];
        writeInt(payload, Normal._0, lastStreamId & Integer.MAX_VALUE);
        writeInt(payload, Normal._4, errorCode);
        for (int index = Normal._0; index < debug.size(); index++) {
            payload[Normal._8 + index] = debug.getByte(index);
        }
        return new ByteString(payload);
    }

    /**
     * Writes one network-order integer into an owned byte array.
     *
     * @param target destination byte array
     * @param offset index of the first destination byte
     * @param value  integer bit pattern to encode
     */
    private static void writeInt(final byte[] target, final int offset, final int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + Normal._1] = (byte) (value >>> Normal._16);
        target[offset + Normal._2] = (byte) (value >>> Normal._8);
        target[offset + Normal._3] = (byte) value;
    }

    /**
     * Reads a network-order long directly from immutable bytes.
     *
     * @param bytes  immutable source bytes
     * @param offset index of the first encoded byte
     * @return decoded signed 64-bit bit pattern
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
     * @param headers header fields to validate and copy
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
     * @param settings settings values to encode in wire order
     * @return payload
     */
    private static ByteString settingsPayload(final Http2Settings settings) {
        final int[] ids = settings.ids();
        final byte[] payload = new byte[ids.length * Normal._6];
        int offset = Normal._0;
        for (final int id : ids) {
            payload[offset++] = (byte) (id >>> Normal._8);
            payload[offset++] = (byte) id;
            final int value = (int) settings.getLong(id);
            writeInt(payload, offset, value);
            offset += Normal._4;
        }
        return new ByteString(payload);
    }

    /**
     * Decodes a SETTINGS payload.
     *
     * @param type    frame type used to verify SETTINGS semantics
     * @param flags   frame flags controlling ACK behavior
     * @param payload raw SETTINGS payload bytes
     * @return settings or null
     */
    private static Http2Settings decodedSettings(final int type, final int flags, final ByteString payload) {
        if (type != Normal._4 || (flags & Normal._1) != Normal._0 || payload == null || payload.size() == Normal._0) {
            return null;
        }
        final Http2Settings settings = Http2Settings.defaults();
        for (int offset = Normal._0; offset < payload.size(); offset += Normal._6) {
            final int id = ((payload.getByte(offset) & 0xff) << Normal._8)
                    | (payload.getByte(offset + Normal._1) & 0xff);
            final long value = Integer.toUnsignedLong(intAt(payload, offset + Normal._2));
            if (id >= Normal._1 && id <= Normal._6) {
                settings.set(id, value);
            }
        }
        return settings;
    }

    /**
     * Decodes window delta.
     *
     * @param type    frame type used to select window-delta semantics
     * @param payload raw frame payload bytes
     * @return delta
     */
    private static long decodedWindowDelta(final int type, final ByteString payload) {
        if (type != Normal._8 || payload == null || payload.size() != Normal._4) {
            return Normal._0;
        }
        return intAt(payload, Normal._0) & Integer.MAX_VALUE;
    }

    /**
     * Decodes error code.
     *
     * @param type    frame type used to select error-code semantics
     * @param payload raw frame payload bytes
     * @return error code
     */
    private static int decodedErrorCode(final int type, final ByteString payload) {
        if (payload == null) {
            return Normal._0;
        }
        if (type == Normal._3 && payload.size() == Normal._4) {
            return intAt(payload, Normal._0);
        }
        if (type == Normal._7 && payload.size() >= Normal._4 * Normal._2) {
            return intAt(payload, Normal._4);
        }
        return Normal._0;
    }

    /**
     * Decodes a promised stream id.
     *
     * @param type    frame type used to select PUSH_PROMISE semantics
     * @param payload raw PUSH_PROMISE payload bytes
     * @return promised stream id
     */
    private static int decodedPromisedStreamId(final int type, final ByteString payload) {
        if (type != Normal._5 || payload == null || payload.size() < Normal._4) {
            return Normal._0;
        }
        final int id = intAt(payload, Normal._0) & Integer.MAX_VALUE;
        if (id <= Normal._0) {
            throw new ProtocolException("Invalid HTTP/2 promised stream id");
        }
        return id;
    }

    /**
     * Decodes priority metadata.
     *
     * @param type     frame type used to select priority semantics
     * @param streamId stream id
     * @param payload  raw priority payload bytes
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
     * @param type     frame type used to select ALTSVC semantics
     * @param streamId stream id
     * @param payload  raw ALTSVC payload bytes
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
            return new ByteString(copy);
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
