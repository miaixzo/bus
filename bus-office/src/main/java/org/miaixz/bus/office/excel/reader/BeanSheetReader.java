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
