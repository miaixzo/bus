/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.logger.metric.tinylog;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.AbstractAware;
import org.tinylog.Level;
import org.tinylog.configuration.Configuration;
import org.tinylog.format.AdvancedMessageFormatter;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.LoggingProvider;
import org.tinylog.provider.ProviderRegistry;

/**
 * tinylog2 log.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TinyLog extends AbstractAware {

    /**
     * 堆栈增加层数,因为封装因此多了两层,此值用于正确获取当前类名
     */
    private static final int DEPTH = 5;
    private static final LoggingProvider provider = ProviderRegistry.getLoggingProvider();
    private static final MessageFormatter formatter = new AdvancedMessageFormatter(
            Configuration.getLocale(),
            Configuration.isEscapingEnabled()
    );
    private final int level;
    private final String name;

    public TinyLog(Class<?> clazz) {
        this(null == clazz ? Normal.NULL : clazz.getName());
    }

    public TinyLog(String name) {
        this.name = name;
        this.level = provider.getMinimumLevel().ordinal();
    }

    /**
     * 如果最后一个参数为异常参数，则获取之，否则返回null
     *
     * @param arguments 参数
     * @return 最后一个异常参数
     */
    private static Throwable getLastArgumentIfThrowable(Object... arguments) {
        if (ArrayKit.isNotEmpty(arguments) && arguments[arguments.length - 1] instanceof Throwable) {
            return (Throwable) arguments[arguments.length - 1];
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isTrace() {
        return this.level <= Level.TRACE.ordinal();
    }

    @Override
    public void trace(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.TRACE, t, format, arguments);
    }

    @Override
    public boolean isDebug() {
        return this.level <= Level.DEBUG.ordinal();
    }

    @Override
    public void debug(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.DEBUG, t, format, arguments);
    }

    @Override
    public boolean isInfo() {
        return this.level <= Level.INFO.ordinal();
    }

    @Override
    public void info(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.INFO, t, format, arguments);
    }

    @Override
    public boolean isWarn() {
        return this.level <= Level.WARN.ordinal();
    }

    @Override
    public boolean isError() {
        return this.level <= Level.ERROR.ordinal();
    }

    @Override
    public void error(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.ERROR, t, format, arguments);
    }

    @Override
    public void log(String fqcn, org.miaixz.bus.logger.Level level, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, toTinyLevel(level), t, format, arguments);
    }

    @Override
    public boolean isEnabled(org.miaixz.bus.logger.Level level) {
        return this.level <= toTinyLevel(level).ordinal();
    }

    @Override
    public void warn(String fqcn, Throwable t, String format, Object... arguments) {
        logIfEnabled(fqcn, Level.WARN, t, format, arguments);
    }

    /**
     * 在对应日志级别打开情况下打印日志
     *
     * @param fqcn      完全限定类名(Fully Qualified Class Name)，用于定位日志位置
     * @param level     日志级别
     * @param t         异常，null则检查最后一个参数是否为Throwable类型，是则取之，否则不打印堆栈
     * @param format    日志消息模板
     * @param arguments 日志消息参数
     */
    private void logIfEnabled(String fqcn, Level level, Throwable t, String format, Object... arguments) {
        if (null == t) {
            t = getLastArgumentIfThrowable(arguments);
        }
        provider.log(DEPTH, null, level, t, formatter, StringKit.toString(format), arguments);
    }

    /**
     * Tinylog的Level等级
     *
     * @param level Level等级
     * @return Tinylog的Level
     */
    private Level toTinyLevel(org.miaixz.bus.logger.Level level) {
        Level tinyLevel;
        switch (level) {
            case TRACE:
                tinyLevel = Level.TRACE;
                break;
            case DEBUG:
                tinyLevel = Level.DEBUG;
                break;
            case INFO:
                tinyLevel = Level.INFO;
                break;
            case WARN:
                tinyLevel = Level.WARN;
                break;
            case ERROR:
                tinyLevel = Level.ERROR;
                break;
            case OFF:
                tinyLevel = Level.OFF;
                break;
            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
        return tinyLevel;
    }

}
