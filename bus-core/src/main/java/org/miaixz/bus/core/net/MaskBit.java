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
package org.miaixz.bus.core.net;

import java.util.HashMap;

import org.miaixz.bus.core.center.map.BiMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.ip.IPv4;

/**
 * A map that provides a bidirectional mapping between mask bits and subnet masks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MaskBit {

    /**
     * A bidirectional map between mask bits and their corresponding dotted-decimal subnet masks.
     */
    private static final BiMap<Integer, String> MASK_BIT_MAP;

    static {
        MASK_BIT_MAP = new BiMap<>(new HashMap<>(32));
        MASK_BIT_MAP.put(1, "128.0.0.0");
        MASK_BIT_MAP.put(2, "192.0.0.0");
        MASK_BIT_MAP.put(3, "224.0.0.0");
        MASK_BIT_MAP.put(4, "240.0.0.0");
        MASK_BIT_MAP.put(5, "248.0.0.0");
        MASK_BIT_MAP.put(6, "252.0.0.0");
        MASK_BIT_MAP.put(7, "254.0.0.0");
        MASK_BIT_MAP.put(8, "255.0.0.0");
        MASK_BIT_MAP.put(9, "255.128.0.0");
        MASK_BIT_MAP.put(10, "255.192.0.0");
        MASK_BIT_MAP.put(11, "255.224.0.0");
        MASK_BIT_MAP.put(12, "255.240.0.0");
        MASK_BIT_MAP.put(13, "255.248.0.0");
        MASK_BIT_MAP.put(14, "255.252.0.0");
        MASK_BIT_MAP.put(15, "255.254.0.0");
        MASK_BIT_MAP.put(16, "255.255.0.0");
        MASK_BIT_MAP.put(17, "255.255.128.0");
        MASK_BIT_MAP.put(18, "255.255.192.0");
        MASK_BIT_MAP.put(19, "255.255.224.0");
        MASK_BIT_MAP.put(20, "255.255.240.0");
        MASK_BIT_MAP.put(21, "255.255.248.0");
        MASK_BIT_MAP.put(22, "255.255.252.0");
        MASK_BIT_MAP.put(23, "255.255.254.0");
        MASK_BIT_MAP.put(24, "255.255.255.0");
        MASK_BIT_MAP.put(25, "255.255.255.128");
        MASK_BIT_MAP.put(26, "255.255.255.192");
        MASK_BIT_MAP.put(27, "255.255.255.224");
        MASK_BIT_MAP.put(28, "255.255.255.240");
        MASK_BIT_MAP.put(29, "255.255.255.248");
        MASK_BIT_MAP.put(30, "255.255.255.252");
        MASK_BIT_MAP.put(31, "255.255.255.254");
        MASK_BIT_MAP.put(32, "255.255.255.255");
    }

    /**
     * Gets the dotted-decimal subnet mask for a given number of mask bits.
     *
     * @param maskBit The number of mask bits (e.g., 24).
     * @return The corresponding subnet mask (e.g., "255.255.255.0").
     */
    public static String get(final int maskBit) {
        return MASK_BIT_MAP.get(maskBit);
    }

    /**
     * Gets the number of mask bits for a given dotted-decimal subnet mask.
     *
     * @param mask The dotted-decimal subnet mask (e.g., "255.255.255.0").
     * @return The corresponding number of mask bits (e.g., 24), or {@code null} if the mask is invalid.
     */
    public static Integer getMaskBit(final String mask) {
        return MASK_BIT_MAP.getKey(mask);
    }

    /**
     * Gets the subnet mask as a long integer from the number of mask bits.
     *
     * @param maskBit The number of mask bits.
     * @return The subnet mask as a long integer.
     * @throws IllegalArgumentException if the mask bit is invalid.
     */
    public static long getMaskIpLong(final int maskBit) {
        Assert.isTrue(MASK_BIT_MAP.containsKey(maskBit), "Invalid mask bit: {}", maskBit);
        return (Protocol.IPV4_NUM_MAX << (IPv4.IPV4_MASK_BIT_MAX - maskBit)) & Protocol.IPV4_NUM_MAX;
    }

}
