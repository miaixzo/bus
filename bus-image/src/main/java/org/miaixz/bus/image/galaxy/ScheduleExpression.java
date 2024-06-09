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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.core.lang.Symbol;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ScheduleExpression {

    private String dayOfWeek = Symbol.STAR;
    private String hour = Symbol.STAR;
    private int dayOfWeeks = -1;
    private int hours = -1;

    public static ScheduleExpression valueOf(String s) {
        ScheduleExpression result = new ScheduleExpression();
        for (String s1 : Property.split(s, Symbol.C_SPACE)) {
            if (s1.startsWith("hour="))
                try {
                    result.hour(s1.substring(5));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(s);
                }
            else if (s1.startsWith("dayOfWeek="))
                try {
                    result.dayOfWeek(s1.substring(10));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(s);
                }
            else
                throw new IllegalArgumentException(s);
        }
        return result;
    }

    public static ScheduleExpression[] valuesOf(String... ss) {
        ScheduleExpression[] schedules = new ScheduleExpression[ss.length];
        for (int i = 0; i < ss.length; i++)
            schedules[i] = ScheduleExpression.valueOf(ss[i]);
        return schedules;
    }

    public static boolean emptyOrAnyContains(Calendar cal, ScheduleExpression... expressions) {
        return expressions.length == 0 || Stream.of(expressions).anyMatch(expr -> expr.contains(cal));
    }

    public static Calendar ceil(Calendar cal, ScheduleExpression... schedules) {
        if (schedules.length == 0)
            return cal;
        for (ScheduleExpression schedule : schedules)
            if (schedule.contains(cal))
                return cal;
        Calendar cal2 = null;
        for (ScheduleExpression schedule : schedules) {
            Calendar cal3 = schedule.ceil(cal);
            if (null == cal2 || cal2.compareTo(cal3) > 0)
                cal2 = cal3;
        }
        return cal2;
    }

    public ScheduleExpression dayOfWeek(String s) {
        dayOfWeeks = parse(s, 7);
        dayOfWeek = s;
        return this;
    }

    public ScheduleExpression hour(String s) {
        hours = parse(s, 24);
        hour = s;
        return this;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getHour() {
        return hour;
    }

    public boolean contains(Calendar cal) {
        return containsHour(cal) && containsDayOfWeek(cal);
    }

    public Calendar ceil(Calendar cal) {
        if (contains(cal))
            return cal;
        Calendar cal2 = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
        if (!containsDayOfWeek(cal2))
            do
                cal2.add(Calendar.DAY_OF_WEEK, 1);
            while (!containsDayOfWeek(cal2));
        else
            cal2.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        while (!containsHour(cal2))
            cal2.add(Calendar.HOUR_OF_DAY, 1);
        return cal2;
    }

    private boolean containsDayOfWeek(Calendar cal) {
        return ((1 << (cal.get(Calendar.DAY_OF_WEEK) - 1)) & dayOfWeeks) != 0;
    }

    private boolean containsHour(Calendar cal) {
        return ((1 << cal.get(Calendar.HOUR_OF_DAY)) & hours) != 0;
    }

    private int parse(String s, int m) {
        if (s.equals(Symbol.STAR))
            return -1;
        int result = -1 << m;
        for (String s1 : Property.split(s, Symbol.C_COMMA)) {
            String[] range = Property.split(s1, Symbol.C_MINUS);
            if (range.length > 2)
                throw new IllegalArgumentException(s);
            try {
                if (range.length == 1)
                    result |= 1 << parseInt(s1, m);
                else
                    for (int i = parseInt(range[0], m), n = parseInt(range[1], m); i != n; i = (i + 1) % m)
                        result |= 1 << i;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(s);
            }
        }
        return result;
    }

    private int parseInt(String s, int m) {
        int i = Integer.parseInt(s);
        if (i < 0 || i >= m)
            throw new IllegalArgumentException(s);
        return i;
    }

    @Override
    public String toString() {
        return "hour=" + hour + " dayOfWeek=" + dayOfWeek;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        ScheduleExpression that = (ScheduleExpression) o;
        return hours == that.hours && dayOfWeeks == that.dayOfWeeks;
    }

    @Override
    public int hashCode() {
        int result = dayOfWeeks;
        result = 31 * result + hours;
        return 31 * hours + dayOfWeeks;
    }

}
