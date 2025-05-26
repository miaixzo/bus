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
package org.miaixz.bus.logger.magic;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Level;
import org.miaixz.bus.logger.Provider;

/**
 * 抽象日志类 实现了一些通用的接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractProvider implements Provider, Serializable {

    @Serial
    private static final long serialVersionUID = 2852280299793L;

    private static final String FQCN = AbstractProvider.class.getName();

    protected String name;

    @Override
    public boolean isEnabled(final Level level) {
        switch (level) {
        case TRACE:
            return isTraceEnabled();
        case DEBUG:
            return isDebugEnabled();
        case INFO:
            return isInfoEnabled();
        case WARN:
            return isWarnEnabled();
        case ERROR:
            return isErrorEnabled();
        default:
            throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

    @Override
    public void trace(final Throwable t) {
        trace(t, ExceptionKit.getSimpleMessage(t));
    }

    @Override
    public void trace(final String format, final Object... args) {
        trace(null, format, args);
    }

    @Override
    public void trace(final Throwable t, final String format, final Object... args) {
        trace(FQCN, t, format, args);
    }

    @Override
    public void debug(final Throwable t) {
        debug(t, ExceptionKit.getSimpleMessage(t));
    }

    @Override
    public void debug(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            debug((Throwable) args[0], format);
        } else {
            debug(null, format, args);
        }
    }

    @Override
    public void debug(final Throwable t, final String format, final Object... args) {
        debug(FQCN, t, format, args);
    }

    @Override
    public void info(final Throwable t) {
        info(t, ExceptionKit.getSimpleMessage(t));
    }

    @Override
    public void info(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            info((Throwable) args[0], format);
        } else {
            info(null, format, args);
        }
    }

    @Override
    public void info(final Throwable t, final String format, final Object... args) {
        info(FQCN, t, format, args);
    }

    @Override
    public void warn(final Throwable t) {
        warn(t, ExceptionKit.getSimpleMessage(t));
    }

    @Override
    public void warn(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            warn((Throwable) args[0], format);
        } else {
            warn(null, format, args);
        }
    }

    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
        warn(FQCN, t, format, args);
    }

    @Override
    public void error(final Throwable t) {
        this.error(t, ExceptionKit.getSimpleMessage(t));
    }

    @Override
    public void error(final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            error((Throwable) args[0], format);
        } else {
            error(null, format, args);
        }
    }

    @Override
    public void error(final Throwable t, final String format, final Object... args) {
        error(FQCN, t, format, args);
    }

    @Override
    public void log(final Level level, final String format, final Object... args) {
        if (null != args && 1 == args.length && args[0] instanceof Throwable) {
            // 兼容Slf4j中的xxx(String message, Throwable e)
            log(level, (Throwable) args[0], format);
        } else {
            log(level, null, format, args);
        }
    }

    @Override
    public void log(final Level level, final Throwable t, final String format, final Object... args) {
        this.log(FQCN, level, t, format, args);
    }

}
