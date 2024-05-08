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

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.function.ConsumerX;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * CSV文件读取器,参考：FastCSV
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvReader implements Iterable<CsvRow>, Closeable {

    private final Reader reader;
    private final CsvReadConfig config;

    /**
     * 构造,使用默认配置项
     */
    public CsvReader() {
        this(null);
    }

    /**
     * 构造
     *
     * @param config 配置项
     */
    public CsvReader(CsvReadConfig config) {
        this((Reader) null, config);
    }

    /**
     * 构造
     *
     * @param reader {@link Reader}，null表示不设置默认reader
     * @param config 配置项，null表示默认配置
     */
    public CsvReader(Reader reader, CsvReadConfig config) {
        this.config = ObjectKit.defaultIfNull(config, CsvReadConfig::defaultConfig);
        this.reader = reader;
    }

    /**
     * 构造，默认{@link Charset#UTF_8}编码
     *
     * @param file   CSV文件路径，null表示不设置路径
     * @param config 配置项，null表示默认配置
     */
    public CsvReader(File file, CsvReadConfig config) {
        this(file, Charset.UTF_8, config);
    }

    /**
     * 构造，默认{@link Charset#UTF_8}编码
     *
     * @param path   CSV文件路径，null表示不设置路径
     * @param config 配置项，null表示默认配置
     */
    public CsvReader(Path path, CsvReadConfig config) {
        this(path, Charset.UTF_8, config);
    }

    /**
     * 构造
     *
     * @param file    CSV文件路径，null表示不设置路径
     * @param charset 编码
     * @param config  配置项，null表示默认配置
     */
    public CsvReader(File file, java.nio.charset.Charset charset, CsvReadConfig config) {
        this(FileKit.getReader(file, charset), config);
    }

    /**
     * 构造
     *
     * @param path    CSV文件路径，null表示不设置路径
     * @param charset 编码
     * @param config  配置项，null表示默认配置
     */
    public CsvReader(Path path, java.nio.charset.Charset charset, CsvReadConfig config) {
        this(FileKit.getReader(path, charset), config);
    }

    /**
     * 设置字段分隔符,默认逗号
     *
     * @param fieldSeparator 字段分隔符,默认逗号
     */
    public void setFieldSeparator(char fieldSeparator) {
        this.config.setFieldSeparator(fieldSeparator);
    }

    /**
     * 设置 文本分隔符,文本包装符,默认双引号
     *
     * @param textDelimiter 文本分隔符,文本包装符,默认双引号
     */
    public void setTextDelimiter(char textDelimiter) {
        this.config.setTextDelimiter(textDelimiter);
    }

    /**
     * 设置是否首行做为标题行,默认false
     *
     * @param containsHeader 是否首行做为标题行,默认false
     */
    public void setContainsHeader(boolean containsHeader) {
        this.config.setContainsHeader(containsHeader);
    }

    /**
     * 设置是否跳过空白行,默认true
     *
     * @param skipEmptyRows 是否跳过空白行,默认true
     */
    public void setSkipEmptyRows(boolean skipEmptyRows) {
        this.config.setSkipEmptyRows(skipEmptyRows);
    }

    /**
     * 设置每行字段个数不同时是否抛出异常,默认false
     *
     * @param errorOnDifferentFieldCount 每行字段个数不同时是否抛出异常,默认false
     */
    public void setErrorOnDifferentFieldCount(boolean errorOnDifferentFieldCount) {
        this.setErrorOnDifferentFieldCount(errorOnDifferentFieldCount);
    }

    /**
     * 读取CSV文件,默认UTF-8编码
     *
     * @param file CSV文件
     * @return {@link CsvData},包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(File file) throws InternalException {
        return read(file, Charset.UTF_8);
    }

    /**
     * 从字符串中读取CSV数据
     *
     * @param text CSV字符串
     * @return {@link CsvData}，包含数据列表和行信息
     */
    public CsvData read(String text) {
        return read(new StringReader(text));
    }

    /**
     * 从字符串中读取CSV数据
     *
     * @param text       CSV字符串
     * @param rowHandler 行处理器，用于一行一行的处理数据
     */
    public void read(String text, ConsumerX<CsvRow> rowHandler) {
        read(parse(new StringReader(text)), rowHandler);
    }

    /**
     * 读取CSV文件
     *
     * @param file    CSV文件
     * @param charset 文件编码,默认系统编码
     * @return {@link CsvData},包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(File file, java.nio.charset.Charset charset) throws InternalException {
        return read(Objects.requireNonNull(file.toPath(), "file must not be null"), charset);
    }

    /**
     * 读取CSV文件,默认UTF-8编码
     *
     * @param path CSV文件
     * @return {@link CsvData},包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(Path path) throws InternalException {
        return read(path, Charset.UTF_8);
    }

    /**
     * 读取CSV文件
     *
     * @param path    CSV文件
     * @param charset 文件编码,默认系统编码
     * @return {@link CsvData},包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(Path path, java.nio.charset.Charset charset) throws InternalException {
        Assert.notNull(path, "path must not be null");
        try (Reader reader = FileKit.getReader(path, charset)) {
            return read(reader);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 从Reader中读取CSV数据,读取后关闭Reader
     *
     * @param reader Reader
     * @return {@link CsvData},包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public CsvData read(Reader reader) throws InternalException {
        final CsvParser csvParser = parse(reader);
        final List<CsvRow> rows = new ArrayList<>();
        read(csvParser, rows::add);
        final List<String> header = config.headerLineNo > -1 ? csvParser.getHeader() : null;

        return new CsvData(header, rows);
    }

    /**
     * 从Reader中读取CSV数据，读取后关闭Reader
     *
     * @param reader     Reader
     * @param rowHandler 行处理器，用于一行一行的处理数据
     */
    public void read(Reader reader, ConsumerX<CsvRow> rowHandler) {
        read(parse(reader), rowHandler);
    }

    /**
     * 读取CSV数据，读取后关闭Parser
     *
     * @param csvParser  CSV解析器
     * @param rowHandler 行处理器，用于一行一行的处理数据
     */
    private void read(CsvParser csvParser, ConsumerX<CsvRow> rowHandler) {
        try {
            while (csvParser.hasNext()) {
                rowHandler.accept(csvParser.next());
            }
        } finally {
            IoKit.close(csvParser);
        }
    }

    /**
     * 从字符串中读取CSV数据并转换为Bean列表，读取后关闭Reader
     * 此方法默认识别首行为标题行。
     *
     * @param <T>    Bean类型
     * @param csvStr csv字符串
     * @param clazz  Bean类型
     * @return Bean列表
     */
    public <T> List<T> read(String csvStr, Class<T> clazz) {
        // 此方法必须包含标题
        this.config.setContainsHeader(true);

        final List<T> result = new ArrayList<>();
        read(new StringReader(csvStr), (row) -> result.add(row.toBean(clazz)));
        return result;
    }

    /**
     * 从Reader中读取CSV数据，结果为Map，读取后关闭Reader
     * 此方法默认识别首行为标题行
     *
     * @param reader Reader
     * @return {@link CsvData}，包含数据列表和行信息
     * @throws InternalException IO异常
     */
    public List<Map<String, String>> readMapList(Reader reader) throws InternalException {
        // 此方法必须包含标题
        this.config.setContainsHeader(true);

        final List<Map<String, String>> result = new ArrayList<>();
        read(reader, (row) -> result.add(row.getFieldMap()));
        return result;
    }

    /**
     * 从Reader中读取CSV数据并转换为Bean列表，读取后关闭Reader
     * 此方法默认识别首行为标题行
     *
     * @param <T>    Bean类型
     * @param reader Reader
     * @param clazz  Bean类型
     * @return Bean列表
     */
    public <T> List<T> read(Reader reader, Class<T> clazz) {
        // 此方法必须包含标题
        this.config.setContainsHeader(true);

        final List<T> result = new ArrayList<>();
        read(reader, (row) -> result.add(row.toBean(clazz)));
        return result;
    }

    /**
     * 构建 {@link CsvParser}
     *
     * @param reader Reader
     * @return CsvParser
     * @throws InternalException IO异常
     */
    private CsvParser parse(Reader reader) throws InternalException {
        return new CsvParser(reader, config);
    }

    /**
     * 从Reader中读取CSV数据并转换为Bean列表，读取后关闭Reader
     * 此方法默认识别首行为标题行
     *
     * @param <T>            Bean类型
     * @param reader         Reader
     * @param startLineIndex 起始行号,不需要大于 0，因为首行是标题行
     * @param clazz          Bean类型
     * @return Bean列表
     */
    public <T> List<T> read(Reader reader, int startLineIndex, Class<T> clazz) {
        if (startLineIndex < 1) {
            throw new IndexOutOfBoundsException(StringKit.format("start line index {} is lower than first row index 1.", startLineIndex));
        }
        // 此方法必须包含标题
        this.config.setContainsHeader(true);

        final List<T> result = new ArrayList<>();
        read(reader, (row) -> {
            if (row.getOriginalLineNumber() >= startLineIndex) {
                result.add(row.toBean(clazz));
            }
        });
        return result;
    }

    /**
     * 根据Reader创建{@link Stream}，以便使用stream方式读取csv行
     *
     * @return {@link Stream}
     */
    public Stream<CsvRow> stream() {
        return StreamSupport.stream(spliterator(), false)
                .onClose(() -> {
                    try {
                        close();
                    } catch (final IOException e) {
                        throw new InternalException(e);
                    }
                });
    }

    @Override
    public Iterator<CsvRow> iterator() {
        return parse(this.reader);
    }

    @Override
    public void close() throws IOException {
        IoKit.close(this.reader);
    }

}
