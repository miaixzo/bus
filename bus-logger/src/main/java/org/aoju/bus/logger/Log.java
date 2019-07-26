package org.aoju.bus.logger;

import org.aoju.bus.logger.level.*;

/**
 * 日志统一接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Log extends TraceLog, DebugLog, InfoLog, WarnLog, ErrorLog {

    /**
     * @return 日志对象的Name
     */
    String getName();

    /**
     * 是否开启指定日志
     *
     * @param level 日志级别
     * @return 是否开启指定级别
     */
    boolean isEnabled(Level level);

    /**
     * 打印指定级别的日志
     *
     * @param level     级别
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(Level level, String format, Object... arguments);

    /**
     * 打印 指定级别的日志
     *
     * @param level     级别
     * @param t         错误对象
     * @param format    消息模板
     * @param arguments 参数
     */
    void log(Level level, Throwable t, String format, Object... arguments);
}
