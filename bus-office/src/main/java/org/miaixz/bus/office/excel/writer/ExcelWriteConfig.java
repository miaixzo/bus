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
package org.miaixz.bus.office.excel.writer;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.center.map.TableMap;
import org.miaixz.bus.core.center.map.multi.RowKeyTable;
import org.miaixz.bus.core.center.map.multi.Table;
import org.miaixz.bus.core.compare.IndexedCompare;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.ExcelConfig;

/**
 * Excel写出配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelWriteConfig extends ExcelConfig {

    /**
     * 是否只保留别名对应的字段
     */
    protected boolean onlyAlias;
    /**
     * 是否强制插入行 如果为{@code true}，则写入行以下的已存在行下移，{@code false}则利用填充已有行，不存在再创建行
     */
    protected boolean insertRow = true;
    /**
     * 标题顺序比较器
     */
    protected Comparator<String> aliasComparator;

    @Override
    public ExcelWriteConfig setHeaderAlias(final Map<String, String> headerAlias) {
        this.aliasComparator = null;
        return (ExcelWriteConfig) super.setHeaderAlias(headerAlias);
    }

    @Override
    public ExcelWriteConfig addHeaderAlias(final String header, final String alias) {
        this.aliasComparator = null;
        return (ExcelWriteConfig) super.addHeaderAlias(header, alias);
    }

    @Override
    public ExcelWriteConfig removeHeaderAlias(final String header) {
        this.aliasComparator = null;
        return (ExcelWriteConfig) super.removeHeaderAlias(header);
    }

    /**
     * 设置是否只保留别名中的字段值，如果为true，则不设置alias的字段将不被输出，false表示原样输出
     * Bean中设置@Alias时，setOnlyAlias是无效的，这个参数只和addHeaderAlias配合使用，原因是注解是Bean内部的操作，而addHeaderAlias是Writer的操作，不互通。
     *
     * @param isOnlyAlias 是否只保留别名中的字段值
     * @return this
     */
    public ExcelWriteConfig setOnlyAlias(final boolean isOnlyAlias) {
        this.onlyAlias = isOnlyAlias;
        return this;
    }

    /**
     * 设置是否插入行，如果为true，则写入行以下的已存在行下移，false则利用填充已有行，不存在时创建行
     *
     * @param insertRow 是否插入行
     * @return this
     */
    public ExcelWriteConfig setInsertRow(final boolean insertRow) {
        this.insertRow = insertRow;
        return this;
    }

    /**
     * 获取单例的别名比较器，比较器的顺序为别名加入的顺序
     *
     * @return {@link Comparator}
     */
    public Comparator<String> getCachedAliasComparator() {
        final Map<String, String> headerAlias = this.headerAlias;
        if (MapKit.isEmpty(headerAlias)) {
            return null;
        }
        Comparator<String> aliasComparator = this.aliasComparator;
        if (null == aliasComparator) {
            final Set<String> keySet = headerAlias.keySet();
            aliasComparator = new IndexedCompare<>(keySet.toArray(new String[0]));
            this.aliasComparator = aliasComparator;
        }
        return aliasComparator;
    }

    /**
     * 为指定的key列表添加标题别名，如果没有定义key的别名，在onlyAlias为false时使用原key key为别名，value为字段值
     *
     * @param rowMap 一行数据
     * @return 别名列表
     */
    public Table<?, ?, ?> aliasTable(final Map<?, ?> rowMap) {
        final Table<Object, Object, Object> filteredTable = new RowKeyTable<>(new LinkedHashMap<>(), TableMap::new);
        if (MapKit.isEmpty(headerAlias)) {
            rowMap.forEach((key, value) -> filteredTable.put(key, key, value));
        } else {
            rowMap.forEach((key, value) -> {
                final String aliasName = headerAlias.get(StringKit.toString(key));
                if (null != aliasName) {
                    // 别名键值对加入
                    filteredTable.put(key, aliasName, value);
                } else if (!onlyAlias) {
                    // 保留无别名设置的键值对
                    filteredTable.put(key, key, value);
                }
            });
        }

        return filteredTable;
    }

}
