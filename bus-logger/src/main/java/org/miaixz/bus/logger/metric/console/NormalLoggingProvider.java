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
package org.miaixz.bus.logger.metric.console;

import java.io.Serial;

import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.magic.AbstractProvider;

/**
 * 利用 System.out.println 打印日志
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NormalLoggingProvider extends AbstractProvider {

    @Serial
    private static final long serialVersionUID = 2852287011503L;

    /**
     * 日志级别
     */
    private static Level _level = Level.DEBUG;

    /**
     * 构造
     *
     * @param clazz 类
     */
    public NormalLoggingProvider(final Class<?> clazz) {
        this.name = (null == clazz) ? Normal.NULL : clazz.getName();
    }

    /**
     * 构造
     *
     * @param name 类名
     */
    public NormalLoggingProvider(final String name) {
        this.name = name;
    }

    /**
     * 设置自定义的日志显示级别
     *
     * @param level 自定义级别
     */
    public static void setLevel(final Level level) {
        Assert.notNull(level);
        _level = level;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(Level.TRACE);
    }

    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.TRACE, t, format, args);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }

    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.DEBUG, t, format, args);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.INFO, t, format, args);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }

    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.WARN, t, format, args);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
    }

    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        log(fqcn, Level.ERROR, t, format, args);
    }

    @Override
    public void log(final String fqcn, final Level level, final Throwable t, final String format,
            final Object... args) {
        if (!isEnabled(level)) {
            return;
        }
        final Dictionary dict = Dictionary.of().set("date", DateKit.formatNow()).set("level", level.toString())
                .set("name", this.name).set("msg", StringKit.format(format, args));

        final String logMsg = StringKit.formatByMap("[{date}] [{level}] {name}: {msg}", dict);

        // WARN以上级别打印至System.err
        if (level.ordinal() >= Level.WARN.ordinal()) {
            Console.error(t, logMsg);
        } else {
            Console.log(t, logMsg);
        }
    }

    @Override
    public boolean isEnabled(final Level level) {
        return _level.compareTo(level) <= 0;
    }

}
