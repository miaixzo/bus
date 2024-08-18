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
package org.miaixz.bus.office.excel.cell.setters;

import java.io.File;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.miaixz.bus.office.excel.cell.CellSetter;

/**
 * {@link CellSetter} 简单静态工厂类，用于根据值类型创建对应的{@link CellSetter}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellSetterFactory {

    /**
     * 创建值对应类型的{@link CellSetter}
     *
     * @param value 值
     * @return {@link CellSetter}
     */
    public static CellSetter createCellSetter(final Object value) {
        if (null == value) {
            return NullCellSetter.INSTANCE;
        } else if (value instanceof CellSetter) {
            return (CellSetter) value;
        } else if (value instanceof Date) {
            return new DateCellSetter((Date) value);
        } else if (value instanceof TemporalAccessor) {
            return new TemporalAccessorCellSetter((TemporalAccessor) value);
        } else if (value instanceof Calendar) {
            return new CalendarCellSetter((Calendar) value);
        } else if (value instanceof Boolean) {
            return new BooleanCellSetter((Boolean) value);
        } else if (value instanceof RichTextString) {
            return new RichTextCellSetter((RichTextString) value);
        } else if (value instanceof Number) {
            return new NumberCellSetter((Number) value);
        } else if (value instanceof Hyperlink) {
            return new HyperlinkCellSetter((Hyperlink) value);
        } else if (value instanceof byte[]) {
            // 二进制理解为图片
            return new ImgCellSetter((byte[]) value);
        } else if (value instanceof File) {
            return new ImgCellSetter((File) value);
        } else {
            return new CharSequenceCellSetter(value.toString());
        }
    }

}
