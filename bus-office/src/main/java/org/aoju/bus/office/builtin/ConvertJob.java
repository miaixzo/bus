package org.aoju.bus.office.builtin;

import org.aoju.bus.core.lang.exception.InstrumentException;

/**
 * 尚未应用到转换器的完整指定转换.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface ConvertJob {

    /**
     * 执行转换并阻塞，直到转换终止.
     *
     * @throws InstrumentException 如果转换失败.
     */
    void execute() throws InstrumentException;

}
