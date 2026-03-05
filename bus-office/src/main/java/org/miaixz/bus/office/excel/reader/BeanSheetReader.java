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
package org.miaixz.bus.office.excel.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.xyz.BeanKit;

/**
 * Reads an {@link Sheet} into a list of beans.
 *
 * @param <T> The type of the bean to convert each row to.
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanSheetReader<T> implements SheetReader<List<T>> {

    /**
     * The bean class to convert each row to.
     */
    private final Class<T> beanClass;
    /**
     * The map sheet reader used to read the sheet.
     */
    private final MapSheetReader mapSheetReader;

    /**
     * Constructs a new {@code BeanSheetReader}.
     *
     * @param headerRowIndex The row index where the header is located. If the header row is in the middle of the
     *                       content rows to be read, this row will be ignored as data.
     * @param startRowIndex  The starting row index (inclusive, 0-based).
     * @param endRowIndex    The ending row index (inclusive, 0-based).
     * @param beanClass      The type of the Bean corresponding to each row.
     */
    public BeanSheetReader(final int headerRowIndex, final int startRowIndex, final int endRowIndex,
            final Class<T> beanClass) {
        mapSheetReader = new MapSheetReader(headerRowIndex, startRowIndex, endRowIndex);
        this.beanClass = beanClass;
    }

    /**
     * Reads the sheet and converts each row into a bean of the specified type.
     *
     * @param sheet The {@link Sheet} to read.
     * @return A list of beans converted from the sheet rows.
     */
    @Override
    public List<T> read(final Sheet sheet) {
        final List<Map<Object, Object>> mapList = mapSheetReader.read(sheet);
        if (Map.class.isAssignableFrom(this.beanClass)) {
            return (List<T>) mapList;
        }

        final List<T> beanList = new ArrayList<>(mapList.size());
        final CopyOptions copyOptions = CopyOptions.of().setIgnoreError(true);
        for (final Map<Object, Object> map : mapList) {
            beanList.add(BeanKit.toBean(map, this.beanClass, copyOptions));
        }
        return beanList;
    }

    /**
     * Sets the Excel read configuration.
     *
     * @param config The Excel read configuration.
     */
    public void setExcelConfig(final ExcelReadConfig config) {
        this.mapSheetReader.setExcelConfig(config);
    }

}
