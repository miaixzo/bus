package org.aoju.bus.logger.dialect.commons;

import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;

/**
 * Apache Commons Logging
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ApacheCommonsLogFactory extends LogFactory {

    public ApacheCommonsLogFactory() {
        super("Apache Common Logging");
        checkLogExist(org.apache.commons.logging.LogFactory.class);
    }

    @Override
    public Log createLog(String name) {
        try {
            return new ApacheCommonsLog4J(name);
        } catch (Exception e) {
            return new ApacheCommonsLog(name);
        }
    }

    @Override
    public Log createLog(Class<?> clazz) {
        try {
            return new ApacheCommonsLog4J(clazz);
        } catch (Exception e) {
            return new ApacheCommonsLog(clazz);
        }
    }

    @Override
    protected void checkLogExist(Object logClassName) {
        super.checkLogExist(logClassName);
        //Commons Logging在调用getLog时才检查是否有日志实现，在此提前检查，如果没有实现则跳过之
        getLog(ApacheCommonsLogFactory.class);
    }
}
