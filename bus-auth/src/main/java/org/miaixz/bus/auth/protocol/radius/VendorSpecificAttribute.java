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
 * Represents the RFC 2865 Vendor-Id and opaque String inside a Type 26 Vendor-Specific Attribute.
 * <p>
 * The vendor String is intentionally not forced into a vendor-type/vendor-length/value layout because RFC 2865 leaves
 * its internal format to the identified vendor.
 * </p>
 *
 * @param vendorId 24-bit SMI Network Management Private Enterprise Code with an implicit zero high-order octet
 * @param value    opaque vendor-defined String octets following Vendor-Id
 * @author Kimi Liu
 */
public record VendorSpecificAttribute(long vendorId, byte[] value) {

    /**
     * Largest 24-bit Vendor-Id permitted when the high-order wire octet is zero.
     */
    private static final long MAXIMUM_VENDOR_ID = 0x00ff_ffffL;

    /**
     * Maximum vendor String length after the four-octet Vendor-Id.
     */
    private static final int MAXIMUM_VALUE_BYTES = Radius.MAXIMUM_ATTRIBUTE_VALUE_BYTES - Normal._4;

    /**
     * Validates the unsigned Vendor-Id and detaches the opaque value.
     *
     * @param vendorId 24-bit Vendor-Id from 0 through 16777215
     * @param value    one through 249 vendor-defined octets
     * @throws IllegalArgumentException if either component violates its Type 26 wire range
     */
    public VendorSpecificAttribute {
        Assert.isTrue(
                vendorId >= 0 && vendorId <= MAXIMUM_VENDOR_ID,
                "RADIUS Vendor-Id must fit 24 bits with a zero high-order octet");
        Assert.notNull(value, "RADIUS vendor-specific value must not be null");
        Assert.isTrue(
                value.length >= 1 && value.length <= MAXIMUM_VALUE_BYTES,
                "RADIUS vendor-specific value must contain between 1 and 249 octets");
        value = value.clone();
    }

    /**
     * Returns a detached copy of the opaque vendor String.
     *
     * @return copied vendor-defined octets
     */
    @Override
    public byte[] value() {
        return value.clone();
    }

    /**
     * Compares Vendor-Id and opaque vendor String contents.
     *
     * @param other candidate object
     * @return {@code true} when both Type 26 payloads match
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof VendorSpecificAttribute attribute && vendorId == attribute.vendorId
                && Arrays.equals(value, attribute.value);
    }

    /**
     * Computes a content-based Type 26 payload hash.
     *
     * @return payload content hash
     */
    @Override
    public int hashCode() {
        return 31 * Long.hashCode(vendorId) + Arrays.hashCode(value);
    }

    /**
     * Returns structural diagnostics without rendering vendor-defined octets.
     *
     * @return safe Vendor-Id and value length text
     */
    @Override
    public String toString() {
        return "VendorSpecificAttribute[vendorId=" + vendorId + ", valueLength=" + value.length + Symbol.BRACKET_RIGHT;
    }

}
