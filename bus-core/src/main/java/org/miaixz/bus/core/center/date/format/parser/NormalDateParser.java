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
package org.miaixz.bus.core.center.date.format.parser;

import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.DateTime;

/**
 * A global date parser that uses a set of predefined and customizable regular expression rules to parse date strings.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NormalDateParser implements PredicateDateParser {

    /**
     * The default singleton instance of this parser.
     */
    public static final NormalDateParser INSTANCE = new NormalDateParser();

    /**
     * The underlying regular expression-based date parser.
     */
    private final RegexDateParser parser;

    /**
     * Constructs a new {@code NormalDateParser} with a default set of parsing rules.
     */
    public NormalDateParser() {
        parser = createDefault();
    }

    /**
     * Tests if this parser is applicable to the given date string. This implementation always returns {@code true} as
     * it serves as a fallback parser.
     *
     * @param charSequence The date string.
     * @return always {@code true}.
     */
    @Override
    public boolean test(final CharSequence charSequence) {
        return true;
    }

    /**
     * Parses a date string into a {@link DateTime} object. This method is thread-safe.
     *
     * @param source The date string to parse.
     * @return The parsed {@link DateTime} object.
     */
    @Override
    public DateTime parse(final CharSequence source) {
        return (DateTime) parser.parse(source);
    }

    /**
     * Creates the default {@link RegexDateParser} with a comprehensive set of common date format patterns.
     *
     * @return The configured {@link RegexDateParser}.
     */
    private RegexDateParser createDefault() {
        final String yearRegex = "(?<year>\\d{2,4})";
        // Regex for month, matching abbreviated and full English names, or Chinese month names.
        final String monthRegex = "(?<month>[jfmaasond][aepucoe][nbrylgptvc]\\w{0,6}|[一二三四五六七八九十]{1,2}月)";
        final String dayRegex = "(?<day>\\d{1,2})(?:th)?";
        // Regex for the day of the week, optional.
        final String weekRegexWithSuff = "((?<week>[mwfts][oeruha][ndieut](\\w{3,6})?|(星期|周)[一二三四五六日])\\W+)?";
        // Regex for time part, optional.
        final String timeRegexWithPre = "(" + "(\\W+|T)(at\\s)?(?<hour>\\d{1,2})" + "\\W(?<minute>\\d{1,2})"
                + "(\\W(?<second>\\d{1,2}))?秒?" + "(?:[.,](?<nanosecond>\\d{1,9}))?(?<zero>z)?" + "(\\s?(?<m>[ap]m))?"
                + ")?";
        // Date format like "May 8"
        final String dateRegexMonthFirst = monthRegex + "\\W+" + dayRegex;
        // Date format like "02-Jan"
        final String dateRegexDayFirst = dayRegex + "\\W+" + monthRegex;
        // Regex for timezone part.
        final String zoneRegex = "\\s?(?<zone>" + "[a-z ]*" // Matches timezone names like GMT, MST
                + "(\\s?[-+]\\d{1,2}:?(?:\\d{2})?)*" // Matches offsets like +08:00, +0800
                + "(\\s?[(]?[a-z ]+[)]?)?" // Matches display names like (GMT Daylight Time)
                + ")";
        final String maskRegex = "(\\smsk m=[+-]\\d[.]\\d+)?";

        return RegexDateParser.of(
                // Year-Month-Day formats, e.g., 2009-Feb-08 05:57:50 +08:00
                yearRegex + "\\W" + dateRegexMonthFirst + timeRegexWithPre + zoneRegex + maskRegex,
                // Year-Month-Day formats, e.g., 2020-02-08 or 2020年02月08日
                yearRegex + "\\W(?<month>\\d{1,2})(\\W(?<day>\\d{1,2}))?日?" + timeRegexWithPre + zoneRegex + maskRegex,

                // Month-Day-Year formats, e.g., May 8, 2009 05:57:50
                weekRegexWithSuff + dateRegexMonthFirst + "\\W+" + yearRegex + timeRegexWithPre + zoneRegex + maskRegex,
                // Week-Month-Day-Time-Year format, e.g., Mon Jan 2 15:05:05 MST 2020
                weekRegexWithSuff + dateRegexMonthFirst + timeRegexWithPre + zoneRegex + "\\W+" + yearRegex + maskRegex,
                // Week-Day-Month-Year-Time format, e.g., Monday, 02-Jan-06 15:05:05 MST
                weekRegexWithSuff + dateRegexDayFirst + "\\W+" + yearRegex + timeRegexWithPre + zoneRegex + maskRegex,
                // Ambiguous M/d/yyyy or d/M/yyyy format, e.g., 5/12/2020 03:00:50
                "(?<dayOrMonth>\\d{1,2}\\W\\d{1,2})\\W(?<year>\\d{4})" + timeRegexWithPre + zoneRegex + maskRegex,

                // Purely numeric date-time strings
                "^(?<number>\\d{4,19})$");
    }

    /**
     * Sets the preference for parsing ambiguous date formats (e.g., '01/02/2023').
     *
     * @param preferMonthFirst if {@code true}, parse as MM/dd; if {@code false}, parse as dd/MM.
     */
    public synchronized void setPreferMonthFirst(final boolean preferMonthFirst) {
        parser.setPreferMonthFirst(preferMonthFirst);
    }

    /**
     * Registers a custom regular expression for date parsing.
     *
     * @param regex The date regular expression.
     */
    public synchronized void registerRegex(final String regex) {
        parser.addRegex(regex);
    }

    /**
     * Registers a custom compiled {@link Pattern} for date parsing.
     *
     * @param pattern The date regex pattern.
     */
    public synchronized void registerPattern(final Pattern pattern) {
        parser.addPattern(pattern);
    }

}
