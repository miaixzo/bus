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
package org.miaixz.bus.extra.pinyin.provider.tinypinyin;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.pinyin.PinyinProvider;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * Encapsulates the TinyPinyin engine.
 *
 * <p>
 * The TinyPinyin (https://github.com/promeG/TinyPinyin) provider has not been submitted to Maven Central. Therefore,
 * the version packaged by https://github.com/biezhi/TinyPinyin is used.
 * </p>
 *
 * <p>
 * To introduce (dependency):
 * 
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;io.github.biezhi&lt;/groupId&gt;
 *     &lt;artifactId&gt;TinyPinyin&lt;/artifactId&gt;
 *     &lt;version&gt;2.0.3.RELEASE&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TinyPinyinProvider implements PinyinProvider {

    /**
     * Constructs a new TinyPinyinProvider instance. Initializes TinyPinyin with default configuration.
     */
    public TinyPinyinProvider() {
        this(null);
    }

    /**
     * Constructs a new TinyPinyinProvider instance with a custom configuration.
     *
     * @param config The {@link Pinyin.Config} to use for TinyPinyin initialization.
     */
    public TinyPinyinProvider(final Pinyin.Config config) {
        Pinyin.init(config);
    }

    /**
     * Gets the pinyin of a single character. This method is designed to be overridden by subclasses for custom pinyin
     * conversion.
     *
     * Note that TinyPinyin does not support tone marks, so the tone parameter is ignored. Subclasses may override to
     * add custom conversion logic.
     *
     * @param c    The character to convert.
     * @param tone Whether to include tone marks (ignored in this implementation).
     * @return The pinyin string in lowercase, or the original character if not Chinese.
     */
    @Override
    public String getPinyin(final char c, final boolean tone) {
        if (!Pinyin.isChinese(c)) {
            return String.valueOf(c);
        }
        // TinyPinyin does not support tone marks, so the 'tone' parameter is ignored.
        return Pinyin.toPinyin(c).toLowerCase();
    }

    /**
     * Gets the pinyin of a string. This method is designed to be overridden by subclasses for custom pinyin conversion.
     *
     * Note that TinyPinyin does not support tone marks, so the tone parameter is ignored. Subclasses may override to
     * add custom conversion logic.
     *
     * @param str       The string to convert.
     * @param separator The separator to use between pinyin strings.
     * @param tone      Whether to include tone marks (ignored in this implementation).
     * @return The pinyin string in lowercase.
     */
    @Override
    public String getPinyin(final String str, final String separator, final boolean tone) {
        // TinyPinyin does not support tone marks, so the 'tone' parameter is ignored.
        final String pinyin = Pinyin.toPinyin(str, separator);
        return StringKit.isEmpty(pinyin) ? pinyin : pinyin.toLowerCase();
    }

}
