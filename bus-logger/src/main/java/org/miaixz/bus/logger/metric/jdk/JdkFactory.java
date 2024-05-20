/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.logger.metric.jdk;

import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.ResourceKit;
import org.miaixz.bus.logger.Factory;
import org.miaixz.bus.logger.magic.Log;

import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * JDK日志工厂类
 * java.util.logging
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkFactory extends Factory {

    public JdkFactory() {
        super("JDK Logging");
        readConfig();
    }

    @Override
    public Log createLog(String name) {
        return new JdkLog(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new JdkLog(clazz);
    }

    /**
     * 读取ClassPath下的logging.properties配置文件
     */
    private void readConfig() {
        InputStream in = ResourceKit.getStreamSafe("logging.properties");
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
            IoKit.closeQuietly(in);
        }
    }

}
