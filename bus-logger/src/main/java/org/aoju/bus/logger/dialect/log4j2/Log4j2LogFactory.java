package org.aoju.bus.logger.dialect.log4j2;

import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;

/**
 * <a href="http://logging.apache.org/log4j/2.x/index.html">Apache Log4J 2</a> log.<br>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Log4j2LogFactory extends LogFactory {

    public Log4j2LogFactory() {
        super("Log4j2");
        checkLogExist(org.apache.logging.log4j.LogManager.class);
    }

    @Override
    public Log createLog(String name) {
        return new Log4J2(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new Log4J2(clazz);
    }

}
