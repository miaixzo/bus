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
package org.miaixz.bus.office.csv;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;

import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CSV文件读取器基础类，提供灵活的文件、路径中的CSV读取，一次构造可多次调用读取不同数据，参考：FastCSV
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvBaseReader implements Serializable {

    /**
     * 默认编码
     */
    protected static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;
    private static final long serialVersionUID = -1L;
    private final CsvReadConfig config;

    /**
     * 构造，使用默认配置项
     */
    public CsvBaseReader() {
        this(null);
    }

    /**
     * 构造
     *
     * @param config 配置项
     */
    public CsvBaseReader(final CsvReadConfig config) {
        this.config = ObjectKit.defaultIfNull(config, CsvReadConfig::defaultConfig);
    }

    /**
     * 设置字段分隔符，默认逗号','
     *
     * @param fieldSeparator 字段分隔符，默认逗号','
     */
    public void setFieldSeparator(final char fieldSeparator) {
        this.config.setFieldSeparator(fieldSeparator);
    }

    /**
     * 设置 文本分隔符，文本包装符，默认双引号'"'
     *
     * @param textDelimiter 文本分隔符，文本包装符，默认双引号'"'
     */
    public void setTextDelimiter(final char textDelimiter) {
        this.config.setTextDelimiter(textDelimiter);
    }

    /**
     * 设置是否首行做为标题行，默认false
     *
     * @param containsHeader 是否首行做为标题行，默认false
     */
    public void setContainsHeader(final boolean containsHeader) {
        this.config.setContainsHeader(containsHeader);
    }

    /**
     * 设置是否跳过空白行，默认true
     *
     * @param skipEmptyRows 是否跳过空白行，默认true
     */
    public void setSkipEmptyRows(final boolean skipEmptyRows) {
        this.config.setSkipEmptyRows(skipEmptyRows);
    }

    /**
     * 设置每行字段个数不同时是否抛出异常，默认false
     *
     * @param errorOnDifferentFieldCount 每行字段个数不同时是否抛出异常，默认false
     */
    public void setErrorOnDifferentFieldCount(final boolean errorOnDifferentFieldCount) {
        this.config.setErrorOnDifferentFieldCount(errorOnDifferentFieldCount);
    }

    /**
     * 读取CSV文件，默认UTF-8编码
     *
     * @param file CSV文件
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(final File file) throws InternalException {
        return read(file, DEFAULT_CHARSET);
    }

    /**
     * 从字符串中读取CSV数据
     *
     * @param csvStr CSV字符串
     * @return {@link CsvData}，包含数据列表和行信息
     */
    public CsvData readFromString(final String csvStr) {
        return read(new StringReader(csvStr), true);
    }

    /**
     * 从字符串中读取CSV数据
     *
     * @param csvStr     CSV字符串
     * @param rowHandler 行处理器，用于一行一行的处理数据
     */
    public void readFromString(final String csvStr, final ConsumerX<CsvRow> rowHandler) {
        read(parse(new StringReader(csvStr)), true, rowHandler);
    }

    /**
     * 读取CSV文件
     *
     * @param file    CSV文件
     * @param charset 文件编码，默认系统编码
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(final File file, final java.nio.charset.Charset charset) throws InternalException {
        return read(Objects.requireNonNull(file.toPath(), "file must not be null"), charset);
    }

    /**
     * 读取CSV文件，默认UTF-8编码
     *
     * @param path CSV文件
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(final Path path) throws InternalException {
        return read(path, DEFAULT_CHARSET);
    }

    /**
     * 读取CSV文件
     *
     * @param path    CSV文件
     * @param charset 文件编码，默认系统编码
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(final Path path, final java.nio.charset.Charset charset) throws InternalException {
        Assert.notNull(path, "path must not be null");
        return read(FileKit.getReader(path, charset), true);
    }

    /**
     * 从Reader中读取CSV数据，读取后关闭Reader
     *
     * @param reader      Reader
     * @param closeReader 是否关闭Reader
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(final Reader reader, final boolean closeReader) throws InternalException {
        final CsvParser csvParser = parse(reader);
        final List<CsvRow> rows = new ArrayList<>();
        read(csvParser, closeReader, rows::add);
        final List<String> header = config.headerLineNo > -1 ? csvParser.getHeader() : null;

        return new CsvData(header, rows);
    }

    /**
     * 从Reader中读取CSV数据，结果为Map，读取后关闭Reader。 此方法默认识别首行为标题行。
     *
     * @param reader      Reader
     * @param closeReader 是否关闭Reader
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public List<Map<String, String>> readMapList(final Reader reader, final boolean closeReader)
            throws InternalException {
        // 此方法必须包含标题
        this.config.setContainsHeader(true);

        final List<Map<String, String>> result = new ArrayList<>();
        read(reader, closeReader, (row) -> result.add(row.getFieldMap()));
        return result;
    }

    /**
     * 从Reader中读取CSV数据并转换为Bean列表，读取后关闭Reader。 此方法默认识别首行为标题行。
     *
     * @param <T>         Bean类型
     * @param reader      Reader
     * @param closeReader 是否关闭Reader
     * @param clazz       Bean类型
     * @return Bean列表
     */
    public <T> List<T> read(final Reader reader, final boolean closeReader, final Class<T> clazz) {
        // 此方法必须包含标题
        this.config.setContainsHeader(true);

        final List<T> result = new ArrayList<>();
        read(reader, closeReader, (row) -> result.add(row.toBean(clazz)));
        return result;
    }

    /**
     * 从字符串中读取CSV数据并转换为Bean列表，读取后关闭Reader。 此方法默认识别首行为标题行。
     *
     * @param <T>    Bean类型
     * @param csvStr csv字符串
     * @param clazz  Bean类型
     * @return Bean列表
     */
    public <T> List<T> read(final String csvStr, final Class<T> clazz) {
        // 此方法必须包含标题
        this.config.setContainsHeader(true);

        final List<T> result = new ArrayList<>();
        read(new StringReader(csvStr), true, (row) -> result.add(row.toBean(clazz)));
        return result;
    }

    /**
     * 从Reader中读取CSV数据，读取后关闭Reader
     *
     * @param reader      Reader
     * @param closeReader 是否关闭Reader
     * @param rowHandler  行处理器，用于一行一行的处理数据
     * @throws InternalException IO异常
     */
    public void read(final Reader reader, final boolean closeReader, final ConsumerX<CsvRow> rowHandler)
            throws InternalException {
        read(parse(reader), closeReader, rowHandler);
    }

    /**
     * 读取CSV数据，读取后关闭Parser
     *
     * @param csvParser   CSV解析器
     * @param closeParser 是否关闭解析器
     * @param rowHandler  行处理器，用于一行一行的处理数据
     * @throws InternalException IO异常
     */
    private void read(final CsvParser csvParser, final boolean closeParser, final ConsumerX<CsvRow> rowHandler)
            throws InternalException {
        try {
            while (csvParser.hasNext()) {
                rowHandler.accept(csvParser.next());
            }
        } finally {
            if (closeParser) {
                IoKit.closeQuietly(csvParser);
            }
        }
    }

    /**
     * 构建 {@link CsvParser}
     *
     * @param reader Reader
     * @return CsvParser
     * @throws InternalException IO异常
     */
    protected CsvParser parse(final Reader reader) throws InternalException {
        return new CsvParser(reader, this.config);
    }

}
