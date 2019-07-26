package org.aoju.bus.logger.dialect.log4j;

import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;

/**
 * <a href="http://logging.apache.org/log4j/1.2/index.html">Apache Log4J</a> log.<br>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Log4jLogFactory extends LogFactory {

    public Log4jLogFactory() {
        super("Log4j");
        checkLogExist(org.apache.log4j.Logger.class);
    }

    @Override
    public Log createLog(String name) {
        return new Log4J(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new Log4J(clazz);
    }

}
