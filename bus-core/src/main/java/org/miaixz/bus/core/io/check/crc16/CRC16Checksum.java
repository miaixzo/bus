/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.check.crc16;

import java.io.Serial;
import java.io.Serializable;
import java.util.zip.Checksum;

import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * CRC16 Checksum，用于提供多种CRC16算法的通用实现 通过继承此类，重写update和reset完成相应算法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CRC16Checksum implements Checksum, Serializable {

    @Serial
    private static final long serialVersionUID = 2852285067791L;

    /**
     * CRC16 Checksum 结果值
     */
    protected int wCRCin;

    /**
     * 构造
     */
    public CRC16Checksum() {
        reset();
    }

    @Override
    public long getValue() {
        return wCRCin;
    }

    /**
     * 获取16进制的CRC16值
     *
     * @return 16进制的CRC16值
     */
    public String getHexValue() {
        return getHexValue(false);
    }

    /**
     * 获取16进制的CRC16值
     *
     * @param isPadding 不足4位时，是否填充0以满足位数
     * @return 16进制的CRC16值，4位
     */
    public String getHexValue(final boolean isPadding) {
        String hex = HexKit.toHex(getValue());
        if (isPadding) {
            hex = StringKit.padPre(hex, 4, '0');
        }

        return hex;
    }

    @Override
    public void reset() {
        wCRCin = 0x0000;
    }

    /**
     * 计算全部字节
     *
     * @param b 字节
     */
    public void update(final byte[] b) {
        update(b, 0, b.length);
    }

    @Override
    public void update(final byte[] b, final int off, final int len) {
        for (int i = off; i < off + len; i++)
            update(b[i]);
    }

}
