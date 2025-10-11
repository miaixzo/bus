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
package org.miaixz.bus.core.compare;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * A string comparator that mimics the sorting behavior of Windows Explorer. This comparator provides the same sorting
 * results for filenames as seen in Windows Explorer.
 *
 * <p>
 * For example, given an array of filenames: {@code {"xyz2.doc", "xyz1.doc", "xyz12.doc"}}.
 * 
 * <p>
 * A standard sort with {@code Arrays.sort(filenames);} would result in {@code {"xyz1.doc", "xyz12.doc", "xyz2.doc"}}.
 * 
 * <p>
 * Using this comparator with {@code Arrays.sort(filenames, new ExplorerCompare());} results in {@code {"xyz1.doc",
 * "xyz2.doc", "xyz12.doc"}}, which matches the order in Windows Explorer.
 * 
 *
 * @author Kimi Liu
 * @see <a href="https://stackoverflow.com/questions/23205020/java-sort-strings-like-windows-explorer">Java - Sort
 *      Strings like Windows Explorer</a>
 * @since Java 17+
 */
public class ExplorerCompare implements Comparator<CharSequence> {

    /**
     * Singleton instance of the comparator.
     */
    public static final ExplorerCompare INSTANCE = new ExplorerCompare();

    @Override
    public int compare(final CharSequence str1, final CharSequence str2) {
        final Iterator<String> i1 = splitStringPreserveDelimiter(str1).iterator();
        final Iterator<String> i2 = splitStringPreserveDelimiter(str2).iterator();
        while (true) {
            // End of both strings, they are equal
            if (!i1.hasNext() && !i2.hasNext()) {
                return 0;
            }
            // i1 is shorter, so it comes first
            if (!i1.hasNext()) {
                return -1;
            }
            // i2 is shorter, so it comes first
            if (!i2.hasNext()) {
                return 1;
            }

            final String data1 = i1.next();
            final String data2 = i2.next();
            int result;
            try {
                // If both parts are numbers, compare them numerically
                result = Long.compare(Long.parseLong(data1), Long.parseLong(data2));
                // If numbers are equal, the one with more digits comes first (e.g., 01 vs 1)
                if (result == 0) {
                    result = -Integer.compare(data1.length(), data2.length());
                }
            } catch (final NumberFormatException ex) {
                // Otherwise, compare as case-insensitive text
                result = data1.compareToIgnoreCase(data2);
            }

            if (result != 0) {
                return result;
            }
        }
    }

    /**
     * Splits a string into a list of parts, preserving delimiters (digits, dots, whitespace).
     *
     * @param text the character sequence to split.
     * @return a list of string parts.
     */
    private List<String> splitStringPreserveDelimiter(final CharSequence text) {
        final Matcher matcher = Pattern.compile("\\d+|\\.|\\s").matcher(text);
        final List<String> list = new ArrayList<>();
        int pos = 0;
        while (matcher.find()) {
            list.add(StringKit.sub(text, pos, matcher.start()));
            list.add(matcher.group());
            pos = matcher.end();
        }
        list.add(StringKit.subSuf(text, pos));
        return list;
    }

}
