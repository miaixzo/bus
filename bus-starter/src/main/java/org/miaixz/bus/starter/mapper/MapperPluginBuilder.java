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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.mapper.handler.MybatisInterceptor;
import org.miaixz.bus.pager.handler.OperationHandler;
import org.miaixz.bus.pager.handler.PaginationHandler;
import org.miaixz.bus.pager.handler.TenantHandler;
import org.miaixz.bus.pager.handler.TenantProvider;
import org.miaixz.bus.spring.ContextBuilder;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.annotation.PlaceHolderBinder;
import org.miaixz.bus.starter.jdbc.DataSourceHolder;
import org.springframework.core.env.Environment;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * MyBatis 插件构建器，负责初始化并配置 MyBatis 拦截器及其处理器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperPluginBuilder {

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
            // 分页配置
            configurePagination(environment, handlers);
            // 多租户配置
            configureTenant(environment, handlers);
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
    private static void configurePagination(Environment environment, List<MapperHandler> handlers) {
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);
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
    private static void configureTenant(Environment environment, List<MapperHandler> handlers) {
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);

        if (ObjectKit.isNotEmpty(properties) && ObjectKit.isNotEmpty(properties.getConfigurationProperties())) {
            Properties configProps = properties.getConfigurationProperties();

            // 检查是否有任何数据源配置了租户
            boolean hasTenantConfig = configProps.stringPropertyNames().stream().anyMatch(key -> {
                String value = configProps.getProperty(key);
                return value != null && value.contains(Args.TENANT_IGNORE_TABLE);
            });

            if (hasTenantConfig) {
                Logger.info("Enable multi-tenant support, all database operations will include tenant ID support.");

                TenantHandler tenantHandler = new TenantHandler();
                tenantHandler.setProvider(new TenantProvider() {

                    @Override
                    public Expression getTenantId() {
                        String tenantId = ContextBuilder.getTenantId();
                        if (StringKit.isEmpty(tenantId)) {
                            throw new MapperException("Tenant information not found");
                        }
                        return new StringValue(tenantId);
                    }

                    @Override
                    public String getColumn() {
                        // 获取当前数据源的租户列配置
                        String currentDataSourceKey = DataSourceHolder.getKey();

                        // 构建配置键名，例如: com_deepparser.tenant.column
                        String tenantColumnKey = currentDataSourceKey + Symbol.DOT + Args.TENANT_COLUMN_KEY;
                        String tenantColumn = configProps.getProperty(tenantColumnKey);

                        return ObjectKit.isNotEmpty(tenantColumn) ? tenantColumn : Args.TENANT_TABLE_COLUMN;
                    }

                    @Override
                    public boolean ignore(String name) {
                        // 获取当前数据源的配置
                        String currentDataSourceKey = DataSourceHolder.getKey();

                        // 获取表前缀
                        String prefixKey = currentDataSourceKey + Symbol.DOT + Args.TABLE_PREFIX_KEY;
                        String prefix = configProps.getProperty(prefixKey, Normal.EMPTY);

                        // 获取忽略表列表
                        String ignoreKey = currentDataSourceKey + Symbol.DOT + Args.TENANT_IGNORE_KEY;
                        String ignoreTables = configProps.getProperty(ignoreKey, Args.TENANT_IGNORE_TABLE);

                        // 分割 ignoreTables 的值并加上前缀
                        List<String> ignoreTableList = Arrays.stream(ignoreTables.split(Symbol.COMMA))
                                .map(table -> prefix + table.trim()).collect(Collectors.toList());

                        return ignoreTableList.contains(name);
                    }
                });
                handlers.add(tenantHandler);
            }
        }
    }

}
