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
package org.miaixz.bus.auth.protocol.radius.packet;

import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ByteKit;

/**
 * Encodes and strictly decodes complete RADIUS packets in network byte order with a 4096-byte ceiling.
 *
 * @author Kimi Liu
 */
public final class RadiusPacketCodec {

    /**
     * Maximum RFC packet bytes.
     */
    public static final int MAXIMUM_PACKET_BYTES = 4096;

    /**
     * Prevents construction.
     */
    private RadiusPacketCodec() {
        // No initialization required.
    }

    /**
     * Encodes one packet.
     *
     * @param packet packet
     * @return wire bytes
     * @throws ValidateException if the packet is null or its encoded length exceeds 4096 bytes
     */
    public static byte[] encode(final RadiusPacket packet) {
        final RadiusPacket source = Assert
                .notNull(packet, () -> new ValidateException("RADIUS packet must not be null"));
        final ByteArrayOutputStream attributes = new ByteArrayOutputStream();
        source.attributes().forEach(attribute -> write(attributes, attribute));
        final int length = RadiusPacket.HEADER_BYTES + attributes.size();
        Assert.isTrue(
                length <= MAXIMUM_PACKET_BYTES,
                () -> new ValidateException("RADIUS packet exceeds its byte limit"));
        final ByteArrayOutputStream result = new ByteArrayOutputStream(length);
        result.write(source.code());
        result.write(source.identifier());
        result.writeBytes(ByteKit.toBytes((short) length, ByteOrder.BIG_ENDIAN));
        result.writeBytes(source.authenticator());
        result.writeBytes(attributes.toByteArray());
        return result.toByteArray();
    }

    /**
     * Decodes one complete packet.
     *
     * @param wire wire bytes
     * @return packet
     * @throws ValidateException if packet or attribute framing and bounds are invalid
     */
    public static RadiusPacket decode(final byte[] wire) {
        final byte[] source = Arrays.copyOf(
                Assert.notNull(wire, () -> new ValidateException("RADIUS wire packet must not be null")),
                wire.length);
        Assert.isTrue(
                source.length >= RadiusPacket.HEADER_BYTES && source.length <= MAXIMUM_PACKET_BYTES,
                () -> new ValidateException("RADIUS packet length is invalid"));
        final int length = Short.toUnsignedInt(ByteKit.toShort(source, Normal._2, ByteOrder.BIG_ENDIAN));
        Assert.isTrue(length == source.length, () -> new ValidateException("RADIUS declared packet length is invalid"));
        final ArrayList<RadiusAttribute> attributes = new ArrayList<>();
        int index = RadiusPacket.HEADER_BYTES;
        while (index < source.length) {
            Assert.isTrue(
                    index + Normal._2 <= source.length,
                    () -> new ValidateException("RADIUS attribute header is truncated"));
            final int type = Byte.toUnsignedInt(source[index]);
            final int size = Byte.toUnsignedInt(source[index + Normal._1]);
            Assert.isTrue(
                    size >= Normal._2 && index + size <= source.length,
                    () -> new ValidateException("RADIUS attribute length is invalid"));
            final byte[] value = Arrays.copyOfRange(source, index + Normal._2, index + size);
            if (type == RadiusAttribute.VENDOR_SPECIFIC) {
                Assert.isTrue(
                        value.length >= Normal._4,
                        () -> new ValidateException("RADIUS vendor attribute is truncated"));
                final long vendor = Integer.toUnsignedLong(
                        java.nio.ByteBuffer.wrap(value, Normal._0, Normal._4).order(ByteOrder.BIG_ENDIAN).getInt());
                attributes.add(RadiusAttribute.vendor(vendor, Arrays.copyOfRange(value, Normal._4, value.length)));
            } else {
                attributes.add(RadiusAttribute.standard(type, value));
            }
            index += size;
        }
        return new RadiusPacket(Byte.toUnsignedInt(source[Normal._0]), Byte.toUnsignedInt(source[Normal._1]),
                Arrays.copyOfRange(source, Normal._4, RadiusPacket.HEADER_BYTES), attributes);
    }

    /**
     * Writes one attribute.
     *
     * @param output    target buffer
     * @param attribute attribute
     * @throws NullPointerException if either argument is {@code null}
     */
    static void write(final ByteArrayOutputStream output, final RadiusAttribute attribute) {
        output.write(attribute.type());
        final byte[] value = attribute.value();
        final int size = Normal._2 + value.length + (attribute.vendorId() == Normal._0 ? Normal._0 : Normal._4);
        output.write(size);
        if (attribute.vendorId() != Normal._0) {
            output.writeBytes(
                    java.nio.ByteBuffer.allocate(Normal._4).order(ByteOrder.BIG_ENDIAN)
                            .putInt((int) attribute.vendorId()).array());
        }
        output.writeBytes(value);
    }

}
