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
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 数据归档封装，归档即将几个文件或目录打成一个压缩包 支持的归档文件格式为：
 * <ul>
 * <li>{@link ArchiveStreamFactory#AR}</li>
 * <li>{@link ArchiveStreamFactory#CPIO}</li>
 * <li>{@link ArchiveStreamFactory#JAR}</li>
 * <li>{@link ArchiveStreamFactory#TAR}</li>
 * <li>{@link ArchiveStreamFactory#ZIP}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamArchiver implements Archiver {

    private final ArchiveOutputStream<? extends ArchiveEntry> out;

    /**
     * 构造
     *
     * @param charset      编码
     * @param archiverName 归档类型名称，见{@link ArchiveStreamFactory}
     * @param file         归档输出的文件
     */
    public StreamArchiver(final Charset charset, final String archiverName, final File file) {
        this(charset, archiverName, FileKit.getOutputStream(file));
    }

    /**
     * 构造
     *
     * @param charset      编码
     * @param archiverName 归档类型名称，见{@link ArchiveStreamFactory}
     * @param targetStream 归档输出的流
     */
    public StreamArchiver(final Charset charset, final String archiverName, final OutputStream targetStream) {
        if ("tgz".equalsIgnoreCase(archiverName) || "tar.gz".equalsIgnoreCase(archiverName)) {
            // 支持tgz格式解压
            try {
                this.out = new TarArchiveOutputStream(new GzipCompressorOutputStream(targetStream));
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        } else {
            final ArchiveStreamFactory factory = new ArchiveStreamFactory(charset.name());
            try {
                this.out = factory.createArchiveOutputStream(archiverName, targetStream);
            } catch (final ArchiveException e) {
                throw new InternalException(e);
            }
        }

        // 特殊设置
        if (this.out instanceof TarArchiveOutputStream) {
            ((TarArchiveOutputStream) out).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        } else if (this.out instanceof ArArchiveOutputStream) {
            ((ArArchiveOutputStream) out).setLongFileMode(ArArchiveOutputStream.LONGFILE_BSD);
        }
    }

    /**
     * 创建归档器
     *
     * @param charset      编码
     * @param archiverName 归档类型名称，见{@link ArchiveStreamFactory}
     * @param file         归档输出的文件
     * @return StreamArchiver
     */
    public static StreamArchiver of(final Charset charset, final String archiverName, final File file) {
        return new StreamArchiver(charset, archiverName, file);
    }

    /**
     * 创建归档器
     *
     * @param charset      编码
     * @param archiverName 归档类型名称，见{@link ArchiveStreamFactory}
     * @param out          归档输出的流
     * @return StreamArchiver
     */
    public static StreamArchiver of(final Charset charset, final String archiverName, final OutputStream out) {
        return new StreamArchiver(charset, archiverName, out);
    }

    @Override
    public StreamArchiver add(final File file, final String path, final Function<String, String> fileNameEditor,
            final Predicate<File> predicate) throws InternalException {
        try {
            addInternal(file, path, fileNameEditor, predicate);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * 结束已经增加的文件归档，此方法不会关闭归档流，可以继续添加文件
     *
     * @return this
     */
    @Override
    public StreamArchiver finish() {
        try {
            this.out.finish();
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
        IoKit.closeQuietly(this.out);
    }

    /**
     * 将文件或目录加入归档包，目录采取递归读取方式按照层级加入
     *
     * @param file           文件或目录
     * @param path           文件或目录的初始路径，{@code null}表示位于根路径
     * @param fileNameEditor 文件名编辑器
     * @param predicate      文件过滤器，指定哪些文件或目录可以加入，当{@link Predicate#test(Object)}为{@code true}加入。
     */
    private void addInternal(final File file, final String path, final Function<String, String> fileNameEditor,
            final Predicate<File> predicate) throws IOException {
        if (null != predicate && !predicate.test(file)) {
            return;
        }
        final ArchiveOutputStream out = this.out;

        String entryName = (fileNameEditor == null) ? file.getName() : fileNameEditor.apply(file.getName());
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
                    addInternal(childFile, entryName, fileNameEditor, predicate);
                }
            } else {
                // 空文件夹也需要关闭
                out.closeArchiveEntry();
            }
        } else {
            if (file.isFile()) {
                // 文件直接写入
                FileKit.copy(file, out);
            }
            out.closeArchiveEntry();
        }
    }

}
