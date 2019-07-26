package org.aoju.bus.logger;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.level.Level;

import java.io.Serializable;

/**
 * 抽象日志类<br>
 * 实现了一些通用的接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractLog implements Log, Serializable {

    private static final long serialVersionUID = -3211115409504005616L;

    /**
     * 获得消息，调用异常类的getMessage方法
     *
     * @param e 异常
     * @return 消息
     */
    public static String getSimpleMessage(Throwable e) {
        return (null == e) ? "null" : e.getMessage();
    }

    @Override
    public boolean isEnabled(Level level) {
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
                throw new Error(StringUtils.format("Can not identify level: {}", level));
        }
    }

    @Override
    public void trace(Throwable t) {
        this.trace(t, getSimpleMessage(t));
    }

    @Override
    public void debug(Throwable t) {
        this.debug(t, getSimpleMessage(t));
    }

    @Override
    public void info(Throwable t) {
        this.info(t, getSimpleMessage(t));
    }

    @Override
    public void warn(Throwable t) {
        this.warn(t, getSimpleMessage(t));
    }

    @Override
    public void error(Throwable t) {
        this.error(t, getSimpleMessage(t));
    }
}
