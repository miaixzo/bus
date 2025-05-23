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
import java.util.*;

import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.StringTrimer;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * CSV行解析器，参考：FastCSV
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class CsvParser extends ComputeIterator<CsvRow> implements Closeable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852262993113L;

    private static final int DEFAULT_ROW_CAPACITY = 10;

    private final CsvReadConfig config;
    private final CsvTokener tokener;
    /**
     * 当前读取字段
     */
    private final StringBuilder currentField = new StringBuilder(512);
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
    private long lineNo = -1;
    /**
     * 引号内的行数
     */
    private long inQuotesLineCount;
    /**
     * 第一行字段数，用于检查每行字段数是否一致
     */
    private int firstLineFieldCount = -1;
    /**
     * 最大字段数量，用于初始化行，减少扩容
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
     * @param config 配置，null则为默认配置
     */
    public CsvParser(final Reader reader, final CsvReadConfig config) {
        this.config = ObjectKit.defaultIfNull(config, CsvReadConfig::of);
        this.tokener = new CsvTokener(reader);
    }

    /**
     * 获取头部字段列表，如果headerLineNo &lt; 0，抛出异常
     *
     * @return 头部列表
     * @throws IllegalStateException 如果不解析头部或者没有调用nextRow()方法
     */
    public List<String> getHeader() {
        if (config.headerLineNo < 0) {
            throw new IllegalStateException("No header available - header parsing is disabled");
        }
        if (lineNo < config.beginLineNo) {
            throw new IllegalStateException("No header available - call nextRow() first");
        }
        return header.getRaw();
    }

    @Override
    protected CsvRow computeNext() {
        return nextRow();
    }

    /**
     * 读取下一行数据
     *
     * @return CsvRow
     * @throws InternalException IO读取异常
     */
    public CsvRow nextRow() throws InternalException {
        List<String> currentFields;
        int fieldCount;
        while (!finished) {
            currentFields = readLine();
            fieldCount = currentFields.size();
            if (fieldCount < 1) {
                // 空List表示读取结束
                break;
            }

            // 读取范围校验
            if (lineNo < config.beginLineNo) {
                // 未达到读取起始行，继续
                continue;
            }
            if (lineNo > config.endLineNo) {
                // 超出结束行，读取结束
                break;
            }

            // 跳过空行
            if (config.skipEmptyRows && fieldCount == 1 && currentFields.get(0).isEmpty()) {
                // [""]表示空行
                continue;
            }

            // 检查每行的字段数是否一致
            if (config.errorOnDifferentFieldCount) {
                if (firstLineFieldCount < 0) {
                    firstLineFieldCount = fieldCount;
                } else if (fieldCount != firstLineFieldCount) {
                    throw new InternalException(String.format("Line %d has %d fields, but first line has %d fields",
                            lineNo, fieldCount, firstLineFieldCount));
                }
            }

            // 记录最大字段数
            if (fieldCount > maxFieldCount) {
                maxFieldCount = fieldCount;
            }

            // 初始化标题
            if (lineNo == config.headerLineNo && null == header) {
                initHeader(currentFields);
                // 作为标题行后，此行跳过，下一行做为第一行
                continue;
            }

            return new CsvRow(lineNo, null == header ? null : header.headerMap, currentFields);
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
            String field = currentFields.get(i);
            if (MapKit.isNotEmpty(this.config.headerAlias)) {
                // 自定义别名
                field = ObjectKit.defaultIfNull(this.config.headerAlias.get(field), field);
            }
            if (StringKit.isNotEmpty(field) && !localHeaderMap.containsKey(field)) {
                localHeaderMap.put(field, i);
            }
        }

        header = new CsvRow(this.lineNo, Collections.unmodifiableMap(localHeaderMap),
                Collections.unmodifiableList(currentFields));
    }

    /**
     * 读取一行数据，如果读取结束，返回size为0的List 空行是size为1的List，唯一元素是""
     *
     * <p>
     * 行号要考虑注释行和引号包装的内容中的换行
     * </p>
     *
     * @return 一行数据
     * @throws InternalException IO异常
     */
    private List<String> readLine() throws InternalException {
        // 矫正行号
        // 当一行内容包含多行数据时，记录首行行号，但是读取下一行时，需要把多行内容的行数加上
        if (inQuotesLineCount > 0) {
            this.lineNo += this.inQuotesLineCount;
            this.inQuotesLineCount = 0;
        }

        final List<String> currentFields = new ArrayList<>(maxFieldCount > 0 ? maxFieldCount : DEFAULT_ROW_CAPACITY);

        final StringBuilder currentField = this.currentField;
        int preChar = this.preChar;// 前一个特殊分界字符
        boolean inComment = false;

        int c;
        while (true) {
            c = tokener.next();
            if (c < 0) {
                if (currentField.length() > 0 || preChar == config.fieldSeparator) {
                    if (this.inQuotes) {
                        // 未闭合的文本包装，在末尾补充包装符
                        currentField.append(config.textDelimiter);
                    }

                    // 剩余部分作为一个字段
                    addField(currentFields, currentField.toString());
                    currentField.setLength(0);
                }
                // 读取结束
                this.finished = true;
                break;
            }

            // 注释行标记
            if (preChar < 0 || preChar == Symbol.C_CR || preChar == Symbol.C_LF) {
                // 判断行首字符为指定注释字符的注释开始，直到遇到换行符
                // 行首分两种，1是preChar < 0表示文本开始，2是换行符后紧跟就是下一行的开始
                // 如果注释符出现在包装符内，被认为是普通字符
                if (!inQuotes && null != this.config.commentCharacter && c == this.config.commentCharacter) {
                    inComment = true;
                }
            }
            // 注释行处理
            if (inComment) {
                if (c == Symbol.C_CR || c == Symbol.C_LF) {
                    // 注释行以换行符为结尾
                    lineNo++;
                    inComment = false;
                }
                // 跳过注释行中的任何字符
                continue;
            }

            if (inQuotes) {
                // 引号内，作为内容，直到引号结束
                if (c == config.textDelimiter) {
                    // 文本包装符转义
                    final int next = tokener.next();
                    if (next != config.textDelimiter) {
                        // 包装结束
                        inQuotes = false;
                        tokener.back();
                    }
                    // https://datatracker.ietf.org/doc/html/rfc4180#section-2 跳过转义符，只保留被转义的包装符
                } else {
                    // 字段内容中新行
                    if (isLineEnd(c, preChar)) {
                        inQuotesLineCount++;
                    }
                }
                // 普通字段字符
                currentField.append((char) c);
            } else {
                // 非引号内
                if (c == config.fieldSeparator) {
                    // 一个字段结束
                    addField(currentFields, currentField.toString());
                    currentField.setLength(0);
                } else if (c == config.textDelimiter && isFieldBegin(preChar)) {
                    // 引号开始且出现在字段开头
                    inQuotes = true;
                    currentField.append((char) c);
                } else if (c == Symbol.C_CR) {
                    // \r
                    addField(currentFields, currentField.toString());
                    currentField.setLength(0);
                    preChar = c;
                    break;
                } else if (c == Symbol.C_LF) {
                    // \n
                    if (preChar != Symbol.C_CR) {
                        addField(currentFields, currentField.toString());
                        currentField.setLength(0);
                        preChar = c;
                        break;
                    }
                    // 前一个字符是\r，已经处理过这个字段了，此处直接跳过
                } else {
                    currentField.append((char) c);
                }
            }

            preChar = c;
        }

        // restore fields
        this.preChar = preChar;

        lineNo++;
        return currentFields;
    }

    @Override
    public void close() throws IOException {
        tokener.close();
    }

    /**
     * 将字段加入字段列表并自动去包装和去转义
     *
     * @param currentFields 当前的字段列表（即为行）
     * @param field         字段
     */
    private void addField(final List<String> currentFields, String field) {
        final char textDelimiter = this.config.textDelimiter;

        // 忽略多余引号后的换行符
        field = StringKit.trim(field, StringTrimer.TrimMode.SUFFIX, (c -> c == Symbol.C_LF || c == Symbol.C_CR));

        if (StringKit.isWrap(field, textDelimiter)) {
            field = StringKit.sub(field, 1, field.length() - 1);
        }
        if (this.config.trimField) {
            field = StringKit.trim(field);
        }
        currentFields.add(field);
    }

    /**
     * 是否行结束符
     *
     * @param c       符号
     * @param preChar 前一个字符
     * @return 是否结束
     */
    private boolean isLineEnd(final int c, final int preChar) {
        return (c == Symbol.C_CR || c == Symbol.C_LF) && preChar != Symbol.C_CR;
    }

    /**
     * 通过前一个字符，判断是否字段开始，几种情况：
     * <ul>
     * <li>正文开头，无前字符</li>
     * <li>字段分隔符，即上个字段结束</li>
     * <li>换行符，即新行开始</li>
     * </ul>
     *
     * @param preChar 前字符
     * @return 是否字段开始
     */
    private boolean isFieldBegin(final int preChar) {
        return preChar == -1 || preChar == config.fieldSeparator || preChar == Symbol.C_LF || preChar == Symbol.C_CR;
    }

}
