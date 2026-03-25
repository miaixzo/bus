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
package org.miaixz.bus.extra.pinyin;

import java.util.List;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Pinyin engine interface. Implementations of this interface provide specific Pinyin conversion functionalities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PinyinProvider extends Provider {

    /**
     * Converts a single character to its Pinyin. If the character is a Chinese character, its uppercase Pinyin is
     * returned; otherwise, the character itself is returned as a string.
     *
     * @param c The character to convert.
     * @return The Pinyin of the Chinese character, or the character itself if not Chinese.
     */
    default String getPinyin(final char c) {
        return getPinyin(c, false);
    }

    /**
     * Converts a single character to its Pinyin, with an option to retain tone marks. If the character is a Chinese
     * character, its Pinyin is returned; otherwise, the character itself is returned as a string.
     *
     * @param c    The character to convert.
     * @param tone {@code true} to retain tone marks in the Pinyin, {@code false} otherwise.
     * @return The Pinyin of the Chinese character, or the character itself if not Chinese.
     */
    String getPinyin(char c, boolean tone);

    /**
     * Retrieves the complete Pinyin for a given string. Non-Chinese characters remain as is.
     *
     * @param text      The input string.
     * @param separator The separator to use between each Pinyin character.
     * @return The Pinyin representation of the string.
     */
    default String getPinyin(final String text, final String separator) {
        return getPinyin(text, separator, false);
    }

    /**
     * Retrieves the complete Pinyin for a given string, with an option to retain tone marks. Non-Chinese characters
     * remain as is.
     *
     * @param text      The input string.
     * @param separator The separator to use between each Pinyin character.
     * @param tone      {@code true} to retain tone marks in the Pinyin, {@code false} otherwise.
     * @return The Pinyin representation of the string.
     */
    String getPinyin(String text, String separator, boolean tone);

    /**
     * Converts a single character to its Pinyin first letter. If the character is a Chinese character, its Pinyin's
     * first letter is returned; otherwise, the character itself is returned.
     *
     * @param c The character to convert.
     * @return The first letter of the Pinyin for the Chinese character, or the character itself if not Chinese.
     */
    default char getFirstLetter(final char c) {
        return getPinyin(c).charAt(0);
    }

    /**
     * Converts a string to its Pinyin first letters, with a custom separator. Non-Chinese characters remain as is.
     *
     * @param str       The input string.
     * @param separator The separator to use between each first letter.
     * @return The string composed of the first letters of Pinyin for Chinese characters, and original characters for
     *         others.
     */
    default String getFirstLetter(final String str, final String separator) {
        final String splitSeparator = StringKit.isEmpty(separator) ? Symbol.HASH : separator;
        final List<String> split = CharsBacker.split(getPinyin(str, splitSeparator), splitSeparator);
        return CollKit.join(split, separator, (s) -> String.valueOf(!s.isEmpty() ? s.charAt(0) : Normal.EMPTY));
    }

    @Override
    default Object type() {
        return EnumValue.Povider.PINYIN;
    }

}
