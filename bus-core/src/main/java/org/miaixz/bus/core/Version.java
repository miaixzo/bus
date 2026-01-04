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
package org.miaixz.bus.core;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CompareKit;

/**
 * Represents a version string, providing functionality to parse, compare, and manage version numbers. This class is
 * inspired by {@code java.lang.module.ModuleDescriptor.Version} and is designed to handle complex version strings that
 * may include pre-release identifiers and build metadata, loosely following semantic versioning principles.
 * <p>
 * Version components are parsed into three main parts:
 * <ol>
 * <li>A sequence of numbers and strings for the main version (e.g., {@code [8, 3, 0]} for '8.3.0').</li>
 * <li>A pre-release part (e.g., {@code ['alpha', 1]} for '-alpha.1').</li>
 * <li>A build metadata part (e.g., {@code ['build', 123]} for '+build.123').</li>
 * </ol>
 * Comparison logic prioritizes the main version sequence, followed by the pre-release identifiers, and finally the
 * build metadata.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Version implements Comparable<Version>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852290508230L;

    /**
     * The current version of the bus-core library.
     */
    public static final String _VERSION = "8.5.0";

    /**
     * The original, unparsed version string.
     */
    private final String version;
    /**
     * A list of numeric or string components representing the main version number.
     */
    private final List<Object> sequence;
    /**
     * A list of numeric or string components representing the pre-release identifier.
     */
    private final List<Object> pre;
    /**
     * A list of numeric or string components representing the build metadata.
     */
    private final List<Object> build;

    /**
     * Constructs a {@code Version} object by parsing a version string. The format is defined as
     * {@code tok+ ( '-' tok+)? ( '+' tok+)?}, where tokens are separated by '.' or '-'. Numeric parts are compared by
     * value, while string parts are compared lexicographically.
     *
     * @param v The version string to parse. Must not be null.
     */
    public Version(final String v) {
        Assert.notNull(v, "Null version string");
        final int n = v.length();
        if (n == 0) {
            this.version = v;
            this.sequence = Collections.emptyList();
            this.pre = Collections.emptyList();
            this.build = Collections.emptyList();
            return;
        }
        this.version = v;
        this.sequence = new ArrayList<>(4);
        this.pre = new ArrayList<>(2);
        this.build = new ArrayList<>(2);

        int i = 0;
        char c = v.charAt(i);

        final List<Object> sequence = this.sequence;
        final List<Object> pre = this.pre;
        final List<Object> build = this.build;

        // Parse main version
        i = takeNumber(v, i, sequence);

        while (i < n) {
            c = v.charAt(i);
            if (c == '.') {
                i++;
                continue;
            }
            if (c == Symbol.C_MINUS || c == Symbol.C_PLUS) {
                i++;
                break;
            }
            if (CharKit.isNumber(c)) {
                i = takeNumber(v, i, sequence);
            } else {
                i = takeString(v, i, sequence);
            }
        }

        if (c == Symbol.C_MINUS && i >= n) {
            return;
        }

        // Parse pre-release version
        while (i < n) {
            c = v.charAt(i);
            if (c >= '0' && c <= '9')
                i = takeNumber(v, i, pre);
            else
                i = takeString(v, i, pre);
            if (i >= n) {
                break;
            }
            c = v.charAt(i);
            if (c == '.' || c == Symbol.C_MINUS) {
                i++;
                continue;
            }
            if (c == Symbol.C_PLUS) {
                i++;
                break;
            }
        }

        if (c == Symbol.C_PLUS && i >= n) {
            return;
        }

        // Parse build version
        while (i < n) {
            c = v.charAt(i);
            if (c >= '0' && c <= '9') {
                i = takeNumber(v, i, build);
            } else {
                i = takeString(v, i, build);
            }
            if (i >= n) {
                break;
            }
            c = v.charAt(i);
            if (c == '.' || c == Symbol.C_MINUS || c == Symbol.C_PLUS) {
                i++;
            }
        }
    }

    /**
     * Parses a version string into a {@code Version} object.
     *
     * @param v The version string to parse.
     * @return The resulting {@code Version} object.
     * @throws IllegalArgumentException if {@code v} is {@code null}, empty, or cannot be parsed.
     */
    public static Version of(final String v) {
        return new Version(v);
    }

    /**
     * Returns the complete version string of the current library.
     *
     * @return The current library version string.
     */
    public static String all() {
        return _VERSION;
    }

    /**
     * Parses a sequence of digits from the string starting at a given index and adds the resulting integer to the list.
     *
     * @param s   The string to parse.
     * @param i   The starting index.
     * @param acc The list to which the parsed number is added.
     * @return The index of the first character not consumed.
     */
    private static int takeNumber(final String s, int i, final List<Object> acc) {
        char c = s.charAt(i);
        int d = (c - '0');
        final int n = s.length();
        while (++i < n) {
            c = s.charAt(i);
            if (CharKit.isNumber(c)) {
                d = d * 10 + (c - '0');
                continue;
            }
            break;
        }
        acc.add(d);
        return i;
    }

    /**
     * Parses a sequence of non-digit, non-separator characters from the string and adds it to the list. The sequence
     * ends at the next '.', '-', '+', or digit.
     *
     * @param s   The string to parse.
     * @param i   The starting index.
     * @param acc The list to which the parsed string is added.
     * @return The index of the first character not consumed.
     */
    private static int takeString(final String s, int i, final List<Object> acc) {
        final int b = i;
        final int n = s.length();
        while (++i < n) {
            final char c = s.charAt(i);
            if (c != '.' && c != Symbol.C_MINUS && c != Symbol.C_PLUS && !(c >= '0' && c <= '9')) {
                continue;
            }
            break;
        }
        acc.add(s.substring(b, i));
        return i;
    }

    /**
     * Compares this version to another. The comparison is performed by comparing the main version sequences, then the
     * pre-release identifiers, and finally the build metadata. A version with a pre-release identifier is considered
     * older than one without.
     *
     * @param that The other {@code Version} to compare to.
     * @return A negative integer, zero, or a positive integer as this version is less than, equal to, or greater than
     *         the specified version.
     */
    @Override
    public int compareTo(final Version that) {
        int c = compareTokens(this.sequence, that.sequence);
        if (c != 0) {
            return c;
        }
        if (this.pre.isEmpty()) {
            if (!that.pre.isEmpty()) {
                return +1;
            }
        } else {
            if (that.pre.isEmpty()) {
                return -1;
            }
        }
        c = compareTokens(this.pre, that.pre);
        if (c != 0) {
            return c;
        }
        return compareTokens(this.build, that.build);
    }

    /**
     * Checks if this version is equal to another object. Two versions are considered equal if their {@code compareTo}
     * method returns 0.
     *
     * @param ob The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object ob) {
        if (!(ob instanceof Version)) {
            return false;
        }
        return compareTo((Version) ob) == 0;
    }

    /**
     * Computes the hash code for this version. The hash code is based on the original version string.
     *
     * @return The hash code for this version.
     */
    @Override
    public int hashCode() {
        return version.hashCode();
    }

    /**
     * Returns the original string representation of this version.
     *
     * @return The original version string.
     */
    @Override
    public String toString() {
        return version;
    }

    /**
     * Compares two lists of version tokens (which can be Integers or Strings).
     *
     * @param ts1 The first list of tokens.
     * @param ts2 The second list of tokens.
     * @return A negative integer, zero, or a positive integer as the first list is less than, equal to, or greater than
     *         the second.
     */
    private int compareTokens(final List<Object> ts1, final List<Object> ts2) {
        final int n = Math.min(ts1.size(), ts2.size());
        for (int i = 0; i < n; i++) {
            final Object o1 = ts1.get(i);
            final Object o2 = ts2.get(i);
            if ((o1 instanceof Integer && o2 instanceof Integer) || (o1 instanceof String && o2 instanceof String)) {
                final int c = CompareKit.compare(o1, o2, null);
                if (c == 0) {
                    continue;
                }
                return c;
            }
            // Types differ, so support number to string form
            final int c = o1.toString().compareTo(o2.toString());
            if (c == 0) {
                continue;
            }
            return c;
        }
        final List<Object> rest = ts1.size() > ts2.size() ? ts1 : ts2;
        final int e = rest.size();
        for (int i = n; i < e; i++) {
            final Object o = rest.get(i);
            if (o instanceof Integer && ((Integer) o) == 0) {
                continue;
            }
            return ts1.size() - ts2.size();
        }
        return 0;
    }

}
