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
package org.miaixz.bus.fabric.network.kcp;

import java.nio.ByteBuffer;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Immutable KCP packet snapshot.
 *
 * @param type            packet type
 * @param sequence        sequence number
 * @param acknowledgement acknowledged sequence, or -1 for data packets
 * @param window          advertised receive window
 * @param timestamp       sender timestamp in milliseconds
 * @param payload         payload bytes
 * @author Kimi Liu
 * @since Java 21+
 */
public record KcpPacket(Type type, long sequence, long acknowledgement, int window, long timestamp, byte[] payload) {

    /**
     * Packet wire version.
     */
    private static final byte VERSION = 1;

    /**
     * Wire header size.
     */
    private static final int HEADER_BYTES = 1 + 1 + Integer.BYTES + Integer.BYTES + Short.BYTES + Long.BYTES;

    /**
     * Compact sequence header size.
     */
    private static final int COMPACT_SEQUENCE_BYTES = Long.BYTES;

    /**
     * Maximum unsigned 32-bit sequence.
     */
    private static final long MAX_SEQUENCE = 4_294_967_295L;

    /**
     * Maximum UDP payload size.
     */
    private static final int MAX_DATAGRAM = 65_507;

    /**
     * Maximum KCP payload size once the wire header is included.
     */
    private static final int MAX_PAYLOAD = MAX_DATAGRAM - HEADER_BYTES;

    /**
     * Default advertised window.
     */
    private static final int DEFAULT_WINDOW = 32;

    /**
     * Creates a validated packet.
     */
    public KcpPacket {
        if (type == null) {
            throw new ValidateException("KCP packet type must not be null");
        }
        validateSequence(sequence, "KCP sequence");
        if (acknowledgement != -1L) {
            validateSequence(acknowledgement, "KCP acknowledgement");
        }
        if (window < 0 || window > 65_535) {
            throw new ValidateException("KCP window must fit in unsigned 16 bits");
        }
        if (timestamp < 0) {
            throw new ValidateException("KCP timestamp must not be negative");
        }
        if (payload == null || payload.length > MAX_PAYLOAD) {
            throw new ValidateException("KCP payload must be non-null and fit in one datagram");
        }
        if (type == Type.ACK && payload.length > 0) {
            throw new ValidateException("KCP ACK packet must not carry payload");
        }
        if (type == Type.DATA && acknowledgement != -1L) {
            throw new ValidateException("KCP DATA packet must not carry acknowledgement");
        }
        if (type == Type.ACK && acknowledgement < 0) {
            throw new ValidateException("KCP ACK packet must carry acknowledgement");
        }
        payload = ArrayKit.clone(payload);
    }

    /**
     * Creates a data packet.
     *
     * @param sequence sequence
     * @param payload  payload
     */
    public KcpPacket(final long sequence, final byte[] payload) {
        this(Type.DATA, sequence, -1L, DEFAULT_WINDOW, System.currentTimeMillis(), payload);
    }

    /**
     * Creates a data packet.
     *
     * @param sequence sequence
     * @param payload  payload
     * @return packet
     */
    public static KcpPacket of(final long sequence, final byte[] payload) {
        return data(sequence, payload, DEFAULT_WINDOW, System.currentTimeMillis());
    }

    /**
     * Creates a data packet.
     *
     * @param sequence  sequence
     * @param payload   payload
     * @param window    advertised window
     * @param timestamp timestamp
     * @return packet
     */
    public static KcpPacket data(final long sequence, final byte[] payload, final int window, final long timestamp) {
        return new KcpPacket(Type.DATA, sequence, -1L, window, timestamp, payload);
    }

    /**
     * Creates an ACK packet.
     *
     * @param acknowledgement acknowledged sequence
     * @return packet
     */
    public static KcpPacket ack(final long acknowledgement) {
        return ack(acknowledgement, DEFAULT_WINDOW, System.currentTimeMillis());
    }

    /**
     * Creates an ACK packet.
     *
     * @param acknowledgement acknowledged sequence
     * @param window          advertised window
     * @param timestamp       timestamp
     * @return packet
     */
    public static KcpPacket ack(final long acknowledgement, final int window, final long timestamp) {
        return new KcpPacket(Type.ACK, 0L, acknowledgement, window, timestamp, new byte[0]);
    }

    /**
     * Decodes a datagram payload into a packet.
     *
     * @param datagram datagram bytes
     * @return packet
     */
    public static KcpPacket fromDatagram(final byte[] datagram) {
        if (datagram == null) {
            throw new ValidateException("KCP datagram must not be null");
        }
        if (datagram.length > MAX_DATAGRAM) {
            throw new ProtocolException("KCP datagram exceeds maximum UDP payload");
        }
        if (datagram.length > 0 && datagram[0] == VERSION && datagram.length < HEADER_BYTES) {
            throw new ProtocolException("KCP versioned datagram is too short");
        }
        if (datagram.length >= HEADER_BYTES && datagram[0] == VERSION) {
            final ByteBuffer buffer = ByteBuffer.wrap(datagram);
            buffer.get();
            final Type type = Type.fromWire(buffer.get());
            final long sequence = Integer.toUnsignedLong(buffer.getInt());
            final long acknowledgement = Integer.toUnsignedLong(buffer.getInt());
            final int window = Short.toUnsignedInt(buffer.getShort());
            final long timestamp = buffer.getLong();
            final byte[] payload = new byte[buffer.remaining()];
            buffer.get(payload);
            return type == Type.ACK ? ack(acknowledgement, window, timestamp)
                    : data(sequence, payload, window, timestamp);
        }
        if (datagram.length >= COMPACT_SEQUENCE_BYTES) {
            final ByteBuffer buffer = ByteBuffer.wrap(datagram);
            final long sequence = buffer.getLong();
            final byte[] payload = new byte[buffer.remaining()];
            buffer.get(payload);
            return of(sequence, payload);
        }
        throw new ProtocolException("KCP datagram is too short");
    }

    /**
     * Returns the KCP wire header size.
     *
     * @return header bytes
     */
    public static int headerBytes() {
        return HEADER_BYTES;
    }

    /**
     * Returns the maximum datagram payload accepted by UDP.
     *
     * @return max datagram bytes
     */
    public static int maxDatagramBytes() {
        return MAX_DATAGRAM;
    }

    /**
     * Returns the maximum KCP data payload that fits in one datagram.
     *
     * @return max payload bytes
     */
    public static int maxPayloadBytes() {
        return MAX_PAYLOAD;
    }

    /**
     * Encodes this packet to datagram bytes.
     *
     * @return datagram bytes
     */
    public byte[] datagram() {
        final ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + payload.length);
        buffer.put(VERSION);
        buffer.put(type.wire);
        buffer.putInt((int) sequence);
        buffer.putInt((int) (acknowledgement < 0 ? 0 : acknowledgement));
        buffer.putShort((short) window);
        buffer.putLong(timestamp);
        buffer.put(payload);
        return buffer.array();
    }

    /**
     * Returns a payload snapshot.
     *
     * @return payload
     */
    @Override
    public byte[] payload() {
        return ArrayKit.clone(payload);
    }

    /**
     * Returns a read-only payload buffer.
     *
     * @return buffer
     */
    public ByteBuffer buffer() {
        return ByteBuffer.wrap(payload).asReadOnlyBuffer();
    }

    /**
     * Validates sequence and acknowledgement numbers before they are serialized into 32-bit wire fields.
     *
     * @param value sequence value
     * @param name  field name used in validation messages
     */
    private static void validateSequence(final long value, final String name) {
        if (value < 0 || value > MAX_SEQUENCE) {
            throw new ValidateException(name + " out of range");
        }
    }

    /**
     * KCP packet type.
     */
    public enum Type {

        /**
         * Data packet.
         */
        DATA((byte) 1),

        /**
         * Acknowledgement packet.
         */
        ACK((byte) 2);

        /**
         * Wire value.
         */
        private final byte wire;

        /**
         * Binds an enum value to the single-byte wire marker used in KCP packets.
         *
         * @param wire wire marker
         */
        Type(final byte wire) {
            this.wire = wire;
        }

        /**
         * Resolves a wire marker into a packet type.
         *
         * @param wire wire marker
         * @return packet type
         */
        private static Type fromWire(final byte wire) {
            for (final Type type : values()) {
                if (type.wire == wire) {
                    return type;
                }
            }
            throw new ProtocolException("Unsupported KCP packet type: " + wire);
        }

    }

}
