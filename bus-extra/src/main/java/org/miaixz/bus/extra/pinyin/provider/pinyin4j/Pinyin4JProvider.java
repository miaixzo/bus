/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.pinyin.provider.pinyin4j;

import org.apache.logging.log4j.util.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.extra.pinyin.PinyinProvider;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Encapsulates the Pinyin4j engine.
 *
 * <p>
 * Pinyin4j (<a href="http://sourceforge.net/projects/pinyin4j">http://sourceforge.net/projects/pinyin4j</a>)
 * encapsulation.
 * </p>
 *
 * <p>
 * To introduce (dependency):
 * 
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.belerweb&lt;/groupId&gt;
 *     &lt;artifactId&gt;pinyin4j&lt;/artifactId&gt;
 *     &lt;version&gt;2.5.1&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Pinyin4JProvider implements PinyinProvider {

    /**
     * HanyuPinyinOutputFormat instance for Pinyin with tone marks. Configured for lowercase, 'v' for 'ü', and tone
     * marks.
     */
    private static final HanyuPinyinOutputFormat WITH_TONE_MARK;
    /**
     * HanyuPinyinOutputFormat instance for Pinyin without tone marks. Configured for lowercase, 'v' for 'ü', and no
     * tone marks.
     */
    private static final HanyuPinyinOutputFormat WITHOUT_TONE;
    static {
        WITH_TONE_MARK = new HanyuPinyinOutputFormat();
        // lowercase
        WITH_TONE_MARK.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 'ü' uses "v" instead
        WITH_TONE_MARK.setVCharType(HanyuPinyinVCharType.WITH_V);
        WITH_TONE_MARK.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);

        WITHOUT_TONE = new HanyuPinyinOutputFormat();
        // lowercase
        WITHOUT_TONE.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 'ü' uses "v" instead
        WITHOUT_TONE.setVCharType(HanyuPinyinVCharType.WITH_V);
        WITHOUT_TONE.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * Constructs a new Pinyin4JProvider instance. Checks if the Pinyin4j library is available (via {@link PinyinHelper}
     * class).
     */
    public Pinyin4JProvider() {
        // Check if the library is introduced when loading via SPI
        Assert.notNull(PinyinHelper.class);
    }

    /**
     * Gets the pinyin of a single character. This method is designed to be overridden by subclasses for custom pinyin
     * conversion.
     *
     * and returns the first result. Subclasses may override to add custom conversion logic.
     *
     * @param c    The character to convert.
     * @param tone Whether to include tone marks.
     * @return The pinyin string, or the original character if no pinyin is available.
     */
    @Override
    public String getPinyin(final char c, final boolean tone) {
        String result;
        try {
            final String[] results = PinyinHelper.toHanyuPinyinStringArray(c, tone ? WITH_TONE_MARK : WITHOUT_TONE);
            result = ArrayKit.isEmpty(results) ? String.valueOf(c) : results[0];
        } catch (final BadHanyuPinyinOutputFormatCombination e) {
            result = String.valueOf(c);
        }
        return result;
    }

    /**
     * Gets the pinyin of a string. This method is designed to be overridden by subclasses for custom pinyin conversion.
     *
     * and joining with the separator. Subclasses may override to add custom conversion logic.
     *
     * @param str       The string to convert.
     * @param separator The separator to use between pinyin strings.
     * @param tone      Whether to include tone marks.
     * @return The pinyin string.
     * @throws InternalException if a bad pinyin output format combination occurs.
     */
    @Override
    public String getPinyin(final String str, final String separator, final boolean tone) {
        final StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        final int strLen = str.length();
        try {
            for (int i = 0; i < strLen; i++) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    result.append(separator);
                }
                final String[] pinyinStringArray = PinyinHelper
                        .toHanyuPinyinStringArray(str.charAt(i), tone ? WITH_TONE_MARK : WITHOUT_TONE);
                if (ArrayKit.isEmpty(pinyinStringArray)) {
                    result.append(str.charAt(i));
                } else {
                    result.append(pinyinStringArray[0]);
                }
            }
        } catch (final BadHanyuPinyinOutputFormatCombination e) {
            throw new InternalException(e);
        }
        return result.toString();
    }

}
