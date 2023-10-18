/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.date.calendar;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认日历
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class NonWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日历中所有的年
     */
    private List<YearWrapper> years;

    /**
     * 日历中所有的天map，方便快速访问，key 格式：yyyy-MM-dd
     */
    private Map<String, DayWrapper> dayMap = new ConcurrentHashMap<>();

    /**
     * 日历中所有的天list，方便顺序遍历访问
     */
    private List<DayWrapper> dayList = new ArrayList<>();

    public NonWrapper() {

    }

    public NonWrapper(List<YearWrapper> years) {
        this.years = years;
    }

    public NonWrapper(List<YearWrapper> years, Map<String, DayWrapper> dayMap, List<DayWrapper> dayList) {
        this.years = years;
        this.dayMap = dayMap;
        this.dayList = dayList;
    }

}
