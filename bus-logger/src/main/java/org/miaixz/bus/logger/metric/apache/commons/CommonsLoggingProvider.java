/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.logger.metric.apache.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.magic.AbstractProvider;

/**
 * apache commons logging
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CommonsLoggingProvider extends AbstractProvider {

    private static final long serialVersionUID = -1L;

    /**
     * 日志门面
     */
    private final transient Log logger;

    /**
     * 构造
     *
     * @param name 名称
     */
    public CommonsLoggingProvider(final String name) {
        this(LogFactory.getLog(name), name);
    }

    /**
     * 构造
     *
     * @param clazz 日志实现类
     */
    public CommonsLoggingProvider(final Class<?> clazz) {
        this(LogFactory.getLog(clazz), null == clazz ? Normal.NULL : clazz.getName());
    }

    /**
     * 构造
     *
     * @param logger 日志对象
     * @param name   名称
     */
    public CommonsLoggingProvider(final Log logger, final String name) {
        this.logger = logger;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isTraceEnabled()) {
            logger.trace(StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isDebugEnabled()) {
            logger.debug(StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isInfoEnabled()) {
            logger.info(StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(final String format, final Object... args) {
        if (isWarnEnabled()) {
            logger.warn(StringKit.format(format, args));
        }
    }

    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
    }

    @Override
    public void warn(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isWarnEnabled()) {
            logger.warn(StringKit.format(format, args), t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(final String fqcn, final Throwable t, final String format, final Object... args) {
        if (isErrorEnabled()) {
            logger.warn(StringKit.format(format, args), t);
        }

    }

    @Override
    public void log(final String fqcn, final Level level, final Throwable t, final String format,
            final Object... args) {
        switch (level) {
        case TRACE:
            trace(t, format, args);
            break;
        case DEBUG:
            debug(t, format, args);
            break;
        case INFO:
            info(t, format, args);
            break;
        case WARN:
            warn(t, format, args);
            break;
        case ERROR:
            error(t, format, args);
            break;
        default:
            throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

}
