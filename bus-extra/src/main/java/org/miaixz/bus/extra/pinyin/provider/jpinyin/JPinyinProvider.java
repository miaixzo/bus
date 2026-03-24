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
package org.miaixz.bus.extra.pinyin.provider.jpinyin;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.extra.pinyin.PinyinProvider;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

/**
 * Encapsulates the Jpinyin engine.
 * <p>
 * To introduce (dependency):
 * 
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.github.stuxuhai&lt;/groupId&gt;
 *     &lt;artifactId&gt;jpinyin&lt;/artifactId&gt;
 *     &lt;version&gt;1.1.8&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JPinyinProvider implements PinyinProvider {

    /**
     * Constructs a new JPinyinProvider instance. Checks if the Jpinyin library is available (via {@link PinyinHelper}
     * class).
     */
    public JPinyinProvider() {
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
        final String[] results = PinyinHelper
                .convertToPinyinArray(c, tone ? PinyinFormat.WITH_TONE_MARK : PinyinFormat.WITHOUT_TONE);
        return ArrayKit.isEmpty(results) ? String.valueOf(c) : results[0];
    }

    /**
     * Gets the pinyin of a string. This method is designed to be overridden by subclasses for custom pinyin conversion.
     *
     * Subclasses may override to add custom conversion logic or error handling.
     *
     * @param str       The string to convert.
     * @param separator The separator to use between pinyin strings.
     * @param tone      Whether to include tone marks.
     * @return The pinyin string.
     * @throws InternalException if a pinyin exception occurs.
     */
    @Override
    public String getPinyin(final String str, final String separator, final boolean tone) {
        try {
            return PinyinHelper.convertToPinyinString(
                    str,
                    separator,
                    tone ? PinyinFormat.WITH_TONE_MARK : PinyinFormat.WITHOUT_TONE);
        } catch (final PinyinException e) {
            throw new InternalException(e);
        }
    }

}
