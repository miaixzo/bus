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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.ibatis.plugin.Interceptor;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * A builder for creating and configuring MyBatis {@link Interceptor} instances.
 * <p>
 * This class is responsible for initializing and setting up various interceptor handlers, such as for pagination and
 * multi-tenancy, based on the application's configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperPluginBuilder {

    /**
     * Builds and configures the primary {@link MybatisInterceptor}.
     *
     * @param environment The Spring {@link Environment} object, used to retrieve configuration properties.
     * @return A fully configured {@link MybatisInterceptor} instance.
     */
    public static MybatisInterceptor build(Environment environment) {
        List<MapperHandler> handlers = new ArrayList<>();
        // Default handler for basic operations.
        handlers.add(new OperationHandler());

        if (ObjectKit.isNotEmpty(environment)) {
            // Configure pagination if properties are present.
            configurePagination(environment, handlers);
            // Configure multi-tenancy if properties are present.
            configureTenant(environment, handlers);
        }

        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.setHandlers(handlers);
        return interceptor;
    }

    /**
     * Configures MyBatis properties and adds the {@link PaginationHandler}.
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the pagination handler to.
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
            Logger.info("Pagination handler configured and added.");
        }
    }

    /**
     * Configures multi-tenancy properties and adds the {@link TenantHandler}.
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the tenant handler to.
     */
    private static void configureTenant(Environment environment, List<MapperHandler> handlers) {
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);

        if (ObjectKit.isNotEmpty(properties) && ObjectKit.isNotEmpty(properties.getConfigurationProperties())) {
            Properties configProps = properties.getConfigurationProperties();

            // Check if any data source is configured for multi-tenancy.
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
                            throw new MapperException("Tenant information not found in the current context");
                        }
                        return new StringValue(tenantId);
                    }

                    @Override
                    public String getColumn() {
                        // Get the tenant column for the current data source.
                        String currentDataSourceKey = DataSourceHolder.getKey();
                        String tenantColumnKey = currentDataSourceKey + Symbol.DOT + Args.TENANT_COLUMN_KEY;
                        String tenantColumn = configProps.getProperty(tenantColumnKey);
                        return ObjectKit.isNotEmpty(tenantColumn) ? tenantColumn : Args.TENANT_TABLE_COLUMN;
                    }

                    @Override
                    public boolean ignore(String name) {
                        // Get the configuration for the current data source.
                        String currentDataSourceKey = DataSourceHolder.getKey();

                        // Get the table prefix.
                        String prefixKey = currentDataSourceKey + Symbol.DOT + Args.TABLE_PREFIX_KEY;
                        String prefix = configProps.getProperty(prefixKey, Normal.EMPTY);

                        // Get the list of ignored tables.
                        String ignoreKey = currentDataSourceKey + Symbol.DOT + Args.TENANT_IGNORE_KEY;
                        String ignoreTables = configProps.getProperty(ignoreKey, Args.TENANT_IGNORE_TABLE);

                        // Check if the current table is in the ignored list (with prefix).
                        List<String> ignoreTableList = Arrays.stream(ignoreTables.split(Symbol.COMMA))
                                .map(table -> prefix + table.trim()).collect(Collectors.toList());

                        return ignoreTableList.contains(name);
                    }
                });
                handlers.add(tenantHandler);
                Logger.info("Multi-tenancy handler configured and added.");
            }
        }
    }

}
