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
package org.miaixz.bus.office.excel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;
import org.miaixz.bus.office.excel.xyz.CellKit;

/**
 * Common configuration for Excel reading and writing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelConfig {

    /**
     * Alias map for header rows.
     */
    protected Map<String, String> headerAlias;
    /**
     * Cell value processing interface.
     */
    protected CellEditor cellEditor;

    /**
     * Gets the header row alias map.
     *
     * @return The alias map.
     */
    public Map<String, String> getHeaderAlias() {
        return headerAlias;
    }

    /**
     * Sets the header row alias map.
     *
     * @param headerAlias The alias map.
     * @return This {@code ExcelConfig} instance, for chaining.
     */
    public ExcelConfig setHeaderAlias(final Map<String, String> headerAlias) {
        this.headerAlias = headerAlias;
        return this;
    }

    /**
     * Adds a header alias.
     *
     * @param header The original header name.
     * @param alias  The alias for the header.
     * @return This {@code ExcelConfig} instance, for chaining.
     */
    public ExcelConfig addHeaderAlias(final String header, final String alias) {
        Map<String, String> headerAlias = this.headerAlias;
        if (null == headerAlias) {
            headerAlias = new LinkedHashMap<>();
            this.headerAlias = headerAlias;
        }
        headerAlias.put(header, alias);
        return this;
    }

    /**
     * Removes a header alias.
     *
     * @param header The header name whose alias is to be removed.
     * @return This {@code ExcelConfig} instance, for chaining.
     */
    public ExcelConfig removeHeaderAlias(final String header) {
        this.headerAlias.remove(header);
        return this;
    }

    /**
     * Clears all header aliases. The keys in the map are original header names, and values are aliases.
     *
     * @return This {@code ExcelConfig} instance, for chaining.
     */
    public ExcelConfig clearHeaderAlias() {
        return setHeaderAlias(null);
    }

    /**
     * Converts a list of original headers to their aliases. If an alias is not found, the original header is used. If a
     * header is {@code null}, the column index converted to an Excel column name (e.g., A, B) is used as the header.
     *
     * @param headerList The list of original headers.
     * @return A list of aliased headers.
     */
    public List<Object> aliasHeader(final List<Object> headerList) {
        if (CollKit.isEmpty(headerList)) {
            return new ArrayList<>(0);
        }

        final int size = headerList.size();
        final List<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(aliasHeader(headerList.get(i), i));
        }
        return result;
    }

    /**
     * Converts an original header to its alias. If an alias is not found, the original header is used. If the header is
     * {@code null}, the column index converted to an Excel column name (e.g., A, B) is used as the header.
     *
     * @param headerObj The original header object.
     * @param index     The column index of the header. Used when the header object is {@code null}.
     * @return The aliased header object.
     */
    public Object aliasHeader(final Object headerObj, final int index) {
        if (null == headerObj) {
            return CellKit.indexToColName(index);
        }

        if (null != this.headerAlias) {
            return ObjectKit.defaultIfNull(this.headerAlias.get(headerObj.toString()), headerObj);
        }
        return headerObj;
    }

    /**
     * Gets the cell value processor.
     *
     * @return The {@link CellEditor} instance.
     */
    public CellEditor getCellEditor() {
        return this.cellEditor;
    }

    /**
     * Sets the cell value processing logic. When the values in Excel do not meet the reading requirements, a custom
     * editor interface can be provided to customize cell values, for example, converting numeric and date types to
     * strings.
     *
     * @param cellEditor The cell value processing interface.
     * @return This {@code ExcelConfig} instance, for chaining.
     */
    public ExcelConfig setCellEditor(final CellEditor cellEditor) {
        this.cellEditor = cellEditor;
        return this;
    }

}
