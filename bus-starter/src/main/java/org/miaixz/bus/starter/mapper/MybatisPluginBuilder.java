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
package org.miaixz.bus.starter.mapper;

import org.apache.ibatis.plugin.Interceptor;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.pager.plugins.PageSqlHandler;
import org.miaixz.bus.spring.BusXConfig;
import org.miaixz.bus.spring.PlaceBinder;
import org.miaixz.bus.starter.sensitive.SensitiveProperties;
import org.miaixz.bus.starter.sensitive.SensitiveResultSetHandler;
import org.miaixz.bus.starter.sensitive.SensitiveStatementHandler;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * mybatis 插件启用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MybatisPluginBuilder {

    public static List<Interceptor> plugins = new ArrayList<>();

    public static Interceptor[] build(Environment environment) {
        List<Interceptor> list = CollKit.newArrayList(
                new NatureSqlHandler(),
                new ExplainSqlHandler()
        );

        if (ObjectKit.isNotEmpty(environment)) {
            MybatisProperties mybatisProperties = PlaceBinder.bind(environment, MybatisProperties.class, BusXConfig.MYBATIS);
            if (ObjectKit.isNotEmpty(mybatisProperties)) {
                Properties p = new Properties();
                p.setProperty("autoDelimitKeywords", mybatisProperties.getAutoDelimitKeywords());
                p.setProperty("reasonable", mybatisProperties.getReasonable());
                p.setProperty("supportMethodsArguments", mybatisProperties.getSupportMethodsArguments());
                p.setProperty("params", mybatisProperties.getParams());

                PageSqlHandler pageSqlHandler = new PageSqlHandler();
                pageSqlHandler.setProperties(p);
                list.add(pageSqlHandler);
            }

            SensitiveProperties sensitiveProperties = PlaceBinder.bind(environment, SensitiveProperties.class, BusXConfig.MYBATIS);
            if (ObjectKit.isNotEmpty(sensitiveProperties)) {
                Properties p = new Properties();
                p.setProperty("debug", String.valueOf(sensitiveProperties.isDebug()));
                p.setProperty("key", sensitiveProperties.getDecrypt().getKey());
                p.setProperty("type", sensitiveProperties.getDecrypt().getType());
                // 数据解密脱敏
                SensitiveResultSetHandler sensitiveResultSetHandler = new SensitiveResultSetHandler();
                sensitiveResultSetHandler.setProperties(p);
                list.add(sensitiveResultSetHandler);
                p.setProperty("key", sensitiveProperties.getEncrypt().getKey());
                p.setProperty("type", sensitiveProperties.getEncrypt().getType());
                // 数据脱敏加密
                SensitiveStatementHandler sensitiveStatementHandler = new SensitiveStatementHandler();
                sensitiveStatementHandler.setProperties(p);
                list.add(sensitiveStatementHandler);
            }
        }
        plugins.addAll(list);
        return plugins.stream().toArray(Interceptor[]::new);
    }

}
