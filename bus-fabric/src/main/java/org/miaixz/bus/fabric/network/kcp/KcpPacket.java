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
import org.miaixz.bus.fabric.Builder;

/**
 * Immutable KCP packet snapshot.
 *
 * @param version         wire version, or zero for decoded legacy data
 * @param type            packet type
 * @param sequence        sequence number
 * @param acknowledgement acknowledged sequence, or zero for data packets
 * @param window          advertised receive window
 * @param timestamp       sender timestamp in milliseconds
 * @param messageId       V2 logical message identifier
 * @param fragmentIndex   V2 zero-based fragment index
 * @param fragmentCount   V2 fragment count
 * @param payloadBytes    payload bytes
 * @author Kimi Liu
 * @since Java 21+
 */
public record KcpPacket(int version, Type type, long sequence, long acknowledgement, int window, long timestamp,
        long messageId, int fragmentIndex, int fragmentCount, ByteString payloadBytes) {

    /**
     * Creates a validated packet.
     */
    public KcpPacket {
        if (version < Normal._0 || version > Normal._2) {
            throw new ValidateException("KCP version must be legacy, V1, or V2");
        }
        type = Assert.notNull(type, () -> new ValidateException("KCP packet type must not be null"));
        validateSequence(sequence, "KCP sequence");
        validateSequence(acknowledgement, "KCP acknowledgement");
        Assert.checkBetween(
                window,
                Normal._0,
                Normal._65535,
                () -> new ValidateException("KCP window must fit in unsigned 16 bits"));
        Assert.isTrue(timestamp >= Normal._0, () -> new ValidateException("KCP timestamp must not be negative"));
        validateSequence(messageId, "KCP message id");
        validateUnsignedShort(fragmentIndex, "KCP fragment index");
        validateUnsignedShort(fragmentCount, "KCP fragment count");
        payloadBytes = Assert.notNull(
                payloadBytes,
                () -> new ValidateException("KCP payload must be non-null and fit in one datagram"));
        Assert.isTrue(
                payloadBytes.size() <= maxPayloadBytes(version),
                () -> new ValidateException("KCP payload must be non-null and fit in one datagram"));
        if (version == Normal._0 && type != Type.DATA) {
            throw new ValidateException("Legacy KCP packets must be data packets");
        }
        if (type == Type.DATA) {
            if (acknowledgement != Normal._0) {
                throw new ValidateException("KCP DATA packet acknowledgement must be zero");
            }
            if (version < Normal._2
                    && (messageId != Normal._0 || fragmentIndex != Normal._0 || fragmentCount != Normal._0)) {
                throw new ValidateException("Legacy and V1 KCP data packets must not carry fragment fields");
            }
            if (version == Normal._2 && (fragmentCount == Normal._0 || fragmentIndex >= fragmentCount)) {
                throw new ValidateException("KCP V2 fragment index must be less than a positive fragment count");
            }
        } else if (payloadBytes.size() != Normal._0 || messageId != Normal._0 || fragmentIndex != Normal._0
                || fragmentCount != Normal._0) {
            throw new ValidateException("KCP ACK packet must have empty payload and zero fragment fields");
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
        this(Normal._1, Type.DATA, sequence, Normal._0, Normal._32, System.currentTimeMillis(), Normal._0, Normal._0,
                Normal._0, payloadBytes);
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
        return data(sequence, payloadBytes, Normal._32, System.currentTimeMillis());
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
        return new KcpPacket(Normal._1, Type.DATA, sequence, Normal._0, window, timestamp, Normal._0, Normal._0,
                Normal._0, payloadBytes);
    }

    /**
     * Creates a V2 data fragment.
     *
     * @param sequence      sequence
     * @param payload       fragment payload bytes
     * @param window        advertised window
     * @param timestamp     timestamp
     * @param messageId     logical message identifier
     * @param fragmentIndex zero-based fragment index
     * @param fragmentCount fragment count
     * @return packet
     */
    public static KcpPacket dataV2(
            final long sequence,
            final byte[] payload,
            final int window,
            final long timestamp,
            final long messageId,
            final int fragmentIndex,
            final int fragmentCount) {
        return dataV2(
                sequence,
                payload == null ? null : ByteString.of(payload),
                window,
                timestamp,
                messageId,
                fragmentIndex,
                fragmentCount);
    }

    /**
     * Creates a V2 data fragment.
     *
     * @param sequence      sequence
     * @param payloadBytes  fragment payload bytes
     * @param window        advertised window
     * @param timestamp     timestamp
     * @param messageId     logical message identifier
     * @param fragmentIndex zero-based fragment index
     * @param fragmentCount fragment count
     * @return packet
     */
    public static KcpPacket dataV2(
            final long sequence,
            final ByteString payloadBytes,
            final int window,
            final long timestamp,
            final long messageId,
            final int fragmentIndex,
            final int fragmentCount) {
        return new KcpPacket(Normal._2, Type.DATA, sequence, Normal._0, window, timestamp, messageId, fragmentIndex,
                fragmentCount, payloadBytes);
    }

    /**
     * Creates an ACK packet.
     *
     * @param acknowledgement acknowledged sequence
     * @return packet
     */
    public static KcpPacket ack(final long acknowledgement) {
        return ack(acknowledgement, Normal._32, System.currentTimeMillis());
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
        return new KcpPacket(Normal._1, Type.ACK, Normal._0, acknowledgement, window, timestamp, Normal._0, Normal._0,
                Normal._0, ByteString.EMPTY);
    }

    /**
     * Decodes a datagram payload into a packet.
     *
     * @param datagram datagram bytes
     * @return packet
     */
    public static KcpPacket fromDatagram(final byte[] datagram) {
        Assert.notNull(datagram, () -> new ValidateException("KCP datagram must not be null"));
        if (datagram.length > maxDatagramBytes()) {
            throw new ProtocolException("KCP datagram exceeds maximum UDP payload");
        }
        if (datagram.length > Normal._0 && (datagram[Normal._0] == Normal._1 || datagram[Normal._0] == Normal._2)) {
            final int version = Byte.toUnsignedInt(datagram[Normal._0]);
            final int headerBytes = version == Normal._1 ? Builder.KCP_PACKET_V1_HEADER_BYTES
                    : Builder.KCP_PACKET_V2_HEADER_BYTES;
            if (datagram.length < headerBytes) {
                throw new ProtocolException("KCP versioned datagram is too short");
            }
            final Buffer buffer = new Buffer().write(datagram);
            buffer.readByte();
            final Type type = Type.fromWire(buffer.readByte());
            final long sequence = Integer.toUnsignedLong(buffer.readInt());
            final long acknowledgement = Integer.toUnsignedLong(buffer.readInt());
            final int window = Short.toUnsignedInt(buffer.readShort());
            final long timestamp = buffer.readLong();
            final long messageId = version == Normal._2 ? Integer.toUnsignedLong(buffer.readInt()) : Normal._0;
            final int fragmentIndex = version == Normal._2 ? Short.toUnsignedInt(buffer.readShort()) : Normal._0;
            final int fragmentCount = version == Normal._2 ? Short.toUnsignedInt(buffer.readShort()) : Normal._0;
            final ByteString payload = buffer.readByteString();
            return decoded(
                    version,
                    type,
                    sequence,
                    acknowledgement,
                    window,
                    timestamp,
                    messageId,
                    fragmentIndex,
                    fragmentCount,
                    payload);
        }
        if (datagram.length >= Builder.KCP_PACKET_LEGACY_HEADER_BYTES) {
            final Buffer buffer = new Buffer().write(datagram);
            final long sequence = buffer.readLong();
            final ByteString payload = buffer.readByteString();
            return decoded(
                    Normal._0,
                    Type.DATA,
                    sequence,
                    Normal._0,
                    Normal._32,
                    Normal._0,
                    Normal._0,
                    Normal._0,
                    Normal._0,
                    payload);
        }
        throw new ProtocolException("KCP datagram is too short");
    }

    /**
     * Returns the KCP wire header size.
     *
     * @return header bytes
     */
    public static int headerBytes() {
        return Builder.KCP_PACKET_HEADER_BYTES;
    }

    /**
     * Returns the maximum datagram payload accepted by UDP.
     *
     * @return max datagram bytes
     */
    public static int maxDatagramBytes() {
        return Builder.KCP_PACKET_V1_HEADER_BYTES + Builder.KCP_PACKET_V1_MAX_PAYLOAD;
    }

    /**
     * Returns the maximum KCP data payload that fits in one datagram.
     *
     * @return max payload bytes
     */
    public static int maxPayloadBytes() {
        return Builder.KCP_PACKET_MAX_PAYLOAD;
    }

    /**
     * Encodes this packet to datagram bytes.
     *
     * @return datagram bytes
     */
    public byte[] datagram() {
        if (version == Normal._0) {
            throw new ProtocolException("Legacy KCP packets are decode-only");
        }
        final Buffer buffer = new Buffer();
        buffer.writeByte(version);
        buffer.writeByte(type.wire);
        buffer.writeInt((int) sequence);
        buffer.writeInt((int) acknowledgement);
        buffer.writeShort(window);
        buffer.writeLong(timestamp);
        if (version == Normal._2) {
            buffer.writeInt((int) messageId);
            buffer.writeShort(fragmentIndex);
            buffer.writeShort(fragmentCount);
        }
        buffer.write(payloadBytes);
        return buffer.readByteArray();
    }

    /**
     * Builds a decoded packet and maps validation failures to wire protocol failures.
     *
     * @param version         wire version
     * @param type            packet type
     * @param sequence        sequence
     * @param acknowledgement acknowledgement
     * @param window          advertised window
     * @param timestamp       timestamp
     * @param messageId       message identifier
     * @param fragmentIndex   fragment index
     * @param fragmentCount   fragment count
     * @param payload         payload bytes
     * @return decoded packet
     */
    private static KcpPacket decoded(
            final int version,
            final Type type,
            final long sequence,
            final long acknowledgement,
            final int window,
            final long timestamp,
            final long messageId,
            final int fragmentIndex,
            final int fragmentCount,
            final ByteString payload) {
        try {
            return new KcpPacket(version, type, sequence, acknowledgement, window, timestamp, messageId, fragmentIndex,
                    fragmentCount, payload);
        } catch (final ValidateException e) {
            throw new ProtocolException("Invalid KCP datagram", e);
        }
    }

    /**
     * Validates sequence and acknowledgement numbers before they are serialized into 32-bit wire fields.
     *
     * @param value sequence value
     * @param name  field name used in validation messages
     */
    private static void validateSequence(final long value, final String name) {
        Assert.checkBetween(
                value,
                Normal._0,
                Builder.UNSIGNED_INT_MASK,
                () -> new ValidateException(name + " out of range"));
    }

    /**
     * Validates an unsigned 16-bit field.
     *
     * @param value field value
     * @param name  field name
     */
    private static void validateUnsignedShort(final int value, final String name) {
        Assert.checkBetween(value, Normal._0, Normal._65535, () -> new ValidateException(name + " out of range"));
    }

    /**
     * Returns the maximum payload for a wire version.
     *
     * @param version wire version
     * @return maximum payload bytes
     */
    private static int maxPayloadBytes(final int version) {
        return switch (version) {
            case Normal._0 -> maxDatagramBytes() - Builder.KCP_PACKET_LEGACY_HEADER_BYTES;
            case Normal._1 -> Builder.KCP_PACKET_V1_MAX_PAYLOAD;
            case Normal._2 -> Builder.KCP_PACKET_V2_MAX_PAYLOAD;
            default -> throw new ValidateException("Unsupported KCP version");
        };
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
