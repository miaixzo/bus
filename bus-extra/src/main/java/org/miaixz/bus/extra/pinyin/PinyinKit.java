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
package org.miaixz.bus.extra.pinyin;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.PatternKit;

/**
 * Pinyin utility class for quickly obtaining Pinyin.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PinyinKit {

    /**
     * Creates a Pinyin engine.
     *
     * @param engineName The engine name.
     * @return {@link PinyinProvider}
     */
    public static PinyinProvider createEngine(final String engineName) {
        return PinyinFactory.of(engineName);
    }

    /**
     * Gets the global singleton Pinyin engine.
     *
     * @return The global singleton Pinyin engine.
     */
    public static PinyinProvider getEngine() {
        return PinyinFactory.get();
    }

    /**
     * If c is a Chinese character, returns its uppercase Pinyin; otherwise, returns String.valueOf(c).
     *
     * @param c Any character. Chinese characters are converted to Pinyin, others are returned as is.
     * @return Chinese characters are converted to Pinyin, others are returned as is.
     */
    public static String getPinyin(final char c) {
        return getEngine().getPinyin(c);
    }

    /**
     * If c is a Chinese character, returns its uppercase Pinyin; otherwise, returns String.valueOf(c).
     *
     * @param c    Any character. Chinese characters are converted to Pinyin, others are returned as is.
     * @param tone Whether to include tone marks.
     * @return Chinese characters are converted to Pinyin, others are returned as is.
     */
    public static String getPinyin(final char c, final boolean tone) {
        return getEngine().getPinyin(c, tone);
    }

    /**
     * Converts the input string to Pinyin, with spaces separating the Pinyin of each character.
     *
     * @param text Any character. Chinese characters are converted to Pinyin, others are returned as is.
     * @return Chinese characters are converted to Pinyin, others are returned as is.
     */
    public static String getPinyin(final String text) {
        return getPinyin(text, Symbol.SPACE);
    }

    /**
     * Converts the input string to Pinyin, with spaces separating the Pinyin of each character.
     *
     * @param text Any character. Chinese characters are converted to Pinyin, others are returned as is.
     * @param tone Whether to include tone marks.
     * @return Chinese characters are converted to Pinyin, others are returned as is.
     */
    public static String getPinyin(final String text, final boolean tone) {
        return getPinyin(text, Symbol.SPACE, tone);
    }

    /**
     * Converts the input string to Pinyin, inserting a separator between the Pinyin of each character.
     *
     * @param text      Any character. Chinese characters are converted to Pinyin, others are returned as is.
     * @param separator The separator between the Pinyin of each character.
     * @return Chinese characters are converted to Pinyin, others are returned as is.
     */
    public static String getPinyin(final String text, final String separator) {
        return getEngine().getPinyin(text, separator);
    }

    /**
     * Converts the input string to Pinyin, inserting a separator between the Pinyin of each character.
     *
     * @param text      Any character. Chinese characters are converted to Pinyin, others are returned as is.
     * @param separator The separator between the Pinyin of each character.
     * @param tone      Whether to include tone marks.
     * @return Chinese characters are converted to Pinyin, others are returned as is.
     */
    public static String getPinyin(final String text, final String separator, final boolean tone) {
        return getEngine().getPinyin(text, separator, tone);
    }

    /**
     * Converts the input character to the first letter of its Pinyin; other characters are returned as is.
     *
     * @param c Any character. Chinese characters are converted to the first letter of their Pinyin, others are returned
     *          as is.
     * @return The first letter of the Pinyin for Chinese characters, or the original character for others.
     */
    public static char getFirstLetter(final char c) {
        return getEngine().getFirstLetter(c);
    }

    /**
     * Converts the input string to the first letter of its Pinyin; other characters are returned as is.
     *
     * @param text      Any character string. Chinese characters are converted to the first letter of their Pinyin,
     *                  others are returned as is.
     * @param separator The separator.
     * @return The first letters of the Pinyin for Chinese characters, or the original characters for others.
     */
    public static String getFirstLetter(final String text, final String separator) {
        return (text == null) ? null : getEngine().getFirstLetter(text, separator);
    }

    /**
     * Checks if a character is a Chinese character.
     *
     * @param c The character.
     * @return Whether the character is a Chinese character.
     */
    public static boolean isChinese(final char c) {
        return '〇' == c || PatternKit.isMatch(Pattern.CHINESE_PATTERN, String.valueOf(c));
    }

}
