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

import org.miaixz.bus.core.collection.Iterator.ArrayIterator;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.*;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * CSV数据写出器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class CsvWriter implements Closeable, Flushable {

    /**
     * 写出器
     */
    private final Writer writer;
    /**
     * 写出配置
     */
    private final CsvWriteConfig config;
    /**
     * 是否处于新行开始
     */
    private boolean newline = true;
    /**
     * 是否首行，即CSV开始的位置，当初始化时默认为true，一旦写入内容，为false
     */
    private boolean isFirstLine = true;

    /**
     * 构造,覆盖已有文件(如果存在),默认编码UTF-8
     *
     * @param filePath File CSV文件路径
     */
    public CsvWriter(String filePath) {
        this(FileKit.file(filePath));
    }

    /**
     * 构造,覆盖已有文件(如果存在),默认编码UTF-8
     *
     * @param file File CSV文件
     */
    public CsvWriter(File file) {
        this(file, Charset.UTF_8);
    }

    /**
     * 构造,覆盖已有文件(如果存在)
     *
     * @param filePath File CSV文件路径
     * @param charset  编码
     */
    public CsvWriter(String filePath, java.nio.charset.Charset charset) {
        this(FileKit.file(filePath), charset);
    }

    /**
     * 构造,覆盖已有文件(如果存在)
     *
     * @param file    File CSV文件
     * @param charset 编码
     */
    public CsvWriter(File file, java.nio.charset.Charset charset) {
        this(file, charset, false);
    }

    /**
     * 构造
     *
     * @param filePath File CSV文件路径
     * @param charset  编码
     * @param isAppend 是否追加
     */
    public CsvWriter(String filePath, java.nio.charset.Charset charset, boolean isAppend) {
        this(FileKit.file(filePath), charset, isAppend);
    }

    /**
     * 构造
     *
     * @param file     CSV文件
     * @param charset  编码
     * @param isAppend 是否追加
     */
    public CsvWriter(File file, java.nio.charset.Charset charset, boolean isAppend) {
        this(file, charset, isAppend, null);
    }

    /**
     * 构造
     *
     * @param filePath CSV文件路径
     * @param charset  编码
     * @param isAppend 是否追加
     * @param config   写出配置,null则使用默认配置
     */
    public CsvWriter(String filePath, java.nio.charset.Charset charset, boolean isAppend, CsvWriteConfig config) {
        this(FileKit.file(filePath), charset, isAppend, config);
    }

    /**
     * 构造
     *
     * @param file     CSV文件
     * @param charset  编码
     * @param isAppend 是否追加
     * @param config   写出配置,null则使用默认配置
     */
    public CsvWriter(File file, java.nio.charset.Charset charset, boolean isAppend, CsvWriteConfig config) {
        this(FileKit.getWriter(file, charset, isAppend), config);
    }

    /**
     * 构造,使用默认配置
     *
     * @param writer {@link Writer}
     */
    public CsvWriter(Writer writer) {
        this(writer, null);
    }

    /**
     * 构造
     *
     * @param writer Writer
     * @param config 写出配置,null则使用默认配置
     */
    public CsvWriter(Writer writer, CsvWriteConfig config) {
        this.writer = (writer instanceof BufferedWriter) ? writer : new BufferedWriter(writer);
        this.config = ObjectKit.defaultIfNull(config, CsvWriteConfig::defaultConfig);
    }

    /**
     * 设置是否始终使用文本分隔符,文本包装符,默认false,按需添加
     *
     * @param alwaysDelimitText 是否始终使用文本分隔符,文本包装符,默认false,按需添加
     */
    public void setAlwaysDelimitText(boolean alwaysDelimitText) {
        this.setAlwaysDelimitText(alwaysDelimitText);
    }

    /**
     * 设置换行符
     *
     * @param lineDelimiter 换行符
     */
    public void setLineDelimiter(char[] lineDelimiter) {
        this.setLineDelimiter(lineDelimiter);
    }

    /**
     * 将多行写出到Writer
     *
     * @param lines 多行数据
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter write(String[]... lines) throws InternalException {
        return write(new ArrayIterator<>(lines));
    }

    /**
     * 将多行写出到Writer
     *
     * @param lines 多行数据，每行数据可以是集合或者数组
     * @return this
     */
    public CsvWriter write(Iterable<?> lines) {
        if (CollKit.isNotEmpty(lines)) {
            for (Object values : lines) {
                appendLine(Convert.toStrArray(values));
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
    public CsvWriter write(CsvData csvData) {
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
     * @param beans Bean集合
     * @return this
     */
    public CsvWriter writeBeans(Collection<?> beans) {
        if (CollKit.isNotEmpty(beans)) {
            boolean isFirst = true;
            Map<String, Object> map;
            for (Object bean : beans) {
                map = BeanKit.beanToMap(bean);
                if (isFirst) {
                    writeLine(map.keySet().toArray(new String[0]));
                    isFirst = false;
                }
                writeLine(Convert.toStrArray(map.values()));
            }
            flush();
        }
        return this;
    }

    /**
     * 写出一行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter writeLine(String... fields) throws InternalException {
        if (ArrayKit.isEmpty(fields)) {
            return writeLine();
        }
        appendLine(fields);
        return this;
    }

    /**
     * 追加新行(换行)
     *
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter writeLine() throws InternalException {
        try {
            writer.write(config.lineDelimiter);
        } catch (IOException e) {
            throw new InternalException(e);
        }
        newline = true;
        return this;
    }

    /**
     * 写出一行注释，注释符号可自定义
     * 如果注释符不存在，则抛出异常
     *
     * @param comment 注释内容
     * @return this
     */
    public CsvWriter writeComment(String comment) {
        Assert.notNull(this.config.commentCharacter, "Comment is disable!");
        try {
            if (isFirstLine) {
                // 首行不补换行符
                isFirstLine = false;
            } else {
                writer.write(config.lineDelimiter);
            }
            writer.write(this.config.commentCharacter);
            writer.write(comment);
            newline = true;
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        IoKit.close(this.writer);
    }

    @Override
    public void flush() throws InternalException {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 写出一行头部行，支持标题别名
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加
     * @return this
     * @throws InternalException IO异常
     */
    public CsvWriter writeHeaderLine(String... fields) throws InternalException {
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
     * 追加一行,末尾会自动换行,但是追加前不会换行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @throws InternalException IO异常
     */
    private void appendLine(final String... fields) throws InternalException {
        try {
            doAppendLine(fields);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 追加一行,末尾会自动换行,但是追加前不会换行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @throws IOException IO异常
     */
    private void doAppendLine(String... fields) throws IOException {
        if (null != fields) {
            if (isFirstLine) {
                // 首行不补换行符
                isFirstLine = false;
            } else {
                writer.write(config.lineDelimiter);
            }
            for (String field : fields) {
                appendField(field);
            }
            newline = true;
        }
    }

    /**
     * 在当前行追加字段值,自动添加字段分隔符,如果有必要,自动包装字段
     *
     * @param value 字段值,{@code null} 会被做为空串写出
     * @throws IOException IO异常
     */
    private void appendField(final String value) throws IOException {
        boolean alwaysDelimitText = config.alwaysDelimitText;
        char textDelimiter = config.textDelimiter;
        char fieldSeparator = config.fieldSeparator;

        if (false == newline) {
            writer.write(fieldSeparator);
        } else {
            newline = false;
        }

        if (null == value) {
            if (alwaysDelimitText) {
                writer.write(new char[]{textDelimiter, textDelimiter});
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

        // 正文
        if (containsTextDelimiter) {
            for (final char c : valueChars) {
                // 转义文本包装符
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
