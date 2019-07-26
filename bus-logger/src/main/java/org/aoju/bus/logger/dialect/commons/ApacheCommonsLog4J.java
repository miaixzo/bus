package org.aoju.bus.logger.dialect.commons;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.AbstractAware;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;

/**
 * Apache-Commons-Logging日志库的实现封装
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ApacheCommonsLog4J extends AbstractAware {

    private static final long serialVersionUID = -6843151523380063975L;

    private static final String FQCN = ApacheCommonsLog4J.class.getName();

    private final transient Log4JLogger logger;
    private final String name;

    public ApacheCommonsLog4J(Log logger, String name) {
        this.logger = (Log4JLogger) logger;
        this.name = name;
    }

    public ApacheCommonsLog4J(Class<?> clazz) {
        this(LogFactory.getLog(clazz), clazz.getName());
    }

    public ApacheCommonsLog4J(String name) {
        this(LogFactory.getLog(name), name);
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
    public void trace(String format, Object... arguments) {
        trace(null, format, arguments);
    }

    @Override
    public void trace(Throwable t, String format, Object... arguments) {
        logger.getLogger().log(FQCN, Level.TRACE, StringUtils.format(format, arguments), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String format, Object... arguments) {
        debug(null, format, arguments);
    }

    @Override
    public void debug(Throwable t, String format, Object... arguments) {
        logger.getLogger().log(FQCN, Level.DEBUG, StringUtils.format(format, arguments), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String format, Object... arguments) {
        info(null, format, arguments);
    }

    @Override
    public void info(Throwable t, String format, Object... arguments) {
        logger.getLogger().log(FQCN, Level.INFO, StringUtils.format(format, arguments), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String format, Object... arguments) {
        warn(null, format, arguments);
    }

    @Override
    public void warn(Throwable t, String format, Object... arguments) {
        logger.getLogger().log(FQCN, Level.WARN, StringUtils.format(format, arguments), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String format, Object... arguments) {
        error(null, format, arguments);
    }

    @Override
    public void error(Throwable t, String format, Object... arguments) {
        logger.getLogger().log(FQCN, Level.ERROR, StringUtils.format(format, arguments), t);
    }

    @Override
    public void log(org.aoju.bus.logger.level.Level level, String format, Object... arguments) {
        this.log(level, null, format, arguments);
    }

    @Override
    public void log(org.aoju.bus.logger.level.Level level, Throwable t, String format, Object... arguments) {
        this.log(FQCN, level, t, format, arguments);
    }

    @Override
    public void log(String fqcn, org.aoju.bus.logger.level.Level level, Throwable t, String format, Object... arguments) {
        Level log4jLevel;
        switch (level) {
            case TRACE:
                log4jLevel = Level.TRACE;
                break;
            case DEBUG:
                log4jLevel = Level.DEBUG;
                break;
            case INFO:
                log4jLevel = Level.INFO;
                break;
            case WARN:
                log4jLevel = Level.WARN;
                break;
            case ERROR:
                log4jLevel = Level.ERROR;
                break;
            default:
                throw new Error(StringUtils.format("Can not identify level: {}", level));
        }
        logger.getLogger().log(FQCN, log4jLevel, StringUtils.format(format, arguments), t);
    }

}
