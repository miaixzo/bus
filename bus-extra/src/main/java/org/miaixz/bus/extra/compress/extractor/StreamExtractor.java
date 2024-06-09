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
package org.miaixz.bus.extra.compress.extractor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Predicate;

/**
 * 数据解压器，即将归档打包的数据释放
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamExtractor implements Extractor {

    private final ArchiveInputStream<?> in;

    /**
     * 构造
     *
     * @param charset 编码
     * @param file    包文件
     */
    public StreamExtractor(final Charset charset, final File file) {
        this(charset, null, file);
    }

    /**
     * 构造
     *
     * @param charset      编码
     * @param archiverName 归档包格式，null表示自动检测
     * @param file         包文件
     */
    public StreamExtractor(final Charset charset, final String archiverName, final File file) {
        this(charset, archiverName, FileKit.getInputStream(file));
    }

    /**
     * 构造
     *
     * @param charset 编码
     * @param in      包流
     */
    public StreamExtractor(final Charset charset, final InputStream in) {
        this(charset, null, in);
    }

    /**
     * 构造
     *
     * @param charset      编码
     * @param archiverName 归档包格式，null表示自动检测
     * @param in           包流
     */
    public StreamExtractor(final Charset charset, final String archiverName, InputStream in) {
        if (in instanceof ArchiveInputStream) {
            this.in = (ArchiveInputStream<?>) in;
            return;
        }

        final ArchiveStreamFactory factory = new ArchiveStreamFactory(charset.name());
        try {
            in = IoKit.toBuffered(in);
            if (StringKit.isBlank(archiverName)) {
                this.in = factory.createArchiveInputStream(in);
            } else if ("tgz".equalsIgnoreCase(archiverName) || "tar.gz".equalsIgnoreCase(archiverName)) {
                // 支持tgz格式解压
                try {
                    this.in = new TarArchiveInputStream(new GzipCompressorInputStream(in));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            } else {
                this.in = factory.createArchiveInputStream(archiverName, in);
            }
        } catch (final ArchiveException e) {
            // 如果报错可能持有文件句柄，导致无法删除文件
            IoKit.closeQuietly(in);
            throw new InternalException(e);
        }
    }

    @Override
    public InputStream getFirst(final Predicate<ArchiveEntry> predicate) {
        final ArchiveInputStream<?> in = this.in;
        ArchiveEntry entry;
        try {
            while (null != (entry = in.getNextEntry())) {
                if (null != predicate && !predicate.test(entry)) {
                    continue;
                }
                if (entry.isDirectory() || !in.canReadEntryData(entry)) {
                    // 目录或无法读取的文件直接跳过
                    continue;
                }

                return in;
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return null;
    }

    /**
     * 释放（解压）到指定目录，结束后自动关闭流，此方法只能调用一次
     *
     * @param targetDir 目标目录
     * @param predicate 解压文件过滤器，用于指定需要释放的文件，null表示不过滤。当{@link Predicate#test(Object)}为{@code true}时释放。
     */
    @Override
    public void extract(final File targetDir, final Predicate<ArchiveEntry> predicate) {
        try {
            extractInternal(targetDir, predicate);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            close();
        }
    }

    /**
     * 释放（解压）到指定目录
     *
     * @param targetDir 目标目录
     * @param predicate 解压文件过滤器，用于指定需要释放的文件，null表示不过滤。当{@link Predicate#test(Object)}为{@code true}释放。
     * @throws IOException IO异常
     */
    private void extractInternal(final File targetDir, final Predicate<ArchiveEntry> predicate) throws IOException {
        Assert.isTrue(null != targetDir && ((!targetDir.exists()) || targetDir.isDirectory()), "target must be dir.");
        final ArchiveInputStream<?> in = this.in;
        ArchiveEntry entry;
        File outItemFile;
        while (null != (entry = in.getNextEntry())) {
            if (null != predicate && !predicate.test(entry)) {
                continue;
            }
            if (!in.canReadEntryData(entry)) {
                // 无法读取的文件直接跳过
                continue;
            }
            outItemFile = FileKit.file(targetDir, entry.getName());
            if (entry.isDirectory()) {
                // 创建对应目录
                //noinspection ResultOfMethodCallIgnored
                outItemFile.mkdirs();
            } else {
                FileKit.copy(in, outItemFile);
            }
        }
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.in);
    }
}
