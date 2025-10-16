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
package org.miaixz.bus.core.xyz;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.center.regex.RegexValidator;
import org.miaixz.bus.core.compare.LengthCompare;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.Validator;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

/**
 * Regular expression utility class. For common regex patterns, see {@link Validator}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternKit extends RegexValidator {

    /**
     * Keywords in regular expressions that need to be escaped.
     */
    public static final Set<Character> RE_KEYS = SetKit
            .of('$', '(', ')', '*', '+', '.', '[', ']', '?', '\\', '^', '{', '}', '|');

    /**
     * Gets the matched string for group 0.
     *
     * @param regex   The regex.
     * @param content The content to match against.
     * @return The matched string, or `null` if no match.
     */
    public static String getGroup0(final String regex, final CharSequence content) {
        return get(regex, content, 0);
    }

    /**
     * Gets the matched string for group 1.
     *
     * @param regex   The regex.
     * @param content The content to match against.
     * @return The matched string, or `null` if no match.
     */
    public static String getGroup1(final String regex, final CharSequence content) {
        return get(regex, content, 1);
    }

    /**
     * Gets the matched string for a specific group.
     *
     * @param regex      The regex.
     * @param content    The content to match against.
     * @param groupIndex The group index.
     * @return The matched string, or `null` if no match.
     */
    public static String get(final String regex, final CharSequence content, final int groupIndex) {
        if (null == content || null == regex) {
            return null;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return get(pattern, content, groupIndex);
    }

    /**
     * Gets the matched string for a specific named group.
     *
     * @param regex     The regex.
     * @param content   The content to match against.
     * @param groupName The name of the group.
     * @return The matched string, or `null` if no match.
     */
    public static String get(final String regex, final CharSequence content, final String groupName) {
        if (null == content || null == regex) {
            return null;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return get(pattern, content, groupName);
    }

    /**
     * Gets the matched string for group 0 from a compiled pattern.
     *
     * @param pattern The compiled regex pattern.
     * @param content The content to match against.
     * @return The matched string, or `null` if no match.
     */
    public static String getGroup0(final java.util.regex.Pattern pattern, final CharSequence content) {
        return get(pattern, content, 0);
    }

    /**
     * Gets the matched string for group 1 from a compiled pattern.
     *
     * @param pattern The compiled regex pattern.
     * @param content The content to match against.
     * @return The matched string, or `null` if no match.
     */
    public static String getGroup1(final java.util.regex.Pattern pattern, final CharSequence content) {
        return get(pattern, content, 1);
    }

    /**
     * Gets the matched string for a specific group index from a compiled pattern.
     *
     * @param pattern    The compiled regex pattern.
     * @param content    The content to match against.
     * @param groupIndex The group index (0 for the full match).
     * @return The matched string, or `null` if no match.
     */
    public static String get(final java.util.regex.Pattern pattern, final CharSequence content, final int groupIndex) {
        if (null == content || null == pattern) {
            return null;
        }

        final MutableObject<String> result = new MutableObject<>();
        get(pattern, content, matcher -> result.set(matcher.group(groupIndex)));
        return result.get();
    }

    /**
     * Gets the matched string for a specific named group from a compiled pattern.
     *
     * @param pattern   The compiled regex pattern.
     * @param content   The content to match against.
     * @param groupName The name of the group.
     * @return The matched string, or `null` if no match.
     */
    public static String get(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final String groupName) {
        if (null == content || null == pattern || null == groupName) {
            return null;
        }

        final MutableObject<String> result = new MutableObject<>();
        get(pattern, content, matcher -> result.set(matcher.group(groupName)));
        return result.get();
    }

    /**
     * Finds the first match and processes it with a consumer.
     *
     * @param pattern  The compiled regex pattern.
     * @param content  The content to match against.
     * @param consumer The consumer for the `Matcher`.
     */
    public static void get(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final Consumer<Matcher> consumer) {
        if (null == content || null == pattern || null == consumer) {
            return;
        }
        final Matcher m = pattern.matcher(content);
        if (m.find()) {
            consumer.accept(m);
        }
    }

    /**
     * Gets all captured groups from the first match.
     *
     * @param pattern The compiled regex pattern.
     * @param content The content to match against.
     * @return A list of all captured groups.
     */
    public static List<String> getAllGroups(final java.util.regex.Pattern pattern, final CharSequence content) {
        return getAllGroups(pattern, content, true);
    }

    /**
     * Gets all captured groups from the first match.
     *
     * @param pattern    The compiled regex pattern.
     * @param content    The content to match against.
     * @param withGroup0 If `true`, includes group 0 (the full match).
     * @return A list of all captured groups.
     */
    public static List<String> getAllGroups(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final boolean withGroup0) {
        return getAllGroups(pattern, content, withGroup0, false);
    }

    /**
     * Gets all captured groups from all matches.
     *
     * @param pattern    The compiled regex pattern.
     * @param content    The content to match against.
     * @param withGroup0 If `true`, includes group 0.
     * @param findAll    If `true`, finds all matches, not just the first one.
     * @return A list of all captured groups.
     */
    public static List<String> getAllGroups(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final boolean withGroup0,
            final boolean findAll) {
        if (null == content || null == pattern) {
            return null;
        }

        final ArrayList<String> result = new ArrayList<>();
        final Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            final int startGroup = withGroup0 ? 0 : 1;
            final int groupCount = matcher.groupCount();
            for (int i = startGroup; i <= groupCount; i++) {
                result.add(matcher.group(i));
            }

            if (!findAll) {
                break;
            }
        }
        return result;
    }

    /**
     * Finds all named capture groups and their values from the first match.
     *
     * @param pattern The compiled regex pattern.
     * @param content The content to match against.
     * @return A map of group names to their captured values.
     */
    public static Map<String, String> getAllGroupNames(
            final java.util.regex.Pattern pattern,
            final CharSequence content) {
        if (null == content || null == pattern) {
            return null;
        }
        final Matcher m = pattern.matcher(content);
        final Map<String, String> result = MapKit.newHashMap(m.groupCount());
        if (m.find()) {
            final Map<String, Integer> map = MethodKit.invoke(pattern, "namedGroups");
            map.forEach((key, value) -> result.put(key, m.group(value)));
        }
        return result;
    }

    /**
     * Extracts values from a string based on a regex and formats them into a new string using a template.
     *
     * @param pattern  The compiled regex pattern.
     * @param content  The content to match against.
     * @param template The template string, using `$1`, `$2`, etc., for group references.
     * @return The formatted string.
     */
    public static String extractMulti(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            String template) {
        if (null == content || null == pattern || null == template) {
            return null;
        }

        final TreeSet<Integer> varNums = new TreeSet<>((o1, o2) -> CompareKit.compare(o2, o1));
        final Matcher matcherForTemplate = Pattern.GROUP_VAR_PATTERN.matcher(template);
        while (matcherForTemplate.find()) {
            varNums.add(Integer.parseInt(matcherForTemplate.group(1)));
        }

        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (final Integer group : varNums) {
                template = template.replace(Symbol.DOLLAR + group, matcher.group(group));
            }
            return template;
        }
        return null;
    }

    /**
     * Extracts values from a string based on a regex and formats them into a new string using a template.
     *
     * @param regex    The regex string.
     * @param content  The content to match against.
     * @param template The template string.
     * @return The formatted string.
     */
    public static String extractMulti(final String regex, final CharSequence content, final String template) {
        if (null == content || null == regex || null == template) {
            return null;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return extractMulti(pattern, content, template);
    }

    /**
     * Extracts values and removes the matched prefix from the original content.
     *
     * @param pattern       The compiled regex pattern.
     * @param contentHolder A mutable holder for the content.
     * @param template      The template string.
     * @return The formatted string.
     */
    public static String extractMultiAndDelPre(
            final java.util.regex.Pattern pattern,
            final Mutable<CharSequence> contentHolder,
            String template) {
        if (null == contentHolder || null == pattern || null == template) {
            return null;
        }

        final HashSet<String> varNums = findAll(Pattern.GROUP_VAR_PATTERN, template, 1, new HashSet<>());

        final CharSequence content = contentHolder.get();
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            for (final String var : varNums) {
                final int group = Integer.parseInt(var);
                template = template.replace(Symbol.DOLLAR + var, matcher.group(group));
            }
            contentHolder.set(StringKit.sub(content, matcher.end(), content.length()));
            return template;
        }
        return null;
    }

    /**
     * Extracts values and removes the matched prefix from the original content.
     *
     * @param regex         The regex string.
     * @param contentHolder A mutable holder for the content.
     * @param template      The template string.
     * @return The formatted string.
     */
    public static String extractMultiAndDelPre(
            final String regex,
            final Mutable<CharSequence> contentHolder,
            final String template) {
        if (null == contentHolder || null == regex || null == template) {
            return null;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return extractMultiAndDelPre(pattern, contentHolder, template);
    }

    /**
     * Deletes the first occurrence of a regex match.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return The content with the first match removed.
     */
    public static String delFirst(final String regex, final CharSequence content) {
        if (StringKit.hasEmpty(regex, content)) {
            return StringKit.toStringOrNull(content);
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return delFirst(pattern, content);
    }

    /**
     * Deletes the first occurrence of a pattern match.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return The content with the first match removed.
     */
    public static String delFirst(final java.util.regex.Pattern pattern, final CharSequence content) {
        return replaceFirst(pattern, content, Normal.EMPTY);
    }

    /**
     * Replaces the first occurrence of a pattern match.
     *
     * @param pattern     The compiled pattern.
     * @param content     The content.
     * @param replacement The replacement string.
     * @return The modified content.
     */
    public static String replaceFirst(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final String replacement) {
        if (null == pattern || StringKit.isEmpty(content)) {
            return StringKit.toStringOrNull(content);
        }

        return pattern.matcher(content).replaceFirst(replacement);
    }

    /**
     * Deletes the last occurrence of a regex match.
     *
     * @param regex The regex.
     * @param text  The content.
     * @return The content with the last match removed.
     */
    public static String delLast(final String regex, final CharSequence text) {
        if (StringKit.isEmpty(regex) || StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return delLast(pattern, text);
    }

    /**
     * Deletes the last occurrence of a pattern match.
     *
     * @param pattern The compiled pattern.
     * @param text    The content.
     * @return The content with the last match removed.
     */
    public static String delLast(final java.util.regex.Pattern pattern, final CharSequence text) {
        if (null != pattern && StringKit.isNotEmpty(text)) {
            final MatchResult matchResult = lastIndexOf(pattern, text);
            if (null != matchResult) {
                return StringKit.subPre(text, matchResult.start()) + StringKit.subSuf(text, matchResult.end());
            }
        }

        return StringKit.toStringOrNull(text);
    }

    /**
     * Deletes all occurrences of a regex match.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return The content with all matches removed.
     */
    public static String delAll(final String regex, final CharSequence content) {
        if (StringKit.hasEmpty(regex, content)) {
            return StringKit.toStringOrNull(content);
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return delAll(pattern, content);
    }

    /**
     * Deletes all occurrences of a pattern match.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return The content with all matches removed.
     */
    public static String delAll(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (null == pattern || StringKit.isEmpty(content)) {
            return StringKit.toStringOrNull(content);
        }

        return pattern.matcher(content).replaceAll(Normal.EMPTY);
    }

    /**
     * Deletes the content before the first regex match.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return The content after the first match.
     */
    public static String delPre(final String regex, final CharSequence content) {
        if (null == content || null == regex) {
            return StringKit.toStringOrNull(content);
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return delPre(pattern, content);
    }

    /**
     * Deletes the content before the first pattern match.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return The content after the first match.
     */
    public static String delPre(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (null != pattern && StringKit.isNotEmpty(content)) {
            final Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return StringKit.sub(content, matcher.end(), content.length());
            }
        }

        return StringKit.toStringOrNull(content);
    }

    /**
     * Finds all matches and returns a list of the captured group 0.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return A list of results.
     */
    public static List<String> findAllGroup0(final String regex, final CharSequence content) {
        return findAll(regex, content, 0);
    }

    /**
     * Finds all matches and returns a list of the captured group 1.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return A list of results.
     */
    public static List<String> findAllGroup1(final String regex, final CharSequence content) {
        return findAll(regex, content, 1);
    }

    /**
     * Finds all matches for a specific group.
     *
     * @param regex   The regex.
     * @param content The content.
     * @param group   The group index.
     * @return A list of results.
     */
    public static List<String> findAll(final String regex, final CharSequence content, final int group) {
        return findAll(regex, content, group, new ArrayList<>());
    }

    /**
     * Finds all matches for a specific group and adds them to a collection.
     *
     * @param <T>        The collection type.
     * @param regex      The regex.
     * @param content    The content.
     * @param group      The group index.
     * @param collection The collection to add results to.
     * @return The collection.
     */
    public static <T extends Collection<String>> T findAll(
            final String regex,
            final CharSequence content,
            final int group,
            final T collection) {
        if (null == regex) {
            return collection;
        }

        return findAll(Pattern.get(regex, java.util.regex.Pattern.DOTALL), content, group, collection);
    }

    /**
     * Finds all matches for group 0 from a compiled pattern.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return A list of results.
     */
    public static List<String> findAllGroup0(final java.util.regex.Pattern pattern, final CharSequence content) {
        return findAll(pattern, content, 0);
    }

    /**
     * Finds all matches for group 1 from a compiled pattern.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return A list of results.
     */
    public static List<String> findAllGroup1(final java.util.regex.Pattern pattern, final CharSequence content) {
        return findAll(pattern, content, 1);
    }

    /**
     * Finds all matches for a specific group from a compiled pattern.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @param group   The group index.
     * @return A list of results.
     */
    public static List<String> findAll(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final int group) {
        return findAll(pattern, content, group, new ArrayList<>());
    }

    /**
     * Finds all matches for a specific group and adds them to a collection.
     *
     * @param <T>        The collection type.
     * @param pattern    The compiled pattern.
     * @param content    The content.
     * @param group      The group index.
     * @param collection The collection to add results to.
     * @return The collection.
     */
    public static <T extends Collection<String>> T findAll(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final int group,
            final T collection) {
        if (null == pattern || null == content) {
            return null;
        }
        Assert.notNull(collection, "Collection must be not null !");

        findAll(pattern, content, (matcher) -> collection.add(matcher.group(group)));
        return collection;
    }

    /**
     * Finds all matches and processes each one with a consumer.
     *
     * @param pattern  The compiled pattern.
     * @param content  The content.
     * @param consumer The consumer for each `Matcher`.
     */
    public static void findAll(
            final java.util.regex.Pattern pattern,
            final CharSequence content,
            final Consumer<Matcher> consumer) {
        if (null == pattern || null == content) {
            return;
        }

        final Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            consumer.accept(matcher);
        }
    }

    /**
     * Counts the number of matches of a regex in a string.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return The number of matches.
     */
    public static int count(final String regex, final CharSequence content) {
        if (null == regex || null == content) {
            return 0;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return count(pattern, content);
    }

    /**
     * Counts the number of matches of a pattern in a string.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return The number of matches.
     */
    public static int count(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (null == pattern || null == content) {
            return 0;
        }

        int count = 0;
        final Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * Finds the `MatchResult` of the first match of a regex.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return The `MatchResult`, or `null` if no match.
     */
    public static MatchResult indexOf(final String regex, final CharSequence content) {
        if (null == regex || null == content) {
            return null;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return indexOf(pattern, content);
    }

    /**
     * Finds the `MatchResult` of the first match of a pattern.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return The `MatchResult`, or `null` if no match.
     */
    public static MatchResult indexOf(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (null != pattern && null != content) {
            final Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.toMatchResult();
            }
        }

        return null;
    }

    /**
     * Finds the `MatchResult` of the last match of a regex.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return The `MatchResult`, or `null` if no match.
     */
    public static MatchResult lastIndexOf(final String regex, final CharSequence content) {
        if (null == regex || null == content) {
            return null;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return lastIndexOf(pattern, content);
    }

    /**
     * Finds the `MatchResult` of the last match of a pattern.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return The `MatchResult`, or `null` if no match.
     */
    public static MatchResult lastIndexOf(final java.util.regex.Pattern pattern, final CharSequence content) {
        MatchResult result = null;
        if (null != pattern && null != content) {
            final Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                result = matcher.toMatchResult();
            }
        }

        return result;
    }

    /**
     * Finds all `MatchResult`s for a regex.
     *
     * @param regex   The regex.
     * @param content The content.
     * @return A list of `MatchResult`s.
     */
    public static List<MatchResult> allIndexOf(String regex, CharSequence content) {
        if (null == regex || null == content) {
            return null;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return allIndexOf(pattern, content);
    }

    /**
     * Finds all `MatchResult`s for a pattern.
     *
     * @param pattern The compiled pattern.
     * @param content The content.
     * @return A list of `MatchResult`s.
     */
    public static List<MatchResult> allIndexOf(java.util.regex.Pattern pattern, CharSequence content) {
        List<MatchResult> results = null;
        if (null != pattern && null != content) {
            final Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                if (results == null) {
                    results = new ArrayList<>();
                }
                results.add(matcher.toMatchResult());
            }
        }

        return results;
    }

    /**
     * Gets the first integer found in a string.
     *
     * @param stringWithNumber The string containing a number.
     * @return The integer, or `null` if not found.
     */
    public static Integer getFirstNumber(final CharSequence stringWithNumber) {
        return Convert.toInt(get(Pattern.NUMBERS_PATTERN, stringWithNumber, 0), null);
    }

    /**
     * Replaces all occurrences of a regex match using a replacement template with group references.
     *
     * @param content             The content.
     * @param regex               The regex.
     * @param replacementTemplate The replacement template (e.g., "($1)").
     * @return The modified content.
     */
    public static String replaceAll(final CharSequence content, final String regex, final String replacementTemplate) {
        final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL);
        return replaceAll(content, pattern, replacementTemplate);
    }

    /**
     * Replaces all occurrences of a pattern match using a replacement template with group references.
     *
     * @param content             The content.
     * @param pattern             The compiled pattern.
     * @param replacementTemplate The replacement template.
     * @return The modified content.
     */
    public static String replaceAll(
            final CharSequence content,
            final java.util.regex.Pattern pattern,
            String replacementTemplate) {
        if (StringKit.isEmpty(content)) {
            return StringKit.toStringOrNull(content);
        }

        if (null == replacementTemplate) {
            replacementTemplate = Normal.EMPTY;
        }
        Assert.notNull(replacementTemplate, "ReplacementTemplate must be not null !");

        final Matcher matcher = pattern.matcher(content);
        boolean result = matcher.find();
        if (result) {
            final Set<String> varNums = findAll(
                    Pattern.GROUP_VAR_PATTERN,
                    replacementTemplate,
                    1,
                    new TreeSet<>(LengthCompare.INSTANCE.reversed()));
            final StringBuffer sb = new StringBuffer();
            do {
                String replacement = replacementTemplate;
                for (final String var : varNums) {
                    final int group = Integer.parseInt(var);
                    replacement = replacement.replace("$" + var, matcher.group(group));
                }
                matcher.appendReplacement(sb, escape(replacement));
                result = matcher.find();
            } while (result);
            matcher.appendTail(sb);
            return sb.toString();
        }

        return StringKit.toStringOrNull(content);
    }

    /**
     * Replaces all occurrences of a regex match using a replacement function.
     *
     * @param text       The text to replace.
     * @param regex      The regex.
     * @param replaceFun The function to generate the replacement string.
     * @return The modified text.
     */
    public static String replaceAll(
            final CharSequence text,
            final String regex,
            final FunctionX<Matcher, String> replaceFun) {
        return replaceAll(text, java.util.regex.Pattern.compile(regex), replaceFun);
    }

    /**
     * Replaces all occurrences of a pattern match using a replacement function.
     *
     * @param text       The text to replace.
     * @param pattern    The compiled pattern.
     * @param replaceFun The function to generate the replacement string.
     * @return The modified text.
     */
    public static String replaceAll(
            final CharSequence text,
            final java.util.regex.Pattern pattern,
            FunctionX<Matcher, String> replaceFun) {
        if (null == pattern || StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        if (null == replaceFun) {
            replaceFun = Matcher::group;
        }

        final Matcher matcher = pattern.matcher(text);
        final StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, replaceFun.apply(matcher));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Escapes a single special character for use in a regex.
     *
     * @param c The character.
     * @return The escaped character as a string.
     */
    public static String escape(final char c) {
        final StringBuilder builder = new StringBuilder();
        if (RE_KEYS.contains(c)) {
            builder.append('\\');
        }
        builder.append(c);
        return builder.toString();
    }

    /**
     * Escapes special characters in a string for use in a regex.
     *
     * @param content The content.
     * @return The escaped string.
     */
    public static String escape(final CharSequence content) {
        if (StringKit.isBlank(content)) {
            return StringKit.toStringOrNull(content);
        }

        final StringBuilder builder = new StringBuilder();
        final int len = content.length();
        char current;
        for (int i = 0; i < len; i++) {
            current = content.charAt(i);
            if (RE_KEYS.contains(current)) {
                builder.append('\\');
            }
            builder.append(current);
        }
        return builder.toString();
    }

    /**
     * Safely gets a matched group by name from a `Matcher`.
     *
     * @param matcher The matcher object.
     * @param name    The name of the group.
     * @return The matched string for the named group, or `null` if the group does not exist.
     */
    public static String group(final Matcher matcher, final String name) {
        try {
            return matcher.group(name);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

}
