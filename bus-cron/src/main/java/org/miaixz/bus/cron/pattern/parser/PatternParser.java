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
package org.miaixz.bus.cron.pattern.parser;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.cron.pattern.Part;
import org.miaixz.bus.cron.pattern.matcher.AlwaysTrueMatcher;
import org.miaixz.bus.cron.pattern.matcher.PartMatcher;
import org.miaixz.bus.cron.pattern.matcher.PatternMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser for cron expressions that converts a cron string into a list of {@link PatternMatcher} objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternParser {

    private static final PartParser SECOND_VALUE_PARSER = PartParser.of(Part.SECOND);
    private static final PartParser MINUTE_VALUE_PARSER = PartParser.of(Part.MINUTE);
    private static final PartParser HOUR_VALUE_PARSER = PartParser.of(Part.HOUR);
    private static final PartParser DAY_OF_MONTH_VALUE_PARSER = PartParser.of(Part.DAY_OF_MONTH);
    private static final PartParser MONTH_VALUE_PARSER = PartParser.of(Part.MONTH);
    private static final PartParser DAY_OF_WEEK_VALUE_PARSER = PartParser.of(Part.DAY_OF_WEEK);
    private static final PartParser YEAR_VALUE_PARSER = PartParser.of(Part.YEAR);

    /**
     * Parses a cron expression string into a list of {@link PatternMatcher}s. The expression can be a single pattern or
     * multiple patterns separated by '|'.
     *
     * @param cronPattern The cron expression string.
     * @return A list of {@link PatternMatcher}s.
     */
    public static List<PatternMatcher> parse(final String cronPattern) {
        return parseGroupPattern(cronPattern);
    }

    /**
     * Parses a grouped cron expression, where individual patterns are separated by '|'.
     * 
     * <pre>
     *     cronA | cronB | ...
     * </pre>
     *
     * @param groupPattern The grouped cron expression string.
     * @return A list of {@link PatternMatcher}s, one for each individual pattern.
     */
    private static List<PatternMatcher> parseGroupPattern(final String groupPattern) {
        Assert.notBlank(groupPattern, "Cron expression must not be empty!");
        final List<String> patternList = CharsBacker.splitTrim(groupPattern, Symbol.OR);
        final List<PatternMatcher> patternMatchers = new ArrayList<>(patternList.size());
        for (final String pattern : patternList) {
            patternMatchers.add(parseSingle(pattern));
        }
        return patternMatchers;
    }

    /**
     * Parses a single cron expression pattern.
     *
     * @param pattern The single cron pattern string.
     * @return A {@link PatternMatcher} for the given pattern.
     */
    private static PatternMatcher parseSingle(final String pattern) {
        final String[] parts = pattern.split("\\s+");
        Assert.checkBetween(
                parts.length,
                5,
                7,
                () -> new CrontabException("Pattern [{}] is invalid, it must be 5-7 parts!", pattern));

        // An offset is used to support Quartz-style expressions where the first field is seconds (for 6 or 7-part
        // expressions).
        int offset = 0;
        if (parts.length == 6 || parts.length == 7) {
            offset = 1;
        }

        // The second part. If the expression does not include seconds, default to "0" for matching on the minute.
        final String secondPart = (1 == offset) ? parts[0] : "0";

        // The year part.
        final PartMatcher yearMatcher;
        if (parts.length == 7) { // 7-part expression with year
            yearMatcher = YEAR_VALUE_PARSER.parse(parts[6]);
        } else { // 5 or 6-part expression, year is not specified, so it always matches.
            yearMatcher = AlwaysTrueMatcher.INSTANCE;
        }

        return new PatternMatcher(
                // Second
                SECOND_VALUE_PARSER.parse(secondPart),
                // Minute
                MINUTE_VALUE_PARSER.parse(parts[offset]),
                // Hour
                HOUR_VALUE_PARSER.parse(parts[1 + offset]),
                // Day of Month
                DAY_OF_MONTH_VALUE_PARSER.parse(parts[2 + offset]),
                // Month
                MONTH_VALUE_PARSER.parse(parts[3 + offset]),
                // Day of Week
                DAY_OF_WEEK_VALUE_PARSER.parse(parts[4 + offset]),
                // Year
                yearMatcher);
    }

}
