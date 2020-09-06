/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.starter.mapper;

import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.pager.plugin.PageInterceptor;
import org.aoju.bus.starter.sensitive.SensitiveProperties;
import org.aoju.bus.starter.sensitive.SensitiveResultSetHandler;
import org.aoju.bus.starter.sensitive.SensitiveStatementHandler;
import org.apache.ibatis.plugin.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * mybatis 插件启用
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
public class MybatisPluginBuilder {

    public static List<Interceptor> plugins = new ArrayList<>();

    public static Interceptor[] build(MybatisProperties mybatisProperties,
                                      SensitiveProperties sensitiveProperties) {
        if (ObjectKit.isNotEmpty(mybatisProperties)) {
            Properties p = new Properties();
            p.setProperty("autoDelimitKeywords", mybatisProperties.getAutoDelimitKeywords());
            p.setProperty("reasonable", mybatisProperties.getReasonable());
            p.setProperty("supportMethodsArguments", mybatisProperties.getSupportMethodsArguments());
            p.setProperty("returnPageInfo", mybatisProperties.getReturnPageInfo());
            p.setProperty("params", mybatisProperties.getParams());

            PageInterceptor interceptor = new PageInterceptor();
            interceptor.setProperties(p);

            List<Interceptor> list = CollKit.newArrayList(
                    interceptor,
                    new NatureSQLHandler(),
                    new ExplainSQLHandler()
            );

            if (mybatisProperties.isRecordTime()) {
                list.add(new RecordTimeHandler());
            }

            if (ObjectKit.isNotEmpty(sensitiveProperties)) {
                list.add(new SensitiveResultSetHandler(sensitiveProperties));
                list.add(new SensitiveStatementHandler(sensitiveProperties));
            }
            plugins.addAll(list);
        }
        return plugins.stream().toArray(Interceptor[]::new);
    }

}
