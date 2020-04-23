/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric.xdsi;

import org.aoju.bus.image.Format;
import org.aoju.bus.image.galaxy.data.DatePrecision;

import java.util.*;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class SlotBuilder {

    private final SlotType result;

    public SlotBuilder(String name) {
        this.result = new SlotType();
        this.result.setName(name);
    }

    public SlotType build() {
        return this.result;
    }

    public SlotBuilder valueList(String value) {
        return valueList(Collections.singletonList(value));
    }

    public SlotBuilder valueList(Collection<String> values) {
        ValueListType valueList = new ValueListType();
        for (String value : values) {
            valueList.getValue().add(value);
        }
        this.result.setValueList(valueList);
        return this;
    }

    public SlotBuilder valueDTM(Date date) {
        return valueList(Format.formatDT(TimeZone.getTimeZone("UTC"), date, new DatePrecision(Calendar.SECOND)));
    }

    public SlotBuilder slotType(String value) {
        this.result.setSlotType(value);
        return this;
    }

}


