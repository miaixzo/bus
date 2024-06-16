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
package org.miaixz.bus.cron;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

/**
 * 类似unix cron表达式提供解析器和执行器
 * Crontab表达式提供了指定复杂时间组合的能力
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Expression implements Serializable, Cloneable {

    public static final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 100;
    protected static final int SECOND = 0;
    protected static final int MINUTE = 1;
    protected static final int HOUR = 2;
    protected static final int DAY_OF_MONTH = 3;
    protected static final int MONTH = 4;
    protected static final int DAY_OF_WEEK = 5;
    protected static final int YEAR = 6;
    protected static final int ALL_SPEC_INT = 99; // '*'
    protected static final int NO_SPEC_INT = 98; // '?'
    protected static final Integer ALL_SPEC = ALL_SPEC_INT;
    protected static final Integer NO_SPEC = NO_SPEC_INT;
    protected static final Map<String, Integer> monthMap = new HashMap<>(20);
    protected static final Map<String, Integer> dayMap = new HashMap<>(60);
    private static final long serialVersionUID = 1L;

    static {
        monthMap.put("JAN", 0);
        monthMap.put("FEB", 1);
        monthMap.put("MAR", 2);
        monthMap.put("APR", 3);
        monthMap.put("MAY", 4);
        monthMap.put("JUN", 5);
        monthMap.put("JUL", 6);
        monthMap.put("AUG", 7);
        monthMap.put("SEP", 8);
        monthMap.put("OCT", 9);
        monthMap.put("NOV", 10);
        monthMap.put("DEC", 11);

        dayMap.put("SUN", 1);
        dayMap.put("MON", 2);
        dayMap.put("TUE", 3);
        dayMap.put("WED", 4);
        dayMap.put("THU", 5);
        dayMap.put("FRI", 6);
        dayMap.put("SAT", 7);
    }

    private final String cronExpression;
    protected transient TreeSet<Integer> seconds;
    protected transient TreeSet<Integer> minutes;
    protected transient TreeSet<Integer> hours;
    protected transient TreeSet<Integer> daysOfMonth;
    protected transient TreeSet<Integer> months;
    protected transient TreeSet<Integer> daysOfWeek;
    protected transient TreeSet<Integer> years;

    protected transient boolean lastdayOfWeek = false;
    protected transient int nthdayOfWeek = 0;
    protected transient boolean lastdayOfMonth = false;
    protected transient boolean nearestWeekday = false;
    protected transient int lastdayOffset = 0;
    protected transient boolean expressionParsed = false;
    private TimeZone timeZone = null;

    /**
     * 根据指定的参数构造一个新的<code> CronExpression </code>
     *
     * @param cronExpression 新对象应表示的cron表达式的字符串表示形式
     * @throws ParseException 如果字符串表达式不能解析为有效的<code> CronExpression </code>
     */
    public Expression(String cronExpression) throws ParseException {
        if (null == cronExpression) {
            throw new IllegalArgumentException("cronExpression cannot be null");
        }

        this.cronExpression = cronExpression.toUpperCase(Locale.US);

        buildExpression(this.cronExpression);
    }

    /**
     * 构造一个新的{@code CronExpression}作为现有*实例的副本
     *
     * @param expression 要复制的现有cron表达式
     */
    public Expression(Expression expression) {
        this.cronExpression = expression.getCronExpression();
        try {
            buildExpression(cronExpression);
        } catch (ParseException ex) {
            throw new AssertionError();
        }
        if (null != expression.getTimeZone()) {
            setTimeZone((TimeZone) expression.getTimeZone().clone());
        }
    }

    /**
     * 指示是否可以将指定的cron表达式解析为有效的cron表达式
     *
     * @param cronExpression 要评估的表达式
     * @return 指示给定表达式是否为有效cron表达式的布尔值
     */
    public static boolean isValidExpression(String cronExpression) {

        try {
            new Expression(cronExpression);
        } catch (ParseException pe) {
            return false;
        }

        return true;
    }

    public static void validateExpression(String cronExpression) throws ParseException {

        new Expression(cronExpression);
    }

    /**
     * 指示给定的日期是否满足cron表达式
     * 请注意，毫秒将被忽略，因此两个日期在同一秒的不同毫秒
     * 处始终具有相同的结果
     *
     * @param date 评估日期
     * @return 指示给定日期是否满足cron 表达式的布尔值
     */
    public boolean isSatisfiedBy(Date date) {
        Calendar testDateCal = Calendar.getInstance(getTimeZone());
        testDateCal.setTime(date);
        testDateCal.set(Calendar.MILLISECOND, 0);
        Date originalDate = testDateCal.getTime();

        testDateCal.add(Calendar.SECOND, -1);

        Date timeAfter = getTimeAfter(testDateCal.getTime());

        return ((null != timeAfter) && (timeAfter.equals(originalDate)));
    }

    public Date getNextValidTimeAfter(Date date) {
        return getTimeAfter(date);
    }

    public Date getNextInvalidTimeAfter(Date date) {
        long difference = 1000;

        Calendar adjustCal = Calendar.getInstance(getTimeZone());
        adjustCal.setTime(date);
        adjustCal.set(Calendar.MILLISECOND, 0);
        Date lastDate = adjustCal.getTime();
        Date newDate;
        while (difference == 1000) {
            newDate = getTimeAfter(lastDate);
            if (null == newDate)
                break;

            difference = newDate.getTime() - lastDate.getTime();

            if (difference == 1000) {
                lastDate = newDate;
            }
        }

        return new Date(lastDate.getTime() + 1000);
    }

    public TimeZone getTimeZone() {
        if (null == timeZone) {
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return cronExpression;
    }

    protected void buildExpression(String expression) throws ParseException {
        expressionParsed = true;

        try {

            if (null == seconds) {
                seconds = new TreeSet<>();
            }
            if (null == minutes) {
                minutes = new TreeSet<>();
            }
            if (null == hours) {
                hours = new TreeSet<>();
            }
            if (null == daysOfMonth) {
                daysOfMonth = new TreeSet<>();
            }
            if (null == months) {
                months = new TreeSet<>();
            }
            if (null == daysOfWeek) {
                daysOfWeek = new TreeSet<>();
            }
            if (null == years) {
                years = new TreeSet<>();
            }

            int exprOn = SECOND;

            StringTokenizer exprsTok = new StringTokenizer(expression, " \t",
                    false);

            while (exprsTok.hasMoreTokens() && exprOn <= YEAR) {
                String expr = exprsTok.nextToken().trim();

                // throw an exception if L is used with other days of the month
                if (exprOn == DAY_OF_MONTH && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(Symbol.COMMA)) {
                    throw new ParseException("Support for specifying 'L' and 'LW' with other days of the month is not implemented", -1);
                }
                // throw an exception if L is used with other days of the week
                if (exprOn == DAY_OF_WEEK && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(Symbol.COMMA)) {
                    throw new ParseException("Support for specifying 'L' with other days of the week is not implemented", -1);
                }
                if (exprOn == DAY_OF_WEEK && expr.indexOf(Symbol.C_SHAPE) != -1 && expr.indexOf(Symbol.C_SHAPE, expr.indexOf(Symbol.C_SHAPE) + 1) != -1) {
                    throw new ParseException("Support for specifying multiple \"nth\" days is not implemented.", -1);
                }

                StringTokenizer vTok = new StringTokenizer(expr, Symbol.COMMA);
                while (vTok.hasMoreTokens()) {
                    String v = vTok.nextToken();
                    storeExpressionVals(0, v, exprOn);
                }

                exprOn++;
            }

            if (exprOn <= DAY_OF_WEEK) {
                throw new ParseException("Unexpected end of expression.",
                        expression.length());
            }

            if (exprOn <= YEAR) {
                storeExpressionVals(0, Symbol.STAR, YEAR);
            }

            TreeSet<Integer> dow = getSet(DAY_OF_WEEK);
            TreeSet<Integer> dom = getSet(DAY_OF_MONTH);

            // Copying the logic from the UnsupportedOperationException below
            boolean dayOfMSpec = !dom.contains(NO_SPEC);
            boolean dayOfWSpec = !dow.contains(NO_SPEC);

            if (!dayOfMSpec || dayOfWSpec) {
                if (!dayOfWSpec || dayOfMSpec) {
                    throw new ParseException(
                            "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.", 0);
                }
            }
        } catch (ParseException pe) {
            throw pe;
        } catch (Exception e) {
            throw new ParseException("Illegal cron expression format (" + e + ")", 0);
        }
    }

    protected int storeExpressionVals(int pos, String s, int type)
            throws ParseException {

        int incr = 0;
        int i = skipWhiteSpace(pos, s);
        if (i >= s.length()) {
            return i;
        }
        char c = s.charAt(i);
        if ((c >= 'A') && (c <= 'Z') && (!s.equals("L")) && (!s.equals("LW")) && (!s.matches("^L-[0-9]*[W]?"))) {
            String sub = s.substring(i, i + 3);
            int sval = -1;
            int eval = -1;
            if (type == MONTH) {
                sval = getMonthNumber(sub) + 1;
                if (sval <= 0) {
                    throw new ParseException("Invalid Month value: '" + sub + "'", i);
                }
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == Symbol.C_MINUS) {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getMonthNumber(sub) + 1;
                        if (eval <= 0) {
                            throw new ParseException("Invalid Month value: '" + sub + "'", i);
                        }
                    }
                }
            } else if (type == DAY_OF_WEEK) {
                sval = getDayOfWeekNumber(sub);
                if (sval < 0) {
                    throw new ParseException("Invalid Day-of-Week value: '"
                            + sub + "'", i);
                }
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == Symbol.C_MINUS) {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getDayOfWeekNumber(sub);
                        if (eval < 0) {
                            throw new ParseException(
                                    "Invalid Day-of-Week value: '" + sub
                                            + "'", i);
                        }
                    } else if (c == Symbol.C_SHAPE) {
                        try {
                            i += 4;
                            nthdayOfWeek = Integer.parseInt(s.substring(i));
                            if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            throw new ParseException(
                                    "A numeric value between 1 and 5 must follow the '#' option",
                                    i);
                        }
                    } else if (c == 'L') {
                        lastdayOfWeek = true;
                        i++;
                    }
                }

            } else {
                throw new ParseException(
                        "Illegal characters for this position: '" + sub + "'",
                        i);
            }
            if (eval != -1) {
                incr = 1;
            }
            addToSet(sval, eval, incr, type);
            return (i + 3);
        }

        if (c == Symbol.C_QUESTION_MARK) {
            i++;
            if ((i + 1) < s.length()
                    && (s.charAt(i) != Symbol.C_SPACE && s.charAt(i + 1) != Symbol.C_HT)) {
                throw new ParseException("Illegal character after '?': "
                        + s.charAt(i), i);
            }
            if (type != DAY_OF_WEEK && type != DAY_OF_MONTH) {
                throw new ParseException(
                        "'?' can only be specified for Day-of-Month or Day-of-Week.",
                        i);
            }
            if (type == DAY_OF_WEEK && !lastdayOfMonth) {
                int val = daysOfMonth.last();
                if (val == NO_SPEC_INT) {
                    throw new ParseException(
                            "'?' can only be specified for Day-of-Month -OR- Day-of-Week.",
                            i);
                }
            }

            addToSet(NO_SPEC_INT, -1, 0, type);
            return i;
        }

        if (c == Symbol.C_STAR || c == Symbol.C_SLASH) {
            if (c == Symbol.C_STAR && (i + 1) >= s.length()) {
                addToSet(ALL_SPEC_INT, -1, incr, type);
                return i + 1;
            } else if (c == Symbol.C_SLASH
                    && ((i + 1) >= s.length() || s.charAt(i + 1) == Symbol.C_SPACE || s
                    .charAt(i + 1) == Symbol.C_HT)) {
                throw new ParseException("'/' must be followed by an integer.", i);
            } else if (c == Symbol.C_STAR) {
                i++;
            }
            c = s.charAt(i);
            if (c == Symbol.C_SLASH) { // is an increment specified?
                i++;
                if (i >= s.length()) {
                    throw new ParseException("Unexpected end of string.", i);
                }

                incr = getNumericValue(s, i);

                i++;
                if (incr > 10) {
                    i++;
                }
                checkIncrementRange(incr, type, i);
            } else {
                incr = 1;
            }

            addToSet(ALL_SPEC_INT, -1, incr, type);
            return i;
        } else if (c == 'L') {
            i++;
            if (type == DAY_OF_MONTH) {
                lastdayOfMonth = true;
            }
            if (type == DAY_OF_WEEK) {
                addToSet(7, 7, 0, type);
            }
            if (type == DAY_OF_MONTH && s.length() > i) {
                c = s.charAt(i);
                if (c == Symbol.C_MINUS) {
                    ValueSet vs = getValue(0, s, i + 1);
                    lastdayOffset = vs.value;
                    if (lastdayOffset > 30)
                        throw new ParseException("Offset from last day must be <= 30", i + 1);
                    i = vs.pos;
                }
                if (s.length() > i) {
                    c = s.charAt(i);
                    if (c == 'W') {
                        nearestWeekday = true;
                        i++;
                    }
                }
            }
            return i;
        } else if (c >= '0' && c <= '9') {
            int val = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                addToSet(val, -1, -1, type);
            } else {
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(val, s, i);
                    val = vs.value;
                    i = vs.pos;
                }
                i = checkNext(i, s, val, type);
                return i;
            }
        } else {
            throw new ParseException("Unexpected character: " + c, i);
        }

        return i;
    }

    private void checkIncrementRange(int incr, int type, int idxPos) throws ParseException {
        if (incr > 59 && (type == SECOND || type == MINUTE)) {
            throw new ParseException("Increment > 60 : " + incr, idxPos);
        } else if (incr > 23 && (type == HOUR)) {
            throw new ParseException("Increment > 24 : " + incr, idxPos);
        } else if (incr > 31 && (type == DAY_OF_MONTH)) {
            throw new ParseException("Increment > 31 : " + incr, idxPos);
        } else if (incr > 7 && (type == DAY_OF_WEEK)) {
            throw new ParseException("Increment > 7 : " + incr, idxPos);
        } else if (incr > 12 && (type == MONTH)) {
            throw new ParseException("Increment > 12 : " + incr, idxPos);
        }
    }

    protected int checkNext(int pos, String s, int val, int type)
            throws ParseException {

        int end = -1;
        int i = pos;

        if (i >= s.length()) {
            addToSet(val, end, -1, type);
            return i;
        }

        char c = s.charAt(pos);

        if (c == 'L') {
            if (type == DAY_OF_WEEK) {
                if (val < 1 || val > 7)
                    throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
                lastdayOfWeek = true;
            } else {
                throw new ParseException("'L' option is not valid here. (pos=" + i + ")", i);
            }
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == 'W') {
            if (type == DAY_OF_MONTH) {
                nearestWeekday = true;
            } else {
                throw new ParseException("'W' option is not valid here. (pos=" + i + ")", i);
            }
            if (val > 31)
                throw new ParseException("The 'W' option does not make sense with values larger than 31 (max number of days in a month)", i);
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == Symbol.C_SHAPE) {
            if (type != DAY_OF_WEEK) {
                throw new ParseException("'#' option is not valid here. (pos=" + i + ")", i);
            }
            i++;
            try {
                nthdayOfWeek = Integer.parseInt(s.substring(i));
                if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", i);
            }

            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == Symbol.C_MINUS) {
            i++;
            c = s.charAt(i);
            int v = Integer.parseInt(String.valueOf(c));
            end = v;
            i++;
            if (i >= s.length()) {
                addToSet(val, end, 1, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v, s, i);
                end = vs.value;
                i = vs.pos;
            }
            if (i < s.length() && ((c = s.charAt(i)) == Symbol.C_SLASH)) {
                i++;
                c = s.charAt(i);
                int v2 = Integer.parseInt(String.valueOf(c));
                i++;
                if (i >= s.length()) {
                    addToSet(val, end, v2, type);
                    return i;
                }
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(v2, s, i);
                    int v3 = vs.value;
                    addToSet(val, end, v3, type);
                    i = vs.pos;
                    return i;
                } else {
                    addToSet(val, end, v2, type);
                    return i;
                }
            } else {
                addToSet(val, end, 1, type);
                return i;
            }
        }

        if (c == Symbol.C_SLASH) {
            if ((i + 1) >= s.length() || s.charAt(i + 1) == Symbol.C_SPACE || s.charAt(i + 1) == Symbol.C_HT) {
                throw new ParseException("'/' must be followed by an integer.", i);
            }

            i++;
            c = s.charAt(i);
            int v2 = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                checkIncrementRange(v2, type, i);
                addToSet(val, end, v2, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v2, s, i);
                int v3 = vs.value;
                checkIncrementRange(v3, type, i);
                addToSet(val, end, v3, type);
                i = vs.pos;
                return i;
            } else {
                throw new ParseException("Unexpected character '" + c + "' after '/'", i);
            }
        }

        addToSet(val, end, 0, type);
        i++;
        return i;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getExpressionSummary() {
        StringBuilder buf = new StringBuilder();

        buf.append("seconds: ");
        buf.append(getExpressionSetSummary(seconds));
        buf.append(Symbol.LF);
        buf.append("minutes: ");
        buf.append(getExpressionSetSummary(minutes));
        buf.append(Symbol.LF);
        buf.append("hours: ");
        buf.append(getExpressionSetSummary(hours));
        buf.append(Symbol.LF);
        buf.append("daysOfMonth: ");
        buf.append(getExpressionSetSummary(daysOfMonth));
        buf.append(Symbol.LF);
        buf.append("months: ");
        buf.append(getExpressionSetSummary(months));
        buf.append(Symbol.LF);
        buf.append("daysOfWeek: ");
        buf.append(getExpressionSetSummary(daysOfWeek));
        buf.append(Symbol.LF);
        buf.append("lastdayOfWeek: ");
        buf.append(lastdayOfWeek);
        buf.append(Symbol.LF);
        buf.append("nearestWeekday: ");
        buf.append(nearestWeekday);
        buf.append(Symbol.LF);
        buf.append("NthDayOfWeek: ");
        buf.append(nthdayOfWeek);
        buf.append(Symbol.LF);
        buf.append("lastdayOfMonth: ");
        buf.append(lastdayOfMonth);
        buf.append(Symbol.LF);
        buf.append("years: ");
        buf.append(getExpressionSetSummary(years));
        buf.append(Symbol.LF);

        return buf.toString();
    }

    protected String getExpressionSetSummary(java.util.Set<Integer> set) {

        if (set.contains(NO_SPEC)) {
            return Symbol.QUESTION_MARK;
        }
        if (set.contains(ALL_SPEC)) {
            return Symbol.STAR;
        }

        StringBuilder buf = new StringBuilder();

        Iterator<Integer> itr = set.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(Symbol.COMMA);
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    protected String getExpressionSetSummary(java.util.ArrayList<Integer> list) {

        if (list.contains(NO_SPEC)) {
            return Symbol.QUESTION_MARK;
        }
        if (list.contains(ALL_SPEC)) {
            return Symbol.STAR;
        }

        StringBuilder buf = new StringBuilder();

        Iterator<Integer> itr = list.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(Symbol.COMMA);
            }
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    protected int skipWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) == Symbol.C_SPACE || s.charAt(i) == Symbol.C_HT); i++) {
        }

        return i;
    }

    protected int findNextWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) != Symbol.C_SPACE || s.charAt(i) != Symbol.C_HT); i++) {
        }

        return i;
    }

    protected void addToSet(int val, int end, int incr, int type)
            throws ParseException {

        TreeSet<Integer> set = getSet(type);

        if (type == SECOND || type == MINUTE) {
            if ((val < 0 || val > 59 || end > 59) && (val != ALL_SPEC_INT)) {
                throw new ParseException(
                        "Minute and Second values must be between 0 and 59",
                        -1);
            }
        } else if (type == HOUR) {
            if ((val < 0 || val > 23 || end > 23) && (val != ALL_SPEC_INT)) {
                throw new ParseException(
                        "Hour values must be between 0 and 23", -1);
            }
        } else if (type == DAY_OF_MONTH) {
            if ((val < 1 || val > 31 || end > 31) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT)) {
                throw new ParseException(
                        "Day of month values must be between 1 and 31", -1);
            }
        } else if (type == MONTH) {
            if ((val < 1 || val > 12 || end > 12) && (val != ALL_SPEC_INT)) {
                throw new ParseException(
                        "Month values must be between 1 and 12", -1);
            }
        } else if (type == DAY_OF_WEEK) {
            if ((val == 0 || val > 7 || end > 7) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT)) {
                throw new ParseException(
                        "Day-of-Week values must be between 1 and 7", -1);
            }
        }

        if ((incr == 0 || incr == -1) && val != ALL_SPEC_INT) {
            if (val != -1) {
                set.add(val);
            } else {
                set.add(NO_SPEC);
            }

            return;
        }

        int startAt = val;
        int stopAt = end;

        if (val == ALL_SPEC_INT && incr <= 0) {
            incr = 1;
            set.add(ALL_SPEC); // put in a marker, but also fill values
        }

        if (type == SECOND || type == MINUTE) {
            if (stopAt == -1) {
                stopAt = 59;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 0;
            }
        } else if (type == HOUR) {
            if (stopAt == -1) {
                stopAt = 23;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 0;
            }
        } else if (type == DAY_OF_MONTH) {
            if (stopAt == -1) {
                stopAt = 31;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == MONTH) {
            if (stopAt == -1) {
                stopAt = 12;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == DAY_OF_WEEK) {
            if (stopAt == -1) {
                stopAt = 7;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1;
            }
        } else if (type == YEAR) {
            if (stopAt == -1) {
                stopAt = MAX_YEAR;
            }
            if (startAt == -1 || startAt == ALL_SPEC_INT) {
                startAt = 1970;
            }
        }

        // if the end of the range is before the start, then we need to overflow into
        // the next day, month etc. This is done by adding the maximum amount for that
        // type, and using modulus max to determine the value being added.
        int max = -1;
        if (stopAt < startAt) {
            switch (type) {
                case SECOND:
                    max = 60;
                    break;
                case MINUTE:
                    max = 60;
                    break;
                case HOUR:
                    max = 24;
                    break;
                case MONTH:
                    max = 12;
                    break;
                case DAY_OF_WEEK:
                    max = 7;
                    break;
                case DAY_OF_MONTH:
                    max = 31;
                    break;
                case YEAR:
                    throw new IllegalArgumentException("Start year must be less than stop year");
                default:
                    throw new IllegalArgumentException("Unexpected type encountered");
            }
            stopAt += max;
        }

        for (int i = startAt; i <= stopAt; i += incr) {
            if (max == -1) {
                // ie: there's no max to overflow over
                set.add(i);
            } else {
                // take the modulus to get the real value
                int i2 = i % max;

                // 1-indexed ranges should not include 0, and should include their max
                if (i2 == 0 && (type == MONTH || type == DAY_OF_WEEK || type == DAY_OF_MONTH)) {
                    i2 = max;
                }

                set.add(i2);
            }
        }
    }

    TreeSet<Integer> getSet(int type) {
        switch (type) {
            case SECOND:
                return seconds;
            case MINUTE:
                return minutes;
            case HOUR:
                return hours;
            case DAY_OF_MONTH:
                return daysOfMonth;
            case MONTH:
                return months;
            case DAY_OF_WEEK:
                return daysOfWeek;
            case YEAR:
                return years;
            default:
                return null;
        }
    }

    protected ValueSet getValue(int v, String s, int i) {
        char c = s.charAt(i);
        StringBuilder s1 = new StringBuilder(String.valueOf(v));
        while (c >= '0' && c <= '9') {
            s1.append(c);
            i++;
            if (i >= s.length()) {
                break;
            }
            c = s.charAt(i);
        }
        ValueSet val = new ValueSet();

        val.pos = (i < s.length()) ? i : i + 1;
        val.value = Integer.parseInt(s1.toString());
        return val;
    }

    protected int getNumericValue(String s, int i) {
        int endOfVal = findNextWhiteSpace(i, s);
        String val = s.substring(i, endOfVal);
        return Integer.parseInt(val);
    }

    protected int getMonthNumber(String s) {
        Integer integer = monthMap.get(s);

        if (null == integer) {
            return -1;
        }

        return integer;
    }

    protected int getDayOfWeekNumber(String s) {
        Integer integer = dayMap.get(s);

        if (null == integer) {
            return -1;
        }

        return integer;
    }

    public Date getTimeAfter(Date afterTime) {

        // Computation is based on Gregorian year only.
        Calendar cl = new GregorianCalendar(getTimeZone());

        // move ahead one second, since we're computing the time *after* the
        // given time
        afterTime = new Date(afterTime.getTime() + 1000);
        // CronTrigger does not deal with milliseconds
        cl.setTime(afterTime);
        cl.set(Calendar.MILLISECOND, 0);

        boolean gotOne = false;
        // loop until we've computed the next time, or we've past the endTime
        while (!gotOne) {
            if (cl.get(Calendar.YEAR) > 2999) { // prevent endless loop...
                return null;
            }

            SortedSet<Integer> st;
            int t = 0;

            int sec = cl.get(Calendar.SECOND);
            int min = cl.get(Calendar.MINUTE);

            // get second.................................................
            st = seconds.tailSet(sec);
            if (null != st && st.size() != 0) {
                sec = st.first();
            } else {
                sec = seconds.first();
                min++;
                cl.set(Calendar.MINUTE, min);
            }
            cl.set(Calendar.SECOND, sec);

            min = cl.get(Calendar.MINUTE);
            int hr = cl.get(Calendar.HOUR_OF_DAY);
            t = -1;

            // get minute.................................................
            st = minutes.tailSet(min);
            if (null != st && st.size() != 0) {
                t = min;
                min = st.first();
            } else {
                min = minutes.first();
                hr++;
            }
            if (min != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, min);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.MINUTE, min);

            hr = cl.get(Calendar.HOUR_OF_DAY);
            int day = cl.get(Calendar.DAY_OF_MONTH);
            t = -1;

            // get hour...................................................
            st = hours.tailSet(hr);
            if (null != st && st.size() != 0) {
                t = hr;
                hr = st.first();
            } else {
                hr = hours.first();
                day++;
            }
            if (hr != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.DAY_OF_MONTH, day);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.HOUR_OF_DAY, hr);

            day = cl.get(Calendar.DAY_OF_MONTH);
            int mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            t = -1;
            int tmon = mon;

            // get day...................................................
            boolean dayOfMSpec = !daysOfMonth.contains(NO_SPEC);
            boolean dayOfWSpec = !daysOfWeek.contains(NO_SPEC);
            if (dayOfMSpec && !dayOfWSpec) { // get day by day of month rule
                st = daysOfMonth.tailSet(day);
                if (lastdayOfMonth) {
                    if (!nearestWeekday) {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        day -= lastdayOffset;
                        if (t > day) {
                            mon++;
                            if (mon > 12) {
                                mon = 1;
                                tmon = 3333; // ensure test of mon != tmon further below fails
                                cl.add(Calendar.YEAR, 1);
                            }
                            day = 1;
                        }
                    } else {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        day -= lastdayOffset;

                        Calendar tcal = Calendar.getInstance(getTimeZone());
                        tcal.set(Calendar.SECOND, 0);
                        tcal.set(Calendar.MINUTE, 0);
                        tcal.set(Calendar.HOUR_OF_DAY, 0);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

                        int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        int dow = tcal.get(Calendar.DAY_OF_WEEK);

                        if (dow == Calendar.SATURDAY && day == 1) {
                            day += 2;
                        } else if (dow == Calendar.SATURDAY) {
                            day -= 1;
                        } else if (dow == Calendar.SUNDAY && day == ldom) {
                            day -= 2;
                        } else if (dow == Calendar.SUNDAY) {
                            day += 1;
                        }

                        tcal.set(Calendar.SECOND, sec);
                        tcal.set(Calendar.MINUTE, min);
                        tcal.set(Calendar.HOUR_OF_DAY, hr);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        Date nTime = tcal.getTime();
                        if (nTime.before(afterTime)) {
                            day = 1;
                            mon++;
                        }
                    }
                } else if (nearestWeekday) {
                    t = day;
                    day = daysOfMonth.first();

                    Calendar tcal = Calendar.getInstance(getTimeZone());
                    tcal.set(Calendar.SECOND, 0);
                    tcal.set(Calendar.MINUTE, 0);
                    tcal.set(Calendar.HOUR_OF_DAY, 0);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));

                    int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    int dow = tcal.get(Calendar.DAY_OF_WEEK);

                    if (dow == Calendar.SATURDAY && day == 1) {
                        day += 2;
                    } else if (dow == Calendar.SATURDAY) {
                        day -= 1;
                    } else if (dow == Calendar.SUNDAY && day == ldom) {
                        day -= 2;
                    } else if (dow == Calendar.SUNDAY) {
                        day += 1;
                    }


                    tcal.set(Calendar.SECOND, sec);
                    tcal.set(Calendar.MINUTE, min);
                    tcal.set(Calendar.HOUR_OF_DAY, hr);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    Date nTime = tcal.getTime();
                    if (nTime.before(afterTime)) {
                        day = daysOfMonth.first();
                        mon++;
                    }
                } else if (null != st && st.size() != 0) {
                    t = day;
                    day = st.first();
                    // make sure we don't over-run a short month, such as february
                    int lastDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    if (day > lastDay) {
                        day = daysOfMonth.first();
                        mon++;
                    }
                } else {
                    day = daysOfMonth.first();
                    mon++;
                }

                if (day != t || mon != tmon) {
                    cl.set(Calendar.SECOND, 0);
                    cl.set(Calendar.MINUTE, 0);
                    cl.set(Calendar.HOUR_OF_DAY, 0);
                    cl.set(Calendar.DAY_OF_MONTH, day);
                    cl.set(Calendar.MONTH, mon - 1);
                    // '- 1' because calendar is 0-based for this field, and we
                    // are 1-based
                    continue;
                }
            } else if (dayOfWSpec && !dayOfMSpec) { // get day by day of week rule
                if (lastdayOfWeek) { // are we looking for the last XXX day of
                    // the month?
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // did we already miss the
                        // last one?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    }

                    // find date of last occurrence of this day in this month...
                    while ((day + daysToAdd + 7) <= lDay) {
                        daysToAdd += 7;
                    }

                    day += daysToAdd;

                    if (daysToAdd > 0) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are not promoting the month
                        continue;
                    }

                } else if (nthdayOfWeek != 0) {
                    // are we looking for the Nth XXX day in the month?
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    } else if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    boolean dayShifted = daysToAdd > 0;

                    day += daysToAdd;
                    int weekOfMonth = day / 7;
                    if (day % 7 > 0) {
                        weekOfMonth++;
                    }

                    daysToAdd = (nthdayOfWeek - weekOfMonth) * 7;
                    day += daysToAdd;
                    if (daysToAdd < 0
                            || day > getLastDayOfMonth(mon, cl
                            .get(Calendar.YEAR))) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0 || dayShifted) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are NOT promoting the month
                        continue;
                    }
                } else {
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int dow = daysOfWeek.first(); // desired
                    // d-o-w
                    st = daysOfWeek.tailSet(cDow);
                    if (null != st && st.size() > 0) {
                        dow = st.first();
                    }

                    int daysToAdd = 0;
                    if (cDow < dow) {
                        daysToAdd = dow - cDow;
                    }
                    if (cDow > dow) {
                        daysToAdd = dow + (7 - cDow);
                    }

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // will we pass the end of
                        // the month?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0) { // are we swithing days?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day + daysToAdd);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' because calendar is 0-based for this field,
                        // and we are 1-based
                        continue;
                    }
                }
            } else { // dayOfWSpec && !dayOfMSpec
                throw new UnsupportedOperationException(
                        "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
            }
            cl.set(Calendar.DAY_OF_MONTH, day);

            mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            int year = cl.get(Calendar.YEAR);
            t = -1;

            // test for expressions that never generate a valid fire date,
            // but keep looping...
            if (year > MAX_YEAR) {
                return null;
            }

            // get month...................................................
            st = months.tailSet(mon);
            if (null != st && st.size() != 0) {
                t = mon;
                mon = st.first();
            } else {
                mon = months.first();
                year++;
            }
            if (mon != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, mon - 1);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.MONTH, mon - 1);
            // '- 1' because calendar is 0-based for this field, and we are
            // 1-based

            year = cl.get(Calendar.YEAR);
            t = -1;

            // get year...................................................
            st = years.tailSet(year);
            if (null != st && st.size() != 0) {
                t = year;
                year = st.first();
            } else {
                return null; // ran out of years...
            }

            if (year != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, 0);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.YEAR, year);

            gotOne = true;
        } // while( !done )

        return cl.getTime();
    }

    /**
     * Advance the calendar to the particular hour paying particular attention
     * to daylight saving problems.
     *
     * @param cal  the calendar to operate on
     * @param hour the hour to set
     */
    protected void setCalendarHour(Calendar cal, int hour) {
        cal.set(Calendar.HOUR_OF_DAY, hour);
        if (cal.get(Calendar.HOUR_OF_DAY) != hour && hour != 24) {
            cal.set(Calendar.HOUR_OF_DAY, hour + 1);
        }
    }

    /**
     * NOT YET IMPLEMENTED: Returns the time before the given time
     * that the <code>CronExpression</code> matches.
     *
     * @param endTime end time
     * @return date
     */
    public Date getTimeBefore(Date endTime) {
        return null;
    }

    /**
     * NOT YET IMPLEMENTED: Returns the final time that the
     * <code>CronExpression</code> will match.
     *
     * @return time
     */
    public Date getFinalFireTime() {
        return null;
    }

    protected int getLastDayOfMonth(int monthNum, int year) {

        switch (monthNum) {
            case 1:
                return 31;
            case 2:
                return (DateKit.isLeapYear(year)) ? 29 : 28;
            case 3:
                return 31;
            case 4:
                return 30;
            case 5:
                return 31;
            case 6:
                return 30;
            case 7:
                return 31;
            case 8:
                return 31;
            case 9:
                return 30;
            case 10:
                return 31;
            case 11:
                return 30;
            case 12:
                return 31;
            default:
                throw new IllegalArgumentException("Illegal month number: "
                        + monthNum);
        }
    }


    private void readObject(java.io.ObjectInputStream stream)
            throws java.io.IOException, ClassNotFoundException {

        stream.defaultReadObject();
        try {
            buildExpression(cronExpression);
        } catch (Exception ignore) {
        }
    }

    class ValueSet {
        public int value;

        public int pos;
    }

}