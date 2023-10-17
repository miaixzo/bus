/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.net;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.map.DuplexingMap;

import java.util.HashMap;

/**
 * 掩码位和掩码之间的Map对应
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MaskBit {

    /**
     * 掩码位与掩码的点分十进制的双向对应关系
     */
    private static final DuplexingMap<Integer, String> MASK_BIT_MAP;

    static {
        MASK_BIT_MAP = new DuplexingMap<>(new HashMap<>(32));
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
     * 根据掩码位获取掩码
     *
     * @param maskBit 掩码位
     * @return 掩码
     */
    public static String get(final int maskBit) {
        return MASK_BIT_MAP.get(maskBit);
    }

    /**
     * 根据掩码获取掩码位
     *
     * @param mask 掩码的点分十进制表示，如 255.255.255.0
     * @return 掩码位，如 24；如果掩码不合法，则返回null
     */
    public static Integer getMaskBit(final String mask) {
        return MASK_BIT_MAP.getKey(mask);
    }

    /**
     * 根据掩码位获取掩码IP(Long型)
     *
     * @param maskBit 掩码位
     * @return 掩码IP(Long型)
     */
    public static long getMaskIpLong(final int maskBit) {
        Assert.isTrue(MASK_BIT_MAP.containsKey(maskBit), "非法的掩码位数：{}", maskBit);
        return (0xffffffffL << (32 - maskBit)) & 0xffffffffL;
    }

}
