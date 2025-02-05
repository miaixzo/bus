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
package org.miaixz.bus.core.xyz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import org.miaixz.bus.core.io.stream.EmptyOutputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * 校验码工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CheckKit {

    /**
     * 计算文件CRC32校验码
     *
     * @param file 文件，不能为目录
     * @return CRC32值
     * @throws InternalException IO异常
     */
    public static long checksumCRC32(final File file) throws InternalException {
        return checksum(file, new CRC32()).getValue();
    }

    /**
     * 计算流CRC32校验码，计算后关闭流
     *
     * @param in 文件，不能为目录
     * @return CRC32值
     * @throws InternalException IO异常
     */
    public static long checksumCRC32(final InputStream in) throws InternalException {
        return checksum(in, new CRC32()).getValue();
    }

    /**
     * 计算文件校验码
     *
     * @param file     文件，不能为目录
     * @param checksum {@link Checksum}
     * @return Checksum
     * @throws InternalException IO异常
     */
    public static Checksum checksum(final File file, final Checksum checksum) throws InternalException {
        Assert.notNull(file, "File is null !");
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Checksums can't be computed on directories");
        }
        try {
            return checksum(Files.newInputStream(file.toPath()), checksum);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 计算流的校验码，计算后关闭流
     *
     * @param in       流
     * @param checksum {@link Checksum}
     * @return Checksum
     * @throws InternalException IO异常
     */
    public static Checksum checksum(InputStream in, Checksum checksum) throws InternalException {
        Assert.notNull(in, "InputStream is null !");
        if (null == checksum) {
            checksum = new CRC32();
        }
        try {
            in = new CheckedInputStream(in, checksum);
            IoKit.copy(in, EmptyOutputStream.INSTANCE);
        } finally {
            IoKit.closeQuietly(in);
        }
        return checksum;
    }

    /**
     * 计算流的校验码，计算后关闭流
     *
     * @param in       流
     * @param checksum {@link Checksum}
     * @return Checksum
     * @throws InternalException IO异常
     */
    public static long checksumValue(final InputStream in, final Checksum checksum) {
        return checksum(in, checksum).getValue();
    }

}
