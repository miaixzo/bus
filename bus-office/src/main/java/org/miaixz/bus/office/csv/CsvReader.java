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
package org.miaixz.bus.office.csv;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.IoKit;

import java.io.Closeable;
import java.io.File;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * CSV文件读取器，参考：FastCSV
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvReader extends CsvBaseReader implements Iterable<CsvRow>, Closeable {

    private static final long serialVersionUID = -1L;

    private final Reader reader;

    /**
     * 构造，使用默认配置项
     */
    public CsvReader() {
        this(null);
    }

    /**
     * 构造
     *
     * @param config 配置项
     */
    public CsvReader(final CsvReadConfig config) {
        this((Reader) null, config);
    }

    /**
     * 构造，默认{@link #DEFAULT_CHARSET}编码
     *
     * @param file   CSV文件路径，null表示不设置路径
     * @param config 配置项，null表示默认配置
     */
    public CsvReader(final File file, final CsvReadConfig config) {
        this(file, DEFAULT_CHARSET, config);
    }

    /**
     * 构造，默认{@link #DEFAULT_CHARSET}编码
     *
     * @param path   CSV文件路径，null表示不设置路径
     * @param config 配置项，null表示默认配置
     */
    public CsvReader(final Path path, final CsvReadConfig config) {
        this(path, DEFAULT_CHARSET, config);
    }

    /**
     * 构造
     *
     * @param file    CSV文件路径，null表示不设置路径
     * @param charset 编码
     * @param config  配置项，null表示默认配置
     */
    public CsvReader(final File file, final Charset charset, final CsvReadConfig config) {
        this(FileKit.getReader(file, charset), config);
    }

    /**
     * 构造
     *
     * @param path    CSV文件路径，null表示不设置路径
     * @param charset 编码
     * @param config  配置项，null表示默认配置
     */
    public CsvReader(final Path path, final Charset charset, final CsvReadConfig config) {
        this(FileKit.getReader(path, charset), config);
    }

    /**
     * 构造
     *
     * @param reader {@link Reader}，null表示不设置默认reader
     * @param config 配置项，null表示默认配置
     */
    public CsvReader(final Reader reader, final CsvReadConfig config) {
        super(config);
        this.reader = reader;
    }

    /**
     * 读取CSV文件，此方法只能调用一次
     * 调用此方法的前提是构造中传入文件路径或Reader
     *
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read() throws InternalException {
        return read(this.reader, false);
    }

    /**
     * 读取CSV数据，此方法只能调用一次
     * 调用此方法的前提是构造中传入文件路径或Reader
     *
     * @param rowHandler 行处理器，用于一行一行的处理数据
     * @throws InternalException IO异常
     */
    public void read(final ConsumerX<CsvRow> rowHandler) throws InternalException {
        read(this.reader, false, rowHandler);
    }

    /**
     * 根据Reader创建{@link Stream}，以便使用stream方式读取csv行
     *
     * @return {@link Stream}
     */
    public Stream<CsvRow> stream() {
        return StreamSupport
                .stream(spliterator(), false)
                .onClose(this::close);
    }

    @Override
    public Iterator<CsvRow> iterator() {
        return parse(this.reader);
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.reader);
    }

}
