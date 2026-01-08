/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.pinyin.PinyinProvider;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;

/**
 * Encapsulates the houbb Pinyin engine.
 *
 * <p>
 * houbb pinyin (<a href="https://github.com/houbb/pinyin">https://github.com/houbb/pinyin</a>) encapsulation.
 * </p>
 *
 * <p>
 * To introduce (dependency):
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

    /**
     * Constructs a new HoubbProvider instance. Checks if the houbb pinyin library is available (via
     * {@link PinyinHelper} class).
     */
    public HoubbProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(PinyinHelper.class);
    }

    /**
     * Gets the pinyin of a single character. This method is designed to be overridden by subclasses for custom pinyin
     * conversion.
     *
     * Subclasses may override to add custom conversion logic.
     *
     * @param c    The character to convert.
     * @param tone Whether to include tone marks.
     * @return The pinyin string.
     */
    @Override
    public String getPinyin(final char c, final boolean tone) {
        final String result;
        result = PinyinHelper.toPinyin(String.valueOf(c), tone ? PinyinStyleEnum.DEFAULT : PinyinStyleEnum.NORMAL);
        return result;
    }

    /**
     * Gets the pinyin of a string. This method is designed to be overridden by subclasses for custom pinyin conversion.
     *
     * Subclasses may override to add custom conversion logic.
     *
     * @param str       The string to convert.
     * @param separator The separator to use between pinyin strings.
     * @param tone      Whether to include tone marks.
     * @return The pinyin string.
     */
    @Override
    public String getPinyin(final String str, final String separator, final boolean tone) {
        final String result;
        result = PinyinHelper.toPinyin(str, tone ? PinyinStyleEnum.DEFAULT : PinyinStyleEnum.NORMAL, separator);
        return result;
    }

}
