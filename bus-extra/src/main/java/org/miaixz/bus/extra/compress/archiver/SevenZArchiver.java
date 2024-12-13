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
package org.miaixz.bus.extra.compress.archiver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 7zip格式的归档封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SevenZArchiver implements Archiver {

    private final SevenZOutputFile sevenZOutputFile;

    private SeekableByteChannel channel;
    private OutputStream out;

    /**
     * 构造
     *
     * @param file 归档输出的文件
     */
    public SevenZArchiver(final File file) {
        try {
            this.sevenZOutputFile = new SevenZOutputFile(file);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 构造
     *
     * @param out 归档输出的流
     */
    public SevenZArchiver(final OutputStream out) {
        this.out = out;
        this.channel = new SeekableInMemoryByteChannel();
        try {
            this.sevenZOutputFile = new SevenZOutputFile(channel);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 构造
     *
     * @param channel 归档输出的文件
     */
    public SevenZArchiver(final SeekableByteChannel channel) {
        try {
            this.sevenZOutputFile = new SevenZOutputFile(channel);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取{@link SevenZOutputFile}以便自定义相关设置
     *
     * @return {@link SevenZOutputFile}
     */
    public SevenZOutputFile getSevenZOutputFile() {
        return this.sevenZOutputFile;
    }

    @Override
    public SevenZArchiver add(final File file, final String path, final Function<String, String> fileNameEditor,
            final Predicate<File> filter) {
        try {
            addInternal(file, path, fileNameEditor, filter);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public SevenZArchiver finish() {
        try {
            this.sevenZOutputFile.finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        try {
            finish();
        } catch (final Exception ignore) {
            // ignore
        }
        if (null != out && this.channel instanceof SeekableInMemoryByteChannel) {
            try {
                out.write(((SeekableInMemoryByteChannel) this.channel).array());
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        IoKit.closeQuietly(this.sevenZOutputFile);
    }

    /**
     * 将文件或目录加入归档包，目录采取递归读取方式按照层级加入
     *
     * @param file           文件或目录
     * @param path           文件或目录的初始路径，null表示位于根路径
     * @param fileNameEditor 文件名编辑器
     * @param filter         文件过滤器，指定哪些文件或目录可以加入，当{@link Predicate#test(Object)}为{@code true}保留，null表示保留全部
     */
    private void addInternal(final File file, final String path, final Function<String, String> fileNameEditor,
            final Predicate<File> filter) throws IOException {
        if (null != filter && !filter.test(file)) {
            return;
        }
        final SevenZOutputFile out = this.sevenZOutputFile;

        String entryName = (null == fileNameEditor) ? file.getName() : fileNameEditor.apply(file.getName());
        if (StringKit.isNotEmpty(path)) {
            // 非空拼接路径，格式为：path/name
            entryName = StringKit.addSuffixIfNot(path, Symbol.SLASH) + entryName;
        }
        out.putArchiveEntry(out.createArchiveEntry(file, entryName));

        if (file.isDirectory()) {
            // 目录遍历写入
            final File[] files = file.listFiles();
            if (ArrayKit.isNotEmpty(files)) {
                for (final File childFile : files) {
                    addInternal(childFile, entryName, fileNameEditor, filter);
                }
            }
        } else {
            if (file.isFile()) {
                // 文件直接写入
                out.write(FileKit.readBytes(file));
            }
            out.closeArchiveEntry();
        }
    }

}
