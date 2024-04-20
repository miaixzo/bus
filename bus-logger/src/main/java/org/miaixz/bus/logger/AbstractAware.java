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
package org.miaixz.bus.logger;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.level.Level;

import java.io.Serializable;

/**
 * 抽象日志类
 * 实现了一些通用的接口
 *
 * @author Kimi Liu
 * @since Java 17++
 */
public abstract class AbstractAware implements Log, Serializable {

    private static final String FQCN = AbstractAware.class.getName();

    @Override
    public boolean isEnabled(Level level) {
        switch (level) {
            case TRACE:
                return isTrace();
            case DEBUG:
                return isDebug();
            case INFO:
                return isInfo();
            case WARN:
                return isWarn();
            case ERROR:
                return isError();
            default:
                throw new Error(StringKit.format("Can not identify level: {}", level));
        }
    }

    @Override
    public void trace(Throwable t) {
        trace(t, (null == t) ? Normal.NULL : t.getMessage());
    }

    @Override
    public void trace(String format, Object... arguments) {
        trace(null, format, arguments);
    }

    @Override
    public void trace(Throwable t, String format, Object... arguments) {
        trace(FQCN, t, format, arguments);
    }

    @Override
    public void debug(Throwable t) {
        debug(t, (null == t) ? Normal.NULL : t.getMessage());
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            debug((Throwable) arguments[0], format);
        } else {
            debug(null, format, arguments);
        }
    }

    @Override
    public void debug(Throwable t, String format, Object... arguments) {
        debug(FQCN, t, format, arguments);
    }

    @Override
    public void info(Throwable t) {
        info(t, (null == t) ? Normal.NULL : t.getMessage());
    }

    @Override
    public void info(String format, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            info((Throwable) arguments[0], format);
        } else {
            info(null, format, arguments);
        }
    }

    @Override
    public void info(Throwable t, String format, Object... arguments) {
        info(FQCN, t, format, arguments);
    }

    @Override
    public void warn(Throwable t) {
        warn(t, (null == t) ? Normal.NULL : t.getMessage());
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            warn((Throwable) arguments[0], format);
        } else {
            warn(null, format, arguments);
        }
    }

    @Override
    public void warn(Throwable t, String format, Object... arguments) {
        warn(FQCN, t, format, arguments);
    }

    @Override
    public void error(Throwable t) {
        this.error(t, (null == t) ? Normal.NULL : t.getMessage());
    }

    @Override
    public void error(String format, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            error((Throwable) arguments[0], format);
        } else {
            error(null, format, arguments);
        }
    }

    @Override
    public void error(Throwable t, String format, Object... arguments) {
        error(FQCN, t, format, arguments);
    }

    @Override
    public void log(Level level, String format, Object... arguments) {
        if (null != arguments && 1 == arguments.length && arguments[0] instanceof Throwable) {
            log(level, (Throwable) arguments[0], format);
        } else {
            log(level, null, format, arguments);
        }
    }

    @Override
    public void log(Level level, Throwable t, String format, Object... arguments) {
        this.log(FQCN, level, t, format, arguments);
    }

}
