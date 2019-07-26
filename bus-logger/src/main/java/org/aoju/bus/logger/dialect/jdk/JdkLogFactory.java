package org.aoju.bus.logger.dialect.jdk;

import org.aoju.bus.core.lang.Console;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ResourceUtils;
import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * JDK日志工厂类
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/logging/index.html">java.util.logging</a> log.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JdkLogFactory extends LogFactory {

    public JdkLogFactory() {
        super("JDK Logging");
        readConfig();
    }

    @Override
    public Log createLog(String name) {
        return new Jdk(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new Jdk(clazz);
    }

    /**
     * 读取ClassPath下的logging.properties配置文件
     */
    private void readConfig() {
        //避免循环引用，Log初始化的时候不使用相关工具类
        InputStream in = ResourceUtils.getStreamSafe("logging.properties");
        if (null == in) {
            System.err.println("[WARN] Can not find [logging.properties], use [%JRE_HOME%/lib/logging.properties] as default!");
            return;
        }

        try {
            LogManager.getLogManager().readConfiguration(in);
        } catch (Exception e) {
            Console.error(e, "Read [logging.properties] from classpath error!");
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (Exception e1) {
                Console.error(e, "Read [logging.properties] from [%JRE_HOME%/lib/logging.properties] error!");
            }
        } finally {
            IoUtils.close(in);
        }
    }

}
