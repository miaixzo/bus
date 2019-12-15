package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.builtin.MadeInOffice;

/**
 * office管理器知道如何执行{@link MadeInOffice}。在执行转换任务之前必须启动office管理器，
 * 并且在不再需要它时必须停止它。停止后就无法重新启动office管理器
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface OfficeManager {

    /**
     * 执行指定的任务并阻塞，直到任务终止.
     *
     * @param task 要执行的任务.
     * @throws InstrumentException 如果发生错误
     */
    void execute(MadeInOffice task) throws InstrumentException;

    /**
     * 获取管理器是否正在运行.
     *
     * @return 如果管理器正在运行，则为{@code true}，否则为{@code false}.
     */
    boolean isRunning();

    /**
     * 启动管理器.
     *
     * @throws InstrumentException 如果管理器不能启动.
     */
    void start() throws InstrumentException;

    /**
     * 停止管理器.
     *
     * @throws InstrumentException 如果管理器不能停止.
     */
    void stop() throws InstrumentException;

}
