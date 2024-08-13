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
package org.miaixz.bus.extra.pinyin.provider.houbb;

import org.miaixz.bus.extra.pinyin.PinyinProvider;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;

/**
 * 封装了 houbb Pinyin 的引擎。
 *
 * <p>
 * houbb pinyin(https://github.com/houbb/pinyin)封装。
 * </p>
 *
 * <p>
 * 引入：
 * 
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.github.houbb&lt;/groupId&gt;
 *     &lt;artifactId&gt;pinyin&lt;/artifactId&gt;
 *     &lt;version&gt;0.2.0&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HoubbProvider implements PinyinProvider {

    // 汉字拼音输出的格式
    private PinyinStyleEnum format;

    /**
     * 构造
     */
    public HoubbProvider() {
        this(null);
    }

    /**
     * 构造
     *
     * @param format 格式
     */
    public HoubbProvider(final PinyinStyleEnum format) {
        init(format);
    }

    /**
     * 初始化
     *
     * @param format 格式
     */
    public void init(PinyinStyleEnum format) {
        if (null == format) {
            format = PinyinStyleEnum.NORMAL;
        }
        this.format = format;
    }

    @Override
    public String getPinyin(final char c) {
        final String result;
        result = PinyinHelper.toPinyin(String.valueOf(c), format);
        return result;
    }

    @Override
    public String getPinyin(final String text, final String separator) {
        final String result;
        result = PinyinHelper.toPinyin(text, format, separator);
        return result;
    }

}
