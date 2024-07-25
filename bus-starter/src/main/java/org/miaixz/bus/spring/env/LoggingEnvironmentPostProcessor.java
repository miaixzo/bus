/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.spring.env;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过 {@link EnvironmentPostProcessor} 实现日志配置检测，初始化等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {


    /**
     * 保持版本兼容，设置以下系统属性:
     *
     * <pre>
     *  1、spring.output.ansi.enabled
     *  2、logging.path
     *  3、file.encoding
     *  4、logging.pattern.console
     *  5、logging.pattern.file
     * </pre>
     *
     * @param context 缓存信息
     * @param keep    是否跳过
     */
    public static void keepCompatible(Map<String, String> context, boolean keep) {
        if (!keep) {
            return;
        }
        // 日志路径
        String loggingPath = System.getProperty(GeniusBuilder.LOGGING_PATH, context.get(GeniusBuilder.LOGGING_PATH));
        System.setProperty(GeniusBuilder.LOGGING_PATH, loggingPath);
        // 日志编码
        String fileEncoding = System.getProperty(Keys.FILE_ENCODING, context.get(Keys.FILE_ENCODING));
        System.setProperty(Keys.FILE_ENCODING, fileEncoding);

        // 控制台日志
        String patternConsole = System.getProperty(GeniusBuilder.LOGGING_PATTERN_CONSOLE, context.get(GeniusBuilder.LOGGING_PATTERN_CONSOLE));
        if (StringKit.isEmpty(patternConsole)) {
            patternConsole = "%green(%d{yyyy-MM-dd HH:mm:ss.SSSXXX}) [%highlight(%5p)] %magenta(${PID:- }) %yellow(-) %highlight(%-50.50logger{50}) %yellow(%5.5L) %cyan(:) %magenta(%m%n)";
        }
        System.setProperty(GeniusBuilder.LOGGING_PATTERN_CONSOLE, patternConsole);

        // 文件日志
        String patternFile = System.getProperty(GeniusBuilder.LOGGING_PATTERN_FILE, context.get(GeniusBuilder.LOGGING_PATTERN_FILE));
        if (StringKit.isEmpty(patternFile)) {
            patternFile = "%d{yyyy-MM-dd HH:mm:ss.SSSXXX} [%5p] ${PID:- } - %-50.50logger{50} %5.5L : %m%n";
        }
        System.setProperty(GeniusBuilder.LOGGING_PATTERN_FILE, patternFile);

    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        Map<String, String> context = new HashMap<>();
        loadLogConfiguration(GeniusBuilder.LOGGING_PATH, environment.getProperty(GeniusBuilder.LOGGING_PATH),
                context, Keys.get(Keys.USER_NAME) + GeniusBuilder.BUS_LOGGING_PATH);
        loadLogConfiguration(Keys.FILE_ENCODING, environment.getProperty(Keys.FILE_ENCODING), context, null);

        loadLogConfiguration(GeniusBuilder.LOGGING_PATTERN_CONSOLE, environment.getProperty(GeniusBuilder.LOGGING_PATTERN_CONSOLE), context, null);

        loadLogConfiguration(GeniusBuilder.LOGGING_PATTERN_FILE, environment.getProperty(GeniusBuilder.LOGGING_PATTERN_FILE), context, null);

        keepCompatible(context, true);
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    /**
     * 加载配置信息
     *
     * @param key          属性Key
     * @param value        属性Key对应值
     * @param context      缓存信息
     * @param defaultValue 默认值
     */
    public void loadLogConfiguration(String key, String value, Map<String, String> context, String defaultValue) {
        if (StringKit.hasText(value)) {
            context.put(key, value);
        } else if (StringKit.hasText(defaultValue)) {
            context.put(key, defaultValue);
        }
    }

}
