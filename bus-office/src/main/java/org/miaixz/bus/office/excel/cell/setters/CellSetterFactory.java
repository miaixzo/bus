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
package org.miaixz.bus.office.excel.cell.setters;

import java.io.File;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;

/**
 * A simple static factory class for {@link CellSetter}, used to create corresponding {@link CellSetter} based on value
 * type.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CellSetterFactory {

    /**
     * Creates a {@link CellSetter} for the given value's type.
     *
     * @param value The value to be set in the cell.
     * @return A {@link CellSetter} instance corresponding to the value's type.
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
            // Binary data is interpreted as an image.
            return new PictureCellSetter((byte[]) value);
        } else if (value instanceof File) {
            return new PictureCellSetter((File) value);
        } else {
            return new CharSequenceCellSetter(value.toString());
        }
    }

}
