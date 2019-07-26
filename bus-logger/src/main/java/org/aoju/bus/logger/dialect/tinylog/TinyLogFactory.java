package org.aoju.bus.logger.dialect.tinylog;

import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;

/**
 * <a href="http://www.tinylog.org/">TinyLog</a> log.<br>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class TinyLogFactory extends LogFactory {

    /**
     * 构造
     */
    public TinyLogFactory() {
        super("TinyLog");
        checkLogExist(org.pmw.tinylog.Logger.class);
    }

    @Override
    public Log createLog(String name) {
        return new TinyLog(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new TinyLog(clazz);
    }

}
