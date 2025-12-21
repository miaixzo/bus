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
package org.miaixz.bus.core.lang;

/**
 * Utility class for common date and time format patterns. Provides clear explanations for various formatting strings.
 * Common date format patterns:
 * <ul>
 * <li>yyyy-MM-dd Example: 2022-08-05</li>
 * <li>yyyy年MM月dd日 Example: 2022年08月05日 (Chinese format)</li>
 * <li>yyyy-MM-dd HH:mm:ss Example: 2022-08-05 12:59:59</li>
 * <li>yyyy-MM-dd HH:mm:ss.SSS Example: 2022-08-05 12:59:59.559</li>
 * <li>yyyy-MM-dd HH:mm:ss.SSSZ Example: 2022-08-05 12:59:59.559+0800 (East Eight Zone China Time), 2022-08-05
 * 04:59:59.559+0000 (Iceland 0 Time Zone), Year Month Day Hour Minute Second Millisecond Time Zone</li>
 * <li>yyyy-MM-dd HH:mm:ss.SSSz Example: 2022-08-05 12:59:59.559UTC (Coordinated Universal Time = 0 Time Zone),
 * 2022-08-05T12:59:59.599GMT (Iceland 0 Time Zone), 2022-08-05T12:59:59.599CST (East Eight Zone China Time),
 * 2022-08-23T03:45:00.599EDT (US Northeast New York Time, -0400), Year Month Day Hour Minute Second Millisecond Time
 * Zone</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z' Example: 2022-08-05T12:59:59.559Z, where: `''` single quotes indicate escape
 * characters, `T`: separator, `Z`: generally refers to UTC, meaning 0 time zone time</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ Example: 2022-08-05T11:59:59.559+0800, where: `Z` indicates time zone</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss.SSSX Example: 2022-08-05T12:59:59.559+08, where: `X`: two-digit time zone, `+08` means:
 * East 8 Zone, China Time Zone</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss.SSSXX Example: 2022-08-05T12:59:59.559+0800, where: `XX`: four-digit time zone</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss.SSSXXX Example: 2022-08-05T12:59:59.559+08:00, where: `XXX`: five-digit time zone</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss Example: 2022-08-05T12:59:59+08</li>
 * <li>yyyy-MM-dd'T'HH:mm:ssXXX Example: 2022-08-05T12:59:59+08:00</li>
 * <li>yyyy-MM-dd'T'HH:mm:ssZ Example: 2022-08-05T12:59:59+0800</li>
 * <li>yyyy-MM-dd'T'HH:mm:ss'Z' Example: 2022-08-05T12:59:59Z</li>
 * <li>EEE MMM dd HH:mm:ss z yyyy Example: Fri Aug 05 12:59:00 UTC+08:00 2022</li>
 * <li>EEE MMM dd HH:mm:ss zzz yyyy Example: Fri Aug 05 12:59:00 UTC+08:00 2022, where `z` indicates UTC time zone, but:
 * 1 to 3 `z`s make no difference</li>
 * <li>EEE, dd MMM yyyy HH:mm:ss z Example: Fri, 05 Aug 2022 12:59:59 UTC+08:00</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Fields {

    /**
     * Constructs a new Fields. Utility class constructor for static access.
     */
    private Fields() {
    }

    /**
     * Year format: yyyy
     */
    public static final String NORM_YEAR = "yyyy";
    /**
     * Year and month format: yyyy-MM
     */
    public static final String NORM_MONTH = "yyyy-MM";

    /**
     * Simple year and month format: yyyyMM
     */
    public static final String SIMPLE_MONTH = "yyyyMM";

    /**
     * Standard date format: yyyy-MM-dd
     */
    public static final String NORM_DATE = "yyyy-MM-dd";

    /**
     * Format wildcard: HH:mm
     */
    public static final String NORM_HOUR_MINUTE = "HH:mm";

    /**
     * Standard time format: HH:mm:ss
     */
    public static final String NORM_TIME = "HH:mm:ss";

    /**
     * Standard date and time format, accurate to minute: yyyy-MM-dd HH:mm
     */
    public static final String NORM_DATETIME_MINUTE = "yyyy-MM-dd HH:mm";

    /**
     * Standard date and time format, accurate to second: yyyy-MM-dd HH:mm:ss
     */
    public static final String NORM_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * Standard date and time format, accurate to millisecond: yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String NORM_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * ISO8601 date and time format, accurate to millisecond: yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final String NORM_DATETIME_COMMA_MS = "yyyy-MM-dd HH:mm:ss,SSS";

    /**
     * Chinese date format: M月d日
     */
    public static final String CN_MONTH = "M月d日";

    /**
     * Standard Chinese date format: yyyy年MM月dd日
     */
    public static final String CN_DATE = "yyyy年MM月dd日";

    /**
     * Standard Chinese date and time format: yyyy年MM月dd日HH时mm分ss秒
     */
    public static final String CN_DATE_TIME = "yyyy年MM月dd日HH时mm分ss秒";

    /**
     * Pure date format: yyyyMMdd
     */
    public static final String PURE_DATE = "yyyyMMdd";

    /**
     * Pure hour and minute format: HHmm
     */
    public static final String PURE_HOUR_MINUTE = "HHmm";
    /**
     * Pure time format: HHmmss
     */
    public static final String PURE_TIME = "HHmmss";

    /**
     * Pure date and time format: yyyyMMddHHmmss
     */
    public static final String PURE_DATETIME = "yyyyMMddHHmmss";

    /**
     * Pure date and time format, accurate to millisecond: yyyyMMddHHmmssSSS
     */
    public static final String PURE_DATETIME_MS = "yyyyMMddHHmmssSSS";

    /**
     * Format wildcard: yyyyMMddHHmmss.SSS
     */
    public static final String PURE_DATETIME_TIP_PATTERN = "yyyyMMddHHmmss.SSS";

    /**
     * HTTP header date and time format: EEE, dd MMM yyyy HH:mm:ss z
     */
    public static final String HTTP_DATETIME = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * JDK date and time format: EEE MMM dd HH:mm:ss zzz yyyy
     */
    public static final String JDK_DATETIME = "EEE MMM dd HH:mm:ss zzz yyyy";

    /**
     * ISO8601 date and time: yyyy-MM-dd'T'HH:mm:ss. According to ISO8601 standard, 'T' separates date and time by
     * default, and no 'Z' at the end indicates local time zone.
     */
    public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * UTC time: yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static final String ISO8601_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * UTC time: yyyy-MM-dd'T'HH:mm:ss'Z'. According to ISO8601 standard, 'Z' suffix indicates UTC time.
     */
    public static final String UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * ISO8601 time: yyyy-MM-dd'T'HH:mm:ssZ, where Z indicates a time offset, such as +0800.
     */
    public static final String ISO8601_WITH_ZONE_OFFSET = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * ISO8601 time: yyyy-MM-dd'T'HH:mm:ssXXX
     */
    public static final String ISO8601_WITH_XXX_OFFSET = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * ISO8601 time: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final String UTC_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * ISO8601 time: yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final String ISO8601_MS_WITH_ZONE_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * ISO8601 time: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static final String ISO8601_MS_WITH_XXX_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * ISO8601 time: yyyy-MM-dd HH:mm:ss 'UTC'
     */
    public static final String ISO8601_MS_WITH_UTC = "yyyy-MM-dd HH:mm:ss 'UTC'";

    /**
     * Format: seconds timestamp (Unix timestamp)
     */
    public static final String FORMAT_SECONDS = "#sss";
    /**
     * Format: milliseconds timestamp
     */
    public static final String FORMAT_MILLISECONDS = "#SSS";

}
