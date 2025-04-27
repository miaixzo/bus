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
package org.miaixz.bus.extra.compress.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.RandomAccess;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * 7z格式数据解压器，即将归档打包的数据释放
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SevenZExtractor implements Extractor, RandomAccess {

    private final SevenZFile sevenZFile;

    /**
     * 构造
     *
     * @param file 包文件
     */
    public SevenZExtractor(final File file) {
        this(file, null);
    }

    /**
     * 构造
     *
     * @param file     包文件
     * @param password 密码，null表示无密码
     */
    public SevenZExtractor(final File file, final char[] password) {
        try {
            this.sevenZFile = SevenZFile.builder().setFile(file).setPassword(password).get();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 构造
     *
     * @param in 包流
     */
    public SevenZExtractor(final InputStream in) {
        this(in, null);
    }

    /**
     * 构造
     *
     * @param in       包流
     * @param password 密码，null表示无密码
     */
    public SevenZExtractor(final InputStream in, final char[] password) {
        this(new SeekableInMemoryByteChannel(IoKit.readBytes(in)), password);
    }

    /**
     * 构造
     *
     * @param channel {@link SeekableByteChannel}
     */
    public SevenZExtractor(final SeekableByteChannel channel) {
        this(channel, null);
    }

    /**
     * 构造
     *
     * @param channel  {@link SeekableByteChannel}
     * @param password 密码，null表示无密码
     */
    public SevenZExtractor(final SeekableByteChannel channel, final char[] password) {
        try {
            this.sevenZFile = SevenZFile.builder().setSeekableByteChannel(channel).setPassword(password).get();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

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

    @Override
    public InputStream getFirst(final Predicate<ArchiveEntry> predicate) {
        final SevenZFile sevenZFile = this.sevenZFile;
        for (final SevenZArchiveEntry entry : sevenZFile.getEntries()) {
            if (null != predicate && !predicate.test(entry)) {
                continue;
            }
            if (entry.isDirectory()) {
                continue;
            }

            try {
                // 此处使用查找entry对应Stream方式，由于只调用一次，也只遍历一次
                return sevenZFile.getInputStream(entry);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }

        return null;
    }

    /**
     * 释放（解压）到指定目录
     *
     * @param targetDir 目标目录
     * @param predicate 解压文件过滤器，用于指定需要释放的文件，null表示不过滤。当{@link Predicate#test(Object)}为{@code true}时释放。
     * @throws IOException IO异常
     */
    private void extractInternal(final File targetDir, final Predicate<ArchiveEntry> predicate) throws IOException {
        Assert.isTrue(null != targetDir && ((!targetDir.exists()) || targetDir.isDirectory()), "target must be dir.");
        final SevenZFile sevenZFile = this.sevenZFile;
        SevenZArchiveEntry entry;
        File outItemFile;
        while (null != (entry = sevenZFile.getNextEntry())) {
            if (null != predicate && !predicate.test(entry)) {
                continue;
            }
            outItemFile = FileKit.file(targetDir, entry.getName());
            if (entry.isDirectory()) {
                // 创建对应目录
                // noinspection ResultOfMethodCallIgnored
                outItemFile.mkdirs();
            } else if (entry.hasStream()) {
                // 读取entry对应数据流
                // 此处直接读取而非调用sevenZFile.getInputStream(entry)，因为此方法需要遍历查找entry对应位置，性能不好。
                FileKit.copy(new Seven7EntryInputStream(sevenZFile, entry), outItemFile);
            } else {
                // 无数据流的文件创建为空文件
                FileKit.touch(outItemFile);
            }
        }
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.sevenZFile);
    }
}
