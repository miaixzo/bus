/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.compress;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.exception.ValidateException;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.ZipKit;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Zip文件或流读取器，一般用于Zip文件解压
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipReader implements Closeable {

    private ZipFile zipFile;
    private ZipInputStream in;

    /**
     * 构造
     *
     * @param zipFile 读取的的Zip文件
     * @param charset 编码
     */
    public ZipReader(File zipFile, Charset charset) {
        this.zipFile = ZipKit.zipFile(zipFile, charset);
    }

    /**
     * 构造
     *
     * @param zipFile 读取的的Zip文件
     */
    public ZipReader(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    /**
     * 构造
     *
     * @param in      读取的的Zip文件流
     * @param charset 编码
     */
    public ZipReader(InputStream in, Charset charset) {
        this.in = new ZipInputStream(in, charset);
    }

    /**
     * 构造
     *
     * @param zin 读取的的Zip文件流
     */
    public ZipReader(ZipInputStream zin) {
        this.in = zin;
    }

    /**
     * 创建ZipReader
     *
     * @param zipFile 生成的Zip文件
     * @param charset 编码
     * @return this
     */
    public static ZipReader of(File zipFile, Charset charset) {
        return new ZipReader(zipFile, charset);
    }

    /**
     * 创建ZipReader
     *
     * @param in      Zip输入的流，一般为输入文件流
     * @param charset 编码
     * @return this
     */
    public static ZipReader of(InputStream in, Charset charset) {
        return new ZipReader(in, charset);
    }

    /**
     * 检查Zip bomb漏洞
     *
     * @param entry {@link ZipEntry}
     * @return 检查后的{@link ZipEntry}
     */
    private static ZipEntry checkZipBomb(final ZipEntry entry) {
        if (null == entry) {
            return null;
        }
        final long compressedSize = entry.getCompressedSize();
        final long uncompressedSize = entry.getSize();
        if (compressedSize < 0 || uncompressedSize < 0 ||
                // 默认压缩比例是100倍，一旦发现压缩率超过这个阈值，被认为是Zip bomb
                compressedSize * 100 < uncompressedSize) {
            throw new ValidateException("Zip bomb attack detected, invalid sizes: compressed {}, uncompressed {}, name {}",
                    compressedSize, uncompressedSize, entry.getName());
        }
        return entry;
    }

    /**
     * 获取指定路径的文件流
     * 如果是文件模式，则直接获取Entry对应的流，如果是流模式，则遍历entry后，找到对应流返回
     *
     * @param path 路径
     * @return 文件流
     */
    public InputStream get(String path) {
        if (null != this.zipFile) {
            final ZipFile zipFile = this.zipFile;
            final ZipEntry entry = zipFile.getEntry(path);
            if (null != entry) {
                return ZipKit.get(zipFile, entry);
            }
        } else {
            try {
                this.in.reset();
                ZipEntry zipEntry;
                while (null != (zipEntry = in.getNextEntry())) {
                    if (zipEntry.getName().equals(path)) {
                        return this.in;
                    }
                }
            } catch (IOException e) {
                throw new InternalException(e);
            }
        }

        return null;
    }

    /**
     * 解压到指定目录中
     *
     * @param outFile 解压到的目录
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public File readTo(File outFile) throws InternalException {
        return readTo(outFile, null);
    }

    /**
     * 解压到指定目录中
     *
     * @param outFile     解压到的目录
     * @param entryFilter 过滤器，排除不需要的文件
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public File readTo(File outFile, Predicate<ZipEntry> entryFilter) throws InternalException {
        read((zipEntry) -> {
            if (null == entryFilter || entryFilter.test(zipEntry)) {
                String path = zipEntry.getName();
                if (FileKit.isWindows()) {
                    path = StringKit.replace(path, "*", "_");
                }
                final File outItemFile = FileKit.file(outFile, path);
                if (zipEntry.isDirectory()) {
                    outItemFile.mkdirs();
                } else {
                    InputStream in;
                    if (null != this.zipFile) {
                        in = ZipKit.get(this.zipFile, zipEntry);
                    } else {
                        in = this.in;
                    }
                    // 文件
                    FileKit.writeFromStream(in, outItemFile, false);
                }
            }
        });
        return outFile;
    }

    /**
     * 读取并处理Zip文件中的每一个{@link ZipEntry}
     *
     * @param consumer {@link ZipEntry}处理器
     * @return this
     * @throws InternalException IO异常
     */
    public ZipReader read(Consumer<ZipEntry> consumer) throws InternalException {
        if (null != this.zipFile) {
            readFromZipFile(consumer);
        } else {
            readFromStream(consumer);
        }
        return this;
    }

    @Override
    public void close() throws InternalException {
        if (null != this.zipFile) {
            IoKit.close(this.zipFile);
        } else {
            IoKit.close(this.in);
        }
    }

    /**
     * 读取并处理Zip文件中的每一个{@link ZipEntry}
     *
     * @param consumer {@link ZipEntry}处理器
     */
    private void readFromZipFile(Consumer<ZipEntry> consumer) {
        final Enumeration<? extends ZipEntry> em = zipFile.entries();
        while (em.hasMoreElements()) {
            consumer.accept(checkZipBomb(em.nextElement()));
        }
    }

    /**
     * 读取并处理Zip流中的每一个{@link ZipEntry}
     *
     * @param consumer {@link ZipEntry}处理器
     * @throws InternalException IO异常
     */
    private void readFromStream(Consumer<ZipEntry> consumer) throws InternalException {
        try {
            ZipEntry zipEntry;
            while (null != (zipEntry = checkZipBomb(in.getNextEntry()))) {
                consumer.accept(zipEntry);
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

}
