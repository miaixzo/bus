/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.office.excel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.office.excel.cell.editors.CellEditor;

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
            return ListKit.zero();
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
