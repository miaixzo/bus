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
package org.miaixz.bus.core.text;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Naming convention encapsulation, mainly for camel case naming, connector naming, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamingCase {

    /**
     * Converts a camel case string to an underscore case (SnakeCase, underScoreCase). If the input camel case string is
     * empty, an empty string is returned. Rules:
     * <ul>
     * <li>Words are separated by underscores.</li>
     * <li>The first letter of each word is lowercase.</li>
     * </ul>
     * Examples:
     * 
     * <pre>
     * HelloWorld = hello_world
     * Hello_World = hello_world
     * HelloWorld_test = hello_world_test
     * </pre>
     *
     * @param text The camel case string to convert, can also be in underscore format.
     * @return The converted string in underscore case.
     */
    public static String toUnderlineCase(final CharSequence text) {
        return toSymbolCase(text, Symbol.C_UNDERLINE);
    }

    /**
     * Converts a camel case string to a kebab case (hyphen-separated). If the input camel case string is empty, an
     * empty string is returned. Rules:
     * <ul>
     * <li>Words are separated by hyphens.</li>
     * <li>The first letter of each word is lowercase.</li>
     * </ul>
     * Examples:
     * 
     * <pre>
     * HelloWorld = hello-world
     * Hello_World = hello-world
     * HelloWorld_test = hello-world-test
     * </pre>
     *
     * @param text The camel case string to convert, can also be in underscore format.
     * @return The converted string in kebab case.
     */
    public static String toKebabCase(final CharSequence text) {
        return toSymbolCase(text, Symbol.C_MINUS);
    }

    /**
     * Converts a camel case string to a symbol-separated string. If the input camel case string is empty, an empty
     * string is returned.
     *
     * @param text   The camel case string to convert, can also be in symbol-separated format.
     * @param symbol The separator character.
     * @return The converted string in symbol-separated format.
     */
    public static String toSymbolCase(final CharSequence text, final char symbol) {
        if (text == null) {
            return null;
        }

        final int length = text.length();
        final StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < length; i++) {
            c = text.charAt(i);
            if (Character.isUpperCase(c)) {
                final Character preChar = (i > 0) ? text.charAt(i - 1) : null;
                final Character nextChar = (i < text.length() - 1) ? text.charAt(i + 1) : null;

                if (null != preChar) {
                    if (symbol == preChar) {
                        // Previous character is a separator
                        if (null == nextChar || Character.isLowerCase(nextChar)) {
                            // Normal uppercase first letter, e.g., _Abb -> _abb
                            c = Character.toLowerCase(c);
                        }
                        // Next character is uppercase, treat as proper noun, e.g., _AB -> _AB
                    } else if (Character.isLowerCase(preChar)) {
                        // Previous character is lowercase
                        sb.append(symbol);
                        if (null == nextChar || Character.isLowerCase(nextChar) || CharKit.isNumber(nextChar)) {
                            // Normal uppercase first letter, e.g., aBcc -> a_bcc
                            c = Character.toLowerCase(c);
                        }
                        // Next character is uppercase, treat as proper noun, e.g., aBC -> a_BC
                    } else {
                        // Previous character is uppercase
                        if (null != nextChar && Character.isLowerCase(nextChar)) {
                            // Normal uppercase first letter, e.g., ABcc -> A_bcc
                            sb.append(symbol);
                            c = Character.toLowerCase(c);
                        }
                        // Next character is uppercase, treat as proper noun, e.g., ABC -> ABC
                    }
                } else {
                    // First character, need to determine whether to convert to lowercase based on the next character
                    if (null == nextChar || Character.isLowerCase(nextChar)) {
                        // Normal uppercase first letter, e.g., Abc -> abc
                        c = Character.toLowerCase(c);
                    }
                    // Next character is uppercase, treat as proper noun, e.g., ABC -> ABC
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Converts an underscore-separated string to PascalCase. Rules:
     * <ul>
     * <li>Words are not separated by spaces or any connectors.</li>
     * <li>The first letter of the first word is uppercase.</li>
     * <li>The first letter of subsequent words is also uppercase.</li>
     * </ul>
     * If the input underscore-separated string is empty, an empty string is returned. Example: hello_world = HelloWorld
     *
     * @param name The underscore-separated string to convert.
     * @return The converted string in PascalCase.
     */
    public static String toPascalCase(final CharSequence name) {
        return StringKit.upperFirst(toCamelCase(name));
    }

    /**
     * Converts an underscore-separated string to camelCase. If the input underscore-separated string is empty, an empty
     * string is returned. Rules:
     * <ul>
     * <li>Words are not separated by spaces or any connectors.</li>
     * <li>The first letter of the first word is lowercase.</li>
     * <li>The first letter of subsequent words is uppercase.</li>
     * </ul>
     * Example: hello_world = helloWorld
     *
     * @param name The underscore-separated string to convert.
     * @return The converted string in camelCase.
     */
    public static String toCamelCase(final CharSequence name) {
        return toCamelCase(name, Symbol.C_UNDERLINE);
    }

    /**
     * Converts a symbol-separated string to camelCase. If the input symbol-separated string is empty, an empty string
     * is returned.
     *
     * @param name   The custom symbol-separated string to convert.
     * @param symbol The separator character in the original string.
     * @return The converted string in camelCase.
     */
    public static String toCamelCase(final CharSequence name, final char symbol) {
        return toCamelCase(name, symbol, true);
    }

    /**
     * Converts a symbol-separated string to camelCase. If the input symbol-separated string is empty, an empty string
     * is returned. When {@code otherCharToLower} is {@code true}, the following cases apply:
     * <ul>
     * <li>If the given string is all uppercase, it is converted to lowercase, e.g., NAME becomes name.</li>
     * <li>If the given string is mixed case, it is assumed to be already in camel case, and only the first letter is
     * lowercased, e.g., TableName becomes tableName.</li>
     * </ul>
     *
     * @param name             The custom symbol-separated string to convert.
     * @param symbol           The separator character in the original string.
     * @param otherCharToLower Whether other characters after the separator need to be converted to lowercase.
     * @return The converted string in camelCase.
     */
    public static String toCamelCase(final CharSequence name, final char symbol, final boolean otherCharToLower) {
        if (null == name) {
            return null;
        }

        final String name2 = name.toString();
        if (StringKit.contains(name2, symbol)) {
            final int length = name2.length();
            final StringBuilder sb = new StringBuilder(length);
            boolean upperCase = false;
            for (int i = 0; i < length; i++) {
                final char c = name2.charAt(i);

                if (c == symbol) {
                    upperCase = true;
                } else if (upperCase) {
                    sb.append(Character.toUpperCase(c));
                    upperCase = false;
                } else {
                    sb.append(otherCharToLower ? Character.toLowerCase(c) : c);
                }
            }
            return sb.toString();
        } else {
            if (otherCharToLower) {
                if (StringKit.isUpperCase(name2)) {
                    return name2.toLowerCase();
                }
                return StringKit.lowerFirst(name2);
            }

            return name2;
        }
    }

    /**
     * Checks if all letters in the given string are uppercase. The criteria are as follows:
     * 
     * <pre>
     * 1. Uppercase letters include A-Z.
     * 2. Other non-letter Unicode characters are also considered uppercase.
     * </pre>
     *
     * @param text The string to check.
     * @return {@code true} if all letters are uppercase, {@code false} otherwise.
     */
    public static boolean isUpperCase(final CharSequence text) {
        if (null == text) {
            return false;
        }
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            if (Character.isLowerCase(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all letters in the given string are lowercase. The criteria are as follows:
     * 
     * <pre>
     * 1. Lowercase letters include a-z.
     * 2. Other non-letter Unicode characters are also considered lowercase.
     * </pre>
     *
     * @param text The string to check.
     * @return {@code true} if all letters are lowercase, {@code false} otherwise.
     */
    public static boolean isLowerCase(final CharSequence text) {
        if (null == text) {
            return false;
        }
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Swaps the case of letters in the given string. Uppercase becomes lowercase, and lowercase becomes uppercase.
     *
     * <pre>
     * StringKit.swapCase(null)                 = null
     * StringKit.swapCase("")                   = ""
     * StringKit.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
     * </pre>
     *
     * @param text The string to swap case.
     * @return The string with swapped case.
     */
    public static String swapCase(final String text) {
        if (StringKit.isEmpty(text)) {
            return text;
        }

        final char[] buffer = text.toCharArray();

        for (int i = 0; i < buffer.length; i++) {
            final char ch = buffer[i];
            if (Character.isUpperCase(ch) || Character.isTitleCase(ch)) {
                buffer[i] = Character.toLowerCase(ch);
            } else if (Character.isLowerCase(ch)) {
                buffer[i] = Character.toUpperCase(ch);
            }
        }
        return new String(buffer);
    }

}
