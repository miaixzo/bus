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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable KCP packet snapshot.
 *
 * @param type            packet type
 * @param sequence        sequence number
 * @param acknowledgement acknowledged sequence, or -1 for data packets
 * @param window          advertised receive window
 * @param timestamp       sender timestamp in milliseconds
 * @param payloadBytes    payload bytes
 * @author Kimi Liu
 * @since Java 21+
 */
public record KcpPacket(Type type, long sequence, long acknowledgement, int window, long timestamp,
                        ByteString payloadBytes) {

    /**
     * Packet wire version.
     */
    private static final byte VERSION = (byte) Normal._1;

    /**
     * Wire header size.
     */
    private static final int HEADER_BYTES = Byte.BYTES + Byte.BYTES + Integer.BYTES + Integer.BYTES + Short.BYTES
            + Long.BYTES;

    /**
     * Compact sequence header size.
     */
    private static final int COMPACT_SEQUENCE_BYTES = Long.BYTES;

    /**
     * Maximum unsigned 32-bit sequence.
     */
    private static final long MAX_SEQUENCE = 0xffff_ffffL;

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
    private static final int DEFAULT_WINDOW = Normal._32;

    /**
     * Acknowledgement marker used by data packets.
     */
    private static final long NO_ACKNOWLEDGEMENT = Normal.__1;

    /**
     * Creates a validated packet.
     */
    public KcpPacket {
        type = Assert.notNull(type, () -> new ValidateException("KCP packet type must not be null"));
        validateSequence(sequence, "KCP sequence");
        if (acknowledgement != NO_ACKNOWLEDGEMENT) {
            validateSequence(acknowledgement, "KCP acknowledgement");
        }
        Assert.checkBetween(
                window,
                Normal._0,
                Normal._65535,
                () -> new ValidateException("KCP window must fit in unsigned 16 bits"));
        Assert.isTrue(timestamp >= Normal._0, () -> new ValidateException("KCP timestamp must not be negative"));
        payloadBytes = Assert.notNull(
                payloadBytes,
                () -> new ValidateException("KCP payload must be non-null and fit in one datagram"));
        Assert.isTrue(
                payloadBytes.size() <= MAX_PAYLOAD,
                () -> new ValidateException("KCP payload must be non-null and fit in one datagram"));
        if (type == Type.ACK && payloadBytes.size() > Normal._0) {
            throw new ValidateException("KCP ACK packet must not carry payload");
        }
        if (type == Type.DATA && acknowledgement != NO_ACKNOWLEDGEMENT) {
            throw new ValidateException("KCP DATA packet must not carry acknowledgement");
        }
        if (type == Type.ACK && acknowledgement < Normal._0) {
            throw new ValidateException("KCP ACK packet must carry acknowledgement");
        }
    }

    /**
     * Creates a data packet.
     *
     * @param sequence sequence
     * @param payload  payload
     */
    public KcpPacket(final long sequence, final byte[] payload) {
        this(sequence, payload == null ? null : ByteString.of(payload));
    }

    /**
     * Creates a data packet.
     *
     * @param sequence     sequence
     * @param payloadBytes payload bytes
     */
    public KcpPacket(final long sequence, final ByteString payloadBytes) {
        this(Type.DATA, sequence, NO_ACKNOWLEDGEMENT, DEFAULT_WINDOW, System.currentTimeMillis(), payloadBytes);
    }

    /**
     * Creates a data packet.
     *
     * @param sequence sequence
     * @param payload  payload
     * @return packet
     */
    public static KcpPacket of(final long sequence, final byte[] payload) {
        return of(sequence, payload == null ? null : ByteString.of(payload));
    }

    /**
     * Creates a data packet.
     *
     * @param sequence     sequence
     * @param payloadBytes payload bytes
     * @return packet
     */
    public static KcpPacket of(final long sequence, final ByteString payloadBytes) {
        return data(sequence, payloadBytes, DEFAULT_WINDOW, System.currentTimeMillis());
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
        return data(sequence, payload == null ? null : ByteString.of(payload), window, timestamp);
    }

    /**
     * Creates a data packet.
     *
     * @param sequence     sequence
     * @param payloadBytes payload bytes
     * @param window       advertised window
     * @param timestamp    timestamp
     * @return packet
     */
    public static KcpPacket data(
            final long sequence,
            final ByteString payloadBytes,
            final int window,
            final long timestamp) {
        return new KcpPacket(Type.DATA, sequence, NO_ACKNOWLEDGEMENT, window, timestamp, payloadBytes);
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
        return new KcpPacket(Type.ACK, Normal._0, acknowledgement, window, timestamp, ByteString.EMPTY);
    }

    /**
     * Decodes a datagram payload into a packet.
     *
     * @param datagram datagram bytes
     * @return packet
     */
    public static KcpPacket fromDatagram(final byte[] datagram) {
        Assert.notNull(datagram, () -> new ValidateException("KCP datagram must not be null"));
        if (datagram.length > MAX_DATAGRAM) {
            throw new ProtocolException("KCP datagram exceeds maximum UDP payload");
        }
        if (datagram.length > Normal._0 && datagram[Normal._0] == VERSION && datagram.length < HEADER_BYTES) {
            throw new ProtocolException("KCP versioned datagram is too short");
        }
        if (datagram.length >= HEADER_BYTES && datagram[Normal._0] == VERSION) {
            final Buffer buffer = new Buffer().write(datagram);
            buffer.readByte();
            final Type type = Type.fromWire(buffer.readByte());
            final long sequence = Integer.toUnsignedLong(buffer.readInt());
            final long acknowledgement = Integer.toUnsignedLong(buffer.readInt());
            final int window = Short.toUnsignedInt(buffer.readShort());
            final long timestamp = buffer.readLong();
            final ByteString payload = buffer.readByteString();
            return type == Type.ACK ? ack(acknowledgement, window, timestamp)
                    : data(sequence, payload, window, timestamp);
        }
        if (datagram.length >= COMPACT_SEQUENCE_BYTES) {
            final Buffer buffer = new Buffer().write(datagram);
            final long sequence = buffer.readLong();
            final ByteString payload = buffer.readByteString();
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
        final Buffer buffer = new Buffer();
        buffer.writeByte(VERSION);
        buffer.writeByte(type.wire);
        buffer.writeInt((int) sequence);
        buffer.writeInt((int) (acknowledgement < Normal._0 ? Normal._0 : acknowledgement));
        buffer.writeShort(window);
        buffer.writeLong(timestamp);
        buffer.write(payloadBytes);
        return buffer.readByteArray();
    }

    /**
     * Validates sequence and acknowledgement numbers before they are serialized into 32-bit wire fields.
     *
     * @param value sequence value
     * @param name  field name used in validation messages
     */
    private static void validateSequence(final long value, final String name) {
        Assert.checkBetween(value, Normal._0, MAX_SEQUENCE, () -> new ValidateException(name + " out of range"));
    }

    /**
     * KCP packet type.
     */
    public enum Type {

        /**
         * Data packet.
         */
        DATA((byte) Normal._1),

        /**
         * Acknowledgement packet.
         */
        ACK((byte) Normal._2);

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
