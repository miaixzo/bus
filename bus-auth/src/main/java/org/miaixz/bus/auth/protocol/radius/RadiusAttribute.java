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
import org.miaixz.bus.core.lang.Symbol;

/**
 * Preserves one raw RADIUS Type-Length-Value attribute without guessing its registered data type.
 * <p>
 * The model retains unknown Type values and original octets so protocol extensions and vendor-defined content survive
 * decode and encode unchanged. Typed interpretation belongs to the operation handler selected by the deployment.
 * </p>
 *
 * @param type  unsigned one-octet RADIUS Attribute Type value object
 * @param value raw Attribute Value octets excluding Type and Length
 * @author Kimi Liu
 */
public record RadiusAttribute(Type type, byte[] value) {

    /**
     * Validates and detaches one raw Attribute value.
     *
     * @param type  unsigned Type value object
     * @param value zero through 253 raw Value octets
     * @throws IllegalArgumentException if Type or Value violates the RADIUS Attribute wire range
     */
    public RadiusAttribute {
        Assert.notNull(type, "RADIUS Attribute Type must not be null");
        Assert.notNull(value, "RADIUS Attribute value must not be null");
        Assert.isTrue(
                value.length <= Radius.MAXIMUM_ATTRIBUTE_VALUE_BYTES,
                "RADIUS Attribute value must not exceed 253 octets");
        value = value.clone();
    }

    /**
     * Returns a detached copy of the raw Attribute Value.
     *
     * @return copied Value octets
     */
    @Override
    public byte[] value() {
        return value.clone();
    }

    /**
     * Compares the unsigned Type and raw Value contents.
     *
     * @param other candidate object
     * @return {@code true} when both wire values are equal
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof RadiusAttribute attribute && type.equals(attribute.type)
                && Arrays.equals(value, attribute.value);
    }

    /**
     * Computes a content-based hash for the Type and raw Value.
     *
     * @return content hash code
     */
    @Override
    public int hashCode() {
        return 31 * type.hashCode() + Arrays.hashCode(value);
    }

    /**
     * Returns non-sensitive structural diagnostics without rendering Attribute Value octets.
     *
     * @return Type and Value length only
     */
    @Override
    public String toString() {
        return "RadiusAttribute[type=" + type.value() + ", valueLength=" + value.length + Symbol.BRACKET_RIGHT;
    }

    /**
     * Represents one open IANA RADIUS Attribute Type registry value.
     * <p>
     * Values not named by {@link Radius.Attributes} remain valid so extensions and Vendor-Specific content can pass
     * through without coercion or loss.
     * </p>
     *
     * @param value unsigned one-octet Type value
     * @author Kimi Liu
     */
    public record Type(int value) {

        /**
         * Validates one open-registry Attribute Type.
         *
         * @throws IllegalArgumentException if {@code value} is outside 1 through 255
         */
        public Type {
            Assert.isTrue(value >= 1 && value <= 255, "RADIUS Attribute Type must be between 1 and 255");
        }

        /**
         * Reports whether this type is one of the registered values used by the implemented profile.
         *
         * @return {@code true} for a value named by {@link Radius.Attributes}
         */
        public boolean registered() {
            return switch (value) {
                case Radius.Attributes.USER_NAME, Radius.Attributes.USER_PASSWORD, Radius.Attributes.CHAP_PASSWORD, Radius.Attributes.NAS_IP_ADDRESS, Radius.Attributes.REPLY_MESSAGE, Radius.Attributes.STATE, Radius.Attributes.VENDOR_SPECIFIC, Radius.Attributes.NAS_IDENTIFIER, Radius.Attributes.PROXY_STATE, Radius.Attributes.ACCT_STATUS_TYPE, Radius.Attributes.CHAP_CHALLENGE, Radius.Attributes.EAP_MESSAGE, Radius.Attributes.MESSAGE_AUTHENTICATOR, Radius.Attributes.ERROR_CAUSE -> true;
                default -> false;
            };
        }

    }

}
