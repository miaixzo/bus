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
package org.miaixz.bus.core.center.date.format;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Function;

import org.miaixz.bus.core.center.date.Units;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Duration formatter, used to format the duration between two dates. Depending on the {@link Level}, calling the
 * {@link #format()} method will return something like:
 * <ul>
 * <li>XX hours XX minutes XX seconds</li>
 * <li>XX days XX hours</li>
 * <li>XX months XX days XX hours</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormatPeriod implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852255710766L;

    /**
     * The maximum number of formatting levels.
     */
    private final int levelMaxCount;
    /**
     * The duration in milliseconds.
     */
    private long betweenMs;
    /**
     * The formatting level.
     */
    private Level level;
    /**
     * The formatter function for levels.
     */
    private Function<Level, String> formatter = Level::getName;
    /**
     * Whether it is in simple mode. This flag is used to customize whether to output parts with 0 in between. If
     * {@code true}, outputs "1 hour 3 seconds"; if {@code false}, outputs "1 hour 0 minutes 3 seconds".
     */
    private boolean simpleMode = true;
    /**
     * The separator, defaults to "". Can be adjusted via {@link #setSeparator(String)}.
     */
    private String separator = Normal.EMPTY;

    /**
     * Constructs a {@code FormatPeriod} instance.
     *
     * @param betweenMs     The duration in milliseconds.
     * @param level         The level, divided into 5 levels: day, hour, minute, second, millisecond. Formats to the
     *                      corresponding level based on the input level.
     * @param levelMaxCount The maximum number of formatting levels. If the number of levels is 1, but the level is up
     *                      to seconds, only one level will be displayed.
     */
    public FormatPeriod(final long betweenMs, final Level level, final int levelMaxCount) {
        this.betweenMs = betweenMs;
        this.level = level;
        this.levelMaxCount = levelMaxCount;
    }

    /**
     * Creates a {@link FormatPeriod} instance.
     *
     * @param betweenMs The duration in milliseconds.
     * @param level     The level, divided into 5 levels: day, hour, minute, second, millisecond. Formats to the
     *                  corresponding level based on the input level.
     * @return A new {@link FormatPeriod} instance.
     */
    public static FormatPeriod of(final long betweenMs, final Level level) {
        return of(betweenMs, level, 0);
    }

    /**
     * Creates a {@link FormatPeriod} instance.
     *
     * @param betweenMs     The duration in milliseconds.
     * @param level         The level, divided into 5 levels: day, hour, minute, second, millisecond. Formats to the
     *                      corresponding level based on the input level.
     * @param levelMaxCount The maximum number of formatting levels. If the number of levels is 1, but the level is up
     *                      to seconds, only one level will be displayed.
     * @return A new {@link FormatPeriod} instance.
     */
    public static FormatPeriod of(final long betweenMs, final Level level, final int levelMaxCount) {
        return new FormatPeriod(betweenMs, level, levelMaxCount);
    }

    /**
     * Formats the duration output.
     *
     * @return The formatted string.
     */
    public String format() {
        final StringBuilder sb = new StringBuilder();
        if (betweenMs > 0) {
            final long day = betweenMs / Units.DAY.getMillis();
            final long hour = betweenMs / Units.HOUR.getMillis() - day * 24;
            final long minute = betweenMs / Units.MINUTE.getMillis() - day * 24 * 60 - hour * 60;

            final long BetweenOfSecond = ((day * 24 + hour) * 60 + minute) * 60;
            final long second = betweenMs / Units.SECOND.getMillis() - BetweenOfSecond;
            final long millisecond = betweenMs - (BetweenOfSecond + second) * 1000;

            final int level = this.level.ordinal();
            int levelCount = 0;

            // Day
            if (isLevelCountValid(levelCount) && day > 0) {
                sb.append(day).append(formatter.apply(Level.DAY)).append(separator);
                levelCount++;
            }

            // Hour
            if (isLevelCountValid(levelCount) && level >= Level.HOUR.ordinal()) {
                if (hour > 0 || (!this.simpleMode && StringKit.isNotEmpty(sb))) {
                    sb.append(hour).append(formatter.apply(Level.HOUR)).append(separator);
                    levelCount++;
                }
            }

            // Minute
            if (isLevelCountValid(levelCount) && level >= Level.MINUTE.ordinal()) {
                if (minute > 0 || (!this.simpleMode && StringKit.isNotEmpty(sb))) {
                    sb.append(minute).append(formatter.apply(Level.MINUTE)).append(separator);
                    levelCount++;
                }
            }

            // Second
            if (isLevelCountValid(levelCount) && level >= Level.SECOND.ordinal()) {
                if (second > 0 || (!this.simpleMode && StringKit.isNotEmpty(sb))) {
                    sb.append(second).append(formatter.apply(Level.SECOND)).append(separator);
                    levelCount++;
                }
            }

            // Millisecond
            if (isLevelCountValid(levelCount) && millisecond > 0 && level >= Level.MILLISECOND.ordinal()) {
                sb.append(millisecond).append(formatter.apply(Level.MILLISECOND)).append(separator);
            }
        }

        if (StringKit.isEmpty(sb)) {
            sb.append(0).append(formatter.apply(this.level));
        } else if (StringKit.isNotEmpty(separator)) {
            sb.delete(sb.length() - separator.length(), sb.length());
        }
        // Custom implementations may have trailing spaces
        return sb.toString().trim();
    }

    /**
     * Gets the duration in milliseconds.
     *
     * @return The duration in milliseconds.
     */
    public long getBetweenMs() {
        return betweenMs;
    }

    /**
     * Sets the duration in milliseconds.
     *
     * @param betweenMs The duration in milliseconds.
     * @return This {@code FormatPeriod} instance.
     */
    public FormatPeriod setBetweenMs(final long betweenMs) {
        this.betweenMs = betweenMs;
        return this;
    }

    /**
     * Gets the formatting level.
     *
     * @return The {@link Level} of formatting.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Sets the formatting level.
     *
     * @param level The {@link Level} of formatting.
     * @return This {@code FormatPeriod} instance.
     */
    public FormatPeriod setLevel(final Level level) {
        this.level = level;
        return this;
    }

    /**
     * Sets whether to use simple mode. This flag is used to customize whether to output parts with 0 in between. If
     * {@code true}, outputs "1 hour 3 seconds"; if {@code false}, outputs "1 hour 0 minutes 3 seconds".
     *
     * @param simpleMode {@code true} for simple mode, {@code false} otherwise.
     * @return This {@code FormatPeriod} instance.
     */
    public FormatPeriod setSimpleMode(final boolean simpleMode) {
        this.simpleMode = simpleMode;
        return this;
    }

    /**
     * Sets the level formatter.
     *
     * @param formatter The level formatter function.
     * @return This {@code FormatPeriod} instance.
     */
    public FormatPeriod setFormatter(final Function<Level, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    /**
     * Sets the separator.
     *
     * @param separator The separator string.
     * @return This {@code FormatPeriod} instance.
     */
    public FormatPeriod setSeparator(final String separator) {
        this.separator = StringKit.toStringOrEmpty(separator);
        return this;
    }

    @Override
    public String toString() {
        return format();
    }

    /**
     * Checks if the level count is valid. A valid definition is: levelMaxCount is greater than 0 (set), and the current
     * level count does not exceed this maximum value.
     *
     * @param levelCount The current level count.
     * @return {@code true} if the level count is valid, {@code false} otherwise.
     */
    private boolean isLevelCountValid(final int levelCount) {
        return this.levelMaxCount <= 0 || levelCount < this.levelMaxCount;
    }

    /**
     * Enumeration for formatting levels.
     */
    public enum Level {

        /**
         * Day
         */
        DAY("天"),
        /**
         * Hour
         */
        HOUR("小时"),
        /**
         * Minute
         */
        MINUTE("分"),
        /**
         * Second
         */
        SECOND("秒"),
        /**
         * Millisecond
         */
        MILLISECOND("毫秒");

        /**
         * The name of the level.
         */
        private final String name;

        /**
         * Constructs a {@code Level} enum constant.
         *
         * @param name The name of the level.
         */
        Level(final String name) {
            this.name = name;
        }

        /**
         * Gets the name of the level.
         *
         * @return The name of the level.
         */
        public String getName() {
            return this.name;
        }
    }

}
