/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.text.csv;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * CSV行解析器,参考：FastCSV
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public final class CsvParser implements Closeable {

    private static final int DEFAULT_ROW_CAPACITY = 10;

    private final Reader reader;
    private final CsvReadConfig config;

    private final char[] buf = new char[IoUtils.DEFAULT_LARGE_BUFFER_SIZE];
    /**
     * 当前读取字段
     */
    private final TextUtils currentField = new TextUtils(512);
    /**
     * 当前位置
     */
    private int bufPos;
    /**
     * 读取一段后数据长度
     */
    private int bufLen;
    /**
     * 拷贝开始的位置,一般为上一行的结束位置
     */
    private int copyStart;
    /**
     * 前一个特殊分界字符
     */
    private int preChar = -1;
    /**
     * 是否在引号包装内
     */
    private boolean inQuotes;
    /**
     * 标题行
     */
    private CsvRow header;
    /**
     * 当前行号
     */
    private long lineNo;
    /**
     * 第一行字段数,用于检查每行字段数是否一致
     */
    private int firstLineFieldCount = -1;
    /**
     * 最大字段数量
     */
    private int maxFieldCount;
    /**
     * 是否读取结束
     */
    private boolean finished;

    /**
     * CSV解析器
     *
     * @param reader Reader
     * @param config 配置,null则为默认配置
     */
    public CsvParser(final Reader reader, CsvReadConfig config) {
        this.reader = Objects.requireNonNull(reader, "reader must not be null");
        this.config = ObjectUtils.defaultIfNull(config, CsvReadConfig.defaultConfig());
    }

    /**
     * 获取头部字段列表,如果containsHeader设置为false则抛出异常
     *
     * @return 头部列表
     * @throws IllegalStateException 如果不解析头部或者没有调用nextRow()方法
     */
    public List<String> getHeader() {
        if (false == config.containsHeader) {
            throw new IllegalStateException("No header available - header parsing is disabled");
        }
        if (lineNo == 0) {
            throw new IllegalStateException("No header available - call nextRow() first");
        }
        return header.fields;
    }

    /**
     * 读取下一行数据
     *
     * @return CsvRow
     * @throws InstrumentException IO读取异常
     */
    public CsvRow nextRow() throws InstrumentException {
        long startingLineNo;
        List<String> currentFields;
        int fieldCount;
        while (false == finished) {
            startingLineNo = ++lineNo;
            currentFields = readLine();
            if (null == currentFields) {
                break;
            }
            fieldCount = currentFields.size();
            // 末尾
            if (fieldCount == 0) {
                break;
            }

            // 跳过空行
            if (config.skipEmptyRows && fieldCount == 1 && currentFields.get(0).isEmpty()) {
                continue;
            }

            // 检查每行的字段数是否一致
            if (config.errorOnDifferentFieldCount) {
                if (firstLineFieldCount == -1) {
                    firstLineFieldCount = fieldCount;
                } else if (fieldCount != firstLineFieldCount) {
                    throw new InstrumentException(String.format("Line %d has %d fields, but first line has %d fields", lineNo, fieldCount, firstLineFieldCount));
                }
            }

            // 记录最大字段数
            if (fieldCount > maxFieldCount) {
                maxFieldCount = fieldCount;
            }

            //初始化标题
            if (config.containsHeader && null == header) {
                initHeader(currentFields);
                // 作为标题行后,此行跳过,下一行做为第一行
                continue;
            }

            return new CsvRow(startingLineNo, null == header ? null : header.headerMap, currentFields);
        }

        return null;
    }

    /**
     * 当前行做为标题行
     *
     * @param currentFields 当前行字段列表
     */
    private void initHeader(final List<String> currentFields) {
        final Map<String, Integer> localHeaderMap = new LinkedHashMap<>(currentFields.size());
        for (int i = 0; i < currentFields.size(); i++) {
            final String field = currentFields.get(i);
            if (StringUtils.isNotEmpty(field) && false == localHeaderMap.containsKey(field)) {
                localHeaderMap.put(field, i);
            }
        }

        header = new CsvRow(this.lineNo, Collections.unmodifiableMap(localHeaderMap), Collections.unmodifiableList(currentFields));
    }

    /**
     * 读取一行数据
     *
     * @return 一行数据
     * @throws InstrumentException IO异常
     */
    private List<String> readLine() throws InstrumentException {
        final List<String> currentFields = new ArrayList<>(maxFieldCount > 0 ? maxFieldCount : DEFAULT_ROW_CAPACITY);

        final TextUtils localCurrentField = currentField;
        final char[] localBuf = this.buf;
        int localBufPos = bufPos;//当前位置
        int localPreChar = preChar;//前一个特殊分界字符
        int localCopyStart = copyStart;//拷贝起始位置
        int copyLen = 0; //拷贝长度

        while (true) {
            if (bufLen == localBufPos) {
                // 此Buffer读取结束,开始读取下一段

                if (copyLen > 0) {
                    localCurrentField.append(localBuf, localCopyStart, copyLen);
                }
                try {
                    bufLen = reader.read(localBuf);
                } catch (IOException e) {
                    throw new InstrumentException(e);
                }

                if (bufLen < 0) {
                    // CSV读取结束
                    finished = true;

                    if (localPreChar == config.fieldSeparator || localCurrentField.hasContent()) {
                        //剩余部分作为一个字段
                        currentFields.add(localCurrentField.toStringAndReset());
                    }
                    break;
                }

                //重置
                localCopyStart = localBufPos = copyLen = 0;
            }

            final char c = localBuf[localBufPos++];

            if (inQuotes) {
                //引号内,做为内容,直到引号结束
                if (c == config.textDelimiter) {
                    // End of quoted text
                    inQuotes = false;
                } else {
                    if ((c == Symbol.C_CR || c == Symbol.C_LF) && localPreChar != Symbol.C_CR) {
                        lineNo++;
                    }
                }
                copyLen++;
            } else {
                if (c == config.fieldSeparator) {
                    //一个字段结束
                    if (copyLen > 0) {
                        localCurrentField.append(localBuf, localCopyStart, copyLen);
                        copyLen = 0;
                    }
                    currentFields.add(StringUtils.unWrap(localCurrentField.toStringAndReset(), config.textDelimiter));
                    localCopyStart = localBufPos;
                } else if (c == config.textDelimiter) {
                    // 引号开始
                    inQuotes = true;
                    copyLen++;
                } else if (c == Symbol.C_CR) {
                    if (copyLen > 0) {
                        localCurrentField.append(localBuf, localCopyStart, copyLen);
                    }
                    currentFields.add(StringUtils.unWrap(localCurrentField.toStringAndReset(), config.textDelimiter));
                    localPreChar = c;
                    localCopyStart = localBufPos;
                    break;
                } else if (c == Symbol.C_LF) {
                    if (localPreChar != Symbol.C_CR) {
                        if (copyLen > 0) {
                            localCurrentField.append(localBuf, localCopyStart, copyLen);
                        }
                        currentFields.add(StringUtils.unWrap(localCurrentField.toStringAndReset(), config.textDelimiter));
                        localPreChar = c;
                        localCopyStart = localBufPos;
                        break;
                    }
                    localCopyStart = localBufPos;
                } else {
                    copyLen++;
                }
            }

            localPreChar = c;
        }

        // restore fields
        bufPos = localBufPos;
        preChar = localPreChar;
        copyStart = localCopyStart;

        return currentFields;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
