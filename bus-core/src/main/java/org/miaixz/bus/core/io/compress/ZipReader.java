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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ZipKit;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
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

    private static final int DEFAULT_MAX_SIZE_DIFF = 100;
    private final ZipResource resource;
    /**
     * 检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
     */
    private int maxSizeDiff = DEFAULT_MAX_SIZE_DIFF;

    /**
     * 构造
     *
     * @param zipFile 读取的的Zip文件
     */
    public ZipReader(final ZipFile zipFile) {
        this(new ZipFileResource(zipFile));
    }

    /**
     * 构造
     *
     * @param zin 读取的的Zip文件流
     */
    public ZipReader(final ZipInputStream zin) {
        this(new ZipStream(zin));
    }

    /**
     * 构造
     *
     * @param resource 读取的的Zip文件流
     */
    public ZipReader(final ZipResource resource) {
        this.resource = resource;
    }

    /**
     * 创建ZipReader
     *
     * @param zipFile 生成的Zip文件
     * @param charset 编码
     * @return ZipReader
     */
    public static ZipReader of(final File zipFile, final Charset charset) {
        return new ZipReader(ZipKit.toZipFile(zipFile, charset));
    }

    /**
     * 创建ZipReader
     *
     * @param in      Zip输入的流，一般为输入文件流
     * @param charset 编码
     * @return ZipReader
     */
    public static ZipReader of(final InputStream in, final Charset charset) {
        return new ZipReader(new ZipInputStream(in, charset));
    }

    /**
     * 设置检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
     *
     * @param maxSizeDiff 检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
     * @return this
     */
    public ZipReader setMaxSizeDiff(final int maxSizeDiff) {
        this.maxSizeDiff = maxSizeDiff;
        return this;
    }

    /**
     * 获取指定路径的文件流 如果是文件模式，则直接获取Entry对应的流，如果是流模式，则遍历entry后，找到对应流返回
     *
     * @param path 路径
     * @return 文件流
     */
    public InputStream get(final String path) {
        return this.resource.get(path);
    }

    /**
     * 解压到指定目录中
     *
     * @param outFile 解压到的目录
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public File readTo(final File outFile) throws InternalException {
        return readTo(outFile, null);
    }

    /**
     * 解压到指定目录中
     *
     * @param outFile     解压到的目录
     * @param entryFilter 过滤器，只保留{@link Predicate#test(Object)}结果为{@code true}的文件
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public File readTo(final File outFile, final Predicate<ZipEntry> entryFilter) throws InternalException {
        read((zipEntry) -> {
            if (null == entryFilter || entryFilter.test(zipEntry)) {
                readEntry(zipEntry, outFile);
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
    public ZipReader read(final Consumer<ZipEntry> consumer) throws InternalException {
        resource.read(consumer, this.maxSizeDiff);
        return this;
    }

    @Override
    public void close() throws InternalException {
        IoKit.closeQuietly(this.resource);
    }

    /**
     * 读取一个ZipEntry的数据到目标目录下，如果entry是个目录，则创建对应目录，否则解压并写出到文件
     *
     * @param zipEntry entry
     * @param outFile  写出到的目录
     */
    private void readEntry(final ZipEntry zipEntry, final File outFile) {
        String path = zipEntry.getName();
        if (FileKit.isWindows()) {
            // Win系统下
            path = StringKit.replace(path, Symbol.STAR, Symbol.UNDERLINE);
        }
        // FileKit.file会检查slip漏洞，漏洞说明见http://blog.nsfocus.net/zip-slip-2/
        final File outItemFile = FileKit.file(outFile, path);
        if (zipEntry.isDirectory()) {
            // 目录
            outItemFile.mkdirs();
        } else {
            // 文件
            FileKit.copy(this.resource.get(zipEntry), outItemFile);
        }
    }

}
