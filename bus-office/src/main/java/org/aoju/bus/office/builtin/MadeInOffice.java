package org.aoju.bus.office.builtin;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.metric.OfficeManager;

/**
 * 表示由{@link OfficeManager}执行的任务.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface MadeInOffice {

    /**
     * 在上下文中执行任务.
     *
     * @param context office环境上下文.
     * @throws InstrumentException 如果发生错误.
     */
    void execute(Context context) throws InstrumentException;

}
