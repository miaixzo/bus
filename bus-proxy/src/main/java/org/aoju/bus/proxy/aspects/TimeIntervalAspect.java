package org.aoju.bus.proxy.aspects;


import org.aoju.bus.core.date.TimeInterval;
import org.aoju.bus.logger.Logger;

import java.lang.reflect.Method;

/**
 * 通过日志打印方法的执行时间的切面
 *
 * @author Kimi Liu
 * @version 5.2.0
 * @since JDK 1.8+
 */
public class TimeIntervalAspect extends SimpleAspect {

    private static final long serialVersionUID = 1L;

    private TimeInterval interval = new TimeInterval();

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        interval.start();
        return true;
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        Logger.info("Method [{}.{}] execute spend [{}]ms return value [{}]",
                target.getClass().getName(),
                method.getName(),
                interval.intervalMs(),
                returnVal);
        return true;
    }

}
