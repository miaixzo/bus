/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.DatePrecision;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 日期格式化等工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Format extends java.text.Format {

    public static final Date[] EMPTY_DATES = {};
    private static final char[] CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v'
    };
    private static final int LONG_BYTES = 8;
    private static TimeZone cachedTimeZone;
    private final String pattern;
    private final int[][] tagPaths;
    private final int[] index;
    private final Type[] types;
    private final MessageFormat format;

    public Format(String pattern) {
        List<String> tokens = tokenize(pattern);
        int n = tokens.size() / 2;
        this.pattern = pattern;
        this.tagPaths = new int[n][];
        this.index = new int[n];
        this.types = new Type[n];
        this.format = buildMessageFormat(tokens);
    }

    public static Format valueOf(String s) {
        return null != s ? new Format(s) : null;
    }

    private static Calendar cal(TimeZone tz) {
        Calendar cal = (null != tz)
                ? new GregorianCalendar(tz)
                : new GregorianCalendar();
        cal.clear();
        return cal;
    }

    private static Calendar cal(TimeZone tz, Date date) {
        Calendar cal = (null != tz)
                ? new GregorianCalendar(tz)
                : new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    private static void ceil(Calendar cal, int field) {
        cal.add(field, 1);
        cal.add(Calendar.MILLISECOND, -1);
    }

    public static String formatDA(TimeZone tz, Date date) {
        return formatDA(tz, date, new StringBuilder(8)).toString();
    }

    public static StringBuilder formatDA(TimeZone tz, Date date,
                                         StringBuilder toAppendTo) {
        return formatDT(cal(tz, date), toAppendTo, Calendar.DAY_OF_MONTH);
    }

    public static String formatTM(TimeZone tz, Date date) {
        return formatTM(tz, date, new DatePrecision());
    }

    public static String formatTM(TimeZone tz, Date date, DatePrecision precision) {
        return formatTM(cal(tz, date), new StringBuilder(10),
                precision.lastField).toString();
    }

    private static StringBuilder formatTM(Calendar cal,
                                          StringBuilder toAppendTo, int lastField) {
        appendXX(cal.get(Calendar.HOUR_OF_DAY), toAppendTo);
        if (lastField > Calendar.HOUR_OF_DAY) {
            appendXX(cal.get(Calendar.MINUTE), toAppendTo);
            if (lastField > Calendar.MINUTE) {
                appendXX(cal.get(Calendar.SECOND), toAppendTo);
                if (lastField > Calendar.SECOND) {
                    toAppendTo.append(Symbol.C_DOT);
                    appendXXX(cal.get(Calendar.MILLISECOND), toAppendTo);
                }
            }
        }
        return toAppendTo;
    }

    public static String formatDT(TimeZone tz, Date date) {
        return formatDT(tz, date, new DatePrecision());
    }

    public static String formatDT(TimeZone tz, Date date, DatePrecision precision) {
        return formatDT(tz, date, new StringBuilder(23), precision).toString();
    }

    public static StringBuilder formatDT(TimeZone tz, Date date,
                                         StringBuilder toAppendTo, DatePrecision precision) {
        Calendar cal = cal(tz, date);
        formatDT(cal, toAppendTo, precision.lastField);
        if (precision.includeTimezone) {
            int offset = cal.get(Calendar.ZONE_OFFSET)
                    + cal.get(Calendar.DST_OFFSET);
            appendZZZZZ(offset, toAppendTo);
        }
        return toAppendTo;
    }

    private static StringBuilder appendZZZZZ(int offset, StringBuilder sb) {
        if (offset < 0) {
            offset = -offset;
            sb.append(Symbol.C_MINUS);
        } else
            sb.append(Symbol.C_PLUS);
        int min = offset / 60000;
        appendXX(min / 60, sb);
        appendXX(min % 60, sb);
        return sb;
    }

    /**
     * 返回指定时区的格式{@code (+|i)HHMM}的UTC时区偏移量，不涉及夏令时(DST).
     *
     * @param tz 时区
     * @return 来自UTC的时区偏移量，格式为{@code (+|i)HHMM}
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz) {
        return appendZZZZZ(tz.getRawOffset(), new StringBuilder(5)).toString();
    }

    /**
     * 在指定日期以指定时区的格式{@code(+|i)HHMM}从UTC返回时区偏移量
     * 如果未指定日期，则考虑当前日期为夏令时
     *
     * @param tz   时区
     * @param date 日期或{@code null}
     * @return 来自UTC的时区偏移量，格式为{@code (+|i)HHMM}
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz, Date date) {
        return appendZZZZZ(tz.getOffset(null == date
                        ? System.currentTimeMillis() : date.getTime()),
                new StringBuilder(5)).toString();
    }

    private static StringBuilder formatDT(Calendar cal, StringBuilder toAppendTo,
                                          int lastField) {
        appendXXXX(cal.get(Calendar.YEAR), toAppendTo);
        if (lastField > Calendar.YEAR) {
            appendXX(cal.get(Calendar.MONTH) + 1, toAppendTo);
            if (lastField > Calendar.MONTH) {
                appendXX(cal.get(Calendar.DAY_OF_MONTH), toAppendTo);
                if (lastField > Calendar.DAY_OF_MONTH) {
                    formatTM(cal, toAppendTo, lastField);
                }
            }
        }
        return toAppendTo;
    }

    private static void appendXXXX(int i, StringBuilder toAppendTo) {
        if (i < 1000)
            toAppendTo.append('0');
        appendXXX(i, toAppendTo);
    }

    private static void appendXXX(int i, StringBuilder toAppendTo) {
        if (i < 100)
            toAppendTo.append('0');
        appendXX(i, toAppendTo);
    }

    private static void appendXX(int i, StringBuilder toAppendTo) {
        if (i < 10)
            toAppendTo.append('0');
        toAppendTo.append(i);
    }

    public static Date parseDA(TimeZone tz, String s) {
        return parseDA(tz, s, false);
    }

    public static Date parseDA(TimeZone tz, String s, boolean ceil) {
        Calendar cal = cal(tz);
        int length = s.length();
        if (!(length == 8 || length == 10 && !Character.isDigit(s.charAt(4))))
            throw new IllegalArgumentException(s);
        try {
            int pos = 0;
            cal.set(Calendar.YEAR,
                    Integer.parseInt(s.substring(pos, pos + 4)));
            pos += 4;
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            cal.set(Calendar.MONTH,
                    Integer.parseInt(s.substring(pos, pos + 2)) - 1);
            pos += 2;
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            cal.set(Calendar.DAY_OF_MONTH,
                    Integer.parseInt(s.substring(pos)));
            if (ceil)
                ceil(cal, Calendar.DAY_OF_MONTH);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(s);
        }
        return cal.getTime();
    }

    public static Date parseTM(TimeZone tz, String s, DatePrecision precision) {
        return parseTM(tz, s, false, precision);
    }

    public static Date parseTM(TimeZone tz, String s, boolean ceil,
                               DatePrecision precision) {
        return parseTM(cal(tz), s, ceil, precision);
    }

    private static Date parseTM(Calendar cal, String s, boolean ceil,
                                DatePrecision precision) {
        int length = s.length();
        int pos = 0;
        if (pos + 2 > length)
            throw new IllegalArgumentException(s);

        try {
            cal.set(precision.lastField = Calendar.HOUR_OF_DAY,
                    Integer.parseInt(s.substring(pos, pos + 2)));
            pos += 2;
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);

                cal.set(precision.lastField = Calendar.MINUTE,
                        Integer.parseInt(s.substring(pos, pos + 2)));
                pos += 2;
                if (pos < length) {
                    if (!Character.isDigit(s.charAt(pos)))
                        pos++;
                    if (pos + 2 > length)
                        throw new IllegalArgumentException(s);
                    cal.set(precision.lastField = Calendar.SECOND,
                            Integer.parseInt(s.substring(pos, pos + 2)));
                    pos += 2;
                    if (pos < length) {
                        float f = Float.parseFloat(s.substring(pos));
                        if (f >= 1 || f < 0)
                            throw new IllegalArgumentException(s);
                        cal.set(precision.lastField = Calendar.MILLISECOND,
                                (int) (f * 1000));
                        return cal.getTime();
                    }
                }
            }
            if (ceil)
                ceil(cal, precision.lastField);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(s);
        }
        return cal.getTime();
    }

    public static Date parseDT(TimeZone tz, String s, DatePrecision precision) {
        return parseDT(tz, s, false, precision);
    }

    public static TimeZone timeZone(String s) {
        TimeZone tz;
        if (s.length() != 5 || null == (tz = safeTimeZone(s)))
            throw new IllegalArgumentException("Illegal Timezone Offset: " + s);
        return tz;
    }

    private static TimeZone safeTimeZone(String s) {
        String tzid = tzid(s);
        if (null == tzid)
            return null;

        TimeZone tz = cachedTimeZone;
        if (null == tz || !tz.getID().equals(tzid))
            cachedTimeZone = tz = TimeZone.getTimeZone(tzid);

        return tz;
    }

    private static String tzid(String s) {
        int length = s.length();
        if (length > 4) {
            char[] tzid = {'G', 'M', 'T', 0, 0, 0, Symbol.C_COLON, 0, 0};
            s.getChars(length - 5, length - 2, tzid, 3);
            s.getChars(length - 2, length, tzid, 7);
            if ((tzid[3] == Symbol.C_PLUS || tzid[3] == Symbol.C_MINUS)
                    && Character.isDigit(tzid[4])
                    && Character.isDigit(tzid[5])
                    && Character.isDigit(tzid[7])
                    && Character.isDigit(tzid[8])) {
                return new String(tzid);
            }
        }
        return null;
    }

    public static Date parseDT(TimeZone tz, String s, boolean ceil,
                               DatePrecision precision) {
        int length = s.length();
        TimeZone tz1 = safeTimeZone(s);
        if (precision.includeTimezone = null != tz1) {
            length -= 5;
            tz = tz1;
        }
        Calendar cal = cal(tz);
        try {
            int pos = 0;
            if (pos + 4 > length)
                throw new IllegalArgumentException(s);
            cal.set(precision.lastField = Calendar.YEAR,
                    Integer.parseInt(s.substring(pos, pos + 4)));
            pos += 4;
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                cal.set(precision.lastField = Calendar.MONTH,
                        Integer.parseInt(s.substring(pos, pos + 2)) - 1);
                pos += 2;
                if (pos < length) {
                    if (!Character.isDigit(s.charAt(pos)))
                        pos++;
                    if (pos + 2 > length)
                        throw new IllegalArgumentException(s);
                    cal.set(precision.lastField = Calendar.DAY_OF_MONTH,
                            Integer.parseInt(s.substring(pos, pos + 2)));
                    pos += 2;
                    if (pos < length)
                        return parseTM(cal, s.substring(pos, length), ceil,
                                precision);
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(s);
        }
        if (ceil)
            ceil(cal, precision.lastField);
        return cal.getTime();
    }

    private List<String> tokenize(String s) {
        List<String> result = new ArrayList<>();
        StringTokenizer stk = new StringTokenizer(s, "{}", true);
        String tk;
        char delim;
        char prevDelim = '}';
        int level = 0;
        StringBuilder sb = new StringBuilder();
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            delim = tk.charAt(0);
            if (delim == '{') {
                if (level++ == 0) {
                    if (prevDelim == '}')
                        result.add(Normal.EMPTY);
                } else {
                    sb.append(delim);
                }
            } else if (delim == '}') {
                if (--level == 0) {
                    result.add(sb.toString());
                    sb.setLength(0);
                } else if (level > 0) {
                    sb.append(delim);
                } else
                    throw new IllegalArgumentException(s);
            } else {
                if (level == 0)
                    result.add(tk);
                else
                    sb.append(tk);
            }
            prevDelim = delim;
        }
        return result;
    }

    private MessageFormat buildMessageFormat(List<String> tokens) {
        StringBuilder formatBuilder = new StringBuilder(pattern.length());
        int j = 0;
        for (int i = 0; i < tagPaths.length; i++) {
            formatBuilder.append(tokens.get(j++)).append('{').append(i);
            String tagStr = tokens.get(j++);
            int typeStart = tagStr.indexOf(Symbol.C_COMMA) + 1;
            boolean rnd = tagStr.startsWith("rnd");
            if (!rnd && !tagStr.startsWith("now")) {
                int tagStrLen = typeStart != 0
                        ? typeStart - 1
                        : tagStr.length();

                int indexStart = tagStr.charAt(tagStrLen - 1) == Symbol.C_BRACKET_RIGHT
                        ? tagStr.lastIndexOf(Symbol.C_BRACKET_LEFT, tagStrLen - 3) + 1
                        : 0;
                try {
                    tagPaths[i] = Tag.parseTagPath(tagStr.substring(0, indexStart != 0 ? indexStart - 1 : tagStrLen));
                    if (indexStart != 0)
                        index[i] = Integer.parseInt(tagStr.substring(indexStart, tagStrLen - 1));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(pattern);
                }
            }
            if (typeStart != 0) {
                int typeEnd = tagStr.indexOf(Symbol.C_COMMA, typeStart);
                try {
                    types[i] = Type.valueOf(tagStr.substring(typeStart,
                            typeEnd < 0 ? tagStr.length() : typeEnd));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(pattern);
                }
                switch (types[i]) {
                    case number:
                    case date:
                    case time:
                    case choice:
                        formatBuilder.append(
                                typeStart > 0 ? tagStr.substring(typeStart - 1) : tagStr);
                }
            } else {
                types[i] = Type.none;
            }
            if (rnd) {
                switch (types[i]) {
                    case none:
                        types[i] = Type.rnd;
                    case uuid:
                    case uid:
                        break;
                    default:
                        throw new IllegalArgumentException(pattern);
                }
            }
            formatBuilder.append('}');
        }
        if (j < tokens.size())
            formatBuilder.append(tokens.get(j));
        try {
            return new MessageFormat(formatBuilder.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(pattern);
        }
    }

    @Override
    public StringBuffer format(Object object, StringBuffer result, FieldPosition pos) {
        return format.format(toArgs((Attributes) object), result, pos);
    }

    private Object[] toArgs(Attributes attrs) {
        Object[] args = new Object[tagPaths.length];
        for (int i = 0; i < args.length; i++) {
            int[] tagPath = tagPaths[i];
            if (null == tagPath) { // now
                args[i] = types[i].toArg(attrs, 0, index[i]);
            } else {
                int last = tagPath.length - 1;
                Attributes item = attrs;
                for (int j = 0; j < last && null != item; j++) {
                    item = item.getNestedDataset(tagPath[j]);
                }
                args[i] = null != item ? types[i].toArg(item, tagPath[last], index[i]) : null;
            }
        }
        return args;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return pattern;
    }

    private enum Type {
        none {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return attrs.getString(tag, index);
            }
        },
        number {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        date {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return tag != 0 ? attrs.getDate(tag, index) : new Date();
            }
        },
        time {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return tag != 0 ? attrs.getDate(tag, index) : new Date();
            }
        },
        choice {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return attrs.getDouble(tag, index, 0.);
            }
        },
        hash {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                String s = attrs.getString(tag, index);
                return null != s ? Tag.toHexString(s.hashCode()) : null;
            }
        },
        md5 {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                String s = attrs.getString(tag, index);
                return null != s ? getMD5String(s) : null;
            }
        },
        urlencoded {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                String s = attrs.getString(tag, index);
                try {
                    return null != s ? URLEncoder.encode(s, Charset.DEFAULT_UTF_8) : null;
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
            }
        },
        rnd {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return Tag.toHexString(ThreadLocalRandom.current().nextInt());
            }
        },
        uuid {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return UUID.randomUUID();
            }
        },
        uid {
            @Override
            Object toArg(Attributes attrs, int tag, int index) {
                return UID.createUID();
            }
        };

        static String toString32(byte[] ba) {
            long l1 = toLong(ba, 0);
            long l2 = toLong(ba, LONG_BYTES);
            char[] ca = new char[26];
            for (int i = 0; i < 12; i++) {
                ca[i] = CHARS[(int) l1 & 0x1f];
                l1 = l1 >>> 5;
            }
            l1 = l1 | (l2 & 1) << 4;
            ca[12] = CHARS[(int) l1 & 0x1f];
            l2 = l2 >>> 1;
            for (int i = 13; i < 26; i++) {
                ca[i] = CHARS[(int) l2 & 0x1f];
                l2 = l2 >>> 5;
            }

            return new String(ca);
        }

        static long toLong(byte[] ba, int offset) {
            long l = 0;
            for (int i = offset, len = offset + LONG_BYTES; i < len; i++) {
                l |= ba[i] & 0xFF;
                l <<= 8;
            }
            return l;
        }

        abstract Object toArg(Attributes attrs, int tag, int index);

        String getMD5String(String s) {
            try {
                MessageDigest digest = MessageDigest.getInstance(Algorithm.MD5.getValue());
                digest.update(null == s ? new byte[0] : s.getBytes(Charset.UTF_8));
                return toString32(digest.digest());
            } catch (NoSuchAlgorithmException e) {
                return s;
            }
        }
    }

}
