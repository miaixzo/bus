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
package org.miaixz.bus.extra.pinyin.provider.bopomofo4j;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.extra.pinyin.PinyinProvider;

import com.rnkrsoft.bopomofo4j.Bopomofo4j;
import com.rnkrsoft.bopomofo4j.ToneType;

/**
 * Encapsulates the Bopomofo4j engine.
 *
 * <p>
 * Bopomofo4j encapsulation, project:
 * <a href="https://gitee.com/rnkrsoft/Bopomofo4j">https://gitee.com/rnkrsoft/Bopomofo4j</a>.
 * </p>
 *
 * <p>
 * To introduce (dependency):
 * 
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.rnkrsoft.bopomofo4j&lt;/groupId&gt;
 *     &lt;artifactId&gt;bopomofo4j&lt;/artifactId&gt;
 *     &lt;version&gt;1.0.0&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Bopomofo4JProvider implements PinyinProvider {

    /**
     * Constructs a new Bopomofo4JProvider instance. Initializes the Bopomofo4j library for local use.
     */
    public Bopomofo4JProvider() {
        Bopomofo4j.local();
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
        return Bopomofo4j.pinyin(
                String.valueOf(c),
                tone ? ToneType.WITH_VOWEL_TONE : ToneType.WITHOUT_TONE,
                false,
                false,
                Normal.EMPTY);
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
        return Bopomofo4j.pinyin(str, tone ? ToneType.WITH_VOWEL_TONE : ToneType.WITHOUT_TONE, false, false, separator);
    }

}
