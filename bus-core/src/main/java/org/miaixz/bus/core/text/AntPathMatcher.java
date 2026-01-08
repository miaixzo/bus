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
package org.miaixz.bus.core.text;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A path matcher for Ant-style patterns. This implementation is inspired by Spring Framework's AntPathMatcher.
 *
 * <p>
 * The matching rules are as follows:
 * <ul>
 * <li>{@code ?} matches a single character</li>
 * <li>{@code *} matches zero or more characters</li>
 * <li>{@code **} matches zero or more <em>directories</em> in a path</li>
 * <li>{@code {bus:[a-z]+}} matches the regular expression {@code [a-z]+} as a path variable named "bus"</li>
 * </ul>
 *
 * <p>
 * Examples:
 *
 * <ul>
 * <li>{@code com/t?st.jsp} &mdash; matches {@code com/test.jsp}, {@code com/tast.jsp}, or {@code com/txst.jsp}</li>
 * <li>{@code com/*.jsp} &mdash; matches all {@code .jsp} files in the {@code com} directory</li>
 * <li>{@code com/&#42;&#42;/test.jsp} &mdash; matches all {@code test.jsp} files under the {@code com} path</li>
 * <li>{@code org/bus/&#42;&#42;/*.jsp} &mdash; matches all {@code .jsp} files under the {@code org/bus} path</li>
 * <li>{@code com/{filename:\\w+}.jsp} matches {@code com/test.jsp} and captures {@code test} into the {@code filename}
 * variable</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> Both the pattern and the path must be either both absolute or both relative.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AntPathMatcher {

    /**
     * Default path separator: "/".
     */
    public static final String DEFAULT_PATH_SEPARATOR = Symbol.SLASH;

    /**
     * The threshold after which the pattern cache is automatically turned off.
     */
    private static final int CACHE_TURNOFF_THRESHOLD = 65536;

    /**
     * A pre-compiled pattern for extracting URI template variables from a pattern string.
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?}");

    /**
     * An array of characters that are considered wildcards in a pattern.
     */
    private static final char[] WILDCARD_CHARS = { Symbol.C_STAR, Symbol.C_QUESTION_MARK, '{' };

    /**
     * A cache for tokenized pattern strings, mapping a pattern string to its tokenized parts.
     */
    private final Map<String, String[]> tokenizedPatternCache = new ConcurrentHashMap<>(256);
    /**
     * A cache for compiled {@link AntPathStringMatcher} instances, mapping a pattern string to its matcher.
     */
    private final Map<String, AntPathStringMatcher> stringMatcherCache = new ConcurrentHashMap<>(256);

    /**
     * The path separator character used for tokenizing paths and patterns.
     */
    private String pathSeparator;

    /**
     * A cache for patterns that depend on the configured path separator.
     */
    private PathSeparatorPatternCache pathSeparatorPatternCache;

    /**
     * Whether path matching should be case-sensitive. Default is {@code true}.
     */
    private boolean caseSensitive = true;

    /**
     * Whether to trim whitespace from tokens. Default is {@code false}.
     */
    private boolean trimTokens = false;

    /**
     * A flag to control pattern caching. Can be null for automatic mode.
     */
    private volatile Boolean cachePatterns;

    /**
     * Constructs a new instance using the default path separator ('/').
     */
    public AntPathMatcher() {
        this(DEFAULT_PATH_SEPARATOR);
    }

    /**
     * Constructs a new instance with a custom path separator.
     *
     * @param pathSeparator the path separator to use, must not be {@code null}.
     */
    public AntPathMatcher(String pathSeparator) {
        if (null == pathSeparator) {
            pathSeparator = DEFAULT_PATH_SEPARATOR;
        }
        setPathSeparator(pathSeparator);
    }

    /**
     * Sets the path separator to use for pattern matching.
     *
     * @param pathSeparator The separator. If {@code null}, the default separator "/" is used.
     * @return this instance for chaining.
     */
    public AntPathMatcher setPathSeparator(String pathSeparator) {
        if (null == pathSeparator) {
            pathSeparator = DEFAULT_PATH_SEPARATOR;
        }
        this.pathSeparator = pathSeparator;
        this.pathSeparatorPatternCache = new PathSeparatorPatternCache(this.pathSeparator);
        return this;
    }

    /**
     * Sets whether the matching should be case-sensitive. Default is {@code true}.
     *
     * @param caseSensitive {@code true} for case-sensitive matching, {@code false} for case-insensitive.
     * @return this instance for chaining.
     */
    public AntPathMatcher setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    /**
     * Sets whether to trim tokens from the path and pattern before matching. Default is {@code false}.
     *
     * @param trimTokens {@code true} to trim whitespace from path segments.
     * @return this instance for chaining.
     */
    public AntPathMatcher setTrimTokens(final boolean trimTokens) {
        this.trimTokens = trimTokens;
        return this;
    }

    /**
     * Specify whether to cache parsed pattern metadata. A value of {@code true} activates an unlimited pattern cache; a
     * value of {@code false} turns the pattern cache off completely.
     *
     * @param cachePatterns whether to cache patterns.
     * @return this instance for chaining.
     */
    public AntPathMatcher setCachePatterns(final boolean cachePatterns) {
        this.cachePatterns = cachePatterns;
        return this;
    }

    /**
     * Checks if the given path is a pattern (i.e., contains wildcards).
     *
     * @param path The path to check.
     * @return {@code true} if the path is a pattern.
     */
    public boolean isPattern(final String path) {
        if (path == null) {
            return false;
        }
        boolean uriVar = false;
        final int length = path.length();
        for (int i = 0; i < length; i++) {
            char c = path.charAt(i);
            if (c == Symbol.C_STAR || c == Symbol.C_QUESTION_MARK) {
                return true;
            }
            if (c == Symbol.C_BRACE_LEFT) {
                uriVar = true;
                continue;
            }
            if (c == Symbol.C_BRACE_RIGHT && uriVar) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given path matches the given pattern.
     *
     * @param pattern The pattern to match against.
     * @param path    The path to match.
     * @return {@code true} if the path matches the pattern.
     */
    public boolean match(final String pattern, final String path) {
        return doMatch(pattern, path, true, null);
    }

    /**
     * Checks if the given path starts with the given pattern.
     *
     * @param pattern The pattern to match against.
     * @param path    The path to match.
     * @return {@code true} if the path starts with the pattern.
     */
    public boolean matchStart(final String pattern, final String path) {
        return doMatch(pattern, path, false, null);
    }

    /**
     * Performs the actual match of a path against a pattern.
     *
     * @param pattern              The pattern.
     * @param path                 The path.
     * @param fullMatch            If {@code true}, the full path must match. If {@code false}, only the start must
     *                             match.
     * @param uriTemplateVariables A map to capture URI template variables.
     * @return {@code true} if the path matches.
     */
    protected boolean doMatch(
            final String pattern,
            final String path,
            final boolean fullMatch,
            final Map<String, String> uriTemplateVariables) {
        if (path == null || path.startsWith(this.pathSeparator) != pattern.startsWith(this.pathSeparator)) {
            return false;
        }

        final String[] pattDirs = tokenizePattern(pattern);
        if (fullMatch && this.caseSensitive && !isPotentialMatch(path, pattDirs)) {
            return false;
        }

        final String[] pathDirs = tokenizePath(path);
        int pattIdxStart = 0;
        int pattIdxEnd = pattDirs.length - 1;
        int pathIdxStart = 0;
        int pathIdxEnd = pathDirs.length - 1;

        // Match all elements up to the first **
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            final String pattDir = pattDirs[pattIdxStart];
            if ("**".equals(pattDir)) {
                break;
            }
            if (notMatchStrings(pattDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
                return false;
            }
            pattIdxStart++;
            pathIdxStart++;
        }

        if (pathIdxStart > pathIdxEnd) {
            // Path is exhausted, only match if rest of pattern is * or **'s
            if (pattIdxStart > pattIdxEnd) {
                return (pattern.endsWith(this.pathSeparator) == path.endsWith(this.pathSeparator));
            }
            if (!fullMatch) {
                return true;
            }
            if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals(Symbol.STAR)
                    && path.endsWith(this.pathSeparator)) {
                return true;
            }
            for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }
            return true;
        } else if (pattIdxStart > pattIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        } else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
            // Path start definitely matches due to "**" part in pattern.
            return true;
        }

        // up to last '**'
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            final String pattDir = pattDirs[pattIdxEnd];
            if (pattDir.equals("**")) {
                break;
            }
            if (notMatchStrings(pattDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
                return false;
            }
            pattIdxEnd--;
            pathIdxEnd--;
        }
        if (pathIdxStart > pathIdxEnd) {
            // String is exhausted
            for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }
            return true;
        }

        while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            int patIdxTmp = -1;
            for (int i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
                if (pattDirs[i].equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == pattIdxStart + 1) {
                // '**/**' situation, so skip one
                pattIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in text between
            // strIdxStart & strIdxEnd
            final int patLength = (patIdxTmp - pattIdxStart - 1);
            final int strLength = (pathIdxEnd - pathIdxStart + 1);
            int foundIdx = -1;

            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    final String subPat = pattDirs[pattIdxStart + j + 1];
                    final String subStr = pathDirs[pathIdxStart + i + j];
                    if (notMatchStrings(subPat, subStr, uriTemplateVariables)) {
                        continue strLoop;
                    }
                }
                foundIdx = pathIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            pattIdxStart = patIdxTmp;
            pathIdxStart = foundIdx + patLength;
        }

        for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
            if (!pattDirs[i].equals("**")) {
                return false;
            }
        }

        return true;
    }

    private boolean isPotentialMatch(final String path, final String[] pattDirs) {
        if (!this.trimTokens) {
            int pos = 0;
            for (final String pattDir : pattDirs) {
                int skipped = skipSeparator(path, pos, this.pathSeparator);
                pos += skipped;
                skipped = skipSegment(path, pos, pattDir);
                if (skipped < pattDir.length()) {
                    return (skipped > 0 || (!pattDir.isEmpty() && isWildcardChar(pattDir.charAt(0))));
                }
                pos += skipped;
            }
        }
        return true;
    }

    private int skipSegment(final String path, final int pos, final String prefix) {
        int skipped = 0;
        for (int i = 0; i < prefix.length(); i++) {
            final char c = prefix.charAt(i);
            if (isWildcardChar(c)) {
                return skipped;
            }
            final int currPos = pos + skipped;
            if (currPos >= path.length()) {
                return 0;
            }
            if (c == path.charAt(currPos)) {
                skipped++;
            }
        }
        return skipped;
    }

    private int skipSeparator(final String path, final int pos, final String separator) {
        int skipped = 0;
        while (path.startsWith(separator, pos + skipped)) {
            skipped += separator.length();
        }
        return skipped;
    }

    private boolean isWildcardChar(final char c) {
        for (final char candidate : WILDCARD_CHARS) {
            if (c == candidate) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tokenizes the given path pattern into parts, using the configured path separator.
     *
     * @param pattern the pattern to tokenize
     * @return the tokenized pattern parts
     */
    protected String[] tokenizePattern(final String pattern) {
        String[] tokenized = null;
        final Boolean cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns) {
            tokenized = this.tokenizedPatternCache.get(pattern);
        }
        if (tokenized == null) {
            tokenized = tokenizePath(pattern);
            if (cachePatterns == null && this.tokenizedPatternCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                deactivatePatternCache();
                return tokenized;
            }
            if (cachePatterns == null || cachePatterns) {
                this.tokenizedPatternCache.put(pattern, tokenized);
            }
        }
        return tokenized;
    }

    private void deactivatePatternCache() {
        this.cachePatterns = false;
        this.tokenizedPatternCache.clear();
        this.stringMatcherCache.clear();
    }

    /**
     * Tokenizes the given path into parts, using the configured path separator.
     *
     * @param path the path to tokenize
     * @return the tokenized path parts
     */
    protected String[] tokenizePath(final String path) {
        return CharsBacker.split(path, this.pathSeparator, this.trimTokens, true).toArray(new String[0]);
    }

    /**
     * Tests whether a string does not match a pattern.
     *
     * @param pattern              the pattern to match against
     * @param text                 the string to match
     * @param uriTemplateVariables a map to capture URI template variables
     * @return {@code true} if the string does not match, {@code false} otherwise.
     */
    private boolean notMatchStrings(
            final String pattern,
            final String text,
            final Map<String, String> uriTemplateVariables) {
        return !getStringMatcher(pattern).matchStrings(text, uriTemplateVariables);
    }

    /**
     * Builds or retrieves a cached {@link AntPathStringMatcher} for the given pattern.
     *
     * @param pattern the pattern to match against
     * @return a corresponding {@link AntPathStringMatcher}.
     */
    protected AntPathStringMatcher getStringMatcher(final String pattern) {
        AntPathStringMatcher matcher = null;
        final Boolean cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns) {
            matcher = this.stringMatcherCache.get(pattern);
        }
        if (matcher == null) {
            matcher = new AntPathStringMatcher(pattern, this.caseSensitive);
            if (cachePatterns == null && this.stringMatcherCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                deactivatePatternCache();
                return matcher;
            }
            if (cachePatterns == null || cachePatterns) {
                this.stringMatcherCache.put(pattern, matcher);
            }
        }
        return matcher;
    }

    /**
     * Given a pattern and a full path, extracts the part of the path that matches the wildcard pattern.
     *
     * @param pattern The pattern.
     * @param path    The full path.
     * @return The extracted path segment that corresponds to the wildcards.
     */
    public String extractPathWithinPattern(final String pattern, final String path) {
        final String[] patternParts = tokenizePath(pattern);
        final String[] pathParts = tokenizePath(path);
        final StringBuilder builder = new StringBuilder();
        boolean pathStarted = false;

        for (int segment = 0; segment < patternParts.length; segment++) {
            final String patternPart = patternParts[segment];
            if (patternPart.indexOf(Symbol.C_STAR) > -1 || patternPart.indexOf(Symbol.C_QUESTION_MARK) > -1) {
                for (; segment < pathParts.length; segment++) {
                    if (pathStarted || (segment == 0 && !pattern.startsWith(this.pathSeparator))) {
                        builder.append(this.pathSeparator);
                    }
                    builder.append(pathParts[segment]);
                    pathStarted = true;
                }
            }
        }
        return builder.toString();
    }

    /**
     * Extracts URI template variables from a path based on a pattern.
     *
     * @param pattern The pattern with URI templates.
     * @param path    The path to extract variables from.
     * @return A map of variable names to their values.
     * @throws IllegalStateException if the pattern does not match the path.
     */
    public Map<String, String> extractUriTemplateVariables(final String pattern, final String path) {
        final Map<String, String> variables = new LinkedHashMap<>();
        final boolean result = doMatch(pattern, path, true, variables);
        if (!result) {
            throw new IllegalStateException("Pattern \"" + pattern + "\" is not a match for \"" + path + "\"");
        }
        return variables;
    }

    /**
     * Combines two patterns into a new pattern.
     *
     * @param pattern1 the first pattern
     * @param pattern2 the second pattern
     * @return the combination of the two patterns
     * @throws IllegalArgumentException if the two patterns cannot be combined
     */
    public String combine(final String pattern1, final String pattern2) {
        if (StringKit.isEmpty(pattern1) && StringKit.isEmpty(pattern2)) {
            return Normal.EMPTY;
        }
        if (StringKit.isEmpty(pattern1)) {
            return pattern2;
        }
        if (StringKit.isEmpty(pattern2)) {
            return pattern1;
        }

        final boolean pattern1ContainsUriVar = (pattern1.indexOf('{') != -1);
        if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && match(pattern1, pattern2)) {
            return pattern2;
        }

        if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnWildCard())) {
            return concat(pattern1.substring(0, pattern1.length() - 2), pattern2);
        }

        if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnDoubleWildCard())) {
            return concat(pattern1, pattern2);
        }

        final int starDotPos1 = pattern1.indexOf("*.");
        if (pattern1ContainsUriVar || starDotPos1 == -1 || this.pathSeparator.equals(".")) {
            return concat(pattern1, pattern2);
        }

        final String ext1 = pattern1.substring(starDotPos1 + 1);
        final int dotPos2 = pattern2.indexOf('.');
        final String file2 = (dotPos2 == -1 ? pattern2 : pattern2.substring(0, dotPos2));
        final String ext2 = (dotPos2 == -1 ? "" : pattern2.substring(dotPos2));
        final boolean ext1All = (ext1.equals(".*") || ext1.isEmpty());
        final boolean ext2All = (ext2.equals(".*") || ext2.isEmpty());
        if (!ext1All && !ext2All) {
            throw new IllegalArgumentException("Cannot combine patterns: " + pattern1 + " vs " + pattern2);
        }
        final String ext = (ext1All ? ext2 : ext1);
        return file2 + ext;
    }

    private String concat(final String path1, final String path2) {
        final boolean path1EndsWithSeparator = path1.endsWith(this.pathSeparator);
        final boolean path2StartsWithSeparator = path2.startsWith(this.pathSeparator);

        if (path1EndsWithSeparator && path2StartsWithSeparator) {
            return path1 + path2.substring(1);
        } else if (path1EndsWithSeparator || path2StartsWithSeparator) {
            return path1 + path2;
        } else {
            return path1 + this.pathSeparator + path2;
        }
    }

    /**
     * Given a full path, returns a {@link Comparator} suitable for sorting patterns in order of specificity.
     *
     * @param path the full path to use for comparison
     * @return a comparator capable of sorting patterns in order of specificity.
     */
    public Comparator<String> getPatternComparator(final String path) {
        return new AntPatternComparator(path);
    }

    /**
     * A helper class for matching a string against a single Ant-style pattern.
     */
    protected static class AntPathStringMatcher {

        /**
         * A pre-compiled pattern for finding wildcards (`?`, `*`) and URI template variables (`{...}`).
         */
        private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?}|[^/{}]|\\\\[{}])+?)}");
        /**
         * The default regex pattern for a URI template variable if no specific pattern is defined.
         */
        private static final String DEFAULT_VARIABLE_PATTERN = "((?s).*)";
        /**
         * The original raw pattern string.
         */
        private final String rawPattern;
        /**
         * Whether the matching should be case-sensitive.
         */
        private final boolean caseSensitive;
        /**
         * Indicates if the pattern contains no wildcards, allowing for a simple string equality check.
         */
        private final boolean exactMatch;
        /**
         * The compiled {@link Pattern} for this Ant-style pattern.
         */
        private final Pattern pattern;
        /**
         * A list of URI template variable names extracted from the pattern.
         */
        private final List<String> variableNames = new ArrayList<>();

        /**
         * Constructs a new {@code AntPathStringMatcher}.
         *
         * @param pattern       the pattern to match against
         * @param caseSensitive if the matching should be case-sensitive
         */
        public AntPathStringMatcher(final String pattern, final boolean caseSensitive) {
            this.rawPattern = pattern;
            this.caseSensitive = caseSensitive;
            final StringBuilder patternBuilder = new StringBuilder();
            final Matcher matcher = GLOB_PATTERN.matcher(pattern);
            int end = 0;
            while (matcher.find()) {
                patternBuilder.append(quote(pattern, end, matcher.start()));
                final String match = matcher.group();
                if ("?".equals(match)) {
                    patternBuilder.append('.');
                } else if (Symbol.STAR.equals(match)) {
                    patternBuilder.append(".*");
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    final int colonIdx = match.indexOf(Symbol.C_COLON);
                    if (colonIdx == -1) {
                        patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
                        this.variableNames.add(matcher.group(1));
                    } else {
                        final String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
                        patternBuilder.append(Symbol.C_PARENTHESE_LEFT).append(variablePattern)
                                .append(Symbol.C_PARENTHESE_RIGHT);
                        final String variableName = match.substring(1, colonIdx);
                        this.variableNames.add(variableName);
                    }
                }
                end = matcher.end();
            }
            // No glob pattern was found, this is an exact String match
            if (end == 0) {
                this.exactMatch = true;
                this.pattern = null;
            } else {
                this.exactMatch = false;
                patternBuilder.append(quote(pattern, end, pattern.length()));
                this.pattern = (this.caseSensitive ? Pattern.compile(patternBuilder.toString())
                        : Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE));
            }
        }

        private String quote(final String s, final int start, final int end) {
            if (start == end) {
                return "";
            }
            return Pattern.quote(s.substring(start, end));
        }

        /**
         * Matches the given string against the compiled pattern.
         *
         * @param text                 The string to match.
         * @param uriTemplateVariables A map to capture URI template variables.
         * @return {@code true} if the string matches the pattern.
         */
        public boolean matchStrings(final String text, final Map<String, String> uriTemplateVariables) {
            if (this.exactMatch) {
                return this.caseSensitive ? this.rawPattern.equals(text) : this.rawPattern.equalsIgnoreCase(text);
            }
            if (this.pattern != null) {
                final Matcher matcher = this.pattern.matcher(text);
                if (matcher.matches()) {
                    if (uriTemplateVariables != null) {
                        if (this.variableNames.size() != matcher.groupCount()) {
                            throw new IllegalArgumentException(
                                    "The number of capturing groups in the pattern segment " + this.pattern
                                            + " does not match the number of URI template variables it defines.");
                        }
                        for (int i = 1; i <= matcher.groupCount(); i++) {
                            final String name = this.variableNames.get(i - 1);
                            if (name.startsWith(Symbol.STAR)) {
                                throw new IllegalArgumentException("Capturing patterns (" + name + ") are not "
                                        + "supported by the AntPathMatcher.");
                            }
                            uriTemplateVariables.put(name, matcher.group(i));
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A comparator for sorting Ant-style patterns by specificity.
     */
    protected static class AntPatternComparator implements Comparator<String> {

        /**
         * The path to compare patterns against.
         */
        private final String path;

        /**
         * Creates a new AntPatternComparator for the specified path.
         *
         * @param path The path to compare patterns against.
         */
        public AntPatternComparator(final String path) {
            this.path = path;
        }

        /**
         * Compares two patterns to determine which is more specific.
         *
         * @param pattern1 The first pattern.
         * @param pattern2 The second pattern.
         * @return a negative integer, zero, or a positive integer as the first pattern is more specific, equally
         *         specific, or less specific than the second.
         */
        @Override
        public int compare(final String pattern1, final String pattern2) {
            final PatternInfo info1 = new PatternInfo(pattern1);
            final PatternInfo info2 = new PatternInfo(pattern2);

            if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
                return 0;
            }
            if (info1.isLeastSpecific()) {
                return 1;
            }
            if (info2.isLeastSpecific()) {
                return -1;
            }

            final boolean pattern1EqualsPath = pattern1.equals(this.path);
            final boolean pattern2EqualsPath = pattern2.equals(this.path);
            if (pattern1EqualsPath && pattern2EqualsPath) {
                return 0;
            }
            if (pattern1EqualsPath) {
                return -1;
            }
            if (pattern2EqualsPath) {
                return 1;
            }

            if (info1.isPrefixPattern() && info2.isPrefixPattern()) {
                return info2.getLength() - info1.getLength();
            }
            if (info1.isPrefixPattern() && info2.getDoubleWildcards() == 0) {
                return 1;
            }
            if (info2.isPrefixPattern() && info1.getDoubleWildcards() == 0) {
                return -1;
            }

            if (info1.getTotalCount() != info2.getTotalCount()) {
                return info1.getTotalCount() - info2.getTotalCount();
            }

            if (info1.getLength() != info2.getLength()) {
                return info2.getLength() - info1.getLength();
            }

            if (info1.getSingleWildcards() < info2.getSingleWildcards()) {
                return -1;
            }
            if (info2.getSingleWildcards() < info1.getSingleWildcards()) {
                return 1;
            }

            if (info1.getUriVars() < info2.getUriVars()) {
                return -1;
            }
            if (info2.getUriVars() < info1.getUriVars()) {
                return 1;
            }

            return 0;
        }

        /**
         * A helper class to hold information about a pattern's specificity.
         */
        private static class PatternInfo {

            /**
             * The pattern string this info object is for.
             */
            private final String pattern;
            /**
             * The number of URI template variables (`{...}`) in the pattern.
             */
            private int uriVars;
            /**
             * The number of single-character wildcards (`*`) in the pattern.
             */
            private int singleWildcards;
            /**
             * The number of multi-directory wildcards (`**`) in the pattern.
             */
            private int doubleWildcards;
            /**
             * {@code true} if the pattern is a 'catch-all' pattern (/**).
             */
            private boolean catchAllPattern;
            /**
             * {@code true} if the pattern is a prefix pattern (ends with /** but is not /**).
             */
            private boolean prefixPattern;
            /**
             * The calculated length of the pattern, treating variables as a single character.
             */
            private Integer length;

            public PatternInfo(final String pattern) {
                this.pattern = pattern;
                if (this.pattern != null) {
                    initCounters();
                    this.catchAllPattern = this.pattern.equals("/**");
                    this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**");
                }
                if (this.uriVars == 0) {
                    this.length = (this.pattern != null ? this.pattern.length() : 0);
                }
            }

            protected void initCounters() {
                int pos = 0;
                if (this.pattern != null) {
                    while (pos < this.pattern.length()) {
                        if (this.pattern.charAt(pos) == '{') {
                            this.uriVars++;
                            pos++;
                        } else if (this.pattern.charAt(pos) == Symbol.C_STAR) {
                            if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == Symbol.C_STAR) {
                                this.doubleWildcards++;
                                pos += 2;
                            } else if (pos > 0 && !this.pattern.substring(pos - 1).equals(".*")) {
                                this.singleWildcards++;
                                pos++;
                            } else {
                                pos++;
                            }
                        } else {
                            pos++;
                        }
                    }
                }
            }

            public int getUriVars() {
                return this.uriVars;
            }

            public int getSingleWildcards() {
                return this.singleWildcards;
            }

            public int getDoubleWildcards() {
                return this.doubleWildcards;
            }

            public boolean isLeastSpecific() {
                return (this.pattern == null || this.catchAllPattern);
            }

            public boolean isPrefixPattern() {
                return this.prefixPattern;
            }

            public int getTotalCount() {
                return this.uriVars + this.singleWildcards + (2 * this.doubleWildcards);
            }

            public int getLength() {
                if (this.length == null) {
                    this.length = (this.pattern != null
                            ? VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#").length()
                            : 0);
                }
                return this.length;
            }
        }
    }

    /**
     * A simple cache for patterns that depend on the configured path separator.
     */
    private static class PathSeparatorPatternCache {

        /**
         * A pattern string for `/{separator}*`.
         */
        private final String endsOnWildCard;
        /**
         * A pattern string for `/{separator}**`.
         */
        private final String endsOnDoubleWildCard;

        public PathSeparatorPatternCache(final String pathSeparator) {
            this.endsOnWildCard = pathSeparator + Symbol.STAR;
            this.endsOnDoubleWildCard = pathSeparator + Symbol.STAR + Symbol.STAR;
        }

        public String getEndsOnWildCard() {
            return this.endsOnWildCard;
        }

        public String getEndsOnDoubleWildCard() {
            return this.endsOnDoubleWildCard;
        }
    }

}
