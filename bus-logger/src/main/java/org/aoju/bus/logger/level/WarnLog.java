package org.aoju.bus.logger.level;

/**
 * WARN级别日志接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface WarnLog {
    /**
     * @return WARN 等级是否开启
     */
    boolean isWarnEnabled();

    /**
     * 打印 WARN 等级的日志
     *
     * @param t 错误对象
     */
    void warn(Throwable t);

    /**
     * 打印 WARN 等级的日志
     *
     * @param format    消息模板
     * @param arguments 参数
     */
    void warn(String format, Object... arguments);

    /**
     * 打印 WARN 等级的日志
     *
     * @param t         错误对象
     * @param format    消息模板
     * @param arguments 参数
     */
    void warn(Throwable t, String format, Object... arguments);
}
