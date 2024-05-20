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
package org.miaixz.bus.logger.metric.jboss;

import org.jboss.logging.Logger;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.AbstractAware;
import org.miaixz.bus.logger.Level;

/**
 * Jboss-Logging log.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JbossLog extends AbstractAware {

    private final transient Logger logger;

    /**
     * 构造
     *
     * @param logger {@link Logger}
     */
    public JbossLog(Logger logger) {
        this.logger = logger;
    }

    /**
     * 构造
     *
     * @param clazz 日志打印所在类
     */
    public JbossLog(Class<?> clazz) {
        this((null == clazz) ? Normal.NULL : clazz.getName());
    }

    /**
     * 构造
     *
     * @param name 日志打印所在类名
     */
    public JbossLog(String name) {
        this(Logger.getLogger(name));
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTrace() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String fqcn, Throwable t, String format, Object... arguments) {
        if (isTrace()) {
            logger.trace(fqcn, StringKit.format(format, arguments), t);
        }
    }

    @Override
    public boolean isDebug() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String fqcn, Throwable t, String format, Object... arguments) {
        if (isDebug()) {
            logger.debug(fqcn, StringKit.format(format, arguments), t);
        }
    }

    @Override
    public boolean isInfo() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String fqcn, Throwable t, String format, Object... arguments) {
        if (isInfo()) {
            logger.info(fqcn, StringKit.format(format, arguments), t);
        }
    }

    @Override
    public boolean isWarn() {
        return logger.isEnabled(Logger.Level.WARN);
    }

    @Override
    public void warn(String fqcn, Throwable t, String format, Object... arguments) {
        if (isWarn()) {
            logger.warn(fqcn, StringKit.format(format, arguments), t);
        }
    }

    @Override
    public boolean isError() {
        return logger.isEnabled(Logger.Level.ERROR);
    }

    @Override
    public void error(String fqcn, Throwable t, String format, Object... arguments) {
        if (isError()) {
            logger.error(fqcn, StringKit.format(format, arguments), t);
        }
    }

    @Override
    public void log(String fqcn, Level level, Throwable t, String format, Object... arguments) {
        switch (level) {
            case TRACE:
                trace(fqcn, t, format, arguments);
                break;
            case DEBUG:
                debug(fqcn, t, format, arguments);
                break;
            case INFO:
                info(fqcn, t, format, arguments);
                break;
            case WARN:
                warn(fqcn, t, format, arguments);
                break;
            case ERROR:
                error(fqcn, t, format, arguments);
                break;
            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

}
