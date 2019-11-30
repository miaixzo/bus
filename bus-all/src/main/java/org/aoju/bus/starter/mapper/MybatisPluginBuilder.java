/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.starter.mapper;


import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.pager.plugin.PageInterceptor;
import org.aoju.bus.starter.sensitive.SensitiveResultSetHandler;
import org.aoju.bus.starter.sensitive.SensitiveStatementHandler;
import org.apache.ibatis.plugin.Interceptor;

import java.util.Properties;

/**
 * mybatis 插件启用
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public class MybatisPluginBuilder {

    protected static Interceptor[] plugins = {};

    public static Interceptor[] build(MybatisProperties properties) {
        if (ObjectUtils.isNotEmpty(properties)) {
            PageInterceptor interceptor = new PageInterceptor();
            Properties p = new Properties();
            p.setProperty("autoDelimitKeywords", properties.getAutoDelimitKeywords());
            p.setProperty("reasonable", properties.getReasonable());
            p.setProperty("supportMethodsArguments", properties.getSupportMethodsArguments());
            p.setProperty("returnPageInfo", properties.getReturnPageInfo());
            p.setProperty("params", properties.getParams());
            interceptor.setProperties(p);

            plugins = new Interceptor[]{
                    interceptor,
                    new SQLPerformanceHandler(),
                    new SQLExplainHandler(),
                    new SensitiveResultSetHandler(),
                    new SensitiveStatementHandler()};
        }
        return plugins;
    }

}
