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

import java.util.*;

import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.center.list.ListWrapper;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.BeanKit;

/**
 * CSV中一行的表示
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class CsvRow extends ListWrapper<String> {

    final Map<String, Integer> headerMap;
    /**
     * 原始行号
     */
    private final long originalLineNumber;

    /**
     * 构造
     *
     * @param originalLineNumber 对应文件中的第几行
     * @param headerMap          标题Map
     * @param fields             数据列表
     */
    public CsvRow(final long originalLineNumber, final Map<String, Integer> headerMap, final List<String> fields) {
        super(Assert.notNull(fields, "fields must be not null!"));
        this.originalLineNumber = originalLineNumber;
        this.headerMap = headerMap;
    }

    /**
     * 获取原始行号，多行情况下为首行行号。忽略注释行
     *
     * @return the original line number 行号
     */
    public long getOriginalLineNumber() {
        return originalLineNumber;
    }

    /**
     * 获取标题对应的字段内容
     *
     * @param name 标题名
     * @return 字段值，null表示无此字段值
     * @throws IllegalStateException CSV文件无标题行抛出此异常
     */
    public String getByName(final String name) {
        Assert.notNull(this.headerMap, "No header available!");

        final Integer col = headerMap.get(name);
        if (col != null) {
            return get(col);
        }
        return null;
    }

    /**
     * 获取标题与字段值对应的Map
     *
     * @return 标题与字段值对应的Map
     * @throws IllegalStateException CSV文件无标题行抛出此异常
     */
    public Map<String, String> getFieldMap() {
        if (headerMap == null) {
            throw new IllegalStateException("No header available");
        }

        final Map<String, String> fieldMap = new LinkedHashMap<>(headerMap.size(), 1);
        String key;
        Integer col;
        String val;
        for (final Map.Entry<String, Integer> header : headerMap.entrySet()) {
            key = header.getKey();
            col = headerMap.get(key);
            val = null == col ? null : get(col);
            fieldMap.put(key, val);
        }

        return fieldMap;
    }

    /**
     * 一行数据转换为Bean对象，忽略转换错误
     *
     * @param <T>   Bean类型
     * @param clazz bean类
     * @return the object
     */
    public <T> T toBean(final Class<T> clazz) {
        return BeanKit.toBean(getFieldMap(), clazz, CopyOptions.of().setIgnoreError(true));
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return new HashSet<>(this.raw).containsAll(c);
    }

    @Override
    public String get(final int index) {
        return index >= size() ? null : super.get(index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CsvRow{");
        sb.append("originalLineNumber=");
        sb.append(originalLineNumber);
        sb.append(", ");

        sb.append("fields=");
        if (headerMap != null) {
            sb.append('{');
            for (final Iterator<Map.Entry<String, String>> it = getFieldMap().entrySet().iterator(); it.hasNext();) {

                final Map.Entry<String, String> entry = it.next();
                sb.append(entry.getKey());
                sb.append('=');
                if (entry.getValue() != null) {
                    sb.append(entry.getValue());
                }
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append('}');
        } else {
            sb.append(this.raw.toString());
        }

        sb.append('}');
        return sb.toString();
    }

}
