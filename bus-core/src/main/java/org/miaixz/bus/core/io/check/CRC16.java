/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.check;

import org.miaixz.bus.core.io.check.crc16.CRC16Checksum;
import org.miaixz.bus.core.io.check.crc16.CRC16IBM;

import java.io.Serializable;
import java.util.zip.Checksum;

/**
 * CRC16 循环冗余校验码（Cyclic Redundancy Check）实现，默认IBM算法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CRC16 implements Checksum, Serializable {

    private static final long serialVersionUID = -1L;

    private final CRC16Checksum crc16;

    /**
     * 构造
     */
    public CRC16() {
        this(new CRC16IBM());
    }

    /**
     * 构造
     *
     * @param crc16Checksum {@link CRC16Checksum} 实现
     */
    public CRC16(final CRC16Checksum crc16Checksum) {
        this.crc16 = crc16Checksum;
    }

    /**
     * 获取16进制的CRC16值
     *
     * @return 16进制的CRC16值
     */
    public String getHexValue() {
        return this.crc16.getHexValue();
    }

    /**
     * 获取16进制的CRC16值
     *
     * @param isPadding 不足4位时，是否填充0以满足位数
     * @return 16进制的CRC16值，4位
     */
    public String getHexValue(final boolean isPadding) {
        return crc16.getHexValue(isPadding);
    }

    @Override
    public long getValue() {
        return crc16.getValue();
    }

    @Override
    public void reset() {
        crc16.reset();
    }

    @Override
    public void update(final byte[] b, final int off, final int len) {
        crc16.update(b, off, len);
    }

    @Override
    public void update(final int b) {
        crc16.update(b);
    }

}
