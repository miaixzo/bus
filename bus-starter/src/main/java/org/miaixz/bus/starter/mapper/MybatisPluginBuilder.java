/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.starter.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.mapper.handler.MybatisInterceptor;
import org.miaixz.bus.pager.handler.OperationHandler;
import org.miaixz.bus.pager.handler.PaginationHandler;
import org.miaixz.bus.pager.handler.TenantHandler;
import org.miaixz.bus.pager.handler.TenantProvider;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.annotation.PlaceHolderBinder;
import org.springframework.core.env.Environment;

/**
 * MyBatis 插件构建器，负责初始化并配置 MyBatis 拦截器及其处理器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MybatisPluginBuilder {

    /**
     * 构建并配置 MyBatis 拦截器
     *
     * @param environment Spring 环境对象，用于获取配置
     * @return 配置好的 MyBatis 拦截器
     */
    public static MybatisInterceptor build(Environment environment) {
        List<MapperHandler> handlers = new ArrayList<>();
        handlers.add(new OperationHandler());

        if (ObjectKit.isNotEmpty(environment)) {
            configureMybatisProperties(environment, handlers);
            configureTenantProperties(environment, handlers);
        }

        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.setHandlers(handlers);
        return interceptor;
    }

    /**
     * 配置 MyBatis 相关属性，添加分页处理器
     *
     * @param environment Spring 环境对象
     * @param handlers    处理器列表
     */
    private static void configureMybatisProperties(Environment environment, List<MapperHandler> handlers) {
        MybatisProperties properties = PlaceHolderBinder.bind(environment, MybatisProperties.class,
                GeniusBuilder.MYBATIS);
        if (ObjectKit.isNotEmpty(properties)) {
            Properties props = new Properties();
            props.setProperty("autoDelimitKeywords", properties.getAutoDelimitKeywords());
            props.setProperty("reasonable", properties.getReasonable());
            props.setProperty("supportMethodsArguments", properties.getSupportMethodsArguments());
            props.setProperty("params", properties.getParams());

            PaginationHandler paginationHandler = new PaginationHandler();
            paginationHandler.setProperties(props);
            handlers.add(paginationHandler);
        }
    }

    /**
     * 配置多租户相关属性，添加多租户处理器
     *
     * @param environment Spring 环境对象
     * @param handlers    处理器列表
     */
    private static void configureTenantProperties(Environment environment, List<MapperHandler> handlers) {
        MybatisProperties properties = PlaceHolderBinder.bind(environment, MybatisProperties.class,
                GeniusBuilder.MYBATIS);
        if (ObjectKit.isNotEmpty(properties.getConfigurationProperties())
                && ObjectKit.isNotEmpty(properties.getConfigurationProperties().get("tenant.column"))) {
            Logger.info("Enable multi-tenant support, all database operations will include tenant ID support.");
            TenantHandler tenantHandler = new TenantHandler();
            tenantHandler.setProvider(new TenantProvider() {
                @Override
                public String getColumn() {
                    // 租户id,默认tenant_id
                    return Context.INSTANCE.getProperty("tenant.column", "tenant_id");
                }

                @Override
                public boolean ignore(String name) {
                    // 忽略租户隔离主表
                    String prefix = Context.INSTANCE.getProperty(Args.TABLE_PREFIX_KEY, Normal.EMPTY);
                    String ignoreTables = Context.INSTANCE.getProperty("tenant.ignore", "tenant");
                    // 分割 tenant.ignore 的值并加上前缀
                    List<String> ignoreTableList = Arrays.stream(ignoreTables.split(Symbol.COMMA))
                            .map(table -> prefix + table.trim()).collect(Collectors.toList());
                    return ignoreTableList.contains(name);
                }
            });

            handlers.add(tenantHandler);
        }
    }

}