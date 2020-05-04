package org.aoju.bus.image;


import org.aoju.bus.core.lang.exception.InstrumentException;

/**
 * 进程服务管理器
 * 1. 端口监听进程
 * 2. 设备服务进程
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public interface Centre {

    /**
     * 获取管理器是否正在运行
     *
     * @return 如果管理器正在运行，则为{@code true}，否则为{@code false}
     */
    boolean isRunning();

    /**
     * 启动管理器
     *
     * @throws InstrumentException 如果管理器不能启动
     */
    void start() throws InstrumentException;

    /**
     * 停止管理器
     *
     * @throws InstrumentException 如果管理器不能停止
     */
    void stop() throws InstrumentException;

}
