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
package org.miaixz.bus.auth.metric.radius.packet;

import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable standard, unknown, or Vendor-Specific RADIUS attribute. Type 26 optionally exposes the four-byte vendor
 * identifier while retaining the vendor payload without interpretation.
 *
 * @param type     unsigned attribute type
 * @param vendorId zero for standard attributes or an unsigned vendor identifier
 * @param value    raw standard value or vendor payload
 */
public record RadiusAttribute(int type, long vendorId, byte[] value) {

    /**
     * Vendor-Specific attribute type.
     */
    public static final int VENDOR_SPECIFIC = 26;

    /**
     * Maximum standard value bytes.
     */
    public static final int MAXIMUM_VALUE_BYTES = 253;

    /**
     * Validates and snapshots one attribute.
     *
     * @param type     type
     * @param vendorId vendor identifier
     * @param value    value
     */
    public RadiusAttribute {
        Assert.isTrue(type > Normal._0 && type <= 255, () -> new ValidateException("RADIUS attribute type is invalid"));
        Assert.isTrue(
                vendorId >= Normal._0 && vendorId <= 0xFFFF_FFFFL,
                () -> new ValidateException("RADIUS vendor identifier is invalid"));
        value = Arrays.copyOf(
                Assert.notNull(value, () -> new ValidateException("RADIUS attribute value must not be null")),
                value.length);
        final int maximum = vendorId == Normal._0 ? MAXIMUM_VALUE_BYTES : MAXIMUM_VALUE_BYTES - Normal._4;
        Assert.isTrue(
                value.length <= maximum && (vendorId == Normal._0 || type == VENDOR_SPECIFIC),
                () -> new ValidateException("RADIUS attribute length or vendor shape is invalid"));
    }

    /**
     * Creates one standard or unknown attribute.
     *
     * @param type  type
     * @param value raw value
     * @return attribute
     */
    public static RadiusAttribute standard(final int type, final byte[] value) {
        return new RadiusAttribute(type, Normal._0, value);
    }

    /**
     * Creates one Vendor-Specific attribute.
     *
     * @param vendorId vendor identifier
     * @param payload  raw vendor payload
     * @return attribute
     */
    public static RadiusAttribute vendor(final long vendorId, final byte[] payload) {
        Assert.isTrue(vendorId > Normal._0, () -> new ValidateException("RADIUS vendor identifier must be positive"));
        return new RadiusAttribute(VENDOR_SPECIFIC, vendorId, payload);
    }

    /**
     * Returns an independent raw value copy.
     *
     * @return independent raw value copy
     */
    @Override
    public byte[] value() {
        return value.clone();
    }

}
