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
package org.miaixz.bus.core.io.compress;

import org.miaixz.bus.core.lang.exception.ValidateException;

import java.util.zip.ZipEntry;

/**
 * Zip安全相关类，如检查Zip bomb漏洞等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipSecurity {

    /**
     * 检查Zip bomb漏洞
     *
     * @param entry       {@link ZipEntry}
     * @param maxSizeDiff 检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
     * @return 检查后的{@link ZipEntry}
     */
    public static ZipEntry checkZipBomb(final ZipEntry entry, final int maxSizeDiff) {
        if (null == entry) {
            return null;
        }
        if (maxSizeDiff < 0 || entry.isDirectory()) {
            // 目录不检查
            return entry;
        }

        final long compressedSize = entry.getCompressedSize();
        final long uncompressedSize = entry.getSize();
        // Console.logger(entry.getName(), compressedSize, uncompressedSize);
        if (compressedSize < 0 || uncompressedSize < 0 ||
        // 默认压缩比例是100倍，一旦发现压缩率超过这个阈值，被认为是Zip bomb
                compressedSize * maxSizeDiff < uncompressedSize) {
            throw new ValidateException(
                    "Zip bomb attack detected, invalid sizes: compressed {}, uncompressed {}, name {}", compressedSize,
                    uncompressedSize, entry.getName());
        }
        return entry;
    }

}
