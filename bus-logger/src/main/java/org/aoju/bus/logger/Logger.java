package org.aoju.bus.logger;

import org.aoju.bus.core.lang.caller.CallerUtil;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.level.Level;

/**
 * 静态日志类，用于在不引入日志对象的情况下打印日志
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public final class Logger {

    private static final String FQCN = Logger.class.getName();

    private Logger() {
    }

    /**
     * Trace等级日志，小于debug<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void trace(String format, Object... arguments) {
        trace(LogFactory.get(CallerUtil.getCallers()), format, arguments);
    }

    /**
     * Trace等级日志，小于Debug
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void trace(Log log, String format, Object... arguments) {
        if (false == log(log, Level.TRACE, null, format, arguments)) {
            log.trace(format, arguments);
        }
    }

    /**
     * Debug等级日志，小于Info<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void debug(String format, Object... arguments) {
        debug(LogFactory.get(CallerUtil.getCallers()), format, arguments);
    }

    /**
     * Debug等级日志，小于Info
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void debug(Log log, String format, Object... arguments) {
        if (false == log(log, Level.DEBUG, null, format, arguments)) {
            log.debug(format, arguments);
        }
    }

    /**
     * Info等级日志，小于Warn<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void info(String format, Object... arguments) {
        info(LogFactory.get(CallerUtil.getCallers()), format, arguments);
    }

    /**
     * Info等级日志，小于Warn
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void info(Log log, String format, Object... arguments) {
        if (false == log(log, Level.INFO, null, format, arguments)) {
            log.info(format, arguments);
        }
    }

    /**
     * Warn等级日志，小于Error<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(String format, Object... arguments) {
        warn(LogFactory.get(CallerUtil.getCallers()), format, arguments);
    }

    /**
     * Warn等级日志，小于Error<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(Throwable e, String format, Object... arguments) {
        warn(LogFactory.get(CallerUtil.getCallers()), e, StringUtils.format(format, arguments));
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(Log log, String format, Object... arguments) {
        warn(log, null, format, arguments);
    }

    /**
     * Warn等级日志，小于Error
     *
     * @param log       日志对象
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void warn(Log log, Throwable e, String format, Object... arguments) {
        if (false == log(log, Level.WARN, e, format, arguments)) {
            log.warn(e, format, arguments);
        }
    }

    /**
     * Error等级日志<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param e 需在日志中堆栈打印的异常
     */
    public static void error(Throwable e) {
        error(LogFactory.get(CallerUtil.getCallers()), e);
    }

    /**
     * Error等级日志<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(String format, Object... arguments) {
        error(LogFactory.get(CallerUtil.getCallers()), format, arguments);
    }

    /**
     * Error等级日志<br>
     * 由于动态获取Log，效率较低，建议在非频繁调用的情况下使用！！
     *
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(Throwable e, String format, Object... arguments) {
        error(LogFactory.get(CallerUtil.getCallers()), e, format, arguments);
    }

    /**
     * Error等级日志<br>
     *
     * @param log 日志对象
     * @param e   需在日志中堆栈打印的异常
     */
    public static void error(Log log, Throwable e) {
        error(log, e, e.getMessage());
    }

    /**
     * Error等级日志<br>
     *
     * @param log       日志对象
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(Log log, String format, Object... arguments) {
        error(log, null, format, arguments);
    }

    /**
     * Error等级日志<br>
     *
     * @param log       日志对象
     * @param e         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     */
    public static void error(Log log, Throwable e, String format, Object... arguments) {
        if (false == log(log, Level.ERROR, e, format, arguments)) {
            log.error(e, format, arguments);
        }
    }

    /**
     * 打印日志<br>
     *
     * @param level     日志级别
     * @param t         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     * @return 是否为LocationAwareLog日志
     */
    public static boolean log(Level level, Throwable t, String format, Object... arguments) {
        return log(LogFactory.get(CallerUtil.getCallers()), level, t, format, arguments);
    }

    /**
     * 打印日志<br>
     *
     * @param log       日志对象
     * @param level     日志级别
     * @param t         需在日志中堆栈打印的异常
     * @param format    格式文本，{} 代表变量
     * @param arguments 变量对应的参数
     * @return 是否为LocationAwareLog日志
     */
    public static boolean log(Log log, Level level, Throwable t, String format, Object... arguments) {
        if (log instanceof LocationAware) {
            ((LocationAware) log).log(FQCN, level, t, format, arguments);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获得Log
     *
     * @param clazz 日志发出的类
     * @return Log
     */
    public static Log get(Class<?> clazz) {
        return LogFactory.get(clazz);
    }

    /**
     * 获得Log
     *
     * @param name 自定义的日志发出者名称
     * @return Log
     */
    public static Log get(String name) {
        return LogFactory.get(name);
    }

    /**
     * @return 获得日志，自动判定日志发出者
     */
    public static Log get() {
        return LogFactory.get(CallerUtil.getCallers());
    }

}
