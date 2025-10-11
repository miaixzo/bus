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
package org.miaixz.bus.office.excel.writer;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.center.map.TableMap;
import org.miaixz.bus.core.center.map.multiple.RowKeyTable;
import org.miaixz.bus.core.center.map.multiple.Table;
import org.miaixz.bus.core.compare.IndexedCompare;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.ExcelConfig;

/**
 * Excel write configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelWriteConfig extends ExcelConfig {

    /**
     * Whether to retain only fields corresponding to aliases. If {@code true}, fields without aliases will not be
     * output.
     */
    protected boolean onlyAlias;
    /**
     * Whether to force row insertion. If {@code true}, existing rows below the written row will be shifted down. If
     * {@code false}, existing rows will be filled, and new rows will be created only if necessary.
     */
    protected boolean insertRow = true;
    /**
     * Comparator for sorting header names.
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
     * Sets whether to retain only fields corresponding to aliases. If {@code true}, fields without aliases will not be
     * output. If {@code false}, they will be output as is. When {@code @Alias} is set in a Bean, {@code setOnlyAlias}
     * is ineffective. This parameter is only used in conjunction with {@code addHeaderAlias}, because annotations are
     * internal operations of the Bean, while {@code addHeaderAlias} is an operation of the Writer, and they do not
     * interoperate.
     *
     * @param isOnlyAlias {@code true} to retain only aliased fields, {@code false} to output all fields.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setOnlyAlias(final boolean isOnlyAlias) {
        this.onlyAlias = isOnlyAlias;
        return this;
    }

    /**
     * Sets whether to insert rows. If {@code true}, existing rows below the written row will be shifted down. If
     * {@code false}, existing rows will be filled, and new rows will be created only if necessary.
     *
     * @param insertRow {@code true} to insert rows, {@code false} to fill existing rows.
     * @return This {@code ExcelWriteConfig} instance, for chaining.
     */
    public ExcelWriteConfig setInsertRow(final boolean insertRow) {
        this.insertRow = insertRow;
        return this;
    }

    /**
     * Gets the singleton alias comparator. The order of comparison is the order in which aliases were added.
     *
     * @return The {@link Comparator} for aliases.
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
     * Creates an alias table for the specified key list. If no alias is defined for a key, the original key is used
     * when {@code onlyAlias} is {@code false}. The key of the table is the alias, and the value is the field value.
     *
     * @param rowMap A single row of data.
     * @return A {@link Table} representing the aliased data.
     */
    public Table<?, ?, ?> aliasTable(final Map<?, ?> rowMap) {
        final Table<Object, Object, Object> filteredTable = new RowKeyTable<>(new LinkedHashMap<>(), TableMap::new);
        if (MapKit.isEmpty(headerAlias)) {
            rowMap.forEach((key, value) -> filteredTable.put(key, key, value));
        } else {
            rowMap.forEach((key, value) -> {
                final String aliasName = headerAlias.get(StringKit.toString(key));
                if (null != aliasName) {
                    // Add alias key-value pair.
                    filteredTable.put(key, aliasName, value);
                } else if (!onlyAlias) {
                    // Retain key-value pairs without alias settings.
                    filteredTable.put(key, key, value);
                }
            });
        }

        return filteredTable;
    }

}
