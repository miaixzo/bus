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
package org.miaixz.bus.cron.pattern;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.StringJoiner;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.io.Serial;

/**
 * A builder for creating cron expression strings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronPatternBuilder implements Builder<String> {

    @Serial
    private static final long serialVersionUID = 2852287899252L;

    final String[] parts = new String[7];

    /**
     * Creates a new CronPatternBuilder.
     *
     * @return A new {@link CronPatternBuilder} instance.
     */
    public static CronPatternBuilder of() {
        return new CronPatternBuilder();
    }

    /**
     * Sets a list of values for a specific part of the cron expression.
     *
     * @param part   The cron expression part (e.g., SECOND, MINUTE, HOUR).
     * @param values The list of time values.
     * @return this builder instance for chaining.
     */
    public CronPatternBuilder setValues(final Part part, final int... values) {
        for (final int value : values) {
            part.checkValue(value);
        }
        return set(part, ArrayKit.join(values, Symbol.COMMA));
    }

    /**
     * Sets a range of values for a specific part of the cron expression.
     *
     * @param part  The cron expression part (e.g., SECOND, MINUTE, HOUR).
     * @param begin The beginning of the range (inclusive).
     * @param end   The end of the range (inclusive).
     * @return this builder instance for chaining.
     */
    public CronPatternBuilder setRange(final Part part, final int begin, final int end) {
        Assert.notNull(part);
        part.checkValue(begin);
        part.checkValue(end);
        return set(part, StringKit.format("{}-{}", begin, end));
    }

    /**
     * Sets the raw string value for a specific part of the cron expression.
     *
     * @param part  The cron expression part (e.g., SECOND, MINUTE, HOUR).
     * @param value The expression value for the part (e.g., "*", "1,2", "5-12").
     * @return this builder instance for chaining.
     */
    public CronPatternBuilder set(final Part part, final String value) {
        parts[part.ordinal()] = value;
        return this;
    }

    /**
     * Builds the cron expression string.
     *
     * @return The cron expression string.
     */
    @Override
    public String build() {
        for (int i = Part.MINUTE.ordinal(); i < Part.YEAR.ordinal(); i++) {
            // For fields from MINUTE to DAY_OF_WEEK, use the default value ('*') if not set by the user.
            // The SECOND and YEAR fields are ignored if not set.
            if (StringKit.isBlank(parts[i])) {
                parts[i] = Symbol.STAR;
            }
        }

        return StringJoiner.of(Symbol.SPACE).setNullMode(StringJoiner.NullMode.IGNORE).append(this.parts).toString();
    }

}
