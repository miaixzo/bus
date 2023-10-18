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
package org.aoju.bus.image.galaxy;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Property implements Serializable {

    public static String LINE_SEPARATOR = System.getProperty("line.separator");

    private final String name;
    private final Object value;

    public Property(String name, Object value) {
        if (null == name)
            throw new NullPointerException("name");
        if (null == value)
            throw new NullPointerException("value");

        if (!(value instanceof String
                || value instanceof Boolean
                || value instanceof Number))
            throw new IllegalArgumentException("value: " + value.getClass());

        this.name = name;
        this.value = value;
    }

    public Property(String s) {
        int endParamName = s.indexOf(Symbol.C_EQUAL);
        name = s.substring(0, endParamName);
        value = valueOf(s.substring(endParamName + 1));
    }

    private static Object valueOf(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return s.equalsIgnoreCase("true") ? Boolean.TRUE :
                    s.equalsIgnoreCase("false") ? Boolean.FALSE
                            : s;
        }
    }

    public static Property[] valueOf(String[] ss) {
        Property[] properties = new Property[ss.length];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = new Property(ss[i]);
        }
        return properties;
    }

    public static <T> T getFrom(Property[] props, String name, T defVal) {
        for (Property prop : props)
            if (prop.name.equals(name))
                return (T) prop.value;
        return defVal;
    }

    public static StringBuilder appendLine(StringBuilder sb, Object... ss) {
        for (Object s : ss)
            sb.append(s);
        return sb.append(System.getProperty("line.separator"));
    }

    public static String concat(String[] ss, char delim) {
        int n = ss.length;
        if (n == 0)
            return Normal.EMPTY;

        if (n == 1) {
            String s = ss[0];
            return null != s ? s : Normal.EMPTY;
        }
        int len = n - 1;
        for (String s : ss)
            if (null != s)
                len += s.length();

        char[] cs = new char[len];
        int off = 0;
        int i = 0;
        for (String s : ss) {
            if (i++ != 0)
                cs[off++] = delim;
            if (null != s) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static String concat(Collection<String> ss, char delim) {
        int n = ss.size();
        if (n == 0)
            return Normal.EMPTY;

        if (n == 1) {
            String s = ss.iterator().next();
            return null != s ? s : Normal.EMPTY;
        }
        int len = n - 1;
        for (String s : ss)
            if (null != s)
                len += s.length();

        char[] cs = new char[len];
        int off = 0;
        int i = 0;
        for (String s : ss) {
            if (i++ != 0)
                cs[off++] = delim;
            if (null != s) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static Object splitAndTrim(String s, char delim) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0)
            count++;

        if (count == 1)
            return substring(s, 0, s.length());

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = substring(s, delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static String[] split(String s, char delim) {
        if (null == s || s.isEmpty())
            return Normal.EMPTY_STRING_ARRAY;

        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0)
            count++;

        if (count == 1)
            return new String[]{s};

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = s.substring(delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static String cut(String s, int index, char delim) {
        int i = 0;
        int begin = 0;
        int end;
        while ((end = s.indexOf(delim, begin)) >= 0) {
            if (i++ == index)
                return s.substring(begin, end);
            begin = end + 1;
        }
        return i == index ? s.substring(begin) : Normal.EMPTY;
    }

    private static String substring(String s, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && s.charAt(beginIndex) <= Symbol.C_SPACE)
            beginIndex++;
        while (beginIndex < endIndex && s.charAt(endIndex - 1) <= Symbol.C_SPACE)
            endIndex--;
        return beginIndex < endIndex ? s.substring(beginIndex, endIndex) : Normal.EMPTY;
    }

    public static String trimTrailing(String s) {
        int endIndex = s.length();
        while (endIndex > 0 && s.charAt(endIndex - 1) <= Symbol.C_SPACE)
            endIndex--;
        return s.substring(0, endIndex);
    }

    public static int parseIS(String s) {
        return null != s && s.length() != 0
                ? Integer.parseInt(s.charAt(0) == Symbol.C_PLUS ? s.substring(1) : s)
                : 0;
    }

    public static double parseDS(String s) {
        return null != s && s.length() != 0
                ? Double.parseDouble(s.replace(Symbol.C_COMMA, Symbol.C_DOT))
                : 0;
    }

    public static String formatDS(float f) {
        String s = Float.toString(f);
        int l = s.length();
        if (s.startsWith(".0", l - 2))
            return s.substring(0, l - 2);
        int e = s.indexOf('E', l - 5);
        return e > 0 && s.startsWith(".0", e - 2) ? cut(s, e - 2, e) : s;
    }

    public static String formatDS(double d) {
        String s = Double.toString(d);
        int l = s.length();
        if (s.startsWith(".0", l - 2))
            return s.substring(0, l - 2);
        int skip = l - Normal._16;
        int e = s.indexOf('E', l - 5);
        return e < 0 ? (skip > 0 ? s.substring(0, Normal._16) : s)
                : s.startsWith(".0", e - 2) ? cut(s, e - 2, e)
                : skip > 0 ? cut(s, e - skip, e) : s;
    }

    private static String cut(String s, int begin, int end) {
        int l = s.length();
        char[] ch = new char[l - (end - begin)];
        s.getChars(0, begin, ch, 0);
        s.getChars(end, l, ch, begin);
        return new String(ch);
    }

    public static boolean matches(String s, String key,
                                  boolean matchNullOrEmpty, boolean ignoreCase) {
        if (null == key || key.isEmpty())
            return true;

        if (null == s || s.isEmpty())
            return matchNullOrEmpty;

        return containsWildCard(key)
                ? compilePattern(key, ignoreCase).matcher(s).matches()
                : ignoreCase ? key.equalsIgnoreCase(s) : key.equals(s);
    }

    public static Pattern compilePattern(String key, boolean ignoreCase) {
        StringTokenizer stk = new StringTokenizer(key, "*?", true);
        StringBuilder regex = new StringBuilder();
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            char ch1 = tk.charAt(0);
            if (ch1 == Symbol.C_STAR) {
                regex.append(".*");
            } else if (ch1 == Symbol.C_QUESTION_MARK) {
                regex.append(Symbol.DOT);
            } else {
                regex.append("\\Q").append(tk).append("\\E");
            }
        }
        return Pattern.compile(regex.toString(),
                ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
    }

    public static boolean containsWildCard(String s) {
        return (s.indexOf(Symbol.C_STAR) >= 0 || s.indexOf(Symbol.C_QUESTION_MARK) >= 0);
    }

    public static String[] maskNull(String[] ss) {
        return maskNull(ss, Normal.EMPTY_STRING_ARRAY);
    }

    public static <T> T maskNull(T o, T mask) {
        return null == o ? mask : o;
    }

    public static <T> T nullify(T o, T val) {
        return val.equals(o) ? null : o;
    }

    public static String maskEmpty(String s, String mask) {
        return null == s || s.isEmpty() ? mask : s;
    }

    public static String truncate(String s, int maxlen) {
        return s.length() > maxlen ? s.substring(0, maxlen) : s;
    }

    public static <T> boolean equals(T o1, T o2) {
        return o1 == o2 || null != o1 && o1.equals(o2);
    }

    public static String replaceSystemProperties(String s) {
        int i = s.indexOf("${");
        if (i == -1)
            return s;

        StringBuilder sb = new StringBuilder(s.length());
        int j = -1;
        do {
            sb.append(s, j + 1, i);
            if ((j = s.indexOf('}', i + 2)) == -1) {
                j = i - 1;
                break;
            }
            String val = s.startsWith("env.", i + 2)
                    ? System.getenv(s.substring(i + 6, j))
                    : System.getProperty(s.substring(i + 2, j));
            sb.append(null != val ? val : s.substring(i, j + 1));
            i = s.indexOf("${", j + 1);
        } while (i != -1);
        sb.append(s.substring(j + 1));
        return sb.toString();
    }

    public static boolean isUpperCase(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (Character.toUpperCase(ch) != ch)
                return false;
        }
        return len != 0;
    }

    public static boolean isIPAddr(String s) {
        String[] ss = split(s, Symbol.C_COLON);
        if (ss.length > 1)
            return ss.length == 8;

        ss = split(s, Symbol.C_DOT);
        if (ss.length != 4)
            return false;

        for (String s1 : ss) {
            if (s1.length() > 3)
                return false;

            int i = 0;
            char ch;
            switch (s1.length()) {
                case 3:
                    ch = s1.charAt(i++);
                    if (ch != '1' && ch != '2')
                        return false;
                case 2:
                    ch = s1.charAt(i++);
                    if (ch < '0' || ch > '9')
                        return false;
                case 1:
                    ch = s1.charAt(i++);
                    if (ch < '0' || ch > '9')
                        return false;
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static <T> boolean contains(T[] a, T o) {
        for (T t : a)
            if (Objects.equals(t, o))
                return true;
        return false;
    }

    public static <T> T[] requireNotEmpty(T[] a, String message) {
        if (a.length == 0)
            throw new IllegalArgumentException(message);
        return a;
    }

    public static String requireNotEmpty(String s, String message) {
        if (s.isEmpty())
            throw new IllegalArgumentException(message);
        return s;
    }

    public static String[] requireContainsNoEmpty(String[] ss, String message) {
        for (String s : ss)
            requireNotEmpty(s, message);
        return ss;
    }

    public final String getName() {
        return name;
    }

    public final Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (null == object)
            return false;
        if (getClass() != object.getClass())
            return false;

        Property other = (Property) object;
        return name.equals(other.name)
                && value.equals(other.value);
    }

    @Override
    public String toString() {
        return name + Symbol.C_EQUAL + value;
    }

    public void setAt(Object o) {
        String setterName = "set"
                + name.substring(0, 1).toUpperCase(Locale.ENGLISH)
                + name.substring(1);
        try {
            Class<?> clazz = o.getClass();
            if (value instanceof String) {
                clazz.getMethod(setterName, String.class).invoke(o, value);
            } else if (value instanceof Boolean) {
                clazz.getMethod(setterName, boolean.class).invoke(o, value);
            } else {
                try {
                    clazz.getMethod(setterName, double.class)
                            .invoke(o, ((Number) value).doubleValue());
                } catch (NoSuchMethodException e) {
                    try {
                        clazz.getMethod(setterName, float.class)
                                .invoke(o, ((Number) value).floatValue());
                    } catch (NoSuchMethodException e2) {
                        try {
                            clazz.getMethod(setterName, int.class)
                                    .invoke(o, ((Number) value).intValue());
                        } catch (NoSuchMethodException e3) {
                            throw e;
                        }
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
