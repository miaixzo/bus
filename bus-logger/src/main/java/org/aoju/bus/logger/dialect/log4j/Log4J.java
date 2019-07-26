package org.aoju.bus.logger.dialect.log4j;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.AbstractAware;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * <a href="http://logging.apache.org/log4j/1.2/index.html">Apache Log4J</a> log.<br>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Log4J extends AbstractAware {

    private static final long serialVersionUID = -6843151523380063975L;
    private static final String FQCN = Log4J.class.getName();

    private final Logger logger;

    public Log4J(Logger logger) {
        this.logger = logger;
    }

    public Log4J(Class<?> clazz) {
        this(Logger.getLogger(clazz));
    }

    public Log4J(String name) {
        this(Logger.getLogger(name));
    }

    @Override
    public String getName() {
        return logger.getName();
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
        logger.log(FQCN, Level.TRACE, StringUtils.format(format, arguments), t);
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
        logger.log(FQCN, Level.DEBUG, StringUtils.format(format, arguments), t);
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
        logger.log(FQCN, Level.INFO, StringUtils.format(format, arguments), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    @Override
    public void warn(String format, Object... arguments) {
        warn(null, format, arguments);
    }

    @Override
    public void warn(Throwable t, String format, Object... arguments) {
        logger.log(FQCN, Level.WARN, StringUtils.format(format, arguments), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    @Override
    public void error(String format, Object... arguments) {
        error(null, format, arguments);
    }

    @Override
    public void error(Throwable t, String format, Object... arguments) {
        logger.log(FQCN, Level.ERROR, StringUtils.format(format, arguments), t);
    }

    @Override
    public void log(org.aoju.bus.logger.level.Level level, String format, Object... arguments) {
        log(level, null, format, arguments);
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
        logger.log(fqcn, log4jLevel, StringUtils.format(format, arguments), t);
    }

}
