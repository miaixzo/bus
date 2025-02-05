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
 * Windows 资源管理器风格字符串比较器 此比较器模拟了 Windows 资源管理器的文件名排序方式，可得到与其相同的排序结果。
 *
 * <p>
 * 假设有一个数组，包含若干个文件名 {@code {"xyz2.doc", "xyz1.doc", "xyz12.doc"}}
 * </p>
 * <p>
 * 调用 {@code Arrays.sort(filenames);} 时，得到 {@code {"xyz1.doc", "xyz12.doc", "xyz2.doc" }}
 * </p>
 * <p>
 * 调用 {@code Arrays.sort(filenames, new WindowsCompare());} 时，得到 {@code {"xyz1.doc", "xyz2.doc", "xyz12.doc"
 * }}，这与在资源管理器中看到的相同
 * </p>
 *
 * @author Kimi Liu
 * @see <a href="https://stackoverflow.com/questions/23205020/java-sort-strings-like-windows-explorer">Java - Sort
 *      Strings like Windows Explorer</a>
 * @since Java 17+
 */
public class ExplorerCompare implements Comparator<CharSequence> {

    /**
     * 单例
     */
    public static final ExplorerCompare INSTANCE = new ExplorerCompare();

    @Override
    public int compare(final CharSequence str1, final CharSequence str2) {
        final Iterator<String> i1 = splitStringPreserveDelimiter(str1).iterator();
        final Iterator<String> i2 = splitStringPreserveDelimiter(str2).iterator();
        while (true) {
            // 直到这里都是平等的
            if (!i1.hasNext() && !i2.hasNext()) {
                return 0;
            }
            // i1 没有其他部分 -> 排在最前面
            if (!i1.hasNext()) {
                return -1;
            }
            // i1 的部分比 i2 多 -> 紧随其后
            if (!i2.hasNext()) {
                return 1;
            }

            final String data1 = i1.next();
            final String data2 = i2.next();
            int result;
            try {
                // 如果两个数据都是数字，则比较数字
                result = Long.compare(Long.parseLong(data1), Long.parseLong(data2));
                // 如果数字相等，则较长者优先
                if (result == 0) {
                    result = -Integer.compare(data1.length(), data2.length());
                }
            } catch (final NumberFormatException ex) {
                // 比较文本不区分大小写
                result = data1.compareToIgnoreCase(data2);
            }

            if (result != 0) {
                return result;
            }
        }
    }

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
