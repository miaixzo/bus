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
package org.miaixz.bus.auth.protocol.radius;

import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Preserves one complete EAP packet carried by one or more RFC 3579 EAP-Message Attributes.
 * <p>
 * This value validates only the common EAP Code, Identifier, Length, and Type-Data envelope. It deliberately leaves EAP
 * method interpretation to the deployment's RADIUS request handler.
 * </p>
 *
 * @param value complete EAP packet octets
 * @author Kimi Liu
 */
public record EapMessage(byte[] value) {

    /**
     * Minimum common EAP packet header size.
     */
    private static final int HEADER_BYTES = Normal._4;

    /**
     * Validates the common EAP Length and detaches the packet.
     *
     * @param value complete EAP packet containing at least Code, Identifier, and Length
     * @throws IllegalArgumentException if the packet is null, truncated, or its declared Length does not match
     */
    public EapMessage {
        Assert.notNull(value, "EAP packet must not be null");
        Assert.isTrue(value.length >= HEADER_BYTES, "EAP packet must contain at least four octets");
        final int declaredLength = (Byte.toUnsignedInt(value[2]) << 8) | Byte.toUnsignedInt(value[3]);
        Assert.isTrue(declaredLength >= HEADER_BYTES, "EAP packet Length must be at least four octets");
        Assert.isTrue(declaredLength == value.length, "EAP packet Length must equal the complete packet size");
        value = value.clone();
    }

    /**
     * Returns a detached copy of the complete EAP packet.
     *
     * @return copied EAP packet octets
     */
    @Override
    public byte[] value() {
        return value.clone();
    }

    /**
     * Compares complete EAP packet contents.
     *
     * @param other candidate object
     * @return {@code true} when both complete packets match
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof EapMessage message && Arrays.equals(value, message.value);
    }

    /**
     * Computes a content-based EAP packet hash.
     *
     * @return packet content hash
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    /**
     * Returns structural diagnostics without rendering EAP payload octets.
     *
     * @return safe complete-packet length text
     */
    @Override
    public String toString() {
        return "EapMessage[valueLength=" + value.length + Symbol.BRACKET_RIGHT;
    }

}
