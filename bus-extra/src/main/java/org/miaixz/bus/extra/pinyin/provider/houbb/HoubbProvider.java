/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
