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
package org.miaixz.bus.extra.emoji;

import java.util.List;
import java.util.Set;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.EmojiParser.FitzpatrickAction;

/**
 * A utility class for handling emoji characters, based on the emoji-java library. For detailed documentation and a list
 * of aliases, please refer to the emoji-java project:
 * <a href="https://github.com/vdurmont/emoji-java">https://github.com/vdurmont/emoji-java</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EmojiKit {

    /**
     * Checks if the given string consists of a single emoji character.
     *
     * @param text The string to be tested.
     * @return {@code true} if the string is a single emoji, {@code false} otherwise.
     */
    public static boolean isEmoji(final String text) {
        return EmojiManager.isEmoji(text);
    }

    /**
     * Checks if the given string contains any emoji characters.
     *
     * @param text The string to be tested.
     * @return {@code true} if the string contains at least one emoji, {@code false} otherwise.
     */
    public static boolean containsEmoji(final String text) {
        return EmojiManager.containsEmoji(text);
    }

    /**
     * Retrieves a set of all emojis associated with a given tag.
     *
     * @param tag The tag to search for, e.g., "happy".
     * @return A {@link Set} of {@link Emoji} objects, or null if no emojis are found for the tag.
     */
    public static Set<Emoji> getByTag(final String tag) {
        return EmojiManager.getForTag(tag);
    }

    /**
     * Retrieves an {@link Emoji} object for a given alias.
     *
     * @param alias The alias to search for, e.g., "smile".
     * @return The {@link Emoji} object, or null if the alias is not found.
     */
    public static Emoji get(final String alias) {
        return EmojiManager.getForAlias(alias);
    }

    /**
     * Converts emoji aliases (e.g., {@code :smile:}) and their HTML representations (e.g., {@code &#128516;}) in a
     * string to their corresponding Unicode emoji characters.
     * <p>
     * Examples:
     * 
     * <pre>
     *  {@code :smile:} is replaced by {@code 😄}
     *  {@code &#128516;} is replaced by {@code 😄}
     *  {@code :boy|type_6:} is replaced by {@code 👦🏿}
     * </pre>
     *
     * @param text The string containing emoji aliases or HTML representations.
     * @return The string with aliases and HTML representations replaced by Unicode characters.
     */
    public static String toUnicode(final String text) {
        return EmojiParser.parseToUnicode(text);
    }

    /**
     * Converts Unicode emoji characters in a string to their alias representation (e.g., {@code :smile:}). The default
     * {@link FitzpatrickAction} is {@link FitzpatrickAction#PARSE}, which includes the Fitzpatrick modifier type in the
     * alias.
     * <p>
     * Example: {@code 😄} is converted to {@code :smile:}
     * <p>
     * With {@link FitzpatrickAction#PARSE}: {@code 👦🏿} is converted to {@code :boy|type_6:}
     * <p>
     * With {@link FitzpatrickAction#REMOVE}: {@code 👦🏿} is converted to {@code :boy:}
     * <p>
     * With {@link FitzpatrickAction#IGNORE}: {@code 👦🏿} is converted to {@code :boy:🏿}
     *
     * @param text The string containing Unicode emoji characters.
     * @return The string with Unicode emojis replaced by their aliases.
     */
    public static String toAlias(final String text) {
        return toAlias(text, FitzpatrickAction.PARSE);
    }

    /**
     * Converts Unicode emoji characters in a string to their alias representation, with a specified
     * {@link FitzpatrickAction}.
     *
     * @param text              The string containing Unicode emoji characters.
     * @param fitzpatrickAction The action to perform for Fitzpatrick modifiers.
     * @return The string with Unicode emojis replaced by their aliases.
     */
    public static String toAlias(final String text, final FitzpatrickAction fitzpatrickAction) {
        return EmojiParser.parseToAliases(text, fitzpatrickAction);
    }

    /**
     * Converts Unicode emoji characters in a string to their HTML hexadecimal representation.
     * <p>
     * Example: {@code 👦🏿} is converted to {@code &#x1f466;}
     *
     * @param text The string containing Unicode emoji characters.
     * @return The string with Unicode emojis replaced by their HTML hexadecimal representations.
     */
    public static String toHtmlHex(final String text) {
        return toHtml(text, true);
    }

    /**
     * Converts Unicode emoji characters in a string to their HTML decimal representation.
     * <p>
     * Example: {@code 👦🏿} is converted to {@code &#128102;}
     *
     * @param text The string containing Unicode emoji characters.
     * @return The string with Unicode emojis replaced by their HTML decimal representations.
     */
    public static String toHtml(final String text) {
        return toHtml(text, false);
    }

    /**
     * Converts Unicode emoji characters in a string to their HTML representation (either hexadecimal or decimal).
     * <p>
     * Examples:
     * 
     * <pre>
     * If isHex is true: {@code 👦🏿} is converted to {@code &#x1f466;}
     * If isHex is false: {@code 👦🏿} is converted to {@code &#128102;}
     * </pre>
     *
     * @param text  The string containing Unicode emoji characters.
     * @param isHex If {@code true}, converts to hexadecimal; otherwise, converts to decimal.
     * @return The string with Unicode emojis replaced by their HTML representations.
     */
    public static String toHtml(final String text, final boolean isHex) {
        return isHex ? EmojiParser.parseToHtmlHexadecimal(text) : EmojiParser.parseToHtmlDecimal(text);
    }

    /**
     * Removes all emoji characters from a string.
     *
     * @param text The string containing emoji characters.
     * @return The string with all emoji characters removed.
     */
    public static String removeAllEmojis(final String text) {
        return EmojiParser.removeAllEmojis(text);
    }

    /**
     * Extracts all emoji characters from a string.
     *
     * @param text The string containing emoji characters.
     * @return A {@link List} of all emoji characters found in the string.
     */
    public static List<String> extractEmojis(final String text) {
        return EmojiParser.extractEmojis(text);
    }

}
