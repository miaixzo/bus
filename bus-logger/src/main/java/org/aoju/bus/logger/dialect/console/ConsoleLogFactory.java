package org.aoju.bus.logger.dialect.console;

import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;

/**
 * 打印日志
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ConsoleLogFactory extends LogFactory {

    public ConsoleLogFactory() {
        super("Console Logging");
    }

    @Override
    public Log createLog(String name) {
        return new ConsoleLog(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new ConsoleLog(clazz);
    }

}
