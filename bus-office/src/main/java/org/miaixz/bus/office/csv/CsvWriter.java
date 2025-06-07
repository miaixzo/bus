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
package org.miaixz.bus.office.csv;

import java.io.*;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * CSV数据写出器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class CsvWriter implements Closeable, Flushable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852283622258L;

    /**
     * 写出器
     */
    private final Writer writer;
    /**
     * 写出配置
     */
    private final CsvWriteConfig config;
    /**
     * 是否处于新行开始，新行开始用于标识是否在写出字段前写出一个分隔符
     */
    private boolean newline = true;
    /**
     * 是否首行，即CSV开始的位置，当初始化时默认为true，一旦写入内容，为false 用于标识是否补充换行符
     */
    private boolean isFirstLine = true;

    /**
     * 构造，覆盖已有文件（如果存在），默认编码UTF-8
     *
     * @param filePath File CSV文件路径
     */
    public CsvWriter(final String filePath) {
        this(FileKit.file(filePath));
    }

    /**
     * 构造，覆盖已有文件（如果存在），默认编码UTF-8
     *
     * @param file File CSV文件
     */
    public CsvWriter(final File file) {
        this(file, Charset.UTF_8);
    }

    /**
     * 构造，覆盖已有文件（如果存在）
     *
     * @param filePath File CSV文件路径
     * @param charset  编码
     */
    public CsvWriter(final String filePath, final java.nio.charset.Charset charset) {
        this(FileKit.file(filePath), charset);
    }

    /**
     * 构造，覆盖已有文件（如果存在）
     *
     * @param file    File CSV文件
     * @param charset 编码
     */
    public CsvWriter(final File file, final java.nio.charset.Charset charset) {
        this(file, charset, false);
    }

    /**
     * 构造
     *
     * @param filePath File CSV文件路径
     * @param charset  编码
     * @param isAppend 是否追加
     */
    public CsvWriter(final String filePath, final java.nio.charset.Charset charset, final boolean isAppend) {
        this(FileKit.file(filePath), charset, isAppend);
    }

    /**
     * 构造
     *
     * @param file     CSV文件
     * @param charset  编码
     * @param isAppend 是否追加
     */
    public CsvWriter(final File file, final java.nio.charset.Charset charset, final boolean isAppend) {
        this(file, charset, isAppend, null);
    }

    /**
     * 构造
     *
     * @param filePath CSV文件路径
     * @param charset  编码
     * @param isAppend 是否追加
     * @param config   写出配置，null则使用默认配置
     */
    public CsvWriter(final String filePath, final java.nio.charset.Charset charset, final boolean isAppend,
            final CsvWriteConfig config) {
        this(FileKit.file(filePath), charset, isAppend, config);
    }

    /**
     * 构造
     *
     * @param file     CSV文件
     * @param charset  编码
     * @param isAppend 是否追加, append=true模式下，endingLineBreak自动设置为true
     * @param config   写出配置，null则使用默认配置
     */
    public CsvWriter(final File file, final java.nio.charset.Charset charset, final boolean isAppend,
            final CsvWriteConfig config) {
        this(FileKit.getWriter(file, charset, isAppend),
                isAppend ? (config == null ? CsvWriteConfig.defaultConfig().setEndingLineBreak(true)
                        : config.setEndingLineBreak(true)) : config);
    }

    /**
     * 构造，使用默认配置
     *
     * @param writer {@link Writer}
     */
    public CsvWriter(final Writer writer) {
        this(writer, null);
    }

    /**
     * 构造
     *
     * @param writer Writer
     * @param config 写出配置，null则使用默认配置
     */
    public CsvWriter(final Writer writer, final CsvWriteConfig config) {
        this.writer = (writer instanceof BufferedWriter) ? writer : new BufferedWriter(writer);
        this.config = ObjectKit.defaultIfNull(config, CsvWriteConfig::defaultConfig);
    }

    /**
     * 给定字符是否为DDE攻击不安全的字符，包括：
     * <ul>
     * <li>{@code @ }</li>
     * <li>{@code + }</li>
     * <li>{@code - }</li>
     * <li>{@code = }</li>
     * </ul>
     *
     * @param c 被检查的字符
     * @return 是否不安全的字符
     */
    private static boolean isDDEUnsafeChar(final char c) {
        return c == Symbol.C_AT || c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_EQUAL;
    }

    /**
     * 设置是否始终使用文本分隔符，文本包装符，默认false，按需添加
     *
     * @param alwaysDelimitText 是否始终使用文本分隔符，文本包装符，默认false，按需添加
     * @return this
     */
    public CsvWriter setAlwaysDelimitText(final boolean alwaysDelimitText) {
        this.config.setAlwaysDelimitText(alwaysDelimitText);
        return this;
    }

    /**
     * 设置换行符
     *
     * @param lineDelimiter 换行符
     * @return this
     */
    public CsvWriter setLineDelimiter(final char[] lineDelimiter) {
        this.config.setLineDelimiter(lineDelimiter);
        return this;
    }

    /**
     * 设置是否启用dde安全模式，默认false，按需修改 防止使用Excel打开csv文件时存在dde攻击风险 注意此方法会在字段第一个字符包含{@code = + - @}时添加{@code '}作为前缀，防止公式执行
     *
     * @param ddeSafe 是否启用 dde 安全模式
     * @return this
     */
    public CsvWriter setDdeSafe(final boolean ddeSafe) {
        this.config.setDdeSafe(ddeSafe);
        return this;
    }

    /**
     * 将多行写出到Writer
     *
     * @param lines 多行数据
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter write(final String[]... lines) throws InternalException {
        return write(new ArrayIterator<>(lines));
    }

    /**
     * 将多行写出到Writer
     *
     * @param lines 多行数据，每行数据可以是集合或者数组
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter write(final Iterable<?> lines) throws InternalException {
        if (CollKit.isNotEmpty(lines)) {
            for (final Object values : lines) {
                appendLine(Convert.toStringArray(values));
            }
            flush();
        }
        return this;
    }

    /**
     * 将一个 CsvData 集合写出到Writer
     *
     * @param csvData CsvData
     * @return this
     */
    public CsvWriter write(final CsvData csvData) {
        if (csvData != null) {
            // 1、写header
            final List<String> header = csvData.getHeader();
            if (CollKit.isNotEmpty(header)) {
                this.writeHeaderLine(header.toArray(new String[0]));
            }
            // 2、写内容
            this.write(csvData.getRows());
            flush();
        }
        return this;
    }

    /**
     * 将一个Bean集合写出到Writer，并自动生成表头
     *
     * @param beans      Bean集合
     * @param properties 可选属性列表，空表示全部属性
     * @return this
     */
    public CsvWriter writeBeans(final Iterable<?> beans, final String... properties) {
        return writeBeans(beans, true, properties);
    }

    /**
     * 将一个Bean集合写出到Writer，并自动生成表头
     *
     * @param beans           Bean集合
     * @param writeHeaderLine 是否写出表头，即Bean的字段名称列表作为首行
     * @param properties      可选属性列表，空表示全部属性
     * @return this
     */
    public CsvWriter writeBeans(final Iterable<?> beans, final boolean writeHeaderLine, final String... properties) {
        if (CollKit.isNotEmpty(beans)) {
            boolean isFirst = writeHeaderLine;
            Map<String, Object> map;
            for (final Object bean : beans) {
                map = BeanKit.beanToMap(bean, properties);
                if (isFirst) {
                    writeHeaderLine(map.keySet().toArray(new String[0]));
                    isFirst = false;
                }
                writeLine(Convert.toStringArray(map.values()));
            }
            flush();
        }
        return this;
    }

    /**
     * 写出一行头部行，支持标题别名
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter writeHeaderLine(final String... fields) throws InternalException {
        final Map<String, String> headerAlias = this.config.headerAlias;
        if (MapKit.isNotEmpty(headerAlias)) {
            // 标题别名替换
            String alias;
            for (int i = 0; i < fields.length; i++) {
                alias = headerAlias.get(fields[i]);
                if (null != alias) {
                    fields[i] = alias;
                }
            }
        }
        return writeLine(fields);
    }

    /**
     * 写出一行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter writeLine(final String... fields) throws InternalException {
        if (ArrayKit.isEmpty(fields)) {
            return writeLine();
        }
        appendLine(fields);
        return this;
    }

    /**
     * 追加新行（换行）
     *
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter writeLine() throws InternalException {
        try {
            writer.write(config.lineDelimiter);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        newline = true;
        return this;
    }

    /**
     * 写出一行注释，注释符号可自定义 如果注释符不存在，则抛出异常
     *
     * @param comment 注释内容
     * @return this
     * @see CsvConfig#commentCharacter
     */
    public CsvWriter writeComment(final String comment) {
        Assert.notNull(this.config.commentCharacter, "Comment is disable!");
        try {
            if (isFirstLine) {
                // 首行不补充换行符
                isFirstLine = false;
            } else {
                writer.write(config.lineDelimiter);
            }
            writer.write(this.config.commentCharacter);
            writer.write(comment);
            newline = true;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        if (this.config.endingLineBreak) {
            writeLine();
        }
        IoKit.closeQuietly(this.writer);
    }

    @Override
    public void flush() throws InternalException {
        try {
            writer.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 追加一行，末尾会自动换行，但是追加前不会换行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @throws InternalException IO异常
     */
    private void appendLine(final String... fields) throws InternalException {
        try {
            doAppendLine(fields);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 追加一行，末尾会自动换行，但是追加前不会换行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @throws IOException IO异常
     */
    private void doAppendLine(final String... fields) throws IOException {
        if (null != fields) {
            if (isFirstLine) {
                // 首行不补换行符
                isFirstLine = false;
            } else {
                writer.write(config.lineDelimiter);
            }
            for (final String field : fields) {
                appendField(field);
            }
            newline = true;
        }
    }

    /**
     * 在当前行追加字段值，自动添加字段分隔符，如果有必要，自动包装字段
     *
     * @param value 字段值，{@code null} 会被做为空串写出
     * @throws IOException IO异常
     */
    private void appendField(final String value) throws IOException {
        final boolean alwaysDelimitText = config.alwaysDelimitText;
        final char textDelimiter = config.textDelimiter;
        final char fieldSeparator = config.fieldSeparator;

        if (!newline) {
            writer.write(fieldSeparator);
        } else {
            newline = false;
        }

        if (null == value) {
            if (alwaysDelimitText) {
                writer.write(new char[] { textDelimiter, textDelimiter });
            }
            return;
        }

        final char[] valueChars = value.toCharArray();
        boolean needsTextDelimiter = alwaysDelimitText;
        boolean containsTextDelimiter = false;

        for (final char c : valueChars) {
            if (c == textDelimiter) {
                // 字段值中存在包装符
                containsTextDelimiter = needsTextDelimiter = true;
                break;
            } else if (c == fieldSeparator || c == Symbol.C_LF || c == Symbol.C_CR) {
                // 包含分隔符或换行符需要包装符包装
                needsTextDelimiter = true;
            }
        }

        // 包装符开始
        if (needsTextDelimiter) {
            writer.write(textDelimiter);
        }

        // DDE防护，打开不执行公式
        if (config.ddeSafe && isDDEUnsafeChar(valueChars[0])) {
            writer.write('\'');
        }

        // 正文
        if (containsTextDelimiter) {
            for (final char c : valueChars) {
                // 转义文本包装符，如"转义为""
                if (c == textDelimiter) {
                    writer.write(textDelimiter);
                }
                writer.write(c);
            }
        } else {
            writer.write(valueChars);
        }

        // 包装符结尾
        if (needsTextDelimiter) {
            writer.write(textDelimiter);
        }
    }

}
