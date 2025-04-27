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
package org.miaixz.bus.cron;

import java.util.TimeZone;

/**
 * 定时任务配置类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Configure {

    /**
     * 时区
     */
    protected TimeZone timezone = TimeZone.getDefault();
    /**
     * 是否支持秒匹配
     */
    protected boolean matchSecond;

    /**
     * 构造
     */
    public Configure() {

    }

    /**
     * 获得时区，默认为 {@link TimeZone#getDefault()}
     *
     * @return 时区
     */
    public TimeZone getTimeZone() {
        return this.timezone;
    }

    /**
     * 设置时区
     *
     * @param timezone 时区
     * @return this
     */
    public Configure setTimeZone(final TimeZone timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * 是否支持秒匹配
     *
     * @return {@code true}使用，{@code false}不使用
     */
    public boolean isMatchSecond() {
        return this.matchSecond;
    }

    /**
     * 设置是否支持秒匹配，默认不使用
     *
     * @param isMatchSecond {@code true}支持，{@code false}不支持
     * @return this
     */
    public Configure setMatchSecond(final boolean isMatchSecond) {
        this.matchSecond = isMatchSecond;
        return this;
    }

}
