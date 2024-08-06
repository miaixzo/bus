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
package org.miaixz.bus.office.excel;

import org.apache.poi.openxml4j.util.ZipSecureFile;

/**
 * POI的全局设置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelConfig {

    /**
     * 设置解压时的最小压缩比例 为了避免`Zip Bomb`，POI中设置了最小压缩比例，这个比例为：
     * 
     * <pre>
     * 压缩后的大小 / 解压后的大小
     * </pre>
     * 
     * POI的默认值是0.01（即最小压缩到1%），如果文档中的文件压缩比例小于这个值，就会报错。 如果文件中确实存在高压缩比的文件，可以通过这个全局方法自定义比例，从而避免错误。
     *
     * @param ratio 解压后的文件大小与原始文件大小的最小比率，小于等于0表示不检查
     */
    public static void setMinInflateRatio(final double ratio) {
        ZipSecureFile.setMinInflateRatio(ratio);
    }

    /**
     * 设置单个Zip文件中最大文件大小，默认为4GB，即32位zip格式的最大值。
     *
     * @param maxEntrySize 单个Zip文件中最大文件大小，必须大于0
     */
    public static void setMaxEntrySize(final long maxEntrySize) {
        ZipSecureFile.setMaxEntrySize(maxEntrySize);
    }

    /**
     * 设置解压前文本的最大字符数，超过抛出异常。
     *
     * @param maxTextSize 文本的最大字符数
     * @throws IllegalArgumentException for negative maxTextSize
     */
    public static void setMaxTextSize(final long maxTextSize) {
        ZipSecureFile.setMaxTextSize(maxTextSize);
    }

}
