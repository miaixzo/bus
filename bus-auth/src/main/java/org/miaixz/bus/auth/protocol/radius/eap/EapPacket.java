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
package org.miaixz.bus.auth.protocol.radius.eap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.miaixz.bus.auth.protocol.radius.packet.RadiusAttribute;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable EAP Identity or EAP-TLS packet.
 *
 * @param code       EAP code from one through four
 * @param identifier unsigned identifier
 * @param type       zero for Success/Failure, one for Identity, or thirteen for EAP-TLS
 * @param data       type data
 * @author Kimi Liu
 */
public record EapPacket(int code, int identifier, int type, byte[] data) {

    /**
     * Identity type.
     */
    public static final int IDENTITY = Normal._1;

    /**
     * EAP-TLS type.
     */
    public static final int TLS = 13;

    /**
     * Standard RADIUS EAP-Message attribute type.
     */
    public static final int RADIUS_ATTRIBUTE = 79;

    /**
     * Validates and snapshots a packet.
     *
     * @param code       code
     * @param identifier identifier
     * @param type       type
     * @param data       data
     * @throws ValidateException if code, identifier, type, shape, data, or encoded length is invalid
     */
    public EapPacket {
        Assert.isTrue(
                code >= Normal._1 && code <= Normal._4 && identifier >= Normal._0 && identifier <= 255,
                () -> new ValidateException("EAP code or identifier is invalid"));
        Assert.isTrue(
                type == Normal._0 || type == IDENTITY || type == TLS,
                () -> new ValidateException("EAP type is unsupported"));
        Assert.isTrue(
                (code <= Normal._2) == (type != Normal._0),
                () -> new ValidateException("EAP code and type shape is invalid"));
        data = Arrays
                .copyOf(Assert.notNull(data, () -> new ValidateException("EAP data must not be null")), data.length);
        Assert.isTrue(
                data.length + (type == Normal._0 ? Normal._4 : 5) <= 0xFFFF,
                () -> new ValidateException("EAP packet exceeds its length limit"));
        Assert.isTrue(
                type != Normal._0 || data.length == Normal._0,
                () -> new ValidateException("EAP Success and Failure packets must not contain data"));
    }

    /**
     * Decodes one complete EAP packet.
     *
     * @param wire wire bytes
     * @return packet
     * @throws ValidateException if the packet is truncated or its declared length and shape are invalid
     */
    public static EapPacket decode(final byte[] wire) {
        final byte[] source = Arrays.copyOf(
                Assert.notNull(wire, () -> new ValidateException("EAP wire bytes must not be null")),
                wire.length);
        Assert.isTrue(source.length >= Normal._4, () -> new ValidateException("EAP packet is truncated"));
        final ByteBuffer input = ByteBuffer.wrap(source).order(ByteOrder.BIG_ENDIAN);
        final int code = Byte.toUnsignedInt(input.get());
        final int identifier = Byte.toUnsignedInt(input.get());
        final int length = Short.toUnsignedInt(input.getShort());
        Assert.isTrue(length == source.length, () -> new ValidateException("EAP declared length is invalid"));
        Assert.isTrue(
                code > Normal._2 || input.hasRemaining(),
                () -> new ValidateException("EAP request or response type is truncated"));
        final int type = code <= Normal._2 ? Byte.toUnsignedInt(input.get()) : Normal._0;
        final byte[] data = new byte[input.remaining()];
        input.get(data);
        return new EapPacket(code, identifier, type, data);
    }

    /**
     * Decodes one complete standard RADIUS EAP-Message attribute.
     *
     * @param attribute non-null standard type-79 RADIUS attribute
     * @return immutable decoded EAP packet
     * @throws ValidateException if the attribute is null, vendor-specific, has another type, or contains invalid EAP
     */
    public static EapPacket fromRadiusAttribute(final RadiusAttribute attribute) {
        final RadiusAttribute value = Assert
                .notNull(attribute, () -> new ValidateException("RADIUS EAP-Message attribute must not be null"));
        Assert.isTrue(
                value.type() == RADIUS_ATTRIBUTE && value.vendorId() == Normal._0,
                () -> new ValidateException("RADIUS EAP-Message attribute shape is invalid"));
        return decode(value.value());
    }

    /**
     * Returns independent data.
     *
     * @return independent data copy
     */
    @Override
    public byte[] data() {
        return data.clone();
    }

    /**
     * Encodes the packet.
     *
     * @return wire bytes
     */
    public byte[] encode() {
        final int length = data.length + (type == Normal._0 ? Normal._4 : 5);
        final ByteBuffer result = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
        result.put((byte) code).put((byte) identifier).putShort((short) length);
        if (type != Normal._0) {
            result.put((byte) type);
        }
        return result.put(data).array();
    }

    /**
     * Encodes this packet as one standard RADIUS EAP-Message attribute.
     *
     * @return immutable type-79 RADIUS attribute containing the complete EAP packet
     * @throws ValidateException if the encoded EAP packet exceeds one RADIUS attribute value
     */
    public RadiusAttribute toRadiusAttribute() {
        return RadiusAttribute.standard(RADIUS_ATTRIBUTE, encode());
    }

}
