/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.text.csv;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;

import java.io.*;
import java.util.Collection;

/**
 * CSV数据写出器
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
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
        this.config = ObjectKit.defaultIfNull(config, CsvWriteConfig.defaultConfig());
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
     * @throws InstrumentException IO异常
     */
    public CsvWriter write(String[]... lines) throws InstrumentException {
        if (ArrayKit.isNotEmpty(lines)) {
            for (final String[] values : lines) {
                appendLine(values);
            }
            flush();
        }
        return this;
    }

    /**
     * 将多行写出到Writer
     *
     * @param lines 多行数据
     * @return this
     * @throws InstrumentException IO异常
     */
    public CsvWriter write(Collection<String[]> lines) throws InstrumentException {
        if (CollKit.isNotEmpty(lines)) {
            for (final String[] values : lines) {
                appendLine(values);
            }
            flush();
        }
        return this;
    }

    /**
     * 追加新行(换行)
     *
     * @throws InstrumentException IO异常
     */
    public void writeLine() throws InstrumentException {
        try {
            writer.write(config.lineDelimiter);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        newline = true;
    }

    @Override
    public void close() {
        IoKit.close(this.writer);
    }

    @Override
    public void flush() throws InstrumentException {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 追加一行,末尾会自动换行,但是追加前不会换行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @throws InstrumentException IO异常
     */
    private void appendLine(final String... fields) throws InstrumentException {
        try {
            doAppendLine(fields);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 追加一行,末尾会自动换行,但是追加前不会换行
     *
     * @param fields 字段列表 ({@code null} 值会被做为空值追加)
     * @throws IOException IO异常
     */
    private void doAppendLine(final String... fields) throws IOException {
        if (null != fields) {
            for (String field : fields) {
                appendField(field);
            }
            writer.write(config.lineDelimiter);
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
